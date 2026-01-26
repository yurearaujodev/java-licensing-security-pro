package com.br.yat.gerenciador.dao.empresa;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.model.Banco;

public class BancoDao extends GenericDao<Banco> {

	public BancoDao(Connection conn){
		super(conn, "dados_bancarios", "id_banco");
	}

	public Banco save(Banco ban) {
		var sql = "INSERT INTO " + tableName
				+ "(banco,codigo_banco,agencia,conta,tipo,criado_em,atualizado_em,id_empresa) "
				+ "VALUES(?,?,?,?,?,NOW(),NOW(),?)";

		int id = executeInsert(sql, ban.getNomeBanco(), ban.getCodBanco(), ban.getAgenciaBanco(), ban.getContaBanco(),
				ban.getTipoBanco(), ban.getEmpresa().getIdEmpresa());
		ban.setIdBanco(id);
		return ban;

	}

	public Banco update(Banco ban) {
		var sql = "UPDATE " + tableName + " SET banco=?,codigo_banco=?,agencia=?,conta=?,tipo=?atualizado_em=NOW() "
				+ "WHERE " + pkName + " = ?";

		executeUpdate(sql, ban.getNomeBanco(), ban.getCodBanco(), ban.getAgenciaBanco(), ban.getContaBanco(),
				ban.getTipoBanco(), ban.getIdBanco());
		return ban;

	}

	public void delete(int id) {
		executeUpdate("DELETE FROM " + tableName + " WHERE " + pkName + " = ?", id);
	}

	public List<Banco> listAll() {
		return executeQuery("SELECT * FROM " + tableName);
	}

	public List<Banco> listarPorEmpresa(int idEmpresa) {
		String sql = "SELECT * FROM " + tableName + " WHERE id_empresa = ?";
		return executeQuery(sql, idEmpresa);
	}

	public void deleteByEmpresa(int idEmpresa) {
		String sql = "DELETE FROM " + tableName + " WHERE id_empresa=?";
		executeUpdate(sql, idEmpresa);
	}

	@Override
	protected Banco mapResultSetToEntity(ResultSet rs) throws SQLException {
		Banco b = new Banco();
		b.setIdBanco(rs.getInt(pkName));
		b.setNomeBanco(rs.getString("banco"));
		b.setCodBanco(rs.getInt("codigo_banco"));
		b.setAgenciaBanco(rs.getString("agencia"));
		b.setContaBanco(rs.getString("conta"));
		b.setTipoBanco(rs.getString("tipo"));
		return b;
	}

}
