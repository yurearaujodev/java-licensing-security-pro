package com.br.yat.gerenciador.dao.empresa;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.model.Representante;

public class RepresentanteDao extends GenericDao<Representante> {

	public RepresentanteDao(Connection conn) {
		super(conn, "representante_legal", "id_representante");
	}

	public Representante save(Representante rep) {
		var sql = "INSERT INTO " + tableName
				+ "(nome,cpf,rg,cargo,nacionalidade,estado_civil,telefone,email,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,?,?,?,?,?,?,NOW(),NOW(),?)";

		int id = executeInsert(sql, rep.getNomeRepresentante(), rep.getCpfRepresentante(), rep.getRgRepresentante(),
				rep.getCargoRepresentante(), rep.getNacionalidadeRepresentante(), rep.getEstadoCivilRepresentante(),
				rep.getTelefoneRepresentante(), rep.getEmailRepresentante(), rep.getEmpresa().getIdEmpresa());
		rep.setIdRepresentante(id);
		return rep;
	}

	public Representante update(Representante rep) {
		var sql = "UPDATE " + tableName
				+ " SET nome=?,cpf=?,rg=?,cargo=?,nacionalidade=?,estado_civil=?,telefone=?,email=?,atualizado_em=NOW() "
				+ "WHERE " + pkName + " = ?";
		executeUpdate(sql, rep.getNomeRepresentante(), rep.getCpfRepresentante(), rep.getRgRepresentante(),
				rep.getCargoRepresentante(), rep.getNacionalidadeRepresentante(), rep.getEstadoCivilRepresentante(),
				rep.getTelefoneRepresentante(), rep.getEmailRepresentante(), rep.getIdRepresentante());
		return rep;
	}

	public List<Representante> listAll() {
		return executeQuery("SELECT * FROM " + tableName + " WHERE deletado_em IS NULL");
	}

	public List<Representante> listarPorEmpresa(int idEmpresa) {
		String sql = "SELECT * FROM " + tableName + " WHERE id_empresa = ? AND deletado_em IS NULL";
		return executeQuery(sql, idEmpresa);
	}

	@Override
	protected Representante mapResultSetToEntity(ResultSet rs) throws SQLException {
		Representante r = new Representante();
		r.setIdRepresentante(rs.getInt(pkName));
		r.setNomeRepresentante(rs.getString("nome"));
		r.setCpfRepresentante(rs.getString("cpf"));
		r.setRgRepresentante(rs.getString("rg"));
		r.setCargoRepresentante(rs.getString("cargo"));
		r.setNacionalidadeRepresentante(rs.getString("nacionalidade"));
		r.setEstadoCivilRepresentante(rs.getString("estado_civil"));
		r.setTelefoneRepresentante(rs.getString("telefone"));
		r.setEmailRepresentante(rs.getString("email"));
		return r;
	}

	public void syncByEmpresa(int idEmpresa, List<Representante> novos) {
		List<Representante> atuais = listarPorEmpresa(idEmpresa);

		syncByParentId(novos, atuais, Representante::getIdRepresentante, this::save, this::update,
				r -> softDeleteById(r.getIdRepresentante()));
	}

}
