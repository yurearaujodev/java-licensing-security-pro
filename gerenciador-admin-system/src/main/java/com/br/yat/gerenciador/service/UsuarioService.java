package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.domain.event.DomainEventPublisher;
import com.br.yat.gerenciador.domain.event.ErrorEvents;
import com.br.yat.gerenciador.domain.event.UsuarioEvents;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.dto.Niveis;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.security.SecurityService;
import com.br.yat.gerenciador.util.TimeUtils;
import com.br.yat.gerenciador.validation.UsuarioValidationUtils;

public class UsuarioService extends BaseService {

	private final AutenticacaoService authService;
	private final ParametroSistemaService parametroService;
	private final UsuarioPermissaoService permissaoService;
	private final DaoFactory daoFactory;
	private final BootstrapService bootstrapService;

	private static final MenuChave CHAVE_USUARIO = MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES;

	public UsuarioService(AutenticacaoService authService, ParametroSistemaService parametroService,
			UsuarioPermissaoService permissaoService, DaoFactory daoFactory, BootstrapService bootstrapService,
			DomainEventPublisher eventPublisher, SecurityService securityService) {
		super(eventPublisher, securityService);

		this.authService = authService;
		this.parametroService = parametroService;
		this.permissaoService = permissaoService;
		this.daoFactory = daoFactory;
		this.bootstrapService = bootstrapService;
	}

	public List<Usuario> listarUsuarios(String termo, Usuario executor) {
		return execute(conn -> {
			validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.READ);

			UsuarioDao dao = daoFactory.createUsuarioDao(conn);
			return (termo == null || termo.trim().isEmpty()) ? dao.listAll() : dao.listarPorNomeOuEmail(termo);
		});
	}

	public List<Usuario> listarUsuariosVisiveis(String termo, Usuario executor) {
		return filtrarMastersSeNecessario(listarUsuarios(termo, executor), executor);
	}

	public List<Usuario> listarExcluidosVisiveis(Usuario executor) {
		return filtrarMastersSeNecessario(listarExcluidos(executor), executor);
	}

	private List<Usuario> filtrarMastersSeNecessario(List<Usuario> lista, Usuario executor) {

		if (executor != null && UsuarioPolicy.isPrivilegiado(executor)) {
			return lista;
		}

		return lista.stream().filter(u -> !u.isMaster()).toList();
	}

	public List<Usuario> listarUsuariosUltimoLogin(String termo, Usuario executor) {
		return execute(conn -> {
			validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.READ);

			UsuarioDao dao = daoFactory.createUsuarioDao(conn);
			List<Usuario> lista = (termo == null || termo.trim().isEmpty()) ? dao.listAll()
					: dao.listarPorNomeOuEmail(termo);
			lista.forEach(u -> {
				if (u.getUltimoLogin() != null) {
					u.setTempoDesdeUltimoAcesso(TimeUtils.formatarTempoDecorrido(u.getUltimoLogin()));
				}
			});
			return lista;
		});
	}

	public List<Usuario> listarExcluidos(Usuario executor) {
		return execute(conn -> {
			validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.DELETE);
			UsuarioDao dao = daoFactory.createUsuarioDao(conn);
			return dao.listarExcluidos();
		});
	}

	public void salvarUsuario(Usuario usuario, Map<MenuChave, List<String>> permissoesGranulares,
			Map<MenuChave, String> datasExpiracao, Usuario executor) {
		try {
			validarInicial(usuario, permissoesGranulares, datasExpiracao);

			Map<MenuChave, List<String>> permissoesFinal = prepararPermissoes(usuario, permissoesGranulares);
			Map<MenuChave, LocalDateTime> datasFinal = prepararDatas(usuario, datasExpiracao);

			executeInTransactionVoid(conn -> processarUsuario(usuario, executor, permissoesFinal, datasFinal, conn));

		} catch (ValidationException ve) {
			throw ve;
		} catch (RuntimeException e) {
			eventPublisher.publish(new ErrorEvents.ErroSistema("ERRO", "SALVAR_USUARIO", "usuario", e.getMessage()),
					null);
			throw e;
		}
	}

	private void validarInicial(Usuario usuario, Map<MenuChave, List<String>> permissoes,
			Map<MenuChave, String> datasExpiracao) {
		validarDados(usuario, permissoes);
		validarRestricoesMaster(usuario);
		validarDatasExpiracao(datasExpiracao);
	}

	private void processarUsuario(Usuario usuario, Usuario executor, Map<MenuChave, List<String>> permissoesFinal,
			Map<MenuChave, LocalDateTime> datasFinal, Connection conn) {

		boolean isSetupInicial = isSetupInicial(usuario, executor, conn);

		if (!isSetupInicial) {
			validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.WRITE);
		}

		UsuarioDao usuarioDao = daoFactory.createUsuarioDao(conn);
		validarRegrasPersistencia(usuarioDao, usuario);

		Usuario alvoExistente = obterUsuarioExistente(usuario, usuarioDao);
		Usuario estadoAnterior = alvoExistente != null ? Usuario.snapshotParaValidacaoSenha(alvoExistente) : null;

		validarHierarquiaAlteracao(conn, executor, alvoExistente, isSetupInicial);

		boolean senhaAlterada = processarSenhaSeNecessario(usuario, executor, estadoAnterior);

		salvarOuAtualizar(usuarioDao, usuario, estadoAnterior, alvoExistente == null, conn);
		registrarLogSenhaAlterada(conn, usuario, senhaAlterada, alvoExistente == null);

		List<UsuarioPermissao> permissoesSincronizadas = permissaoService.montarPermissoes(conn, usuario, executor,
				isSetupInicial, permissoesFinal, datasFinal);
		permissaoService.sincronizarPermissoes(conn, usuario, permissoesSincronizadas);
	}

	private Map<MenuChave, List<String>> prepararPermissoes(Usuario usuario, Map<MenuChave, List<String>> permissoes) {
		return UsuarioPolicy.ignoraValidacaoPermissao(usuario) ? new HashMap<>()
				: (permissoes != null ? new HashMap<>(permissoes) : new HashMap<>());
	}

	private Map<MenuChave, LocalDateTime> prepararDatas(Usuario usuario, Map<MenuChave, String> datas) {
		if (UsuarioPolicy.ignoraValidacaoPermissao(usuario) || datas == null)
			return new HashMap<>();

		Map<MenuChave, LocalDateTime> datasFinal = new HashMap<>();
		LocalDateTime agora = LocalDateTime.now();

		datas.forEach((menu, dataStr) -> {
			if (dataStr != null && !dataStr.isBlank()) {
				datasFinal.put(menu, parseDataExpiracao(menu, dataStr, agora));
			}
		});

		return datasFinal;
	}

	private boolean isSetupInicial(Usuario usuario, Usuario executor, Connection conn) {
		return bootstrapService.permitirCriacaoInicialMaster(conn, executor, usuario);
	}

	private Usuario obterUsuarioExistente(Usuario usuario, UsuarioDao dao) {
		if (usuario.getIdUsuario() == null || usuario.getIdUsuario() == 0)
			return null;
		Usuario existente = dao.searchById(usuario.getIdUsuario());
		if (existente == null)
			throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "USUÁRIO NÃO ENCONTRADO.");
		return existente;
	}

	private boolean processarSenhaSeNecessario(Usuario usuario, Usuario executor, Usuario estadoAnterior) {
		boolean isNovo = usuario.getIdUsuario() == null || usuario.getIdUsuario() == 0;
		boolean senhaAlterada = authService.processarSenha(usuario, isNovo, executor, estadoAnterior);
		if (senhaAlterada) {
			int diasExpira = parametroService.getInt(ParametroChave.SENHA_EXPIRA_DIAS, 90);
			usuario.setSenhaExpiraEm(LocalDateTime.now().plusDays(diasExpira));
		}
		return senhaAlterada;
	}

	private void registrarLogSenhaAlterada(Connection conn, Usuario usuario, boolean senhaAlterada, boolean isNovo) {
		if (senhaAlterada && !isNovo) {
			eventPublisher.publish(new UsuarioEvents.SenhaAlterada(usuario), conn);
		}
	}

	private void validarDatasExpiracao(Map<MenuChave, String> datasExpiracao) {
		if (datasExpiracao == null || datasExpiracao.isEmpty())
			return;

		LocalDateTime agora = LocalDateTime.now();

		datasExpiracao.forEach((menu, dataStr) -> {
			if (dataStr == null || dataStr.isBlank())
				return;
			parseDataExpiracao(menu, dataStr, agora);
		});
	}

	private LocalDateTime parseDataExpiracao(MenuChave menu, String dataStr, LocalDateTime agora) {
		try {
			LocalDateTime dataDigitada = TimeUtils.parseDataHora(dataStr);
			if (dataDigitada.isBefore(agora.minusMinutes(1)))
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"A DATA DE EXPIRAÇÃO PARA [" + menu + "] NÃO PODE SER NO PASSADO.");
			if (dataDigitada.isAfter(agora.plusYears(10)))
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"A DATA DE EXPIRAÇÃO PARA [" + menu + "] EXCEDEU O LIMITE (MÁX 10 ANOS).");
			return dataDigitada;
		} catch (ValidationException ve) {
			throw ve;
		} catch (Exception e) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"FORMATO DE DATA INVÁLIDO PARA O MENU: " + menu);
		}
	}

	private void validarHierarquiaAlteracao(Connection conn, Usuario executor, Usuario alvoExistente,
			boolean isSetupInicial) {

		if (isSetupInicial || alvoExistente == null)
			return;

		Niveis niveis = obterNiveis(conn, executor, alvoExistente);

		if (!UsuarioPolicy.podeAlterar(executor, alvoExistente, niveis.executor(), niveis.alvo())) {

			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"Privilégios insuficientes para alterar este usuário.");
		}
	}

	private void salvarOuAtualizar(UsuarioDao dao, Usuario usuario, Usuario anterior, boolean isNovo, Connection conn) {
		if (isNovo) {
			int id = dao.save(usuario);
			usuario.setIdUsuario(id);
			eventPublisher.publish(new UsuarioEvents.Criado(usuario), conn);
		} else {
			if (anterior.getStatus() != StatusUsuario.ATIVO && usuario.getStatus() == StatusUsuario.ATIVO) {
				dao.resetTentativasFalhas(usuario.getIdUsuario());
				eventPublisher.publish(new UsuarioEvents.StatusAlterado(anterior, usuario), conn);
			}

			dao.update(usuario);

			Usuario depois = dao.searchById(usuario.getIdUsuario());
			eventPublisher.publish(new UsuarioEvents.Alterado(anterior, depois), conn);

		}
	}

	public void excluirUsuario(int idUsuario, Usuario executor) {
		if (idUsuario <= 0) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "ID DE USUÁRIO INVÁLIDO.");
		}

		try {
			executeInTransactionVoid(conn -> {

				validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.DELETE);

				UsuarioDao dao = daoFactory.createUsuarioDao(conn);
				Usuario anterior = dao.searchById(idUsuario);

				if (anterior == null) {
					throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND,
							"O USUÁRIO QUE VOCÊ ESTÁ TENTANDO EXCLUIR NÃO EXISTE OU JÁ FOI REMOVIDO.");
				}

				Niveis niveis = obterNiveis(conn, executor, anterior);

				if (!UsuarioPolicy.podeExcluir(executor, anterior, niveis.executor(), niveis.alvo())) {

					throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
							"Você não tem permissão para excluir este usuário.");
				}

				dao.softDeleteById(idUsuario);

				eventPublisher.publish(new UsuarioEvents.Excluido(anterior), conn);
			});

		} catch (RuntimeException e) {
			eventPublisher.publish(new ErrorEvents.ErroSistema("ERRO", "EXCLUIR_USUARIO", "usuario", e.getMessage()),
					null);
			throw e;
		}
	}

	private Niveis obterNiveis(Connection conn, Usuario executor, Usuario alvo) {
		PermissaoDao pDao = daoFactory.createPermissaoDao(conn);

		Integer nivelExecutor = pDao.buscarMaiorNivelDoUsuario(executor.getIdUsuario());
		Integer nivelAlvo = pDao.buscarMaiorNivelDoUsuario(alvo.getIdUsuario());

		int nExecutor = nivelExecutor != null ? nivelExecutor : 0;
		int nAlvo = nivelAlvo != null ? nivelAlvo : 0;

		return new Niveis(nExecutor, nAlvo);
	}

	public void restaurarUsuario(int idUsuario, Usuario executor) {
		try {
			executeInTransactionVoid(conn -> {

				validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.DELETE);

				UsuarioDao dao = daoFactory.createUsuarioDao(conn);

				dao.restaurar(idUsuario);

				Usuario depois = dao.searchById(idUsuario);
				eventPublisher.publish(new UsuarioEvents.Restaurado(depois), conn);

			});

		} catch (RuntimeException e) {
			eventPublisher.publish(new ErrorEvents.ErroSistema("ERRO", "RESTAURAR_USUARIO", "usuario", e.getMessage()),
					null);
			throw e;
		}
	}

	private void validarRegrasPersistencia(UsuarioDao dao, Usuario usuario) {
		Usuario masterExistente = dao.buscarMasterUnico();

		// Impede criação de novo master se já existir um
		if (usuario.isMaster()
				&& (masterExistente != null && !masterExistente.getIdUsuario().equals(usuario.getIdUsuario()))) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"JÁ EXISTE UM USUÁRIO MASTER CADASTRADO. NÃO É PERMITIDO ALTERAR OUTRO USUÁRIO PARA MASTER.");
		}

		// Garante que usuários comuns nunca sejam master
		if (!UsuarioPolicy.isPrivilegiado(usuario)) {
			usuario.setMaster(false);
		}

		// Mantém master existente como master
		if (usuario.getIdUsuario() != null && usuario.getIdUsuario() > 0) {
			Usuario base = dao.searchById(usuario.getIdUsuario());
			if (base != null && base.isMaster()) {
				usuario.setMaster(true);
			}
		}

		validarDuplicidadeEmail(dao, usuario);
	}

	private void validarRestricoesMaster(Usuario usuario) {

		if (!UsuarioPolicy.podeAlterarStatusMaster(usuario)) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"O STATUS DO MASTER NÃO PODE SER ALTERADO.");
		}

		if (usuario.getEmpresa() == null || usuario.getEmpresa().getIdEmpresa() == null) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A EMPRESA É OBRIGATÓRIA.");
		}
	}

	public boolean podeEditarPermissoes(Usuario u) {
		return UsuarioPolicy.podeEditarPermissoes(u);
	}

	private void validarDados(Usuario usuario, Map<MenuChave, List<String>> chaves) {
		UsuarioValidationUtils.validarUsuario(usuario);

		if (!UsuarioPolicy.isPrivilegiado(usuario) && (chaves == null || chaves.isEmpty())
				&& usuario.getPerfil() == null) {

			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING,
					"O USUÁRIO DEVE TER AO MENOS UMA PERMISSÃO.");
		}
	}

	private void validarDuplicidadeEmail(UsuarioDao dao, Usuario u) {
		Usuario existente = dao.buscarPorEmailInclusoExcluidos(u.getEmail());
		if (existente != null && (u.getIdUsuario() == null || !existente.getIdUsuario().equals(u.getIdUsuario()))) {
			throw new ValidationException(ValidationErrorType.DUPLICATE_ENTRY,
					"ESTE E-MAIL JÁ ESTÁ CADASTRADO NO SISTEMA (ATIVO OU EXCLUÍDO).");
		}
	}

}