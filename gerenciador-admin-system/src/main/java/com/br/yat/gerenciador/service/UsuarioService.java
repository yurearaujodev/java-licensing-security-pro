package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.dto.UsuarioPermissaoDetalheDTO;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
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

	private static final MenuChave CHAVE_USUARIO = MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES;

	public UsuarioService(AutenticacaoService authService, ParametroSistemaService parametroService,
			UsuarioPermissaoService permissaoService, DaoFactory daoFactory) {
		this.authService = authService;
		this.parametroService = parametroService;
		this.permissaoService = permissaoService;
		this.daoFactory = daoFactory;
	}

	public List<Usuario> listarUsuarios(String termo, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.READ);

			UsuarioDao dao = daoFactory.createUsuarioDao(conn);
			return (termo == null || termo.trim().isEmpty()) ? dao.listAll() : dao.listarPorNomeOuEmail(termo);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR USUÁRIOS", e);
		}
	}

	public List<Usuario> listarUsuariosVisiveis(String termo, Usuario executor) {
		List<Usuario> lista = listarUsuarios(termo, executor);
		if (!executor.isMaster())
			lista.removeIf(Usuario::isMaster);
		return lista;
	}

	public List<Usuario> listarExcluidosVisiveis(Usuario executor) {
		List<Usuario> lista = listarExcluidos(executor);
		if (!executor.isMaster())
			lista.removeIf(Usuario::isMaster);
		return lista;
	}

	public List<Usuario> listarUsuariosUltimoLogin(String termo, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
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
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR", e);
		}
	}

	public List<Usuario> listarExcluidos(Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.DELETE);
			UsuarioDao dao = daoFactory.createUsuarioDao(conn);
			return dao.listarExcluidos();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR EXCLUÍDOS", e);
		}
	}

	public PermissaoContexto obterContextoPermissao(Integer idUsuario, MenuChave menu) {
		if (idUsuario == null || idUsuario <= 0)
			return PermissaoContexto.semPermissao();
		try (Connection conn = ConnectionFactory.getConnection()) {
			Set<TipoPermissao> permissoes = listarPermissoesAtivasPorMenu(conn, idUsuario, menu).stream().map(p -> {
				try {
					return TipoPermissao.valueOf(p);
				} catch (Exception e) {
					return null;
				}
			}).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
			return PermissaoContexto.comum(permissoes);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO OBTER CONTEXTO DE PERMISSÃO",
					e);
		}
	}

	private List<String> listarPermissoesAtivasPorMenu(Connection conn, int idUsuario, MenuChave menu)
			throws SQLException {
		PermissaoDao pDao = daoFactory.createPermissaoDao(conn);
		return pDao.buscarTiposAtivosPorUsuarioEMenu(idUsuario, menu.name());
	}

	public List<MenuChave> carregarPermissoesAtivas(int idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioPermissaoDao dao = daoFactory.createUsuarioPermissaoDao(conn);
			return dao.buscarChavesAtivasPorUsuario(idUsuario);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	public List<Permissao> carregarPermissoesDetalhadas(Integer idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			PermissaoDao pDao = daoFactory.createPermissaoDao(conn);
			return pDao.listarPermissoesAtivasPorUsuario(idUsuario);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR,
					"ERRO AO CARREGAR PERMISSÕES DETALHADAS", e);
		}
	}

	public List<UsuarioPermissaoDetalheDTO> carregarDadosPermissoesEdicao(Integer idUsuario) {

		if (idUsuario == null || idUsuario <= 0)
			return new ArrayList<>();

		try (Connection conn = ConnectionFactory.getConnection()) {

			UsuarioPermissaoDao upDao = daoFactory.createUsuarioPermissaoDao(conn);
			PermissaoDao pDao = daoFactory.createPermissaoDao(conn);

			List<UsuarioPermissao> listaDiretas = upDao.listarDiretasPorUsuario(idUsuario);

			if (listaDiretas.isEmpty())
				return new ArrayList<>();

			List<Permissao> todasPermissoes = pDao.listAll();

			Map<Integer, Permissao> mapaPermissoes = todasPermissoes.stream()
					.collect(Collectors.toMap(Permissao::getIdPermissoes, p -> p));

			return listaDiretas.stream().map(vinculo -> {

				Permissao permissao = mapaPermissoes.get(vinculo.getIdPermissoes());

				if (permissao == null)
					return null;

				return new UsuarioPermissaoDetalheDTO(permissao, vinculo);
			}).filter(Objects::nonNull).toList();

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO CARREGAR PERMISSÕES ESPECIAIS",
					e);
		}
	}

	public void salvarUsuario(Usuario usuario, Map<MenuChave, List<String>> permissoesGranulares,
			Map<MenuChave, String> datasExpiracao, Usuario executor) {

		validarDados(usuario, permissoesGranulares);
		validarRestricoesMaster(usuario);
		validarDatasExpiracao(datasExpiracao);

		final Map<MenuChave, List<String>> permissoesFinal;
		final Map<MenuChave, String> datasFinal;

		if (UsuarioPolicy.isPrivilegiado(usuario)) {
			permissoesFinal = new HashMap<>();
			datasFinal = new HashMap<>();
		} else {
			permissoesFinal = permissoesGranulares != null ? new HashMap<>(permissoesGranulares) : new HashMap<>();

			datasFinal = datasExpiracao != null ? new HashMap<>(datasExpiracao) : new HashMap<>();
		}

		executeInTransactionVoid(
				conn -> executarPersistenciaUsuario(conn, usuario, permissoesFinal, datasFinal, executor));
	}

	private void executarPersistenciaUsuario(Connection conn, Usuario usuario,
			Map<MenuChave, List<String>> permissoesGranulares, Map<MenuChave, String> datasExpiracao,
			Usuario executor) {

		UsuarioDao usuarioDao = daoFactory.createUsuarioDao(conn);
		UsuarioPermissaoDao upDao = daoFactory.createUsuarioPermissaoDao(conn);

		boolean isSetupInicial = (executor == null && usuario.isMaster() && !existeUsuarioMaster(conn));

		if (!isSetupInicial) {
			validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.WRITE);
		}

		validarRegrasPersistencia(usuarioDao, usuario);

		boolean isNovo = (usuario.getIdUsuario() == null || usuario.getIdUsuario() == 0);
		Usuario estadoAnterior = null;
		Usuario alvoExistente = null;

		if (!isNovo) {
			alvoExistente = usuarioDao.searchById(usuario.getIdUsuario());

			if (alvoExistente == null) {
				throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "USUÁRIO NÃO ENCONTRADO.");
			}

			estadoAnterior = Usuario.snapshotParaValidacaoSenha(alvoExistente);
		}

		validarHierarquiaAlteracao(conn, executor, alvoExistente, isSetupInicial);

		boolean senhaAlterada = authService.processarSenha(usuario, isNovo, executor, estadoAnterior);

		if (senhaAlterada) {
			int diasExpira = parametroService.getInt(ParametroChave.SENHA_EXPIRA_DIAS, 90);
			usuario.setSenhaExpiraEm(LocalDateTime.now().plusDays(diasExpira));
		}

		salvarOuAtualizar(usuarioDao, usuario, estadoAnterior, isNovo, conn);

		if (senhaAlterada && !isNovo) {
			registrarLogSucesso(conn, "SEGURANCA", "SENHA_ALTERADA", "usuario", usuario.getIdUsuario(),
					"O executor alterou a senha deste usuário.", null);
		}

		List<UsuarioPermissao> listaFinalSincronismo = permissaoService.montarPermissoes(conn, usuario, executor,
				isSetupInicial, permissoesGranulares, datasExpiracao);

		upDao.syncByUsuario(usuario.getIdUsuario(), listaFinalSincronismo);

		registrarLogPermissoesFinal(conn, usuario, listaFinalSincronismo);
	}

	private boolean existeUsuarioMaster(Connection conn) {
		UsuarioDao dao = daoFactory.createUsuarioDao(conn);
		return dao.buscarMasterUnico() != null;
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

		if (executor != null && executor.getIdUsuario().equals(idUsuario)) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"SEGURANÇA: VOCÊ NÃO PODE EXCLUIR SUA PRÓPRIA CONTA.");
		}
		try (Connection conn = ConnectionFactory.getConnection()) {
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
		} catch (SQLException e) {
			registrarLogErro("ERRO", "EXCLUIR_USUARIO", "usuario", e);
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO ACESSAR BANCO", e);
		}
	}

	public void restaurarUsuario(int idUsuario, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_USUARIO, TipoPermissao.DELETE);

			UsuarioDao dao = daoFactory.createUsuarioDao(conn);
			dao.restaurar(idUsuario);

			Map<String, String> detalhes = new HashMap<>();
			detalhes.put("status_anterior", "EXCLUIDO");
			detalhes.put("status_atual", "ATIVO");
			detalhes.put("acao", "Restauração de registro via lixeira");

			registrarLogSucesso(conn, "CADASTRO", "RESTAURAR_USUARIO", "usuario", idUsuario, null, detalhes);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO RESTAURAR", e);
		}
	}

	public Usuario buscarMasterUnico() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = daoFactory.createUsuarioDao(conn);
			return dao.buscarMasterUnico();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO BUSCAR MASTER", e);
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

	public boolean existeUsuarioMaster() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = daoFactory.createUsuarioDao(conn);
			return dao.buscarMasterUnico() != null;
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO VERIFICAR MASTER", e);
		}
	}

	public boolean podeEditarPermissoes(Usuario u) {
		return UsuarioPolicy.podeEditarPermissoes(u);
	}

	private void validarDados(Usuario usuario, Map<MenuChave, List<String>> chaves) {
		UsuarioValidationUtils.validarUsuario(usuario);

		if ((chaves == null || chaves.isEmpty()) && usuario.getPerfil() == null) {
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

	private void registrarLogPermissoesFinal(Connection conn, Usuario usuario, List<UsuarioPermissao> finais) {
		PermissaoDao pDao = daoFactory.createPermissaoDao(conn);

		List<Permissao> todasPermissoes = pDao.listAll();

		Map<Integer, Permissao> mapaRef = todasPermissoes.stream()
				.collect(Collectors.toMap(Permissao::getIdPermissoes, p -> p));

		String resumo = finais.stream().map(up -> {
			Permissao p = mapaRef.get(up.getIdPermissoes());

			String nome = (p != null) ? p.getChave() + " (" + p.getTipo() + ")" : "ID:" + up.getIdPermissoes();
			String origem = up.isHerdada() ? "[PERFIL]" : "[DIRETA]";

			String expira = (up.getExpiraEm() != null) ? " EXPIRA EM: " + TimeUtils.formatarDataHora(up.getExpiraEm())
					: " (PERMANENTE)";

			return nome + origem + expira;
		}).collect(Collectors.joining(" | "));

		registrarLogSucesso(conn, "SEGURANCA", "SINCRONIZAR_PERMISSOES", "usuario_permissao", usuario.getIdUsuario(),
				"Sincronização de acessos realizada com sucesso.", resumo);
	}

	public Empresa buscarEmpresaFornecedora() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			EmpresaDao dao = daoFactory.createEmpresaDao(conn);
			return dao.buscarPorFornecedora();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR,
					"ERRO AO BUSCAR EMPRESA: " + e.getMessage(), e);
		}
	}
}