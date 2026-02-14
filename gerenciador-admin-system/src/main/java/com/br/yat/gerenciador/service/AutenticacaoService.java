package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.security.PasswordUtils;
import com.br.yat.gerenciador.security.SensitiveData;
import com.br.yat.gerenciador.util.AuditLogHelper;
import com.br.yat.gerenciador.util.TimeUtils;

public class AutenticacaoService extends BaseService {

	private final ParametroSistemaService parametroService;

	public AutenticacaoService(ParametroSistemaService parametroService) {
		this.parametroService = parametroService;
	}

	public Usuario autenticar(String email, char[] senhaPura) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);
			LogSistemaDao logDao = new LogSistemaDao(conn);

			Usuario user = buscarUsuarioOuFalhar(dao, logDao, email);
			validarStatus(user);
			validarBloqueioTemporario(dao, user);

			if (!PasswordUtils.verifyPassword(senhaPura, user.getSenhaHashString())) {
				tratarFalhaLogin(dao, logDao, user, email);
			}

			verificarExpiracaoSenha(user);
			registrarSucessoLogin(dao, logDao, user);

			return user;

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO DE INFRAESTRUTURA NO LOGIN", e);
		} finally {
			SensitiveData.safeClear(senhaPura);
		}
	}

	private Usuario buscarUsuarioOuFalhar(UsuarioDao dao, LogSistemaDao logDao, String email) {
		Usuario user = dao.buscarPorEmail(email);
		if (user == null) {
			Map<String, Object> detalhes = new HashMap<>();
			detalhes.put("tentativa_email", email);
			detalhes.put("motivo", "Email nao encontrado");

			logDao.save(AuditLogHelper.gerarLogErroComDetalhes("SEGURANCA", "LOGIN_FALHA", "usuario",
					"USUÁRIO NÃO ENCONTRADO", detalhes));

			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO OU SENHA INVÁLIDOS.");
		}
		return user;
	}

	private void validarStatus(Usuario user) {
		if (user.getStatus() == StatusUsuario.BLOQUEADO) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"ESTA CONTA FOI BLOQUEADA PELO ADMINISTRADOR.");
		}
		if (user.getStatus() == StatusUsuario.INATIVO) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO INATIVO. CONTATE O SUPORTE.");
		}
	}

	private void tratarFalhaLogin(UsuarioDao dao, LogSistemaDao logDao, Usuario user, String email)
			throws SQLException {
		if (user.isMaster()) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "SENHA INCORRETA.");
		}

		int maxTentativas = parametroService.getInt(ParametroChave.LOGIN_MAX_TENTATIVAS, 5);
		int tentativas = dao.incrementarERetornarTentativas(email);
		user.setTentativasFalhas(tentativas);

		if (tentativas < maxTentativas) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"SENHA INCORRETA. TENTATIVA " + tentativas + " DE " + maxTentativas);
		}

		processarBloqueio(dao, logDao, user, tentativas);
	}

	private void processarBloqueio(UsuarioDao dao, LogSistemaDao logDao, Usuario user, int tentativas)
			throws SQLException {
		int limiteReincidencia = 3;
		int totalBloqueios24h = logDao.contarLogsBloqueioRecentes(user.getIdUsuario(), 24);

		if (totalBloqueios24h >= limiteReincidencia) {
			executarBloqueioPermanente(dao, logDao, user, tentativas, totalBloqueios24h);
		} else {
			executarBloqueioTemporario(dao, logDao, user, tentativas, totalBloqueios24h);
		}
	}

	private void executarBloqueioPermanente(UsuarioDao dao, LogSistemaDao logDao, Usuario user, int tent, int hist)
			throws SQLException {
		dao.bloquearUsuario(user.getIdUsuario());

		Map<String, Object> info = new HashMap<>();
		info.put("tentativas", tent);
		info.put("bloqueios_24h", hist);

		logDao.save(AuditLogHelper.gerarLogErroComDetalhes("SEGURANCA", "BLOQUEIO_PERMANENTE", "usuario",
				"EXCEDEU LIMITE DE REINCIDÊNCIA EM 24H", info));

		throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
				"ESTA CONTA FOI BLOQUEADA PERMANENTEMENTE POR REINCIDÊNCIA.");
	}

	private void executarBloqueioTemporario(UsuarioDao dao, LogSistemaDao logDao, Usuario user, int tent, int hist)
			throws SQLException {
		int minutos = parametroService.getInt(ParametroChave.LOGIN_TEMPO_BLOQUEIO_MIN, 5);
		LocalDateTime ate = LocalDateTime.now().plusMinutes(minutos);

		dao.bloquearTemporariamente(user.getIdUsuario(), ate);
		user.setBloqueadoAte(ate);

		Map<String, Object> info = new HashMap<>();
		info.put("tentativa", tent);
		info.put("sequencia_hoje", hist + 1);

		logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "BLOQUEIO_TEMPORARIO", "usuario", user.getIdUsuario(),
				null, info));

		throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
				"LIMITE ATINGIDO. SUSPENSO ATÉ " + TimeUtils.formatarDataHora(ate));
	}

	private void validarBloqueioTemporario(UsuarioDao dao, Usuario user) throws SQLException {
		if (user.getBloqueadoAte() != null) {
			if (user.getBloqueadoAte().isAfter(LocalDateTime.now())) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"ACESSO SUSPENSO ATÉ " + TimeUtils.formatarDataHora(user.getBloqueadoAte()));
			}
			dao.resetTentativasFalhas(user.getIdUsuario());
			user.setBloqueadoAte(null);
			user.setTentativasFalhas(0);
		}
	}

	private void verificarExpiracaoSenha(Usuario user) {
		if (user.getSenhaExpiraEm() != null && LocalDateTime.now().isAfter(user.getSenhaExpiraEm())) {
			user.setForcarResetSenha(true);
		}
	}

	private void registrarSucessoLogin(UsuarioDao dao, LogSistemaDao logDao, Usuario user) throws SQLException {
		dao.atualizarUltimoLogin(user.getIdUsuario());
		dao.resetTentativasFalhas(user.getIdUsuario());

		Map<String, Object> detalhes = new HashMap<>();
		detalhes.put("info", "Sessao iniciada com sucesso");
		detalhes.put("horario", TimeUtils.formatarDataHora(LocalDateTime.now()));

		logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "LOGIN_SUCESSO", "usuario", user.getIdUsuario(), null,
				detalhes));
	}

	public String gerarHashSeguro(char[] senha) {
		validarComplexidade(senha);
		return PasswordUtils.hashPassword(senha);
	}

	public void validarComplexidade(char[] senha) {
		int min = parametroService.getInt(ParametroChave.SENHA_MIN_TAMANHO, 6);

		if (senha == null || senha.length < min) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"A SENHA DEVE TER NO MÍNIMO " + min + " CARACTERES.");
		}

		boolean maiuscula = false, numero = false, especial = false;
		String caracteresEspeciais = "!@#$%^&*(),.?\":{}|<>";

		for (char c : senha) {
			if (Character.isUpperCase(c))
				maiuscula = true;
			else if (Character.isDigit(c))
				numero = true;
			else if (caracteresEspeciais.indexOf(c) >= 0)
				especial = true;
		}

		if (!(maiuscula && numero && especial)) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"SENHA FRACA: REQUER LETRA MAIÚSCULA, NÚMERO E CARACTERE ESPECIAL.");
		}
	}

	public String resetarSenha(int idUsuarioAlvo, Usuario executor) {
		validarPermissaoReset(executor);

		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);

			validarExistenciaUsuario(dao, idUsuarioAlvo);

			String senhaPadrao = parametroService.getString(ParametroChave.SENHA_RESET_PADRAO, "Mudar@123");
			char[] senhaChars = senhaPadrao.toCharArray();

			try {
				String hash = gerarHashSeguro(senhaChars);

				persistirResetSenha(conn, dao, idUsuarioAlvo, hash, executor.getNome());

				return senhaPadrao;
			} finally {
				SensitiveData.safeClear(senhaChars);
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO RESETAR SENHA", e);
		}
	}

	public void alterarSenhaObrigatoria(int idUsuario, char[] novaSenha, char[] confirmacao) {
		validarIgualdadeSenhas(novaSenha, confirmacao);
		validarComplexidade(novaSenha);

		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = new UsuarioDao(conn);

			int diasValidade = parametroService.getInt(ParametroChave.FORCAR_TROCA_SENHA_DIAS, 90);
			LocalDateTime expiraEm = LocalDateTime.now().plusDays(diasValidade);
			String hash = PasswordUtils.hashPassword(novaSenha);

			persistirTrocaSenhaObrigatoria(conn, dao, idUsuario, hash, expiraEm);

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO SALVAR NOVA SENHA", e);
		} finally {
			SensitiveData.safeClear(novaSenha);
			SensitiveData.safeClear(confirmacao);
		}
	}

	private void validarPermissaoReset(Usuario executor) {
		if (!UsuarioPolicy.isPrivilegiado(executor)) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"APENAS UM USUÁRIO MASTER PODE RESETAR SENHAS.");
		}
	}

	private void validarExistenciaUsuario(UsuarioDao dao, int id) throws SQLException {
		if (dao.searchById(id) == null) {
			throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "USUÁRIO NÃO ENCONTRADO.");
		}
	}

	private void validarIgualdadeSenhas(char[] s1, char[] s2) {
		if (s1 == null || s2 == null || s1.length != s2.length) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "AS SENHAS DIGITADAS NÃO CONFEREM.");
		}

		int result = 0;

		for (int i = 0; i < s1.length; i++) {
			result |= s1[i] ^ s2[i];
		}

		if (result != 0) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "AS SENHAS DIGITADAS NÃO CONFEREM.");
		}
	}

	private void persistirResetSenha(Connection conn, UsuarioDao dao, int idAlvo, String hash, String nomeExec)
			throws SQLException {

		Usuario alvo = dao.searchById(idAlvo);
		if (alvo == null || alvo.isMaster()) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "AÇÃO NÃO PERMITIDA PARA ESTE USUÁRIO.");
		}
		ConnectionFactory.beginTransaction(conn);
		try {
			dao.atualizarSenha(idAlvo, hash);
			dao.resetTentativasFalhas(idAlvo);

			Map<String, String> detalhes = new HashMap<>();
			detalhes.put("executor", nomeExec);
			detalhes.put("mensagem", "Senha resetada para o padrao.");

			new LogSistemaDao(conn).save(
					AuditLogHelper.gerarLogSucesso("SEGURANCA", "RESET_SENHA", "usuario", idAlvo, null, detalhes));

			ConnectionFactory.commitTransaction(conn);
		} catch (SQLException e) {
			ConnectionFactory.rollbackTransaction(conn);
			throw e;
		}
	}

	private void persistirTrocaSenhaObrigatoria(Connection conn, UsuarioDao dao, int id, String hash, LocalDateTime exp)
			throws SQLException {
		ConnectionFactory.beginTransaction(conn);
		try {
			dao.atualizarSenhaAposReset(id, hash, exp);

			Map<String, String> detalhes = new HashMap<>();
			detalhes.put("resultado", "Sucesso");
			detalhes.put("validade_nova_senha", TimeUtils.formatarDataHora(exp));

			new LogSistemaDao(conn).save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "TROCA_SENHA_OBRIGATORIA",
					"usuario", id, null, detalhes));

			ConnectionFactory.commitTransaction(conn);
		} catch (SQLException e) {
			ConnectionFactory.rollbackTransaction(conn);
			throw e;
		}
	}

}