package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;

public class PermissaoDao extends GenericDao<Permissao> {

	public PermissaoDao(Connection conn) {
		super(conn, "permissoes", "id_permissoes");
	}

	public int save(Permissao p) {
		String sql = "INSERT INTO " + tableName
				+ " (chave, tipo, categoria,nivel, criado_em, atualizado_em) VALUES (?, ?, ?,?, NOW(), NOW())";
		return executeInsert(sql, p.getChave(), p.getTipo(), p.getCategoria(), p.getNivel());
	}

	public Permissao findByChave(String chave) {
		String sql = "SELECT * FROM " + tableName + " WHERE chave = ? AND deletado_em IS NULL";
		var lista = executeQuery(sql, chave);
		return lista.isEmpty() ? null : lista.get(0);
	}

	public Permissao findByChaveETipo(String chave, String tipo) {
		String sql = "SELECT * FROM " + tableName + " WHERE chave = ? AND tipo = ? AND deletado_em IS NULL";

		var lista = executeQuery(sql, chave, tipo);

		return lista.isEmpty() ? null : lista.get(0);
	}

	public Permissao findById(Integer idPermissao) {
		String sql = "SELECT * FROM " + tableName + " WHERE id_permissoes = ? AND deletado_em IS NULL";
		var lista = executeQuery(sql, idPermissao);
		return lista.isEmpty() ? null : lista.get(0);
	}
	
	public List<Permissao> listAll() {
	    // Usamos o tableName definido no construtor ("permissoes")
	    String sql = "SELECT * FROM " + tableName + " WHERE deletado_em IS NULL";
	    return executeQuery(sql);
	}
	
	public List<String> buscarTiposAtivosPorUsuarioEMenu(Integer idUsuario, String chaveMenu) {
	    List<String> tipos = new ArrayList<>();
	    String sql = "SELECT p.tipo FROM permissoes p "
	               + "INNER JOIN usuario_permissoes up ON p.id_permissoes = up.id_permissoes "
	               + "WHERE up.id_usuario = ? "
	               + "AND p.chave = ? "
	               + "AND up.ativa = 1 "
	               + "AND p.deletado_em IS NULL "
	               + "AND (up.expira_em IS NULL OR up.expira_em > NOW())"; // <--- VALIDAÇÃO DE TEMPO

	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        bindParameters(stmt, idUsuario, chaveMenu);
	        try (ResultSet rs = stmt.executeQuery()) {
	            while (rs.next()) {
	                tipos.add(rs.getString("tipo"));
	            }
	        }
	    } catch (SQLException e) {
	        throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, "ERRO AO BUSCAR PERMISSÕES", e);
	    }
	    return tipos;
	}

	public List<Permissao> listarPorIds(List<Integer> ids) {
		if (ids == null || ids.isEmpty()) {
			return List.of();
		}

		// Gera placeholders para a query: ?, ?, ?
		String placeholders = ids.stream().map(i -> "?").collect(Collectors.joining(","));
		String sql = "SELECT * FROM " + tableName + " WHERE id_permissoes IN (" + placeholders
				+ ") AND deletado_em IS NULL";

		return executeQuery(sql, ids.toArray());
	}

	public List<Permissao> listarPermissoesAtivasPorUsuario(Integer idUsuario) {
	    String sql = "SELECT p.* FROM permissoes p "
	            + "INNER JOIN usuario_permissoes up ON p.id_permissoes = up.id_permissoes "
	            + "WHERE up.id_usuario = ? AND up.ativa = 1 AND p.deletado_em IS NULL "
	            + "AND (up.expira_em IS NULL OR up.expira_em > NOW())"; // ADICIONADO PARA SEGURANÇA

	    return executeQuery(sql, idUsuario);
	}

	@Override
	protected Permissao mapResultSetToEntity(ResultSet rs) throws SQLException {
		Permissao p = new Permissao();
		p.setIdPermissoes(rs.getInt(pkName));
		p.setChave(rs.getString("chave"));
		p.setTipo(rs.getString("tipo"));
		p.setCategoria(rs.getString("categoria"));
		p.setDescricao(rs.getString("descricao"));
		p.setNivel(rs.getInt("nivel"));
		return p;
	}
}
