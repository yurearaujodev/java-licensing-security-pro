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
		var sql = "INSERT INTO contato_empresa(tipo_contato,valor,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,NOW(),NOW(),?)";

		int id = executeUpdate(sql, cont.getTipoContato(), cont.getValorContato(), cont.getEmpresa().getIdEmpresa());
		return searchById(id);
	}

	public Contato update(Contato cont){
		return cont;
		
		
	}


	public void delete(int id){

	}

	@Override
	public Contato searchById(int id){
		// TODO Auto-generated method stub
		return null;
	}

	public List<Contato> listAll() {
		// TODO Auto-generated method stub
		return null;
	}
	
	public List<Contato> listarPorEmpresa(int idEmpresa){
		return listByForeignKey("id_empresa", idEmpresa);
	}

	@Override
	protected Contato mapResultSetToEntity(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
