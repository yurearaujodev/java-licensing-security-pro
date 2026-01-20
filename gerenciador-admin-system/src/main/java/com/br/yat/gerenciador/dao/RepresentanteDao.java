package com.br.yat.gerenciador.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.model.Representante;

public class RepresentanteDao extends GenericDao<Representante> {

	public RepresentanteDao(Connection conn) throws SQLException {
		super(conn, "representante_legal", "id_representante");
	}

	public Representante save(Representante rep) {
		var sql = "INSERT INTO " + tableName
				+ "(nome,cpf,rg,cargo,nacionalidade,estado_civil,telefone,email,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,?,?,?,?,?,?,NOW(),NOW(),?)";

		int id = executeUpdate(sql, rep.getNomeRepresentante(), rep.getCpfRepresentante(), rep.getRgRepresentante(),
				rep.getCargoRepresentante(), rep.getNacionalidadeRepresentante(), rep.getEstadoCivilRepresentante(),
				rep.getTelefoneRepresentante(), rep.getEmailRepresentante(), rep.getEmpresa().getIdEmpresa());
		return searchById(id);
	}

	public Representante update(Representante rep) {
		var sql = "UPDATE " + tableName
				+ " SET nome=?,cpf=?,rg=?,cargo=?,nacionalidade=?,estado_civil=?,telefone=?,email=?,atualizado_em=NOW() "
				+ "WHERE " + pkName + " = ?";
		executeUpdate(sql, rep.getNomeRepresentante(), rep.getCpfRepresentante(), rep.getRgRepresentante(),
				rep.getCargoRepresentante(), rep.getNacionalidadeRepresentante(), rep.getEstadoCivilRepresentante(),
				rep.getTelefoneRepresentante(), rep.getEmailRepresentante(), rep.getIdRepresentante());
		return searchById(rep.getIdRepresentante());
	}

	public void delete(int id) {
		executeUpdate("DELETE FROM " + tableName + " WHERE " + pkName + " = ?", id);
	}

	public List<Representante> listAll() {
		return executeQuery("SELECT * FROM " + tableName);
	}

	public List<Representante> listarPorEmpresa(int idEmpresa) {
		return listByForeignKey("id_empresa", idEmpresa);
	}

	public void deleteByEmpresa(int idEmpresa) {
		String sql = "DELETE FROM " + tableName + " WHERE id_empresa=?";
		executeUpdate(sql, idEmpresa);
	}

	@Override
	protected Representante mapResultSetToEntity(ResultSet rs) throws SQLException {
		Representante r = new Representante();
		r.setIdRepresentante(rs.getInt(pkName));
		r.setNomeRepresentante(rs.getString("nome"));
		r.setRgRepresentante(rs.getString("rg"));
		r.setCargoRepresentante(rs.getString("cargo"));
		r.setNacionalidadeRepresentante(rs.getString("nacionalidade"));
		r.setEstadoCivilRepresentante(rs.getString("estado_civil"));
		r.setTelefoneRepresentante(rs.getString("telefone"));
		r.setEmailRepresentante(rs.getString("email"));
		return r;
	}

}
