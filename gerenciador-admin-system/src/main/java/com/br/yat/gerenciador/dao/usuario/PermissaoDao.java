package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.model.Permissao;

public class PermissaoDao extends GenericDao<Permissao> {

    public PermissaoDao(Connection conn) {
        super(conn, "permissoes", "id_permissoes");
    }
    
 // ESTE É O MÉTODO QUE ESTAVA FALTANDO
    public int save(Permissao p) {
        String sql = "INSERT INTO " + tableName + " (chave, tipo, categoria, criado_em, atualizado_em) VALUES (?, ?, ?, NOW(), NOW())";
        return executeInsert(sql, p.getChave(), p.getTipo(), p.getCategoria());
    }

    public Permissao findByChave(String chave) {
        String sql = "SELECT * FROM " + tableName + " WHERE chave = ? AND deletado_em IS NULL";
        var lista = executeQuery(sql, chave);
        return lista.isEmpty() ? null : lista.get(0);
    }

    @Override
    protected Permissao mapResultSetToEntity(ResultSet rs) throws SQLException {
        Permissao p = new Permissao();
        p.setIdPermissoes(rs.getInt(pkName));
        p.setChave(rs.getString("chave"));
        p.setTipo(rs.getString("tipo"));
        p.setCategoria(rs.getString("categoria"));
        p.setDescricao(rs.getString("descricao"));
        return p;
    }
}
