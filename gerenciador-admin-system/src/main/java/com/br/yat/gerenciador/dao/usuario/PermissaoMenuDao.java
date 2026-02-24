package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.br.yat.gerenciador.dao.GenericDao;

public class PermissaoMenuDao extends GenericDao<Object> {
	public PermissaoMenuDao(Connection conn) {
		super(conn, "permissao_menu", "id_permissao");
	}

	public void vincular(int idPermissao, int idMenu) {
		String checkSql = "SELECT COUNT(*) FROM permissao_menu WHERE id_permissao = ? AND id_menu = ?";

		Integer count = executeQuerySingle(checkSql, rs -> {
			try {
				return rs.getInt(1);
			} catch (SQLException e) {
				return 0;
			}
		}, idPermissao, idMenu);

		if (count == null || count == 0) {
			String sql = "INSERT INTO permissao_menu (id_permissao, id_menu, criado_em, atualizado_em) VALUES (?, ?, NOW(), NOW())";
			executeUpdate(sql, idPermissao, idMenu);
		}
	}

	@Override
	protected Object mapResultSetToEntity(ResultSet rs) throws SQLException {
		return null;
	}
}
