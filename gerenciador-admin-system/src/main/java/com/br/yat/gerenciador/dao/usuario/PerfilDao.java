package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;

public class PerfilDao extends GenericDao<Perfil> {

	public PerfilDao(Connection conn) {
		super(conn, "perfil", "id_perfil");
	}

	public int save(Perfil p) {
		String sql = "INSERT INTO perfil (nome, descricao, criado_em, atualizado_em) VALUES (?, ?, NOW(), NOW())";
		return executeInsert(sql, p.getNome(), p.getDescricao());
	}

	public void update(Perfil p) {
		String sql = "UPDATE perfil SET nome = ?, descricao = ?, atualizado_em = NOW() WHERE id_perfil = ?";
		executeUpdate(sql, p.getNome(), p.getDescricao(), p.getIdPerfil());
	}

	public List<Perfil> listarExcluidos() {
		String sql = "SELECT * FROM " + tableName + " WHERE deletado_em IS NOT NULL ORDER BY nome ASC";
		return executeQuery(sql);
	}

	public void restaurar(int idPerfil) {
		String sql = "UPDATE " + tableName + " SET deletado_em = NULL, atualizado_em = NOW() " + " WHERE " + pkName
				+ " = ?";
		executeUpdate(sql, idPerfil);
	}

	@Override
	public Perfil searchById(int id) {
		String sql = "SELECT * FROM " + tableName + " WHERE " + pkName + " = ? AND deletado_em IS NULL";
		List<Perfil> resultados = executeQuery(sql, id);
		return resultados.isEmpty() ? null : resultados.get(0);
	}

	@Override
	public void softDeleteById(int id) {
		Perfil perfil = searchById(id);

		if (perfil != null && "MASTER".equalsIgnoreCase(perfil.getNome())) {
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED,
					"ERRO: O PERFIL MASTER NÃO PODE SER EXCLUÍDO.", null);
		}

		super.softDeleteById(id);
	}

	public Perfil searchByIdIncluindoExcluidos(int id) {
		String sql = "SELECT * FROM " + tableName + " WHERE " + pkName + " = ?";
		List<Perfil> resultados = executeQuery(sql, id);
		return resultados.isEmpty() ? null : resultados.get(0);
	}

	public List<Perfil> listAll() {
		String sql = "SELECT * FROM " + tableName + " WHERE deletado_em IS NULL ORDER BY nome ASC";
		return executeQuery(sql);
	}

	public Optional<Perfil> buscarPorNome(String nome) {
		String sql = "SELECT * FROM " + tableName + " WHERE UPPER(nome) = UPPER(?) AND deletado_em IS NULL";
		List<Perfil> resultados = executeQuery(sql, nome);
		return resultados.isEmpty() ? Optional.empty() : Optional.of(resultados.get(0));
	}

	@Override
	protected Perfil mapResultSetToEntity(ResultSet rs) throws SQLException {
		Perfil p = new Perfil();
		p.setIdPerfil(rs.getInt("id_perfil"));
		p.setNome(rs.getString("nome"));
		p.setDescricao(rs.getString("descricao"));
		return p;
	}
}