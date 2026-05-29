package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.util.TimeUtils;

/**
 * Regras de bloqueio por tentativas de login falhas.
 */
final class LoginBloqueioService {

	private static final int LIMITE_REINCIDENCIA_BLOQUEIO_24H = 3;
	private static final int JANELA_HISTORICO_BLOQUEIO_HORAS = 24;

	@FunctionalInterface
	interface LogSucessoRegistrar {
		void registrar(Connection conn, String tipo, String acao, String entidade, Integer idRef, Object antes,
				Object depois);
	}

	private final ParametroSistemaService parametroService;
	private final DaoFactory daoFactory;
	private final LogSucessoRegistrar logSucessoRegistrar;

	LoginBloqueioService(ParametroSistemaService parametroService, DaoFactory daoFactory,
			LogSucessoRegistrar logSucessoRegistrar) {
		this.parametroService = parametroService;
		this.daoFactory = daoFactory;
		this.logSucessoRegistrar = logSucessoRegistrar;
	}

	void validarBloqueioTemporario(Connection conn, UsuarioDao dao, Usuario user) {
		LocalDateTime bloqueadoAte = user.getBloqueadoAte();

		if (bloqueadoAte == null) {
			return;
		}

		if (bloqueadoAte.isAfter(LocalDateTime.now())) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"USUÁRIO BLOQUEADO TEMPORARIAMENTE ATÉ " + TimeUtils.formatarDataHora(bloqueadoAte));
		}

		dao.resetTentativasFalhas(user.getIdUsuario());
		user.setBloqueadoAte(null);
		user.setTentativasFalhas(0);
	}

	void tratarFalhaLogin(Connection conn, UsuarioDao dao, Usuario user) {
		if (user.isMaster()) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "SENHA INCORRETA.");
		}

		int maxTentativas = getMaxTentativas();
		int tentativas = dao.incrementarERetornarTentativas(user.getIdUsuario());

		user.setTentativasFalhas(tentativas);

		if (tentativas >= maxTentativas) {
			processarBloqueio(conn, dao, user, tentativas);
		}
	}

	void lancarExcecaoFalhaLogin(Usuario user) {
		int maxTentativas = getMaxTentativas();
		int tentativas = user.getTentativasFalhas();

		if (tentativas >= maxTentativas) {
			if (user.getStatus() == StatusUsuario.BLOQUEADO) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "USUÁRIO BLOQUEADO.");
			}

			if (user.getBloqueadoAte() != null) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"USUÁRIO BLOQUEADO ATÉ " + TimeUtils.formatarDataHora(user.getBloqueadoAte()));
			}

			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "USUÁRIO BLOQUEADO.");
		}

		throw new ValidationException(ValidationErrorType.INVALID_FIELD,
				"SENHA INCORRETA. TENTATIVA " + tentativas + " DE " + maxTentativas);
	}

	private void processarBloqueio(Connection conn, UsuarioDao dao, Usuario user, int tentativas) {
		LogSistemaDao logDao = daoFactory.createLogSistemaDao(conn);
		int totalBloqueios24h = logDao.contarLogsBloqueioRecentes(user.getIdUsuario(), JANELA_HISTORICO_BLOQUEIO_HORAS);

		if (totalBloqueios24h >= LIMITE_REINCIDENCIA_BLOQUEIO_24H) {
			executarBloqueioPermanente(conn, dao, user, tentativas, totalBloqueios24h);
			return;
		}

		executarBloqueioTemporario(conn, dao, user, tentativas, totalBloqueios24h);
	}

	private void executarBloqueioPermanente(Connection conn, UsuarioDao dao, Usuario user, int tentativas,
			int historico24h) {
		dao.bloquearUsuario(user.getIdUsuario());
		user.setStatus(StatusUsuario.BLOQUEADO);

		Map<String, Object> detalhes = new HashMap<>();
		detalhes.put("tentativas", tentativas);
		detalhes.put("bloqueios_24h", historico24h);

		logSucessoRegistrar.registrar(conn, "SEGURANCA", "BLOQUEIO_PERMANENTE", "usuario", user.getIdUsuario(), null,
				detalhes);
	}

	private void executarBloqueioTemporario(Connection conn, UsuarioDao dao, Usuario user, int tentativas,
			int historico24h) {
		int minutos = parametroService.getInt(ParametroChave.LOGIN_TEMPO_BLOQUEIO_MIN, 5);
		LocalDateTime bloqueadoAte = LocalDateTime.now().plusMinutes(minutos);

		dao.bloquearTemporariamente(user.getIdUsuario(), bloqueadoAte);
		user.setBloqueadoAte(bloqueadoAte);

		Map<String, Object> detalhes = new HashMap<>();
		detalhes.put("tentativa", tentativas);
		detalhes.put("sequencia_hoje", historico24h + 1);

		logSucessoRegistrar.registrar(conn, "SEGURANCA", "BLOQUEIO_TEMPORARIO", "usuario", user.getIdUsuario(), null,
				detalhes);
	}

	private int getMaxTentativas() {
		return parametroService.getInt(ParametroChave.LOGIN_MAX_TENTATIVAS, 5);
	}
}
