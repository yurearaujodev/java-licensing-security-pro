package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.empresa.BancoDao;
import com.br.yat.gerenciador.dao.empresa.ComplementarDao;
import com.br.yat.gerenciador.dao.empresa.ContatoDao;
import com.br.yat.gerenciador.dao.empresa.DocumentoDao;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.empresa.EnderecoDao;
import com.br.yat.gerenciador.dao.empresa.RepresentanteDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Banco;
import com.br.yat.gerenciador.model.Complementar;
import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.Documento;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Endereco;
import com.br.yat.gerenciador.model.Representante;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.dto.EmpresaDTO;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.util.AuditLogHelper;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;

public class EmpresaService extends BaseService {

	private static final MenuChave CHAVE_SALVAR = MenuChave.CADASTROS_EMPRESA_CLIENTE;
	private static final MenuChave CHAVE_CONSULTA = MenuChave.CONSULTAS_EMPRESAS_CLIENTES;

	public EmpresaDTO carregarEmpresaCompleta(int id, TipoCadastro tipo, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_CONSULTA, "READ");
			EmpresaDao empDao = new EmpresaDao(conn);

			Empresa emp = (tipo == TipoCadastro.FORNECEDORA) ? empDao.buscarPorFornecedora() : empDao.searchById(id);

			if (emp == null)
				return null;

			int empId = emp.getIdEmpresa();
			List<Contato> contatos = new ContatoDao(conn).listarPorEmpresa(empId);

			if (tipo == TipoCadastro.CLIENTE) {
				return new EmpresaDTO(emp, contatos, null, null, null, null);
			}

			return new EmpresaDTO(emp, contatos, new RepresentanteDao(conn).listarPorEmpresa(empId),
					new BancoDao(conn).listarPorEmpresa(empId), new ComplementarDao(conn).buscarPorEmpresa(empId),
					new DocumentoDao(conn).listarPorEmpresa(empId));
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public List<Empresa> listarClientesParaTabela(boolean inativos, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_CONSULTA, "READ");
			return new EmpresaDao(conn).listarTodosClientes(inativos);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public List<Empresa> filtrarClientes(String termo, boolean inativos, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_CONSULTA, "READ");
			return new EmpresaDao(conn).filtrarClientes(termo, inativos);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public void salvarEmpresaCompleta(Endereco endereco, Empresa empresa, List<Contato> contatos,
			List<Representante> representantes, List<Banco> bancos, Complementar complementar,
			List<Documento> documentos, Usuario executor) {

		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_SALVAR, "WRITE");

			EmpresaDao empDao = new EmpresaDao(conn);
			Empresa anterior = (empresa.getIdEmpresa() > 0) ? empDao.searchById(empresa.getIdEmpresa()) : null;

			boolean houveMudanca = (anterior == null || !empresa.equals(anterior));

			if (houveMudanca) {
				validarDados(endereco, empresa, contatos, representantes, bancos, complementar, documentos);
			}
			ConnectionFactory.beginTransaction(conn);
			try {
				validarDuplicidadeDocumento(empDao, empresa);
				validarProtecaoFornecedora(empresa, executor);

				Endereco end = salvarEndereco(conn, endereco);
				empresa.setEndereco(end);

				validarExistenciaParaUpdate(empresa, anterior);

				Empresa empSalva = salvarEmpresa(conn, empresa);

				salvarRelacionamentos(conn, empSalva, contatos, representantes, bancos, complementar, documentos);

				registrarLogSucesso(conn, empSalva, anterior);

				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				registrarLogErro("ERRO", "SALVAR_EMPRESA", "empresa", e);
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	private void validarDuplicidadeDocumento(EmpresaDao dao, Empresa e) {
		if (ValidationUtils.isEmpty(e.getDocumentoEmpresa()))
			return;

		Empresa existente = dao.buscarPorCnpjCpf(e.getDocumentoEmpresa());
		if (existente != null && existente.getIdEmpresa() != e.getIdEmpresa()) {
			throw new ValidationException(ValidationErrorType.DUPLICATE_ENTRY,
					"ESTE CNPJ/CPF JÁ ESTÁ CADASTRADO PARA A EMPRESA: " + existente.getRazaoSocialEmpresa());
		}
	}

	private void validarProtecaoFornecedora(Empresa e, Usuario executor) {
		if (e.getTipoEmpresa() == TipoCadastro.FORNECEDORA && (executor != null && !executor.isMaster())) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"APENAS UM USUÁRIO MASTER PODE ALTERAR OS DADOS DA EMPRESA FORNECEDORA.");
		}
	}

	private void validarExistenciaParaUpdate(Empresa atual, Empresa anterior) {
		if (atual.getIdEmpresa() > 0 && anterior == null) {
			throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND,
					"A EMPRESA QUE VOCÊ ESTÁ TENTANDO ALTERAR NÃO FOI ENCONTRADA OU JÁ FOI REMOVIDA.");
		}
	}

	private void salvarRelacionamentos(Connection conn, Empresa emp, List<Contato> contatos, List<Representante> reps,
			List<Banco> bancos, Complementar comp, List<Documento> docs) {

		int id = emp.getIdEmpresa();

		if (contatos != null) {
			contatos.forEach(c -> c.setEmpresa(emp));
			new ContatoDao(conn).syncByEmpresa(id, contatos);
		}

		if (emp.getTipoEmpresa() == TipoCadastro.FORNECEDORA) {
			processarDadosExtrasFornecedora(conn, emp, reps, bancos, comp, docs);
		}
	}

	private void processarDadosExtrasFornecedora(Connection conn, Empresa emp, List<Representante> reps,
			List<Banco> bancos, Complementar comp, List<Documento> docs) {

		int id = emp.getIdEmpresa();
		if (reps != null) {
			reps.forEach(r -> r.setEmpresa(emp));
			new RepresentanteDao(conn).syncByEmpresa(id, reps);
		}
		if (bancos != null) {
			bancos.forEach(b -> b.setEmpresa(emp));
			new BancoDao(conn).syncByEmpresa(id, bancos);
		}
		if (docs != null) {
			docs.forEach(d -> d.setEmpresa(emp));
			new DocumentoDao(conn).syncByEmpresa(id, docs);
		}

		if (comp != null) {
			comp.setEmpresa(emp);
			ComplementarDao cDao = new ComplementarDao(conn);
			if (comp.getIdComplementar() > 0)
				cDao.update(comp);
			else
				cDao.save(comp);
		}
	}

	private void registrarLogSucesso(Connection conn, Empresa atual, Empresa anterior) throws SQLException {
		String acao = (anterior == null) ? "INSERIR_EMPRESA" : "ALTERAR_EMPRESA";
		new LogSistemaDao(conn).save(
				AuditLogHelper.gerarLogSucesso("CADASTRO", acao, "empresa", atual.getIdEmpresa(), anterior, atual));
	}

	private void validarDados(Endereco endereco, Empresa empresa, List<Contato> contatos,
			List<Representante> representantes, List<Banco> bancos, Complementar complementar,
			List<Documento> documentos) {

		EmpresaValidationUtils.validarEndereco(endereco);
		EmpresaValidationUtils.validarEmpresa(empresa);
		EmpresaValidationUtils.validarContatos(contatos);

		if (empresa.getTipoEmpresa() == TipoCadastro.FORNECEDORA) {
			EmpresaValidationUtils.validarEmpresaFiscal(empresa);
			EmpresaValidationUtils.validarRepresentantes(representantes);
			EmpresaValidationUtils.validarBancos(bancos);
			EmpresaValidationUtils.validarComplementar(complementar);
			EmpresaValidationUtils.validarDocumentos(documentos);
		}
	}

	private Endereco salvarEndereco(Connection conn, Endereco endereco) {
		EnderecoDao dao = new EnderecoDao(conn);
		return endereco.getIdEndereco() > 0 ? dao.update(endereco) : dao.save(endereco);
	}

	private Empresa salvarEmpresa(Connection conn, Empresa empresa) {
		EmpresaDao dao = new EmpresaDao(conn);
		return empresa.getIdEmpresa() > 0 ? dao.update(empresa) : dao.save(empresa);
	}

	public void restaurarEmpresa(int idEmpresa, Usuario executor) {

		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_SALVAR, "DELETE");
			EmpresaDao dao = new EmpresaDao(conn);

			dao.restaurar(idEmpresa);

			new LogSistemaDao(conn).save(AuditLogHelper.gerarLogSucesso("CADASTRO", "RESTAURAR_EMPRESA", "empresa",
					idEmpresa, "Status: INATIVO", "Status: ATIVO"));

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO RESTAURAR EMPRESA", e);
		}
	}

	public void ExcluirEmpresa(int idEmpresa, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_SALVAR, "DELETE");

			EmpresaDao dao = new EmpresaDao(conn);
			LogSistemaDao logDao = new LogSistemaDao(conn);

			Empresa empresa = dao.searchById(idEmpresa);
			if (empresa == null) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "EMPRESA NÃO ENCONTRADA.");
			}

			if (empresa.getTipoEmpresa() == TipoCadastro.FORNECEDORA) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"A EMPRESA FORNECEDORA NÃO PODE SER INATIVADA.");
			}

			if (!empresa.isAtivo()) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "EMPRESA JÁ ESTÁ INATIVA.");
			}

			dao.softDeleteById(idEmpresa);
			logDao.save(AuditLogHelper.gerarLogSucesso("CADASTRO", "INATIVAR_EMPRESA", "empresa", idEmpresa, empresa,
					null));
		} catch (SQLException e) {
			registrarLogErro("ERRO", "EXCLUIR_EMPRESA", "empresa", e);
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public Empresa buscarFornecedoraParaSetup() {
		// Note que não chamamos validarAcesso aqui, pois é para o primeiro uso do
		// sistema
		try (Connection conn = ConnectionFactory.getConnection()) {
			Empresa fornecedora = new EmpresaDao(conn).buscarPorFornecedora();
			if (fornecedora == null) {
				throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND,
						"EMPRESA FORNECEDORA NÃO ENCONTRADA. CADASTRE-A PRIMEIRO.");
			}
			return fornecedora;
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

}
