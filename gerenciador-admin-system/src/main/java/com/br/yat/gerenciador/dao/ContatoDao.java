package com.br.yat.gerenciador.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.model.Contato;

public class ContatoDao extends GenericDao<Contato> {

	public ContatoDao(Connection conn) throws SQLException {
		super(conn, "contato_empresa", "id_contato");
	}

	public Contato save(Contato cont) throws SQLException {
		var sql = "INSERT INTO " + tableName + "(tipo_contato,valor,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,NOW(),NOW(),?)";

		int id = executeUpdate(sql, cont.getTipoContato(), cont.getValorContato(), cont.getEmpresa().getIdEmpresa());
		return searchById(id);
	}

	public Contato update(Contato cont) {
		var sql = "UPDATE " + tableName + " SET tipo_contato=?,valor=?,atualizado_em=NOW() " + "WHERE " + pkName
				+ " = ?";
		executeUpdate(sql, cont.getTipoContato(), cont.getValorContato(), cont.getIdContato());
		return searchById(cont.getIdContato());
	}

	public void delete(int id) {
		executeUpdate("DELETE FROM " + tableName + " WHERE " + pkName + " = ?", id);
	}

	public List<Contato> listAll() {
		return executeQuery("SELECT * FROM " + tableName);
	}

	public List<Contato> listarPorEmpresa(int idEmpresa) {
		return listByForeignKey("id_empresa", idEmpresa);
	}

	public void deleteByEmpresa(int idEmpresa) {
		String sql = "DELETE FROM " + tableName + " WHERE id_empresa=?";
		executeUpdate(sql, idEmpresa);
	}

	@Override
	protected Contato mapResultSetToEntity(ResultSet rs) throws SQLException {
		Contato c = new Contato();
		c.setIdContato(rs.getInt(pkName));
		c.setTipoContato(rs.getString("tipo_contato"));
		c.setValorContato(rs.getString("valor"));

		return c;
	}

}
