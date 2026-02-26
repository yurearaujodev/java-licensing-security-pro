package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.security.PermissaoContexto;
import com.br.yat.gerenciador.util.AuditLogHelper;
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
			UsuarioPermissaoService permissaoService, DaoFactory daoFactory, BootstrapService bootstrapService) {
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
	    if (executor == null || !UsuarioPolicy.isPrivilegiado(executor)) {
	        lista.removeIf(Usuario::isMaster);
	    }
	    return lista;
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

	public PermissaoContexto obterContextoPermissao(Integer idUsuario, MenuChave menu) {
		if (idUsuario == null || idUsuario <= 0)
			return PermissaoContexto.semPermissao();
		return execute(conn -> {
			Set<TipoPermissao> permissoes = permissaoService.listarPermissoesAtivasPorMenu(conn, idUsuario, menu)
					.stream().map(p -> {
						try {
							return TipoPermissao.valueOf(p);
						} catch (Exception e) {
							return null;
						}
					}).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
			return PermissaoContexto.comum(permissoes);
		});
	}

	public void salvarUsuario(Usuario usuario, Map<MenuChave, List<String>> permissoesGranulares,
			Map<MenuChave, String> datasExpiracao, Usuario executor) {

		// --- 1. Validações iniciais ---
		validarDados(usuario, permissoesGranulares);
		validarRestricoesMaster(usuario);
		validarDatasExpiracao(datasExpiracao);

		// --- 2. Preparar permissões e datas ---
		Map<MenuChave, List<String>> permissoesFinal = prepararPermissoes(usuario, permissoesGranulares);
		Map<MenuChave, String> datasFinal = prepararDatas(usuario, datasExpiracao);

		// --- 3. Inicia transação ---
		executeInTransactionVoid(conn -> {

			// --- 4. Determinar se é setup inicial ---
			boolean isSetupInicial = isSetupInicial(usuario, executor, conn);

			// --- 5. Valida acesso se não for setup inicial ---
			if (!isSetupInicial) {
				validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.WRITE);
			}

			// --- 6. Valida regras de persistência do usuário ---
			UsuarioDao usuarioDao = daoFactory.createUsuarioDao(conn);
			validarRegrasPersistencia(usuarioDao, usuario);

			// --- 7. Obter estado anterior (para updates) ---
			Usuario alvoExistente = obterUsuarioExistente(usuario, usuarioDao);
			Usuario estadoAnterior = alvoExistente != null ? Usuario.snapshotParaValidacaoSenha(alvoExistente) : null;

			// --- 8. Validar hierarquia e privilégios ---
			validarHierarquiaAlteracao(conn, executor, alvoExistente, isSetupInicial);

			// --- 9. Processar senha se necessário ---
			boolean senhaAlterada = processarSenhaSeNecessario(usuario, executor, estadoAnterior);

			// --- 10. Salvar ou atualizar usuário ---
			salvarOuAtualizar(usuarioDao, usuario, estadoAnterior, alvoExistente == null, conn);

			// --- 11. Registrar log de alteração de senha ---
			registrarLogSenhaAlterada(conn, usuario, senhaAlterada, alvoExistente == null);

			// --- 12. Montar permissões finais e sincronizar ---
			List<UsuarioPermissao> permissoesSincronizadas = permissaoService.montarPermissoes(conn, usuario, executor,
					isSetupInicial, permissoesFinal, datasFinal);
			permissaoService.sincronizarPermissoes(conn, usuario, permissoesSincronizadas);
		});
	}

	private Map<MenuChave, List<String>> prepararPermissoes(Usuario usuario, Map<MenuChave, List<String>> permissoes) {
		return UsuarioPolicy.isPrivilegiado(usuario) ? new HashMap<>()
				: (permissoes != null ? new HashMap<>(permissoes) : new HashMap<>());
	}

	private Map<MenuChave, String> prepararDatas(Usuario usuario, Map<MenuChave, String> datas) {
		return UsuarioPolicy.isPrivilegiado(usuario) ? new HashMap<>()
				: (datas != null ? new HashMap<>(datas) : new HashMap<>());
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
			registrarLogSucesso(conn, "SEGURANCA", "SENHA_ALTERADA", "usuario", usuario.getIdUsuario(),
					"O executor alterou a senha deste usuário.", null);
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

		if (!UsuarioPolicy.isPrivilegiado(executor) && UsuarioPolicy.isPrivilegiado(alvoExistente)) {

			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"APENAS UM MASTER PODE ALTERAR OUTRO MASTER.");
		}

		if (!temMaisPoder(conn, executor, alvoExistente)) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"Privilégios insuficientes para alterar este usuário.");
		}
	}

	private void salvarOuAtualizar(UsuarioDao dao, Usuario usuario, Usuario anterior, boolean isNovo, Connection conn) {
		LogSistemaDao logDao = daoFactory.createLogSistemaDao(conn);

		if (isNovo) {
			int id = dao.save(usuario);
			usuario.setIdUsuario(id);
			registrarLogSucesso(conn, "CADASTRO", "INSERIR_USUARIO", "usuario", id, null, usuario);
		} else {
			if (anterior.getStatus() != StatusUsuario.ATIVO && usuario.getStatus() == StatusUsuario.ATIVO) {
				dao.resetTentativasFalhas(usuario.getIdUsuario());

				Map<String, String> alteracaoStatus = new HashMap<>();
				alteracaoStatus.put("de", anterior.getStatus().name());
				alteracaoStatus.put("para", "ATIVO");
				alteracaoStatus.put("motivo", "Reativação manual via service layer");

				registrarLogSucesso(conn, "SEGURANCA", "REATIVACAO_MANUAL", "usuario", usuario.getIdUsuario(), null,
						alteracaoStatus);
			}

			dao.update(usuario);

			logDao.save(AuditLogHelper.gerarLogSucesso("CADASTRO", "ALTERAR_USUARIO", "usuario", usuario.getIdUsuario(),
					anterior, usuario));
		}
	}

	public void excluirUsuario(int idUsuario, Usuario executor) {
		if (idUsuario <= 0) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "ID DE USUÁRIO INVÁLIDO.");
		}

		if (executor != null && Objects.equals(executor.getIdUsuario(), idUsuario)) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"SEGURANÇA: VOCÊ NÃO PODE EXCLUIR SUA PRÓPRIA CONTA.");
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

				if (!UsuarioPolicy.podeExcluir(executor, anterior)) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
							"Você não tem permissão para excluir este usuário.");
				}

				if (!temMaisPoder(conn, executor, anterior)) {
					throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
							"Você não tem permissão para excluir este usuário, pois ele possui privilégios superiores.");
				}

				dao.softDeleteById(idUsuario);
				registrarLogSucesso(conn, "CADASTRO", "EXCLUIR_USUARIO", "usuario", idUsuario, anterior, null);
			});

		} catch (RuntimeException e) {
			registrarLogErro("ERRO", "EXCLUIR_USUARIO", "usuario", e);
			throw e;
		}
	}

	public void restaurarUsuario(int idUsuario, Usuario executor) {
		try {
			executeInTransactionVoid(conn -> {

				validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.DELETE);

				UsuarioDao dao = daoFactory.createUsuarioDao(conn);
				dao.restaurar(idUsuario);

				Map<String, String> detalhes = new HashMap<>();
				detalhes.put("status_anterior", "EXCLUIDO");
				detalhes.put("status_atual", "ATIVO");
				detalhes.put("acao", "Restauração de registro via lixeira");

				registrarLogSucesso(conn, "CADASTRO", "RESTAURAR_USUARIO", "usuario", idUsuario, null, detalhes);
			});

		} catch (RuntimeException e) {
			registrarLogErro("ERRO", "RESTAURAR_USUARIO", "usuario", e);
			throw e;
		}
	}

	private void validarRegrasPersistencia(UsuarioDao dao, Usuario usuario) {
		if (usuario.isMaster()) {
			Usuario masterExistente = dao.buscarMasterUnico();
			if (masterExistente != null && !masterExistente.getIdUsuario().equals(usuario.getIdUsuario())) {
				throw new ValidationException(ValidationErrorType.DUPLICATE_ENTRY,
						"JÁ EXISTE UM USUÁRIO MASTER CADASTRADO.");
			}
		}
		validarDuplicidadeEmail(dao, usuario);
		if (usuario.getIdUsuario() != null && usuario.getIdUsuario() > 0) {
			Usuario base = dao.searchById(usuario.getIdUsuario());
			if (base != null && base.isMaster())
				usuario.setMaster(true);
		}
	}

	private void validarRestricoesMaster(Usuario usuario) {
		if (usuario.isMaster() && usuario.getStatus() != StatusUsuario.ATIVO) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"O STATUS DO MASTER NÃO PODE SER ALTERADO.");
		}
		if (usuario.getEmpresa() == null || usuario.getEmpresa().getIdEmpresa() == null) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A EMPRESA É OBRIGATÓRIA.");
		}
	}

	private boolean temMaisPoder(Connection conn, Usuario executor, Usuario alvo) {
		if (UsuarioPolicy.isPrivilegiado(executor))
			return true;
		if (UsuarioPolicy.isPrivilegiado(alvo))
			return false;

		PermissaoDao pDao = daoFactory.createPermissaoDao(conn);

		Integer nivelMaxExecutor = pDao.buscarMaiorNivelDoUsuario(executor.getIdUsuario());
		Integer nivelMaxAlvo = pDao.buscarMaiorNivelDoUsuario(alvo.getIdUsuario());

		int nExecutor = (nivelMaxExecutor != null ? nivelMaxExecutor : 0);
		int nAlvo = (nivelMaxAlvo != null ? nivelMaxAlvo : 0);

		return UsuarioPolicy.temHierarquiaParaAlterar(executor, nExecutor, nAlvo);
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
		Usuario existente = dao.buscarPorEmail(u.getEmail());
		if (existente != null && (u.getIdUsuario() == null || !existente.getIdUsuario().equals(u.getIdUsuario()))) {
			throw new ValidationException(ValidationErrorType.DUPLICATE_ENTRY,
					"ESTE E-MAIL JÁ ESTÁ CADASTRADO NO SISTEMA.");
		}
	}

}