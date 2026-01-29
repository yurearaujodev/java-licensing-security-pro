package com.br.yat.gerenciador.dao.empresa;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.enums.TipoContato;

public class ContatoDao extends GenericDao<Contato> {

	public ContatoDao(Connection conn) {
		super(conn, "contato_empresa", "id_contato");
	}

	public Contato save(Contato cont) {
		var sql = "INSERT INTO " + tableName + "(tipo_contato,valor,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,NOW(),NOW(),?)";

		int id = executeInsert(sql, cont.getTipoContato(), cont.getValorContato(), cont.getEmpresa().getIdEmpresa());
		cont.setIdContato(id);
		return cont;
	}

	public Contato update(Contato cont) {
		var sql = "UPDATE " + tableName + " SET tipo_contato=?,valor=?,atualizado_em=NOW() " + "WHERE " + pkName
				+ " = ?";
		executeUpdate(sql, cont.getTipoContato(), cont.getValorContato(), cont.getIdContato());
		return cont;
	}

	public List<Contato> listAll() {
		return executeQuery("SELECT * FROM " + tableName+" WHERE deletado_em IS NULL");
	}

	public List<Contato> listarPorEmpresa(int idEmpresa) {
		String sql = "SELECT * FROM " + tableName + " WHERE id_empresa = ? AND deletado_em IS NULL";
		return executeQuery(sql, idEmpresa);
	}

	@Override
	protected Contato mapResultSetToEntity(ResultSet rs) throws SQLException {
		Contato c = new Contato();
		c.setIdContato(rs.getInt(pkName));
		c.setTipoContato(valueOf(TipoContato.class, rs.getString("tipo_contato")));
		c.setValorContato(rs.getString("valor"));

		return c;
	}

	public void syncByEmpresa(int idEmpresa, List<Contato> novos) {
		List<Contato> atuais = listarPorEmpresa(idEmpresa);

		syncByParentId(novos, atuais, Contato::getIdContato, this::save, this::update, c -> softDeleteById(c.getIdContato()));

	}

}
