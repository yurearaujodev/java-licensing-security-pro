package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.model.Perfil;

public class PerfilDao extends GenericDao<Perfil> {

    public PerfilDao(Connection conn) {
        // Aproveita o construtor da Generic que já valida a conexão
        super(conn, "perfil", "id_perfil");
    }

    public int save(Perfil p) {
        String sql = "INSERT INTO perfil (nome, descricao, criado_em, atualizado_em) VALUES (?, ?, NOW(), NOW())";
        // Usa o executeInsert da Generic que já trata parâmetros e chaves geradas
        return executeInsert(sql, p.getNome(), p.getDescricao());
    }

    public void update(Perfil p) {
        String sql = "UPDATE perfil SET nome = ?, descricao = ?, atualizado_em = NOW() WHERE id_perfil = ?";
        // Usa o executeUpdate da Generic
        executeUpdate(sql, p.getNome(), p.getDescricao(), p.getIdPerfil());
    }

    /**
     * Retorna todos os perfis ativos.
     * Nota: A GenericDao já tem searchById e softDeleteById usando 'deletado_em'.
     */
    public List<Perfil> listAll() {
        String sql = "SELECT * FROM " + tableName + " WHERE deletado_em IS NULL ORDER BY nome ASC";
        return executeQuery(sql);
    }
    
    public Optional<Perfil> buscarPorNome(String nome) {
        // Usamos UPPER para garantir que 'master' ou 'Master' encontrem 'MASTER'
        String sql = "SELECT * FROM " + tableName + " WHERE UPPER(nome) = UPPER(?) AND deletado_em IS NULL";
        List<Perfil> resultados = executeQuery(sql, nome);
        return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
    }

    @Override
    protected Perfil mapResultSetToEntity(ResultSet rs) throws SQLException {
        Perfil p = new Perfil();
        p.setIdPerfil(rs.getInt("id_perfil"));
        p.setNome(rs.getString("nome"));
        p.setDescricao(rs.getString("descricao"));
        return p;
    }
}