package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.MenuSistemaDao;
import com.br.yat.gerenciador.dao.usuario.PerfilDao;
import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
import com.br.yat.gerenciador.dao.usuario.PermissaoMenuDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.domain.event.DomainEventPublisher;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.security.SecurityService;

public class BootstrapService extends BaseService {

	private static final String PERFIL_MASTER = "MASTER";
	private final DaoFactory daoFactory;

	public BootstrapService(DaoFactory daoFactory, DomainEventPublisher eventPublisher,SecurityService securityService) {
		super(eventPublisher, securityService);
		this.daoFactory = daoFactory;
	}

	public Perfil buscarOuCriarPerfilMaster() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			PerfilDao dao = daoFactory.createPerfilDao(conn);
			Optional<Perfil> perfilExistente = dao.buscarPorNome(PERFIL_MASTER);

			if (perfilExistente.isPresent()) {
				return perfilExistente.get();
			}

			PerfilPermissoesDao ppDao = daoFactory.createPerfilPermissoesDao(conn);
			ConnectionFactory.beginTransaction(conn);

			try {
				Perfil novo = new Perfil();
				novo.setNome(PERFIL_MASTER);
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
				registrarLogSucesso(conn, "BOOTSTRAP", "CRIAR_PERFIL_MASTER", "perfil", idGerado, null,
						"Perfil master criado com todas as permissões");
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

	public Usuario buscarMasterUnico() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			UsuarioDao dao = daoFactory.createUsuarioDao(conn);
			return dao.buscarMasterUnico();
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO BUSCAR MASTER", e);
		}
	}

	public boolean existeUsuarioMaster() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			return existeUsuarioMaster(conn); // delega
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO VERIFICAR MASTER", e);
		}
	}

	private boolean existeUsuarioMaster(Connection conn) {
		UsuarioDao dao = daoFactory.createUsuarioDao(conn);
		return dao.buscarMasterUnico() != null;
	}

	public boolean permitirCriacaoInicialMaster(Connection conn, Usuario executor, Usuario novoUsuario) {
		if (executor != null)
			return false;
		if (!novoUsuario.isMaster())
			return false;
		return !existeUsuarioMaster(conn);
	}

	private List<Integer> garantirInfraestruturaMenu(Connection conn, MenuChave chave) throws SQLException {
		PermissaoDao pDao = daoFactory.createPermissaoDao(conn);
		MenuSistemaDao menuDao = daoFactory.createMenuSistemaDao(conn);
		PermissaoMenuDao pmDao = daoFactory.createPermissaoMenuDao(conn);

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

	public boolean isPerfilMaster(Perfil perfil) {
		return perfil != null && PERFIL_MASTER.equalsIgnoreCase(perfil.getNome());
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