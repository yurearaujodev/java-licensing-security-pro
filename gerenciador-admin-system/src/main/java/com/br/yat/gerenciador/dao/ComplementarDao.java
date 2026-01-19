package com.br.yat.gerenciador.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.model.Complementar;

public class ComplementarDao extends GenericDao<Complementar>{

	public ComplementarDao(Connection conn) throws SQLException {
		super(conn,"informacoes_complementares","id_info");
	}

	public Complementar save(Complementar com) {
		var sql="INSERT INTO informacoes_complementares(logotipo,numero_funcionarios,ramo_atividade,observacoes,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,?,?,NOW(),NOW(),?)";
		
		int id = executeUpdate(sql, com.getLogoTipoComplementar(),com.getNumFuncionariosComplementar(),com.getRamoAtividadeComplementar(),
				com.getObsComplementar(),com.getEmpresa().getIdEmpresa()
				);
		return searchById(id);
		
	}

	public Complementar update(Complementar entidade){
		return entidade;
	}

	public void delete(int id){
		
	}

	public Complementar searchById(int id){
		// TODO Auto-generated method stub
		return null;
	}

	public List<Complementar> listAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Complementar mapResultSetToEntity(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
