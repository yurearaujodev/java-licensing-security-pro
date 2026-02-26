package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.dto.UsuarioPermissaoDetalheDTO;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.util.TimeUtils;

public class UsuarioPermissaoService extends BaseService {

	private final DaoFactory daoFactory;

	public UsuarioPermissaoService(DaoFactory daoFactory) {
		this.daoFactory = daoFactory;
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
		PermissaoDao pDao = new PermissaoDao(conn);

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
		upDao.syncByUsuario(usuario.getIdUsuario(), permissoes);
		registrarLogPermissoesFinal(conn, usuario, permissoes);
	}

	private void registrarLogPermissoesFinal(Connection conn, Usuario usuario, List<UsuarioPermissao> finais) {
		PermissaoDao pDao = daoFactory.createPermissaoDao(conn);

		List<Integer> ids = finais.stream().map(UsuarioPermissao::getIdPermissoes).distinct().toList();

		Map<Integer, Permissao> mapaRef = pDao.listarPorIds(ids).stream()
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

}