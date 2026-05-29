package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import com.br.yat.gerenciador.dao.DaoFactory;
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

/**
 * Orquestra autenticação, bloqueio por tentativas e operações de senha.
 */
public class AutenticacaoService extends BaseService {

	private final ParametroSistemaService parametroService;
	private final DaoFactory daoFactory;
	private final SenhaPolicyService senhaPolicyService;
	private final LoginBloqueioService loginBloqueioService;

	public AutenticacaoService(ParametroSistemaService parametroService, DaoFactory daoFactory,
			DomainEventPublisher eventPublisher, SecurityService securityService) {
		super(eventPublisher, securityService);
		this.parametroService = parametroService;
		this.daoFactory = daoFactory;
		this.senhaPolicyService = new SenhaPolicyService(parametroService);
		this.loginBloqueioService = new LoginBloqueioService(parametroService, daoFactory, this::registrarLogSucesso);
	}

	public Usuario autenticar(String email, char[] senhaPura) {
		try {
			boolean[] senhaInvalida = { false };

			Usuario usuario = executeInTransaction(conn -> {
				UsuarioDao dao = daoFactory.createUsuarioDao(conn);
				Usuario encontrado = buscarUsuarioOuFalhar(dao, email, senhaPura);

				validarStatus(encontrado);
				loginBloqueioService.validarBloqueioTemporario(conn, dao, encontrado);

				if (!verificarSenha(senhaPura, encontrado.getSenhaHashString())) {
					loginBloqueioService.tratarFalhaLogin(conn, dao, encontrado);
					senhaInvalida[0] = true;
					return encontrado;
				}

				verificarExpiracaoSenha(encontrado);
				registrarSucessoLogin(conn, dao, encontrado);

				return encontrado;
			});

			if (senhaInvalida[0]) {
				loginBloqueioService.lancarExcecaoFalhaLogin(usuario);
			}

			return usuario;
		} finally {
			SensitiveData.safeClear(senhaPura);
		}
	}

	public boolean processarSenha(Usuario usuario, boolean isNovo, Usuario executor, Usuario estadoAnterior) {
		char[] senhaNova = senhaPolicyService.copiarSenha(usuario.getSenhaHash());
		char[] senhaAntiga = senhaPolicyService.copiarSenha(usuario.getSenhaAntiga());
		char[] senhaConfirmar = senhaPolicyService.copiarSenha(usuario.getConfirmarSenha());

		try {
			if (isNovo && isSenhaVazia(senhaNova)) {
				throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A SENHA É OBRIGATÓRIA.");
			}

			if (isSenhaVazia(senhaNova)) {
				return false;
			}

			senhaPolicyService.validarConfirmacaoSenha(senhaNova, senhaConfirmar);
			validarPermissaoAlteracaoSenha(usuario, isNovo, executor, estadoAnterior, senhaAntiga);

			usuario.setSenhaHashString(senhaPolicyService.gerarHashSeguro(senhaNova));
			return true;
		} finally {
			senhaPolicyService.limparSenhas(senhaNova, senhaAntiga, senhaConfirmar);
		}
	}

	public String gerarHashSeguro(char[] senha) {
		return senhaPolicyService.gerarHashSeguro(senha);
	}

	public void validarComplexidade(char[] senha) {
		senhaPolicyService.validarComplexidade(senha);
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
			char[] senhaPadraoChars = senhaPadrao.toCharArray();
			char[] senhaParaHash = null;

			try {
				senhaParaHash = senhaPolicyService.copiarSenha(senhaPadraoChars);
				String hash = senhaPolicyService.gerarHashSeguro(senhaParaHash);

				dao.atualizarSenha(idUsuarioAlvo, hash);
				dao.resetTentativasFalhas(idUsuarioAlvo);

				registrarLogSucesso(conn, "SEGURANCA", "RESET_SENHA", "usuario", idUsuarioAlvo, null,
						"Senha resetada para padrão");

				return senhaPadrao;
			} finally {
				senhaPolicyService.limparSenhas(senhaParaHash, senhaPadraoChars);
			}
		});
	}

	public void alterarSenhaObrigatoria(int idUsuario, char[] novaSenha, char[] confirmacao) {
		char[] senhaNova = senhaPolicyService.copiarSenha(novaSenha);
		char[] senhaConfirmacao = senhaPolicyService.copiarSenha(confirmacao);

		try {
			senhaPolicyService.validarIgualdadeSenhas(senhaNova, senhaConfirmacao);
			senhaPolicyService.validarComplexidade(senhaNova);

			executeInTransaction(conn -> {
				UsuarioDao dao = daoFactory.createUsuarioDao(conn);

				int diasValidade = parametroService.getInt(ParametroChave.FORCAR_TROCA_SENHA_DIAS, 90);
				LocalDateTime senhaExpiraEm = LocalDateTime.now().plusDays(diasValidade);
				String hash = PasswordUtils.hashPassword(senhaNova);

				dao.atualizarSenhaAposReset(idUsuario, hash, senhaExpiraEm);

				Map<String, String> detalhes = new HashMap<>();
				detalhes.put("resultado", "Sucesso");
				detalhes.put("validade_nova_senha", TimeUtils.formatarDataHora(senhaExpiraEm));

				registrarLogSucesso(conn, "SEGURANCA", "TROCA_SENHA_OBRIGATORIA", "usuario", idUsuario, null,
						detalhes);

				return null;
			});
		} finally {
			senhaPolicyService.limparSenhas(senhaNova, senhaConfirmacao);
		}
	}

	// --- Autenticação ---

	private Usuario buscarUsuarioOuFalhar(UsuarioDao dao, String email, char[] senhaPura) {
		Usuario usuario = dao.buscarPorEmailParaLogin(email);

		if (usuario == null) {
			registrarLogErro("SEGURANCA", "LOGIN_FALHA", "usuario",
					new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO NÃO ENCONTRADO: " + email));

			verificarSenha(senhaPura, PasswordUtils.dummyHashForTimingMitigation());

			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO OU SENHA INVÁLIDOS.");
		}

		return usuario;
	}

	private boolean verificarSenha(char[] senha, String hashArmazenado) {
		char[] senhaCopia = senhaPolicyService.copiarSenha(senha);
		try {
			return PasswordUtils.verifyPassword(senhaCopia, hashArmazenado);
		} finally {
			SensitiveData.safeClear(senhaCopia);
		}
	}

	private void validarStatus(Usuario usuario) {
		if (usuario.getStatus() == StatusUsuario.BLOQUEADO) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"ESTA CONTA FOI BLOQUEADA PELO ADMINISTRADOR.");
		}

		if (usuario.getStatus() == StatusUsuario.INATIVO) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO INATIVO. CONTATE O SUPORTE.");
		}
	}

	private void verificarExpiracaoSenha(Usuario usuario) {
		if (usuario.getSenhaExpiraEm() != null && LocalDateTime.now().isAfter(usuario.getSenhaExpiraEm())) {
			usuario.setForcarResetSenha(true);
		}
	}

	private void registrarSucessoLogin(Connection conn, UsuarioDao dao, Usuario usuario) {
		dao.atualizarUltimoLogin(usuario.getIdUsuario());
		dao.resetTentativasFalhas(usuario.getIdUsuario());

		Map<String, Object> detalhes = new HashMap<>();
		detalhes.put("info", "Sessao iniciada com sucesso");
		detalhes.put("horario", TimeUtils.formatarDataHora(LocalDateTime.now()));

		registrarLogSucesso(conn, "SEGURANCA", "LOGIN_SUCESSO", "usuario", usuario.getIdUsuario(), null, detalhes);
	}

	// --- Permissões e senha ---

	private void validarPermissaoAlteracaoSenha(Usuario usuario, boolean isNovo, Usuario executor,
			Usuario estadoAnterior, char[] senhaAntiga) {
		boolean alterandoPropriaSenha = !isNovo && executor != null && executor.getIdUsuario() != null
				&& executor.getIdUsuario().equals(usuario.getIdUsuario());

		if (alterandoPropriaSenha) {
			if (estadoAnterior == null || !verificarSenha(senhaAntiga, estadoAnterior.getSenhaHashString())) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "A SENHA ANTIGA ESTÁ INCORRETA.");
			}
			return;
		}

		if (!isNovo && !UsuarioPolicy.isPrivilegiado(executor)) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "Sem permissão para alterar senha.");
		}
	}

	private void validarPermissaoReset(Usuario executor) {
		if (!UsuarioPolicy.isPrivilegiado(executor)) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"APENAS UM USUÁRIO MASTER PODE RESETAR SENHAS.");
		}
	}

	private static boolean isSenhaVazia(char[] senha) {
		return senha == null || senha.length == 0;
	}
}
