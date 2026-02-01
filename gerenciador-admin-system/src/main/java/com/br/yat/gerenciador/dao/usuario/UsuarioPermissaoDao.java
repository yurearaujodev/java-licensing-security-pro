package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;

public class UsuarioPermissaoDao extends GenericDao<UsuarioPermissao> {

    public UsuarioPermissaoDao(Connection conn) {
        super(conn, "usuario_permissoes", "id_usuario");
    }

    public List<MenuChave> buscarChavesAtivasPorUsuario(int idUsuario) {
        List<MenuChave> chaves = new ArrayList<>();
        String sql = "SELECT p.chave FROM usuario_permissoes up "
                   + "INNER JOIN permissoes p ON up.id_permissoes = p.id_permissoes "
                   + "WHERE up.id_usuario = ? AND up.ativa = 1 AND up.deletado_em IS NULL";

        try (var stmt = conn.prepareStatement(sql)) {
            stmt.setInt(1, idUsuario);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    try {
                        chaves.add(MenuChave.valueOf(rs.getString("chave")));
                    } catch (Exception e) { continue; }
                }
            }
        } catch (SQLException e) {
            throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, e.getMessage(), e);
        }
        return chaves;
    }

    public void saveOrUpdate(UsuarioPermissao up) {
        String sql = "INSERT INTO usuario_permissoes (id_usuario, id_permissoes, ativa, id_usuario_concedeu, criado_em, atualizado_em) "
                   + "VALUES (?, ?, ?, ?, NOW(), NOW()) "
                   + "ON DUPLICATE KEY UPDATE ativa = ?, deletado_em = NULL, atualizado_em = NOW(), id_usuario_concedeu = ?";
        
        int idConcedeu = up.getUsuarioConcedeu().getIdUsuario();
        int ativaInt = up.isAtiva() ? 1 : 0;

        executeUpdate(sql, 
                up.getIdUsuario(), up.getIdPermissoes(), ativaInt, idConcedeu,
                ativaInt, idConcedeu);
    }

    public void disableAllFromUser(int idUsuario) {
        String sql = "UPDATE usuario_permissoes SET ativa = 0, deletado_em = NOW() WHERE id_usuario = ?";
        executeUpdate(sql, idUsuario);
    }

    @Override
    protected UsuarioPermissao mapResultSetToEntity(ResultSet rs) throws SQLException {
        UsuarioPermissao up = new UsuarioPermissao();
        up.setIdUsuario(rs.getInt("id_usuario"));
        up.setIdPermissoes(rs.getInt("id_permissoes"));
        up.setAtiva(rs.getBoolean("ativa"));
        return up;
    }
}
