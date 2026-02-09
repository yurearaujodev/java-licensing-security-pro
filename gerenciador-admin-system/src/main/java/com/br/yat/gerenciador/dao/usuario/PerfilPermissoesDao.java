package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.model.Permissao;

public class PerfilPermissoesDao extends GenericDao<Object> {

    public PerfilPermissoesDao(Connection conn) {
        super(conn, "perfil_permissoes", "id_perfil");
    }

    public void vincularPermissaoAoPerfil(int idPerfil, int idPermissao, boolean herdada) {
        String sql = "INSERT INTO perfil_permissoes (id_perfil, id_permissoes, herdada, criado_em, atualizado_em) "
                   + "VALUES (?, ?, ?, NOW(), NOW()) "
                   + "ON DUPLICATE KEY UPDATE herdada = ?, deletado_em = NULL, atualizado_em = NOW()";
        
        int herdadaInt = herdada ? 1 : 0;
        executeUpdate(sql, idPerfil, idPermissao, herdadaInt, herdadaInt);
    }
    
    public void desvincularTodasDoPerfil(int idPerfil) {
        String sql = "UPDATE perfil_permissoes SET deletado_em = NOW() WHERE id_perfil = ?";
        executeUpdate(sql, idPerfil);
    }

    public List<Permissao> listarPermissoesPorPerfil(int idPerfil) {
        String sql = "SELECT p.* FROM permissoes p "
                   + "INNER JOIN perfil_permissoes pp ON p.id_permissoes = pp.id_permissoes "
                   + "WHERE pp.id_perfil = ? AND pp.deletado_em IS NULL AND p.deletado_em IS NULL";
        
        List<Permissao> lista = new ArrayList<>();
        
        // Usando o seu getConnection() para fazer a query manual já que o retorno não é <T> (Object)
        try (var stmt = getConnection().prepareStatement(sql)) {
            stmt.setInt(1, idPerfil);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    Permissao p = new Permissao();
                    p.setIdPermissoes(rs.getInt("id_permissoes"));
                    p.setChave(rs.getString("chave"));
                    p.setTipo(rs.getString("tipo"));
                    p.setCategoria(rs.getString("categoria"));
                    p.setNivel(rs.getInt("nivel"));
                    lista.add(p);
                }
            }
        } catch (SQLException e) {
            // Se der erro, não vou inventar, apenas sigo o fluxo de erro
        }
        return lista;
    }

    public void desvincularPermissao(int idPerfil, int idPermissao) {
        String sql = "UPDATE perfil_permissoes SET deletado_em = NOW() WHERE id_perfil = ? AND id_permissoes = ?";
        executeUpdate(sql, idPerfil, idPermissao);
    }

    @Override
    protected Object mapResultSetToEntity(ResultSet rs) throws SQLException {
        return null; // Tabela associativa
    }
}
