package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Usuario;

public class UsuarioDao extends GenericDao<Usuario> {

	public UsuarioDao(Connection conn) {
		super(conn, "usuario", "id_usuario");
	}

	public int save(Usuario u) {
		String sql = "INSERT INTO " + tableName
				+ " (nome, email, senha_hash, status, tentativas_falhas, id_empresa, criado_em, atualizado_em) "
				+ "VALUES (?, ?, ?, ?, 0, ?, NOW(), NOW())";

		// Extraímos o ID da empresa de forma segura
		Integer idEmpresa = (u.getIdEmpresa() != null) ? u.getIdEmpresa().getIdEmpresa() : null;

		int id = executeInsert(sql, u.getNome(), u.getEmail(), u.getSenhaHashString(), u.getStatus(), idEmpresa);

		u.setIdUsuario(id);
		u.setTentativasFalhas(0);
		return id;
	}

	public void update(Usuario u) {
		String sql = "UPDATE " + tableName
				+ " SET nome = ?, email = ?, status = ?, id_empresa = ?, atualizado_em = NOW() WHERE id_usuario = ?";
		executeUpdate(sql, u.getNome(), u.getEmail(), u.getStatus(),
				u.getIdEmpresa() != null ? u.getIdEmpresa().getIdEmpresa() : null, u.getIdUsuario());
	}

	public Usuario buscarPorEmail(String email) {
		String sql = "SELECT * FROM " + tableName + " WHERE email = ? AND deletado_em IS NULL";
		var lista = executeQuery(sql, email);
		return lista.isEmpty() ? null : lista.get(0);
	}

	public void atualizarUltimoLogin(int idUsuario) {
		String sql = "UPDATE " + tableName + " SET ultimo_login = NOW(), tentativas_falhas = 0 WHERE id_usuario = ?";
		executeUpdate(sql, idUsuario);
	}

	public void incrementarTentativasFalhas(String email) {
		String sql = "UPDATE " + tableName + " SET tentativas_falhas = tentativas_falhas + 1 WHERE email = ?";
		executeUpdate(sql, email);
	}

	public void bloquearUsuario(int idUsuario) {
		String sql = "UPDATE " + tableName + " SET status = 'BLOQUEADO', atualizado_em = NOW() WHERE id_usuario = ?";
		executeUpdate(sql, idUsuario);
	}

	public List<Usuario> listAll() {
		// Note que mudamos e.razao_social para e.razao_social AS razao_social_empresa
		String sql = "SELECT u.*, e.razao_social AS razao_social_empresa " + "FROM " + tableName + " u "
				+ "LEFT JOIN empresa e ON u.id_empresa = e.id_empresa " + "WHERE u.deletado_em IS NULL";
		return executeQuery(sql);
	}

	public List<Usuario> listarPorNomeOuEmail(String termo) {
		String sql = "SELECT u.*, e.razao_social AS razao_social_empresa " + "FROM " + tableName + " u "
				+ "LEFT JOIN empresa e ON u.id_empresa = e.id_empresa " + "WHERE (u.nome LIKE ? OR u.email LIKE ?) "
				+ "AND u.deletado_em IS NULL";

		String likeTermo = "%" + termo + "%";
		return executeQuery(sql, likeTermo, likeTermo);
	}

	@Override
	protected Usuario mapResultSetToEntity(ResultSet rs) throws SQLException {
		Usuario u = new Usuario();
		u.setIdUsuario(rs.getInt(pkName));
		u.setNome(rs.getString("nome"));
		u.setEmail(rs.getString("email"));
		u.setSenhaHashString(rs.getString("senha_hash"));
		u.setStatus(rs.getString("status"));

		// IMPORTANTE: Criar o objeto Empresa para evitar NullPointerException
		Empresa emp = new Empresa();
		emp.setIdEmpresa(rs.getInt("id_empresa"));

		// Tenta pegar a razão social se ela existir no SELECT (devido ao JOIN)
		try {
			emp.setRazaoSocialEmpresa(rs.getString("razao_social_empresa"));
		} catch (SQLException e) {
			// Se não houver join, o objeto empresa terá apenas o ID
		}

		u.setIdEmpresa(emp);

		u.setTentativasFalhas(rs.getInt("tentativas_falhas"));
		Timestamp ts = rs.getTimestamp("ultimo_login");
		if (ts != null)
			u.setUltimoLogin(ts.toLocalDateTime());

		return u;
	}
}
