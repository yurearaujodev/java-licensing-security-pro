package com.br.yat.gerenciador.dao.empresa;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.model.Documento;

public class DocumentoDao extends GenericDao<Documento> {

	public DocumentoDao(Connection conn) {
		super(conn, "documento_empresa", "id_documento");
	}

	public Documento save(Documento doc) {
		var sql = "INSERT INTO documento_empresa(tipo,arquivo,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,NOW(),NOW(),?)";
		int id = executeInsert(sql, doc.getTipoDocumento(), doc.getArquivoDocumento(), doc.getEmpresa().getIdEmpresa());
		doc.setIdDocumento(id);
		return doc;
	}

	public Documento update(Documento doc) {
		var sql = "UPDATE " + tableName + " SET tipo=?,arquivo=?,atualizado_em=NOW() " + "WHERE " + pkName + " = ?";

		executeUpdate(sql, doc.getTipoDocumento(), doc.getArquivoDocumento(), doc.getIdDocumento());
		return doc;
	}

	public List<Documento> listAll() {
		return executeQuery("SELECT * FROM " + tableName + " WHERE deletado_em IS NULL");
	}

	public List<Documento> listarPorEmpresa(int idEmpresa) {
		String sql = "SELECT * FROM " + tableName + " WHERE id_empresa = ? AND deletado_em IS NULL";
		return executeQuery(sql, idEmpresa);
	}

	@Override
	protected Documento mapResultSetToEntity(ResultSet rs) throws SQLException {
		Documento d = new Documento();
		d.setIdDocumento(rs.getInt(pkName));
		d.setTipoDocumento(rs.getString("tipo"));
		d.setArquivoDocumento(rs.getString("arquivo"));
		return d;
	}

	public void syncByEmpresa(int idEmpresa, List<Documento> novos) {
		List<Documento> atuais = listarPorEmpresa(idEmpresa);

		syncByParentId(novos, atuais, Documento::getIdDocumento, this::save, this::update,
				d -> softDeleteById(d.getIdDocumento()));
	}

}
