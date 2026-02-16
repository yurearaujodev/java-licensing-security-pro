package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Function;

import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;
import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.util.AuditLogHelper;

public abstract class BaseService {

	protected void validarAcesso(Connection conn, Usuario executor, MenuChave chave, TipoPermissao tipoOperacao)
			throws SQLException {

		if (executor != null && Sessao.isExpirada()) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"SUA SESSÃO EXPIROU POR INATIVIDADE. OPERAÇÃO BLOQUEADA.");
		}

		if (executor == null) {
			EmpresaDao dao = new EmpresaDao(conn);
			if (dao.buscarPorFornecedora() != null) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "SESSÃO INVÁLIDA.");
			}
			return;
		}

		UsuarioDao uDao = new UsuarioDao(conn);
		Usuario atualNoBanco = uDao.searchById(executor.getIdUsuario());

		if (atualNoBanco == null
				|| atualNoBanco.getStatus() != StatusUsuario.ATIVO) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"USUÁRIO INATIVO OU REMOVIDO. ACESSO IMEDIATAMENTE REVOGADO.");
		}

		if (executor.isMaster())
			return;

		UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
		Integer idPerfil = (executor.getPerfil() != null) ? executor.getPerfil().getIdPerfil() : 0;

		boolean temPermissao = upDao.usuarioPossuiAcessoCompleto(executor.getIdUsuario(), idPerfil, chave.name(),
				tipoOperacao.name());

		if (!temPermissao) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"ACESSO NEGADO: VOCÊ NÃO TEM PERMISSÃO DE " + tipoOperacao + " NESTA TELA.");
		}
	}

	protected void registrarLogErro(String tipo, String acao, String entidade, Exception e) {
		try (Connection connLog = ConnectionFactory.getConnection()) {
			String msgErro = (e.getMessage() != null) ? e.getMessage() : e.toString();

			var log = AuditLogHelper.gerarLogErro(tipo, acao, entidade, msgErro);

			new LogSistemaDao(connLog).save(log);
		} catch (Exception ex) {
			System.err.println("Falha crítica ao gravar log de erro: " + ex.getMessage());
		}
	}

	protected void registrarLogSucesso(Connection conn, String tipo, String acao, String entidade, Integer idRef,
			Object antes, Object depois) {
		try {
			LogSistemaDao logDao = new LogSistemaDao(conn);

			var log = AuditLogHelper.gerarLogSucesso(tipo, acao, entidade, idRef, antes, depois);

			logDao.save(log);

		} catch (Exception e) {
			System.err.println("Falha ao registrar log de sucesso: " + e.getMessage());
		}
	}

	protected <T> T execute(Function<Connection, T> action) {
		try (Connection conn = ConnectionFactory.getConnection()) {

			return action.apply(conn);

		} catch (SQLException e) {

			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, e);

		} catch (Exception e) {

			throw new DataAccessException(DataAccessErrorType.INTERNAL_ERROR, e);
		}
	}

}