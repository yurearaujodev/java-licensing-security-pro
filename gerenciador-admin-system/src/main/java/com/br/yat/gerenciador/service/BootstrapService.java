//package com.br.yat.gerenciador.service;
//
//import java.sql.Connection;
//import java.sql.SQLException;
//import java.util.ArrayList;
//import java.util.Arrays;
//import java.util.List;
//import java.util.Optional;
//
//import com.br.yat.gerenciador.configurations.ConnectionFactory;
//import com.br.yat.gerenciador.dao.DaoFactory;
//import com.br.yat.gerenciador.dao.LogSistemaDao;
//import com.br.yat.gerenciador.dao.usuario.MenuSistemaDao;
//import com.br.yat.gerenciador.dao.usuario.PerfilDao;
//import com.br.yat.gerenciador.dao.usuario.PerfilPermissoesDao;
//import com.br.yat.gerenciador.dao.usuario.PermissaoDao;
//import com.br.yat.gerenciador.dao.usuario.PermissaoMenuDao;
//import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
//import com.br.yat.gerenciador.exception.DataAccessException;
//import com.br.yat.gerenciador.exception.ValidationException;
//import com.br.yat.gerenciador.model.Perfil;
//import com.br.yat.gerenciador.model.Permissao;
//import com.br.yat.gerenciador.model.Usuario;
//import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
//import com.br.yat.gerenciador.model.enums.MenuChave;
//import com.br.yat.gerenciador.model.enums.TipoPermissao;
//import com.br.yat.gerenciador.model.enums.ValidationErrorType;
//import com.br.yat.gerenciador.util.AuditLogHelper;
//
//public class BootstrapService extends BaseService {
//
//    private static final String PERFIL_MASTER = "MASTER";
//    private final DaoFactory daoFactory;
//
//    public BootstrapService(DaoFactory daoFactory) {
//        this.daoFactory = daoFactory;
//    }
//
//    /**
//     * Busca ou cria o perfil MASTER com todas as permissões do sistema.
//     * Método idempotente: seguro para execução múltipla.
//     */
//    public Perfil buscarOuCriarPerfilMaster() {
//        try (Connection conn = ConnectionFactory.getConnection()) {
//            PerfilDao dao = daoFactory.createPerfilDao(conn);
//
//            Optional<Perfil> perfilExistente = dao.buscarPorNome(PERFIL_MASTER);
//            if (perfilExistente.isPresent()) {
//                return perfilExistente.get();
//            }
//
//            PerfilPermissoesDao ppDao = daoFactory.createPerfilPermissoesDao(conn);
//            ConnectionFactory.beginTransaction(conn);
//
//            try {
//                Perfil novo = new Perfil();
//                novo.setNome(PERFIL_MASTER);
//                novo.setDescricao("PERFIL ADMINISTRADOR MASTER (SETUP INICIAL)");
//
//                int idGerado = dao.save(novo);
//                novo.setIdPerfil(idGerado);
//
//                for (MenuChave chave : MenuChave.values()) {
//                    List<Integer> idsPermissoes = garantirInfraestruturaMenu(conn, chave);
//                    for (Integer idPerm : idsPermissoes) {
//                        ppDao.vincularPermissaoAoPerfil(idGerado, idPerm, true);
//                    }
//                }
//
//                ConnectionFactory.commitTransaction(conn);
//                registrarLogSucesso(conn, "BOOTSTRAP", "CRIAR_PERFIL_MASTER", "perfil", idGerado, null, "Perfil master criado com todas as permissões");
//                return novo;
//
//            } catch (Exception e) {
//                ConnectionFactory.rollbackTransaction(conn);
//                registrarLogErro("ERRO", "SETUP_MASTER", "perfil", e);
//                throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO NO SETUP DO MASTER", e);
//            }
//
//        } catch (SQLException e) {
//            throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "FALHA NA CONEXÃO AO GERENCIAR PERFIL MASTER", e);
//        }
//    }
//
//    /**
//     * Verifica se já existe um usuário master cadastrado no banco.
//     */
//    public boolean existeUsuarioMaster() {
//        try (Connection conn = ConnectionFactory.getConnection()) {
//            UsuarioDao dao = daoFactory.createUsuarioDao(conn);
//            return dao.buscarMasterUnico() != null;
//        } catch (SQLException e) {
//            throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO VERIFICAR MASTER", e);
//        }
//    }
//
//    /**
//     * Busca o único usuário master cadastrado.
//     */
//    public Usuario buscarMasterUnico() {
//        try (Connection conn = ConnectionFactory.getConnection()) {
//            UsuarioDao dao = daoFactory.createUsuarioDao(conn);
//            return dao.buscarMasterUnico();
//        } catch (SQLException e) {
//            throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "ERRO AO BUSCAR MASTER", e);
//        }
//    }
//
//    /**
//     * Garante que a infraestrutura de permissões para um menu esteja registrada no banco.
//     * Cria as permissões (READ, WRITE, DELETE, etc.) se não existirem.
//     */
//    private List<Integer> garantirInfraestruturaMenu(Connection conn, MenuChave chave) throws SQLException {
//        PermissaoDao pDao = daoFactory.createPermissaoDao(conn);
//        MenuSistemaDao menuDao = daoFactory.createMenuSistemaDao(conn);
//        PermissaoMenuDao pmDao = daoFactory.createPermissaoMenuDao(conn);
//
//        String categoria = chave.getCategoria();
//        String descricaoBase = chave.getDescricao();
//        int nivel = chave.getNivel();
//
//        int idMenu = menuDao.save(chave.name(), categoria);
//        List<Integer> idsGerados = new ArrayList<>();
//        List<String> tiposOperacao = Arrays.stream(TipoPermissao.values()).map(Enum::name).toList();
//
//        for (String tipo : tiposOperacao) {
//            Permissao permissaoBanco = pDao.findByChaveETipo(chave.name(), tipo);
//            int idPerm;
//            String descricaoFinal = montarDescricao(descricaoBase, tipo);
//
//            if (permissaoBanco == null) {
//                Permissao novaP = new Permissao();
//                novaP.setChave(chave.name());
//                novaP.setTipo(tipo);
//                novaP.setCategoria(categoria);
//                novaP.setNivel(nivel);
//                novaP.setDescricao(descricaoFinal);
//
//                idPerm = pDao.save(novaP);
//                pmDao.vincular(idPerm, idMenu);
//            } else {
//                idPerm = permissaoBanco.getIdPermissoes();
//                if (permissaoBanco.getNivel() != nivel || !permissaoBanco.getDescricao().equals(descricaoFinal)) {
//                    permissaoBanco.setNivel(nivel);
//                    permissaoBanco.setDescricao(descricaoFinal);
//                    pDao.update(permissaoBanco);
//                }
//            }
//            idsGerados.add(idPerm);
//        }
//        return idsGerados;
//    }
//
//    private String montarDescricao(String base, String tipo) {
//        return base + " [" + tipo + "]";
//    }
//
//    /**
//     * Verifica se o perfil informado é o perfil MASTER.
//     */
//    public boolean isPerfilMaster(Perfil perfil) {
//        return perfil != null && PERFIL_MASTER.equalsIgnoreCase(perfil.getNome());
//    }
//}