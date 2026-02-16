package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.dao.usuario.MenuSistemaDao;
import com.br.yat.gerenciador.dao.usuario.PerfilDao;
import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoMenuDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.policy.UsuarioPolicy;
import com.br.yat.gerenciador.util.AuditLogHelper;
import com.br.yat.gerenciador.util.ValidationUtils;

public class PerfilService extends BaseService {

	private static final MenuChave CHAVE_ACESSO = MenuChave.CONFIGURACAO_PERMISSAO;

	public void salvarPerfil(Perfil perfil, Map<MenuChave, List<String>> permissoes, Usuario executor) {
		validarCampos(perfil);

		try (Connection conn = ConnectionFactory.getConnection()) {

			validarAcesso(conn, executor, CHAVE_ACESSO, TipoPermissao.WRITE);

			ConnectionFactory.beginTransaction(conn);

			try {
				PerfilDao perfilDao = new PerfilDao(conn);
				PerfilPermissoesDao ppDao = new PerfilPermissoesDao(conn);
				PermissaoDao pDao = new PermissaoDao(conn);
				LogSistemaDao logDao = new LogSistemaDao(conn);

				boolean isNovo = isNovoPerfil(perfil);
				if (isNovo) {
					criarPerfil(perfilDao, logDao, perfil);
				} else {
					atualizarPerfil(perfilDao, ppDao, logDao, perfil);
				}

				sincronizarPermissoesPerfil(ppDao, pDao, perfil.getIdPerfil(), permissoes, executor);

				ConnectionFactory.commitTransaction(conn);

			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				registrarLogErro("ERRO", "SALVAR_PERFIL", "perfil", e);
				throw e;
			}

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, e.getMessage(), e);
		}
	}

	private void criarPerfil(PerfilDao perfilDao, LogSistemaDao logDao, Perfil perfil) {

		int id = perfilDao.save(perfil);
		perfil.setIdPerfil(id);

		logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "CRIAR_PERFIL", "perfil", id, null, perfil.getNome()));
	}

	private void atualizarPerfil(PerfilDao perfilDao, PerfilPermissoesDao ppDao, LogSistemaDao logDao, Perfil perfil) {

		validarImutabilidadeMaster(perfilDao, perfil);

		perfilDao.update(perfil);

		logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "ALTERAR_PERFIL", "perfil", perfil.getIdPerfil(),
				"Update", perfil.getNome()));

		ppDao.desvincularTodasDoPerfil(perfil.getIdPerfil());
	}

	private boolean isNovoPerfil(Perfil perfil) {
		return perfil.getIdPerfil() == null || perfil.getIdPerfil() == 0;
	}

	private void validarImutabilidadeMaster(PerfilDao dao, Perfil perfil) {
		Perfil anterior = dao.searchById(perfil.getIdPerfil());

		if (anterior != null && isMaster(anterior) && !isMaster(perfil)) {
			throw new ValidationException(ValidationErrorType.ACCESS_DENIED, "O NOME DO PERFIL MASTER É IMUTÁVEL.");
		}
	}

	private boolean isMaster(Perfil perfil) {
		return "MASTER".equalsIgnoreCase(perfil.getNome());
	}

	private void sincronizarPermissoesPerfil(PerfilPermissoesDao ppDao, PermissaoDao pDao, int idPerfil,
			Map<MenuChave, List<String>> permissoes, Usuario executor) {
		if (permissoes == null || permissoes.isEmpty()) {
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
			for (String tipo : tipos) {
				Permissao p = pDao.findByChaveETipo(chave.name(), tipo);
				if (p != null) {

					validarAtribuicaoPermissao(p, isPrivilegiado, idsPermitidos, nivelTetoExecutor);

					ppDao.vincularPermissaoAoPerfil(idPerfil, p.getIdPermissoes(), true);
				}
			}
		});
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

	public Perfil buscarOuCriarPerfilMaster() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			PerfilDao dao = new PerfilDao(conn);

			Optional<Perfil> perfilExistente = dao.buscarPorNome("MASTER");

			if (perfilExistente.isPresent()) {
				return perfilExistente.get();
			}

			PerfilPermissoesDao ppDao = new PerfilPermissoesDao(conn);
			ConnectionFactory.beginTransaction(conn);

			try {
				Perfil novo = new Perfil();
				novo.setNome("MASTER");
				novo.setDescricao("PERFIL ADMINISTRADOR MASTER (SETUP INICIAL)");

				int idGerado = dao.save(novo);
				novo.setIdPerfil(idGerado);

				for (MenuChave chave : MenuChave.values()) {
					List<Integer> idsPermissoes = garantirInfraestruturaMenu(conn, chave);
					for (Integer idPerm : idsPermissoes) {
						ppDao.vincularPermissaoAoPerfil(idGerado, idPerm, true);
					}
				}

				ConnectionFactory.commitTransaction(conn);
				return novo;

			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				registrarLogErro("ERRO", "SETUP_MASTER", "perfil", e);
				throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO NO SETUP DO MASTER", e);
			}

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR,
					"FALHA NA CONEXÃO AO GERENCIAR PERFIL MASTER", e);
		}
	}

	public void excluirPerfil(int idPerfil, Usuario executor) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			validarAcesso(conn, executor, CHAVE_ACESSO, TipoPermissao.DELETE);

			PerfilDao perfilDao = new PerfilDao(conn);
			LogSistemaDao logDao = new LogSistemaDao(conn);

			Perfil perfil = perfilDao.searchById(idPerfil);

			if (perfil == null) {
				throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "PERFIL NÃO ENCONTRADO.");
			}

			if ("MASTER".equalsIgnoreCase(perfil.getNome())) {
				throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
						"O PERFIL MASTER NÃO PODE SER EXCLUÍDO OU DESATIVADO.");
			}

			ConnectionFactory.beginTransaction(conn);
			try {
				perfilDao.softDeleteById(idPerfil);

				logDao.save(AuditLogHelper.gerarLogSucesso("SEGURANCA", "EXCLUIR_PERFIL", "perfil", idPerfil,
						perfil.getNome(), "Perfil movido para lixeira (Soft Delete)"));

				ConnectionFactory.commitTransaction(conn);
			} catch (Exception e) {
				ConnectionFactory.rollbackTransaction(conn);
				registrarLogErro("ERRO", "EXCLUIR_PERFIL", "perfil", e);
				throw e;
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO DE CONEXÃO AO EXCLUIR PERFIL", e);
		}
	}
	
	public List<Perfil> listarExcluidos(Usuario executor) {
	    try (Connection conn = ConnectionFactory.getConnection()) {

	        validarAcesso(conn, executor, CHAVE_ACESSO, TipoPermissao.DELETE);

	        return new PerfilDao(conn).listarExcluidos();

	    } catch (SQLException e) {
	        throw new DataAccessException(
	            DataAccessErrorType.CONNECTION_ERROR,
	            "ERRO AO LISTAR PERFIS EXCLUÍDOS",
	            e
	        );
	    }
	}

	public void restaurarPerfil(int idPerfil, Usuario executor) {

	    try (Connection conn = ConnectionFactory.getConnection()) {

	        validarAcesso(conn, executor, CHAVE_ACESSO, TipoPermissao.DELETE);

	        PerfilDao dao = new PerfilDao(conn);

	        dao.restaurar(idPerfil);

	        Map<String, String> detalhes = new HashMap<>();
	        detalhes.put("acao", "Restauração de perfil via lixeira");

	        registrarLogSucesso(conn,
	                "SEGURANCA",
	                "RESTAURAR_PERFIL",
	                "perfil",
	                idPerfil,
	                null,
	                detalhes);

	    } catch (SQLException e) {
	        throw new DataAccessException(
	            DataAccessErrorType.CONNECTION_ERROR,
	            "ERRO AO RESTAURAR PERFIL",
	            e
	        );
	    }
	}

	public List<Permissao> carregarPermissoesDetalhadas(Integer idUsuario) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			PermissaoDao pDao = new PermissaoDao(conn);
			return pDao.listarPermissoesAtivasPorUsuario(idUsuario);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR,
					"ERRO AO CARREGAR PERMISSÕES DETALHADAS", e);
		}
	}

	public List<String> listarPermissoesAtivasPorMenu(int idUsuario, MenuChave menu) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			PermissaoDao pDao = new PermissaoDao(conn);
			return pDao.buscarTiposAtivosPorUsuarioEMenu(idUsuario, menu.name());
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "FALHA NA CONEXÃO", e);
		}
	}

	private void validarCampos(Perfil p) {

		if (p == null) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "PERFIL NÃO PODE SER NULO.");
		}

		if (ValidationUtils.isEmpty(p.getNome())) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "NOME DO PERFIL É OBRIGATÓRIO.");
		}
	}

	public List<Perfil> listarTodos() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new PerfilDao(conn).listAll();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO LISTAR PERFIS", e);
		}
	}

	public List<Permissao> listarPermissoesDoPerfil(int idPerfil) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return new PerfilPermissoesDao(conn).listarPermissoesPorPerfil(idPerfil);
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO BUSCAR PERMISSÕES", e);
		}
	}

	public List<Integer> garantirInfraestruturaMenu(Connection conn, MenuChave chave) throws SQLException {
		PermissaoDao pDao = new PermissaoDao(conn);
		MenuSistemaDao menuDao = new MenuSistemaDao(conn);
		PermissaoMenuDao pmDao = new PermissaoMenuDao(conn);

		String categoria = chave.getCategoria();
		String descricaoBase = chave.getDescricao();
		int nivel = chave.getNivel();

		int idMenu = menuDao.save(chave.name(), categoria);

		List<Integer> idsGerados = new ArrayList<>();
		List<String> tiposOperacao = Arrays.stream(TipoPermissao.values()).map(Enum::name).toList();

		for (String tipo : tiposOperacao) {
			Permissao permissaoBanco = pDao.findByChaveETipo(chave.name(), tipo);
			int idPerm;
			String descricaoFinal = montarDescricao(descricaoBase, tipo);

			if (permissaoBanco == null) {
				Permissao novaP = new Permissao();
				novaP.setChave(chave.name());
				novaP.setTipo(tipo);
				novaP.setCategoria(categoria);
				novaP.setNivel(nivel);
				novaP.setDescricao(descricaoFinal);

				idPerm = pDao.save(novaP);
				pmDao.vincular(idPerm, idMenu);
			} else {
				idPerm = permissaoBanco.getIdPermissoes();
				if (permissaoBanco.getNivel() != nivel || !permissaoBanco.getDescricao().equals(descricaoFinal)) {

					permissaoBanco.setNivel(nivel);
					permissaoBanco.setDescricao(descricaoFinal);
					pDao.update(permissaoBanco);
				}

			}
			idsGerados.add(idPerm);
		}
		return idsGerados;
	}

	private String montarDescricao(String base, String tipo) {
		return base + " [" + tipo + "]";
	}

}