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
		var sql="INSERT INTO representante_legal(nome,cpf,rg,cargo,nacionalidade,estado_civil,telefone,email,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,?,?,?,?,?,?,NOW(),NOW(),?)";
		
		int id = executeUpdate(sql, rep.getNomeRepresentante(),rep.getCpfRepresentante(),rep.getRgRepresentante(),rep.getCargoRepresentante(),
				rep.getNacionalidadeRepresentante(),rep.getEstadoCivilRepresentante(),rep.getTelefoneRepresentante(),rep.getEmailRepresentante(),
				rep.getEmpresa().getIdEmpresa()
				);
		return searchById(id);
	}

	public Representante update(Representante entidade) {
		return entidade;

	}

	public void delete(int id) {
		// TODO Auto-generated method stub

	}

	public Representante searchById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Representante> listAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Representante mapResultSetToEntity(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
