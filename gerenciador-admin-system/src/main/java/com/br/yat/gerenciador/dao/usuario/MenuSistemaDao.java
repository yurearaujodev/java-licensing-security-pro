package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.model.MenuSistema;

public class MenuSistemaDao extends GenericDao<MenuSistema> {
	public MenuSistemaDao(Connection conn) {
		super(conn, "menu_sistema", "id_menu");
	}

	public int save(String chave, String modulo) {
		String sql = "INSERT INTO menu_sistema (chave, modulo, criado_em, atualizado_em) VALUES (?, ?, NOW(), NOW())";
		return executeInsert(sql, chave, modulo);
	}

	public Integer buscarIdPorChave(String chave) {
		String sql = "SELECT id_menu FROM menu_sistema WHERE chave = ? AND deletado_em IS NULL";
		return executeQuerySingle(sql, rs -> {
			try {
				return rs.getInt("id_menu");
			} catch (SQLException e) {
				return null;
			}
		}, chave);
	}

	@Override
	protected MenuSistema mapResultSetToEntity(ResultSet rs) throws SQLException {
		MenuSistema menu = new MenuSistema();
		menu.setIdMenu(rs.getInt("id_menu"));
		menu.setChave(rs.getString("chave"));
		menu.setModulo(rs.getString("modulo"));
		return menu;
	}
}