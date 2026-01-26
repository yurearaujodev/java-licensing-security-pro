package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.dao.empresa.BancoDao;
import com.br.yat.gerenciador.dao.empresa.ComplementarDao;
import com.br.yat.gerenciador.dao.empresa.ContatoDao;
import com.br.yat.gerenciador.dao.empresa.DocumentoDao;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.empresa.EnderecoDao;
import com.br.yat.gerenciador.dao.empresa.RepresentanteDao;
import com.br.yat.gerenciador.model.Banco;
import com.br.yat.gerenciador.model.Complementar;
import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.Documento;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.EmpresaDTO;
import com.br.yat.gerenciador.model.Endereco;
import com.br.yat.gerenciador.model.Representante;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.database.ConnectionFactory;
import com.br.yat.gerenciador.util.exception.DataAccessException;
import com.br.yat.gerenciador.util.exception.ValidationException;
import com.br.yat.gerenciador.util.validation.DocumentValidator;
import com.br.yat.gerenciador.util.validation.FormatValidator;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;

public class EmpresaService {

	public void salvarEmpresaCompleta(Endereco endereco, Empresa empresa, List<Contato> contatos,
			List<Representante> representantes, List<Banco> bancos, Complementar complementar,
			List<Documento> documentos) {

		EmpresaValidationUtils.validarEndereco(endereco);
		EmpresaValidationUtils.validarEmpresa(empresa);
		EmpresaValidationUtils.validarContatos(contatos);

		boolean isFornecedora = empresa.getTipoEmpresa()==TipoCadastro.FORNECEDORA;
		if (isFornecedora) {
			EmpresaValidationUtils.validarEmpresaFiscal(empresa);
			validarRepresentante(representantes);
			validarBanco(bancos);
			validarComplementarIndividual(complementar);
			validarDocumentos(documentos);
		}

		try (Connection conn = ConnectionFactory.getConnection()) {
			ConnectionFactory.beginTransaction(conn);
			try {
				EnderecoDao endDao = new EnderecoDao(conn);
				if (endereco.getIdEndereco() > 0) {
					endDao.update(endereco);
				} else {
					endereco = endDao.save(endereco);
				}

				empresa.setEndereco(endereco);
				EmpresaDao empDao = new EmpresaDao(conn);
				if (empresa.getIdEmpresa() > 0) {
					empresa = empDao.update(empresa);
				} else {
					empresa = empDao.save(empresa);
				}

				salvarFornecedora(conn, empresa, contatos, representantes, bancos, complementar, documentos,
						isFornecedora);
				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				throw e;
			}

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO DE CONEXÃO COM O BANCO DE DADOS",
					e);
		}
	}

	public EmpresaDTO carregarForncedoraCompleta() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			EmpresaDao empDao = new EmpresaDao(conn);
			Empresa emp = empDao.buscarPorFornecedora();

			if (emp == null)
				return null;

			int id = emp.getIdEmpresa();

			List<Contato> contatos = new ContatoDao(conn).listarPorEmpresa(id);
			List<Representante> reps = new RepresentanteDao(conn).listarPorEmpresa(id);
			List<Banco> bancos = new BancoDao(conn).listarPorEmpresa(id);
			Complementar comp = new ComplementarDao(conn).buscarPorEmpresa(id);
			List<Documento> docs = new DocumentoDao(conn).listarPorEmpresa(id);

			return new EmpresaDTO(emp, contatos, reps, bancos, comp, docs);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public EmpresaDTO carregarClienteCompleto(int id) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			EmpresaDao empDao = new EmpresaDao(conn);
			Empresa emp = empDao.searchById(id);
			if (emp == null)
				return null;

			List<Contato> contatos = new ContatoDao(conn).listarPorEmpresa(id);
			return new EmpresaDTO(emp, contatos, null, null, null, null);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public List<Empresa> listarClientesParaTabela() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new EmpresaDao(conn).listarTodosClientes();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public List<Empresa> filtrarClientes(String termo) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new EmpresaDao(conn).filtrarClientes(termo);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public void validarRepresentante(Representante r) {
		if (r == null)
			return;

		if (r.getNomeRepresentante() != null && ValidationUtils.isEmpty(r.getNomeRepresentante())) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"NOME DO REPRESENTANTE É OBRIGATÓRIO.");
		}

		if (r.getCpfRepresentante() != null) {
			if (!DocumentValidator.isValidaCPF(r.getCpfRepresentante())) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "CPF INVÁLIDO.");
			}
		}

		if (!ValidationUtils.isEmpty(r.getTelefoneRepresentante())) {
			if (!FormatValidator.isValidPhoneNumberBR(r.getTelefoneRepresentante())) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "TELEFONE INVÁLIDO.");
			}
		}

	}

	public void validarRepresentante(List<Representante> representantes) {
		if (representantes == null || representantes.isEmpty())
			return;
		for (Representante r : representantes) {
			if (ValidationUtils.isEmpty(r.getNomeRepresentante())) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "NOME OBRIGATÓRIO.");
			}

			if (ValidationUtils.isEmpty(r.getCpfRepresentante())) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "CPF OBRIGATÓRIO.");
			}

			validarRepresentante(r);
		}
	}

	public void validarBanco(Banco b) {
		if (b == null)
			return;

		if (b.getCodBanco() != 0) {
			if (b.getCodBanco() < 1 || b.getCodBanco() > 999) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"CÓDIGO DO BANCO DEVE ESTAR ENTRE 001 E 999.");
			}
		}
		if (b.getNomeBanco() != null && ValidationUtils.isEmpty(b.getNomeBanco())) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "NOME DO BANCO É OBRIGATÓRIO.");
		}
	}

	public void validarBanco(List<Banco> bancos) {
		if (bancos == null || bancos.isEmpty()) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"AO MENOS UM BANCO DEVE SER CADASTRADO.");
		}
		for (Banco b : bancos) {
			if (ValidationUtils.isEmpty(b.getNomeBanco())) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
						"NOME DO BANCO NA LISTA NÃO PODE ESTAR VAZIO.");
			}

			validarBanco(b);
		}
	}

	public void validarComplementarIndividual(Complementar comp) {
		if (comp == null)
			return;

		if (comp.getRamoAtividadeComplementar() != null
				&& ValidationUtils.isEmpty(comp.getRamoAtividadeComplementar())) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"RAMO DE ATIVIDADE É OBRIGATÓRIO.");
		}
	}

	public void validarDocumentos(List<Documento> doc) {
		if (doc == null || doc.isEmpty()) {
			return;
		}
		for (Documento d : doc) {
			if (ValidationUtils.isEmpty(d.getTipoDocumento())) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
						"TIPO DE DOCUMENTO INVÁLIDO NA LISTA.");
			}
			if (ValidationUtils.isEmpty(d.getArquivoDocumento())) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
						"CAMINHO DO ARQUIVO INVÁLIDO.");
			}
		}
	}

	private void salvarFornecedora(Connection conn, Empresa empresa, List<Contato> contatos,
			List<Representante> representantes, List<Banco> bancos, Complementar complementares,
			List<Documento> documentos, boolean isFornecedora) {

		int id = empresa.getIdEmpresa();

		ContatoDao contDao = new ContatoDao(conn);
		contDao.deleteByEmpresa(id);
		if (contatos != null) {
			for (Contato c : contatos) {
				c.setEmpresa(empresa);
				contDao.save(c);
			}
		}

		if (isFornecedora) {

			RepresentanteDao repDao = new RepresentanteDao(conn);
			repDao.deleteByEmpresa(id);
			if (representantes != null) {
				for (Representante r : representantes) {
					r.setEmpresa(empresa);
					repDao.save(r);
				}
			}
			BancoDao banDao = new BancoDao(conn);
			banDao.deleteByEmpresa(id);
			if (bancos != null) {
				for (Banco b : bancos) {
					b.setEmpresa(empresa);
					banDao.save(b);
				}
			}
			if (complementares != null) {
				complementares.setEmpresa(empresa);
				ComplementarDao compDao = new ComplementarDao(conn);
				if (complementares.getIdComplementar() > 0) {
					compDao.update(complementares);
				} else {
					compDao.save(complementares);
				}
			}

			DocumentoDao docDao = new DocumentoDao(conn);
			docDao.deleteByEmpresa(id);
			if (documentos != null) {
				for (Documento d : documentos) {
					d.setEmpresa(empresa);
					docDao.save(d);
				}
			}

		}

	}
}
