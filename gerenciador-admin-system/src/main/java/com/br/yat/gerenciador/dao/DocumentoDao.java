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
		int id = executeUpdate(sql, doc.getTipoDocumento(),doc.getArquivoDocumento(),doc.getEmpresa().getIdEmpresa());
		return searchById(id);
	}

	public Documento update(Documento entidade) {
		return entidade;

	}

	public void delete(int id) {
		// TODO Auto-generated method stub

	}

	public Documento searchById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Documento> listAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Documento mapResultSetToEntity(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
