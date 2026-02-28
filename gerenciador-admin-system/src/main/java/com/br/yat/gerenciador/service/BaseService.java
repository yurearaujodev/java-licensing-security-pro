package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.function.Consumer;
import java.util.function.Function;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.domain.event.DomainEventPublisher;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.security.SecurityService;
import com.br.yat.gerenciador.util.AuditLogHelper;

public abstract class BaseService {

	protected final SecurityService securityService;
	protected final DomainEventPublisher eventPublisher;
	protected static final Logger logger = LogManager.getLogger(BaseService.class);

	protected BaseService(DomainEventPublisher eventPublisher, SecurityService securityService) {
		this.eventPublisher = eventPublisher;
		this.securityService = securityService;
	}

	protected void validarAcesso(Connection conn, Usuario executor, MenuChave chave, TipoPermissao tipoOperacao) {
		securityService.validarAcesso(conn, executor, chave, tipoOperacao);
	}

	protected void registrarLogErro(String tipo, String acao, String entidade, Exception e) {
		try (Connection connLog = ConnectionFactory.getConnection()) {
			String msgErro = (e.getMessage() != null) ? e.getMessage() : e.toString();

			var log = AuditLogHelper.gerarLogErro(tipo, acao, entidade, msgErro);

			new LogSistemaDao(connLog).save(log);
		} catch (Exception ex) {
			System.err.println("Falha crítica ao gravar log de erro" + ex.getMessage());
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

		} catch (ValidationException e) {
			throw e;

		} catch (SQLException e) {

			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, e);

		} catch (Exception e) {

			throw new DataAccessException(DataAccessErrorType.INTERNAL_ERROR, e);
		}
	}

	protected <T> T executeInTransaction(Function<Connection, T> action) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			ConnectionFactory.beginTransaction(conn);

			try {
				T result = action.apply(conn);
				ConnectionFactory.commitTransaction(conn);
				return result;

			} catch (ValidationException e) {
				ConnectionFactory.rollbackTransaction(conn);
				throw e;

			} catch (SQLException e) {
				ConnectionFactory.rollbackTransaction(conn);
				throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, e);

			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				throw new DataAccessException(DataAccessErrorType.INTERNAL_ERROR, e);
			}

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e);
		}
	}

	protected void executeInTransactionVoid(Consumer<Connection> action) {
		executeInTransaction(conn -> {
			action.accept(conn);
			return null;
		});
	}

}