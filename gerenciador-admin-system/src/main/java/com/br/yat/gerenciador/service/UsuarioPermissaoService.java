package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioPermissaoDao;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.util.TimeUtils;

public class UsuarioPermissaoService {

	public List<UsuarioPermissao> montarPermissoes(Connection conn, Usuario usuario, Usuario executor,
			boolean isSetupInicial, Map<MenuChave, List<String>> permissoesGranulares,
			Map<MenuChave, String> datasExpiracao) throws SQLException {
		List<UsuarioPermissao> perfil = carregarPermissoesDoPerfil(conn, usuario);
		List<UsuarioPermissao> diretas = carregarPermissoesGranulares(conn, usuario, permissoesGranulares,
				datasExpiracao);
		Map<Integer, UsuarioPermissao> mapa = new LinkedHashMap<>();
		perfil.forEach(up -> mapa.put(up.getIdPermissoes(), up));
		diretas.forEach(up -> mapa.put(up.getIdPermissoes(), up));
		List<UsuarioPermissao> finais = new ArrayList<>(mapa.values());
		finais.forEach(up -> up.setIdUsuario(usuario.getIdUsuario()));
		if (!isSetupInicial && !UsuarioPolicy.isPrivilegiado(usuario)) {
			validarHierarquiaUsuarioPermissao(conn, executor, finais);
		}
		return finais;
	}

	/* ===== Métodos extraídos SEM ALTERAÇÃO DE REGRA ===== */

	private List<UsuarioPermissao> carregarPermissoesDoPerfil(Connection conn, Usuario usuario) {
		if (usuario.getPerfil() == null || usuario.getPerfil().getIdPerfil() == null) {
			return List.of();
		}
		PerfilPermissoesDao ppDao = new PerfilPermissoesDao(conn);
		return ppDao.listarPermissoesPorPerfil(usuario.getPerfil().getIdPerfil()).stream().map(p -> {
			UsuarioPermissao up = new UsuarioPermissao();
			up.setIdPermissoes(p.getIdPermissoes());
			up.setAtiva(true);
			up.setHerdada(true);
			return up;
		}).toList();
	}

	private List<UsuarioPermissao> carregarPermissoesGranulares(Connection conn, Usuario usuario,
			Map<MenuChave, List<String>> permissoesGranulares, Map<MenuChave, String> datasTexto) throws SQLException {
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
				LocalDateTime exp = datasTexto != null && datasTexto.containsKey(chave)
						? TimeUtils.parseDataHora(datasTexto.get(chave))
						: null;
				for (String tipo : tipos) {
					Permissao p = pDao.findByChaveETipo(chave.name(), tipo);
					if (p != null) {
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

	private UsuarioPermissao criar(Integer idUsuario, Integer idPermissao) {
		UsuarioPermissao up = new UsuarioPermissao();
		up.setIdUsuario(idUsuario);
		up.setIdPermissoes(idPermissao);
		up.setAtiva(true);
		return up;
	}
	
	private void validarHierarquiaUsuarioPermissao(Connection conn, Usuario executor,
	        List<UsuarioPermissao> permissoes) {

	    if (executor == null || UsuarioPolicy.isPrivilegiado(executor))
	        return;

	    UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
	    PermissaoDao pDao = new PermissaoDao(conn);

	    Integer teto = pDao.buscarMaiorNivelDoUsuario(executor.getIdUsuario());
	    int nivelTeto = teto != null ? teto : 0;

	    Map<Integer, LocalDateTime> mapaExecutor =
	            upDao.listarPorUsuario(executor.getIdUsuario()).stream()
	                    .collect(Collectors.toMap(
	                            UsuarioPermissao::getIdPermissoes,
	                            up -> up.getExpiraEm() == null
	                                    ? LocalDateTime.MAX
	                                    : up.getExpiraEm(),
	                            (a, b) -> a
	                    ));

	    // 🔥 Carrega todas permissões uma única vez
	    Map<Integer, Permissao> mapaPermissoes =
	            pDao.listAll().stream()
	                    .collect(Collectors.toMap(
	                            Permissao::getIdPermissoes,
	                            p -> p
	                    ));

	    for (UsuarioPermissao up : permissoes) {

	        Permissao p = mapaPermissoes.get(up.getIdPermissoes());
	        if (p == null)
	            continue;

	        if (!mapaExecutor.containsKey(up.getIdPermissoes())) {
	            throw new ValidationException(
	                    ValidationErrorType.ACCESS_DENIED,
	                    "VOCÊ NÃO POSSUI ACESSO A [" + p.getChave() + "].");
	        }

	        if (p.getNivel() > nivelTeto) {
	            throw new ValidationException(
	                    ValidationErrorType.ACCESS_DENIED,
	                    "NÍVEL INSUFICIENTE PARA [" + p.getChave() + "].");
	        }

	        LocalDateTime expExecutor = mapaExecutor.get(up.getIdPermissoes());
	        LocalDateTime expAlvo =
	                up.getExpiraEm() == null ? LocalDateTime.MAX : up.getExpiraEm();

	        if (expAlvo.isAfter(expExecutor)) {
	            throw new ValidationException(
	                    ValidationErrorType.ACCESS_DENIED,
	                    "DATA INVÁLIDA PARA [" + p.getChave() + "].");
	        }
	    }
	}


//	private void validarHierarquiaUsuarioPermissao(Connection conn, Usuario executor,
//			List<UsuarioPermissao> permissoes) {
//		if (executor == null || UsuarioPolicy.isPrivilegiado(executor))
//			return;
//		UsuarioPermissaoDao upDao = new UsuarioPermissaoDao(conn);
//		PermissaoDao pDao = new PermissaoDao(conn);
//		Integer teto = pDao.buscarMaiorNivelDoUsuario(executor.getIdUsuario());
//		int nivelTeto = teto != null ? teto : 0;
//		Map<Integer, LocalDateTime> mapaExecutor = upDao.listarPorUsuario(executor.getIdUsuario()).stream()
//				.collect(Collectors.toMap(UsuarioPermissao::getIdPermissoes,
//						up -> up.getExpiraEm() == null ? LocalDateTime.MAX : up.getExpiraEm(), (a, b) -> a));
//		for (UsuarioPermissao up : permissoes) {
//			Permissao p = pDao.findById(up.getIdPermissoes());
//			if (p == null)
//				continue;
//			if (!mapaExecutor.containsKey(up.getIdPermissoes())) {
//				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
//						"VOCÊ NÃO POSSUI ACESSO A [" + p.getChave() + "].");
//			}
//			if (p.getNivel() > nivelTeto) {
//				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
//						"NÍVEL INSUFICIENTE PARA [" + p.getChave() + "].");
//			}
//			LocalDateTime expExecutor = mapaExecutor.get(up.getIdPermissoes());
//			LocalDateTime expAlvo = up.getExpiraEm() == null ? LocalDateTime.MAX : up.getExpiraEm();
//			if (expAlvo.isAfter(expExecutor)) {
//				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
//						"DATA INVÁLIDA PARA [" + p.getChave() + "].");
//			}
//		}
//	}
}