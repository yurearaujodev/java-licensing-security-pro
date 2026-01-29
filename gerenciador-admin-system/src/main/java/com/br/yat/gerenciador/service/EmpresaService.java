package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.config.ConnectionFactory;
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
import com.br.yat.gerenciador.model.dto.EmpresaDTO;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.validation.EmpresaValidationUtils;

public class EmpresaService {

	public EmpresaDTO carregarEmpresaCompleta(int id, TipoCadastro tipo) {
		try (Connection conn = ConnectionFactory.getConnection()) {
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

	public void salvarEmpresaCompleta(Endereco endereco, Empresa empresa, List<Contato> contatos,
			List<Representante> representantes, List<Banco> bancos, Complementar complementar,
			List<Documento> documentos) {

		validarDados(endereco, empresa, contatos, representantes, bancos, complementar, documentos);

		try (Connection conn = ConnectionFactory.getConnection()) {
			ConnectionFactory.beginTransaction(conn);
			try {
				Endereco end = salvarEndereco(conn, endereco);
				empresa.setEndereco(end);

				Empresa emp = salvarEmpresa(conn, empresa);

				salvarRelacionamentos(conn, emp, contatos, representantes, bancos, complementar, documentos);

				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
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

	private void salvarRelacionamentos(Connection conn, Empresa empresa, List<Contato> contatos,
			List<Representante> representantes, List<Banco> bancos, Complementar complementar,
			List<Documento> documentos) {

		int id = empresa.getIdEmpresa();

		ContatoDao contDao = new ContatoDao(conn);
		if (contatos != null) {
			contatos.forEach(c -> c.setEmpresa(empresa));
			contDao.syncByEmpresa(id, contatos);
		}

		if (empresa.getTipoEmpresa() != TipoCadastro.FORNECEDORA)
			return;

		RepresentanteDao repDao = new RepresentanteDao(conn);
		repDao.deleteByEmpresa(id);
		if (representantes != null)
			representantes.forEach(r -> {
				r.setEmpresa(empresa);
				repDao.save(r);
			});

		BancoDao banDao = new BancoDao(conn);
		banDao.deleteByEmpresa(id);
		if (bancos != null)
			bancos.forEach(b -> {
				b.setEmpresa(empresa);
				banDao.save(b);
			});

		if (complementar != null) {
			complementar.setEmpresa(empresa);
			ComplementarDao compDao = new ComplementarDao(conn);
			if (complementar.getIdComplementar() > 0)
				compDao.update(complementar);
			else
				compDao.save(complementar);
		}

		DocumentoDao docDao = new DocumentoDao(conn);
		docDao.deleteByEmpresa(id);
		if (documentos != null)
			documentos.forEach(d -> {
				d.setEmpresa(empresa);
				docDao.save(d);
			});
	}

	public void inativarEmpresa(int idEmpresa) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			EmpresaDao dao = new EmpresaDao(conn);

			Empresa empresa = dao.searchById(idEmpresa);
			if (empresa == null) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "EMPRESA NÃO ENCONTRADA.");
			}

			if (!empresa.isAtivo()) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "EMPRESA JÁ ESTÁ INATIVA.");
			}

			dao.softDeleteById(idEmpresa);

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

}
