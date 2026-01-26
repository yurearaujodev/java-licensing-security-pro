package com.br.yat.gerenciador.dao.empresa;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.model.Endereco;

public class EnderecoDao extends GenericDao<Endereco> {

	public EnderecoDao(Connection conn){
		super(conn, "endereco", "id_endereco");
	}

	public Endereco save(Endereco end) {
		String sql = "INSERT INTO " + tableName
				+ "(cep,logradouro,complemento,bairro,numero,cidade,estado,pais,criado_em,atualizado_em) "
				+ "VALUES(?,?,?,?,?,?,?,?, NOW(),NOW())";

		int id = executeInsert(sql, end.getCepEndereco(), end.getLogradouroEndereco(), end.getComplementoEndereco(),
				end.getBairroEndereco(), end.getNumeroEndereco(), end.getCidadeEndereco(), end.getEstadoEndereco(),
				end.getPaisEndereco());

		end.setIdEndereco(id);
		return end;
	}

	public Endereco update(Endereco end) {
		String sql = "UPDATE " + tableName
				+ " SET cep=?, logradouro=?, complemento=?, bairro=?, numero=?, cidade=?, estado=?, pais=?,atualizado_em=NOW() "
				+ "WHERE " + pkName + "= ?";
		executeUpdate(sql, end.getCepEndereco(), end.getLogradouroEndereco(), end.getComplementoEndereco(),
				end.getBairroEndereco(), end.getNumeroEndereco(), end.getCidadeEndereco(), end.getEstadoEndereco(),
				end.getPaisEndereco(), end.getIdEndereco());
		return end;
	}

	public void delete(int id) {
		executeUpdate("DELETE FROM " + tableName + " WHERE " + pkName + " = ?", id);
	}

	public List<Endereco> listAll() {
		return executeQuery("SELECT * FROM " + tableName);
	}

	@Override
	protected Endereco mapResultSetToEntity(ResultSet rs) throws SQLException {
		Endereco e = new Endereco();
		e.setIdEndereco(rs.getInt(pkName));
		e.setCepEndereco(rs.getString("cep"));
		e.setLogradouroEndereco(rs.getString("logradouro"));
		e.setComplementoEndereco(rs.getString("complemento"));
		e.setBairroEndereco(rs.getString("bairro"));
		e.setNumeroEndereco(rs.getString("numero"));
		e.setCidadeEndereco(rs.getString("cidade"));
		e.setEstadoEndereco(rs.getString("estado"));
		e.setPaisEndereco(rs.getString("pais"));
		return e;
	}

}
