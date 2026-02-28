package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.domain.event.DomainEventPublisher;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.security.PasswordUtils;
import com.br.yat.gerenciador.security.SecurityService;
import com.br.yat.gerenciador.security.SensitiveData;
import com.br.yat.gerenciador.util.TimeUtils;

public class AutenticacaoService extends BaseService {

	private final ParametroSistemaService parametroService;
	private final DaoFactory daoFactory;

	public AutenticacaoService(ParametroSistemaService parametroService, DaoFactory daoFactory,
			DomainEventPublisher eventPublisher, SecurityService securityService) {
		super(eventPublisher, securityService);
		this.parametroService = parametroService;
		this.daoFactory = daoFactory;
	}

	public Usuario autenticar(String email, char[] senhaPura) {
		try {
			return executeInTransaction(conn -> {

				UsuarioDao dao = daoFactory.createUsuarioDao(conn);

				Usuario user = buscarUsuarioOuFalhar(conn, dao, email);

				validarStatus(user);
				validarBloqueioTemporario(dao, user);

				if (!PasswordUtils.verifyPassword(senhaPura, user.getSenhaHashString())) {
					tratarFalhaLogin(conn, dao, user, email);
				}

				verificarExpiracaoSenha(user);
				registrarSucessoLogin(conn, dao, user);

				return user;
			});
		} finally {
			SensitiveData.safeClear(senhaPura);
		}
	}

	private Usuario buscarUsuarioOuFalhar(Connection conn, UsuarioDao dao, String email) {
		Usuario user = dao.buscarPorEmail(email);
		if (user == null) {
			registrarLogErro("SEGURANCA", "LOGIN_FALHA", "usuario",
					new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO NÃO ENCONTRADO: " + email));

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

	private void tratarFalhaLogin(Connection conn, UsuarioDao dao, Usuario user, String email) {

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

		processarBloqueio(conn, dao, user, tentativas);
	}

	private void processarBloqueio(Connection conn, UsuarioDao dao, Usuario user, int tentativas) {

		LogSistemaDao logDao = daoFactory.createLogSistemaDao(conn);

		int limiteReincidencia = 3;
		int totalBloqueios24h = logDao.contarLogsBloqueioRecentes(user.getIdUsuario(), 24);

		if (totalBloqueios24h >= limiteReincidencia) {
			executarBloqueioPermanente(conn, dao, user, tentativas, totalBloqueios24h);
		} else {
			executarBloqueioTemporario(conn, dao, user, tentativas, totalBloqueios24h);
		}
	}

	private void executarBloqueioPermanente(Connection conn, UsuarioDao dao, Usuario user, int tentativas,
			int historico24h) {

		dao.bloquearUsuario(user.getIdUsuario());

		Map<String, Object> detalhes = new HashMap<>();
		detalhes.put("tentativas", tentativas);
		detalhes.put("bloqueios_24h", historico24h);

		registrarLogSucesso(conn, "SEGURANCA", "BLOQUEIO_PERMANENTE", "usuario", user.getIdUsuario(), null, detalhes);

		throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
				"ESTA CONTA FOI BLOQUEADA PERMANENTEMENTE POR REINCIDÊNCIA.");
	}

	private void executarBloqueioTemporario(Connection conn, UsuarioDao dao, Usuario user, int tentativas,
			int historico24h) {

		int minutos = parametroService.getInt(ParametroChave.LOGIN_TEMPO_BLOQUEIO_MIN, 5);

		LocalDateTime ate = LocalDateTime.now().plusMinutes(minutos);

		dao.bloquearTemporariamente(user.getIdUsuario(), ate);
		user.setBloqueadoAte(ate);

		Map<String, Object> detalhes = new HashMap<>();
		detalhes.put("tentativa", tentativas);
		detalhes.put("sequencia_hoje", historico24h + 1);

		registrarLogSucesso(conn, "SEGURANCA", "BLOQUEIO_TEMPORARIO", "usuario", user.getIdUsuario(), null, detalhes);

		throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
				"LIMITE ATINGIDO. SUSPENSO ATÉ " + TimeUtils.formatarDataHora(ate));
	}

	private void validarBloqueioTemporario(UsuarioDao dao, Usuario user) {

		LocalDateTime bloqueado = user.getBloqueadoAte();

		if (bloqueado == null)
			return;

		if (bloqueado.isAfter(LocalDateTime.now())) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"USUÁRIO BLOQUEADO TEMPORARIAMENTE ATÉ " + TimeUtils.formatarDataHora(bloqueado));
		}

		dao.resetTentativasFalhas(user.getIdUsuario());
		user.setBloqueadoAte(null);
		user.setTentativasFalhas(0);
	}

	private void verificarExpiracaoSenha(Usuario user) {
		if (user.getSenhaExpiraEm() != null && LocalDateTime.now().isAfter(user.getSenhaExpiraEm())) {
			user.setForcarResetSenha(true);
		}
	}

	private void registrarSucessoLogin(Connection conn, UsuarioDao dao, Usuario user) {

		dao.atualizarUltimoLogin(user.getIdUsuario());
		dao.resetTentativasFalhas(user.getIdUsuario());

		Map<String, Object> detalhes = new HashMap<>();
		detalhes.put("info", "Sessao iniciada com sucesso");
		detalhes.put("horario", TimeUtils.formatarDataHora(LocalDateTime.now()));

		registrarLogSucesso(conn, "SEGURANCA", "LOGIN_SUCESSO", "usuario", user.getIdUsuario(), null, detalhes);
	}

	public boolean processarSenha(Usuario usuario, boolean isNovo, Usuario executor, Usuario estadoAnterior) {
		char[] senhaNova = usuario.getSenhaHash();
		char[] senhaAntiga = usuario.getSenhaAntiga();
		char[] senhaConfirmar = usuario.getConfirmarSenha();

		try {
			if (isNovo && (senhaNova == null || senhaNova.length == 0))
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A SENHA É OBRIGATÓRIA.");

			if (senhaNova == null || senhaNova.length == 0)
				return false;

			if (senhaConfirmar == null || senhaNova.length != senhaConfirmar.length) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
			}

			int diff = 0;
			for (int i = 0; i < senhaNova.length; i++) {
				diff |= senhaNova[i] ^ senhaConfirmar[i];
			}

			if (diff != 0) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A CONFIRMAÇÃO DE SENHA NÃO CONFERE.");
			}

			boolean alterandoPropriaSenha = !isNovo && executor != null && executor.getIdUsuario() != null
					&& executor.getIdUsuario().equals(usuario.getIdUsuario());

			if (alterandoPropriaSenha) {
				if (estadoAnterior == null
						|| !PasswordUtils.verifyPassword(senhaAntiga, estadoAnterior.getSenhaHashString())) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "A SENHA ANTIGA ESTÁ INCORRETA.");
				}
			} else if (!isNovo && !UsuarioPolicy.isPrivilegiado(executor)) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "Sem permissão para alterar senha.");
			}

			usuario.setSenhaHashString(gerarHashSeguro(senhaNova));
			return true;
		} finally {
			SensitiveData.safeClear(senhaNova);
			SensitiveData.safeClear(senhaAntiga);
			SensitiveData.safeClear(senhaConfirmar);
		}
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

		return executeInTransaction(conn -> {

			UsuarioDao dao = daoFactory.createUsuarioDao(conn);
			Usuario alvo = dao.searchById(idUsuarioAlvo);

			if (alvo == null) {
				throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "USUÁRIO NÃO ENCONTRADO.");
			}

			if (alvo.isMaster()) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"AÇÃO NÃO PERMITIDA PARA ESTE USUÁRIO.");
			}

			String senhaPadrao = parametroService.getString(ParametroChave.SENHA_RESET_PADRAO, "Mudar@123");

			char[] senhaChars = senhaPadrao.toCharArray();

			try {

				String hash = gerarHashSeguro(senhaChars);

				dao.atualizarSenha(idUsuarioAlvo, hash);
				dao.resetTentativasFalhas(idUsuarioAlvo);

				registrarLogSucesso(conn, "SEGURANCA", "RESET_SENHA", "usuario", idUsuarioAlvo, null,
						"Senha resetada para padrão");

				return senhaPadrao;

			} finally {
				SensitiveData.safeClear(senhaChars);
			}
		});
	}

	public void alterarSenhaObrigatoria(int idUsuario, char[] novaSenha, char[] confirmacao) {

		validarIgualdadeSenhas(novaSenha, confirmacao);
		validarComplexidade(novaSenha);

		try {
			executeInTransaction(conn -> {

				UsuarioDao dao = daoFactory.createUsuarioDao(conn);

				int dias = parametroService.getInt(ParametroChave.FORCAR_TROCA_SENHA_DIAS, 90);

				LocalDateTime expira = LocalDateTime.now().plusDays(dias);
				String hash = PasswordUtils.hashPassword(novaSenha);

				dao.atualizarSenhaAposReset(idUsuario, hash, expira);

				Map<String, String> detalhes = new HashMap<>();
				detalhes.put("resultado", "Sucesso");
				detalhes.put("validade_nova_senha", TimeUtils.formatarDataHora(expira));

				registrarLogSucesso(conn, "SEGURANCA", "TROCA_SENHA_OBRIGATORIA", "usuario", idUsuario, null, detalhes);

				return null;
			});
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

}