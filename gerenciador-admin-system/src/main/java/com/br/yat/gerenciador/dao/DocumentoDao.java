package com.br.yat.gerenciador.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.model.Documento;

public class DocumentoDao extends GenericDao<Documento> {

	public DocumentoDao(Connection conn) throws SQLException {
		super(conn, "documento_empresa", "id_documento");
	}

	public Documento save(Documento doc) {
		var sql = "INSERT INTO documento_empresa(tipo,arquivo,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,NOW(),NOW(),?)";
		int id = executeUpdate(sql, doc.getTipoDocumento(), doc.getArquivoDocumento(), doc.getEmpresa().getIdEmpresa());
		return searchById(id);
	}

	public Documento update(Documento doc) {
		var sql = "UPDATE" + tableName + " SET tipo=?,arquivo=?,atualizado_em=NOW() " + "WHERE " + pkName + " = ?";

		executeUpdate(sql, doc.getTipoDocumento(), doc.getArquivoDocumento(), doc.getIdDocumento());
		return searchById(doc.getIdDocumento());
	}

	public void delete(int id) {
		executeUpdate("DELETE FROM " + tableName + " WHERE " + pkName + " = ?", id);
	}

	public List<Documento> listAll() {
		return executeQuery("SELECT * FROM " + tableName);
	}

	public List<Documento> listarPorEmpresa(int idEmpresa) {
		return listByForeignKey("id_empresa", idEmpresa);
	}

	public void deleteByEmpresa(int idEmpresa) {
		String sql = "DELETE FROM " + tableName + " WHERE id_empresa=?";
		executeUpdate(sql, idEmpresa);
	}

	@Override
	protected Documento mapResultSetToEntity(ResultSet rs) throws SQLException {
		Documento d = new Documento();
		d.setIdDocumento(rs.getInt(pkName));
		d.setTipoDocumento(rs.getString("tipo"));
		d.setArquivoDocumento(rs.getString("arquivo"));
		return d;
	}

}
