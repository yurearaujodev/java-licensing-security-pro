package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;
import com.br.yat.gerenciador.domain.event.DomainEventPublisher;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.dto.UsuarioPermissaoDetalheDTO;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.security.PermissaoContexto;
import com.br.yat.gerenciador.security.SecurityService;
import com.br.yat.gerenciador.util.Diferenca;
import com.br.yat.gerenciador.util.DiferencaMapperUtil;
import com.br.yat.gerenciador.util.TimeUtils;

public class UsuarioPermissaoService extends BaseService {

	private final DaoFactory daoFactory;
	private final AuditLogService auditLogService;

	public UsuarioPermissaoService(DaoFactory daoFactory, AuditLogService auditLogService, DomainEventPublisher eventPublisher,SecurityService securityService) {
		super(eventPublisher, securityService);
		this.daoFactory = daoFactory;
		this.auditLogService = auditLogService;
	}

	public List<UsuarioPermissao> montarPermissoes(Connection conn, Usuario usuario, Usuario executor,
			boolean isSetupInicial, Map<MenuChave, List<String>> permissoesGranulares,
			Map<MenuChave, String> datasExpiracao) {

		if (usuario == null || usuario.getIdUsuario() == null) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"USUÁRIO INVÁLIDO PARA MONTAGEM DE PERMISSÕES.");
		}

		if (!isSetupInicial && executor == null) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"EXECUTOR NÃO INFORMADO PARA VALIDAÇÃO DE HIERARQUIA.");
		}

		List<UsuarioPermissao> todasPermissoes = carregarPermissoesPerfilEGranulares(conn, usuario,
				permissoesGranulares, datasExpiracao);

		if (!isSetupInicial && !UsuarioPolicy.isPrivilegiado(executor)) {
			aplicarValidacaoHierarquia(conn, executor, todasPermissoes);
		}

		return todasPermissoes;
	}

	private List<UsuarioPermissao> carregarPermissoesPerfilEGranulares(Connection conn, Usuario usuario,
			Map<MenuChave, List<String>> permissoesGranulares, Map<MenuChave, String> datasTexto) {
		List<UsuarioPermissao> perfil = carregarPermissoesDoPerfil(conn, usuario);
		List<UsuarioPermissao> diretas = carregarPermissoesGranulares(conn, usuario, permissoesGranulares, datasTexto);

		Map<Integer, UsuarioPermissao> mapa = new LinkedHashMap<>();
		perfil.forEach(up -> mapa.put(up.getIdPermissoes(), up));
		diretas.forEach(up -> mapa.put(up.getIdPermissoes(), up));

		List<UsuarioPermissao> finais = new ArrayList<>(mapa.values());
		finais.forEach(up -> up.setIdUsuario(usuario.getIdUsuario()));
		return finais;
	}

	private List<UsuarioPermissao> carregarPermissoesDoPerfil(Connection conn, Usuario usuario) {
		if (usuario.getPerfil() == null || usuario.getPerfil().getIdPerfil() == null) {
			return List.of();
		}
		PerfilPermissoesDao ppDao = daoFactory.createPerfilPermissoesDao(conn);
		return ppDao.listarPermissoesPorPerfil(usuario.getPerfil().getIdPerfil()).stream().map(p -> {
			UsuarioPermissao up = new UsuarioPermissao();
			up.setIdPermissoes(p.getIdPermissoes());
			up.setAtiva(true);
			up.setHerdada(true);
			return up;
		}).toList();
	}

	private List<UsuarioPermissao> carregarPermissoesGranulares(Connection conn, Usuario usuario,
			Map<MenuChave, List<String>> permissoesGranulares, Map<MenuChave, String> datasTexto) {

		List<UsuarioPermissao> novas = new ArrayList<>();
		PermissaoDao pDao = daoFactory.createPermissaoDao(conn);

		if (usuario.isMaster()) {
			for (Permissao p : pDao.listAll()) {
				novas.add(criar(usuario.getIdUsuario(), p.getIdPermissoes()));
			}
			return novas;
		}

		if (permissoesGranulares != null) {
			permissoesGranulares.forEach((chave, tipos) -> {
				if (tipos == null || tipos.isEmpty())
					return;
				LocalDateTime exp = parseDataExpiracaoSegura(chave, datasTexto);

				for (String tipo : tipos) {
					Permissao p = pDao.findByChaveETipo(chave.name(), tipo);
					if (p == null)
						continue;

					boolean jaExisteNoPerfil = false;

					if (usuario.getPerfil() != null && usuario.getPerfil().getPermissoes() != null) {

						jaExisteNoPerfil = usuario.getPerfil().getPermissoes().stream().anyMatch(
								per -> per.getChave().equals(chave.name()) && per.getTipo().equalsIgnoreCase(tipo));
					}

					if (!jaExisteNoPerfil || exp != null) {
						UsuarioPermissao up = criar(usuario.getIdUsuario(), p.getIdPermissoes());
						up.setHerdada(false);
						up.setExpiraEm(exp);
						novas.add(up);
					}
				}
			});
		}

		return novas;
	}

	private LocalDateTime parseDataExpiracaoSegura(MenuChave chave, Map<MenuChave, String> datasTexto) {
		if (datasTexto == null || !datasTexto.containsKey(chave))
			return null;

		String dataStr = datasTexto.get(chave);
		if (dataStr == null || dataStr.isBlank())
			return null;

		try {
			LocalDateTime dataDigitada = TimeUtils.parseDataHora(dataStr);

			if (dataDigitada == null) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"FORMATO DE DATA INVÁLIDO PARA O MENU: " + chave);
			}

			LocalDateTime agora = LocalDateTime.now();

			if (dataDigitada.isBefore(agora.minusMinutes(1))) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"A DATA DE EXPIRAÇÃO PARA [" + chave + "] NÃO PODE SER NO PASSADO.");
			}
			if (dataDigitada.isAfter(agora.plusYears(10))) {
				throw new ValidationException(ValidationErrorType.INVALID_FIELD,
						"A DATA DE EXPIRAÇÃO PARA [" + chave + "] EXCEDEU O LIMITE (MÁX 10 ANOS).");
			}
			return dataDigitada;
//		} catch (ValidationException ve) {
//			throw ve;
		} catch (Exception e) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD,
					"FORMATO DE DATA INVÁLIDO PARA O MENU: " + chave);
		}
	}

	private UsuarioPermissao criar(Integer idUsuario, Integer idPermissao) {
		UsuarioPermissao up = new UsuarioPermissao();
		up.setIdUsuario(idUsuario);
		up.setIdPermissoes(idPermissao);
		up.setAtiva(true);
		return up;
	}

	private void aplicarValidacaoHierarquia(Connection conn, Usuario executor, List<UsuarioPermissao> permissoes) {
		if (executor == null || UsuarioPolicy.isPrivilegiado(executor))
			return;

		UsuarioPermissaoDao upDao = daoFactory.createUsuarioPermissaoDao(conn);
		PermissaoDao pDao = daoFactory.createPermissaoDao(conn);

		Integer teto = pDao.buscarMaiorNivelDoUsuario(executor.getIdUsuario());
		int nivelTeto = teto != null ? teto : 0;

		Map<Integer, LocalDateTime> mapaExecutor = upDao.listarPorUsuario(executor.getIdUsuario()).stream()
				.collect(Collectors.toMap(UsuarioPermissao::getIdPermissoes,
						up -> up.getExpiraEm() == null ? LocalDateTime.MAX : up.getExpiraEm(), (a, b) -> a));

		List<Integer> ids = permissoes.stream().map(UsuarioPermissao::getIdPermissoes).distinct().toList();
		if (ids.isEmpty())
			return;

		Map<Integer, Permissao> mapaPermissoes = pDao.listarPorIds(ids).stream()
				.collect(Collectors.toMap(Permissao::getIdPermissoes, p -> p));

		for (UsuarioPermissao up : permissoes) {
			Permissao p = mapaPermissoes.get(up.getIdPermissoes());
			if (p == null)
				continue;

			if (!mapaExecutor.containsKey(up.getIdPermissoes())) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"VOCÊ NÃO POSSUI ACESSO A [" + p.getChave() + "].");
			}

			if (p.getNivel() > nivelTeto) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"NÍVEL INSUFICIENTE PARA [" + p.getChave() + "].");
			}

			LocalDateTime expExecutor = mapaExecutor.get(up.getIdPermissoes());
			LocalDateTime expAlvo = up.getExpiraEm() == null ? LocalDateTime.MAX : up.getExpiraEm();

			if (expAlvo.isAfter(expExecutor)) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"DATA INVÁLIDA PARA [" + p.getChave() + "].");
			}
		}
	}

	public List<String> listarPermissoesAtivasPorMenu(Connection conn, int idUsuario, MenuChave menu) {
		PermissaoDao pDao = daoFactory.createPermissaoDao(conn);
		return pDao.buscarTiposAtivosPorUsuarioEMenu(idUsuario, menu.name());
	}

	public List<MenuChave> carregarPermissoesAtivas(int idUsuario) {
		return execute(conn -> {
			UsuarioPermissaoDao dao = daoFactory.createUsuarioPermissaoDao(conn);
			return dao.buscarChavesAtivasPorUsuario(idUsuario);
		});
	}

	public List<Permissao> carregarPermissoesDetalhadas(Integer idUsuario) {
		return execute(conn -> {
			PermissaoDao pDao = daoFactory.createPermissaoDao(conn);
			return pDao.listarPermissoesAtivasPorUsuario(idUsuario);
		});
	}

	public List<UsuarioPermissaoDetalheDTO> carregarDadosPermissoesEdicao(Integer idUsuario) {
		if (idUsuario == null || idUsuario <= 0)
			return new ArrayList<>();

		return execute(conn -> {
			UsuarioPermissaoDao upDao = daoFactory.createUsuarioPermissaoDao(conn);
			PermissaoDao pDao = daoFactory.createPermissaoDao(conn);

			List<UsuarioPermissao> listaDiretas = upDao.listarDiretasPorUsuario(idUsuario);
			if (listaDiretas.isEmpty())
				return new ArrayList<>();

			List<Integer> ids = listaDiretas.stream().map(UsuarioPermissao::getIdPermissoes).distinct().toList();

			// Cache local de permissões
			Map<Integer, Permissao> mapaPermissoes = pDao.listarPorIds(ids).stream()
					.collect(Collectors.toMap(Permissao::getIdPermissoes, p -> p));

			return listaDiretas.stream()
					.map(vinculo -> Optional.ofNullable(mapaPermissoes.get(vinculo.getIdPermissoes()))
							.map(permissao -> new UsuarioPermissaoDetalheDTO(permissao, vinculo)))
					.filter(Optional::isPresent).map(Optional::get).toList();
		});
	}

	public void sincronizarPermissoes(Connection conn, Usuario usuario, List<UsuarioPermissao> permissoes) {

		UsuarioPermissaoDao upDao = daoFactory.createUsuarioPermissaoDao(conn);

		List<UsuarioPermissao> antes = upDao.listarPorUsuario(usuario.getIdUsuario());

		upDao.syncByUsuario(usuario.getIdUsuario(), permissoes);

		List<UsuarioPermissao> depois = upDao.listarPorUsuario(usuario.getIdUsuario());

		registrarLogDiferencaUsuario(conn, usuario, antes, depois);
	}

	private void registrarLogDiferencaUsuario(Connection conn, Usuario usuario, List<UsuarioPermissao> antes,
			List<UsuarioPermissao> depois) {

		Diferenca<String> diff = DiferencaMapperUtil.calcular(antes, depois, this::mapearPermissaoUsuario);

		auditLogService.registrarAlteracaoPermissoesUsuario(conn, usuario, diff);
	}

	private String mapearPermissaoUsuario(UsuarioPermissao up) {
		return up.getIdPermissoes() + "|" + up.isHerdada() + "|"
				+ (up.getExpiraEm() != null ? up.getExpiraEm().truncatedTo(ChronoUnit.SECONDS) : "SEM_EXPIRACAO") + "|"
				+ up.isAtiva();
	}

	public void sincronizarPermissoesPerfil(Connection conn, int idPerfil, Map<MenuChave, List<String>> permissoes,
			Usuario executor) {

		PerfilPermissoesDao ppDao = daoFactory.createPerfilPermissoesDao(conn);
		PermissaoDao pDao = daoFactory.createPermissaoDao(conn);

		sincronizarPermissoesPerfil(conn, ppDao, pDao, idPerfil, permissoes, executor);
	}

	private void sincronizarPermissoesPerfil(Connection conn, PerfilPermissoesDao ppDao, PermissaoDao pDao,
			int idPerfil, Map<MenuChave, List<String>> permissoes, Usuario executor) {

		List<Permissao> antes = ppDao.listarPermissoesPorPerfil(idPerfil);

		ppDao.desvincularTodasDoPerfil(idPerfil);

		if (permissoes == null || permissoes.isEmpty()) {
			registrarLogDiferencaPerfil(conn, idPerfil, executor, antes, List.of());
			return;
		}

		final Integer nivelTetoExecutor;
		final Set<Integer> idsPermitidos;

		boolean isPrivilegiado = UsuarioPolicy.isPrivilegiado(executor);

		if (isPrivilegiado) {
			nivelTetoExecutor = null;
			idsPermitidos = null;
		} else {
			nivelTetoExecutor = pDao.buscarMaiorNivelDoUsuario(executor.getIdUsuario());
			idsPermitidos = pDao.listarPermissoesAtivasPorUsuario(executor.getIdUsuario()).stream()
					.map(Permissao::getIdPermissoes).collect(Collectors.toSet());
		}

		permissoes.forEach((chave, tipos) -> {
			if (tipos == null)
				return;

			for (String tipo : tipos) {

				Permissao p = pDao.findByChaveETipo(chave.name(), tipo);
				if (p == null)
					continue;

				validarAtribuicaoPermissao(p, isPrivilegiado, idsPermitidos, nivelTetoExecutor);

				ppDao.vincularPermissaoAoPerfil(idPerfil, p.getIdPermissoes(), true);
			}
		});

		List<Permissao> depois = ppDao.listarPermissoesPorPerfil(idPerfil);

		registrarLogDiferencaPerfil(conn, idPerfil, executor, antes, depois);
	}

	private void registrarLogDiferencaPerfil(Connection conn, int idPerfil, Usuario executor, List<Permissao> antes,
			List<Permissao> depois) {

		Diferenca<String> diff = DiferencaMapperUtil.calcular(antes, depois, p -> p.getChave() + ":" + p.getTipo());

		auditLogService.registrarAlteracaoPermissoesPerfil(conn, idPerfil, diff);
	}

	private void validarAtribuicaoPermissao(Permissao permissao, boolean isPrivilegiado, Set<Integer> idsPermitidos,
			Integer nivelTetoExecutor) {

		if (isPrivilegiado) {
			return;
		}

		if (idsPermitidos == null || !idsPermitidos.contains(permissao.getIdPermissoes())) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
					"VOCÊ NÃO PODE ATRIBUIR [" + permissao.getChave() + "] POIS NÃO A POSSUI.");
		}

		int teto = nivelTetoExecutor != null ? nivelTetoExecutor : 0;

		if (permissao.getNivel() > teto) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "NÍVEL INSUFICIENTE: A permissão ["
					+ permissao.getChave() + "] exige nível " + permissao.getNivel() + " e seu teto é " + teto);
		}
	}

	public PermissaoContexto obterContextoPermissao(Integer idUsuario, MenuChave menu) {
		if (idUsuario == null || idUsuario <= 0)
			return PermissaoContexto.semPermissao();
		return execute(conn -> {
			Set<TipoPermissao> permissoes = listarPermissoesAtivasPorMenu(conn, idUsuario, menu).stream().map(p -> {
				try {
					return TipoPermissao.valueOf(p);
				} catch (Exception e) {
					return null;
				}
			}).filter(Objects::nonNull).collect(Collectors.toUnmodifiableSet());
			return PermissaoContexto.comum(permissoes);
		});
	}

}