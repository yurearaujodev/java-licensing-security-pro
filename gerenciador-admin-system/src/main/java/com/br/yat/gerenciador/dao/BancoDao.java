package com.br.yat.gerenciador.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.model.Banco;

public class BancoDao extends GenericDao<Banco> {

	public BancoDao(Connection conn) throws SQLException {
		super(conn, "dados_bancarios", "id_banco");
	}

	public Banco save(Banco ban) {
		var sql = "INSERT INTO dados_bancarios(banco,codigo_banco,agencia,conta,tipo,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,?,?,?,NOW(),NOW(),?)";
		
		int id = executeUpdate(sql, ban.getNomeBanco(),ban.getCodBanco(),ban.getAgenciaBanco(),ban.getContaBanco(),
				ban.getTipoBanco(),ban.getEmpresa().getIdEmpresa()
				);
		return searchById(id);
		
	}

	public Banco update(Banco entidade) {
		return entidade;

	}

	public void delete(int id) {
		// TODO Auto-generated method stub

	}

	public Banco searchById(int id) {
		// TODO Auto-generated method stub
		return null;
	}

	public List<Banco> listAll() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	protected Banco mapResultSetToEntity(ResultSet rs) throws SQLException {
		// TODO Auto-generated method stub
		return null;
	}

}
