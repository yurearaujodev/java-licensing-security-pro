package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.StatusUsuario;

public class UsuarioDao extends GenericDao<Usuario> {

	public UsuarioDao(Connection conn) {
		super(conn, "usuario", "id_usuario");
	}

	public int save(Usuario u) {
		String sql = "INSERT INTO " + tableName
				+ " (nome, email, senha_hash, status, tentativas_falhas, id_empresa, is_master, criado_em, atualizado_em) "
				+ "VALUES (?, ?, ?, ?, 0, ?, ?, NOW(), NOW())";

		Integer idEmpresa = (u.getEmpresa() != null) ? u.getEmpresa().getIdEmpresa() : null;

		int id = executeInsert(sql, u.getNome(), u.getEmail(), u.getSenhaHashString(), u.getStatus(), idEmpresa,
				u.isMaster());

		u.setIdUsuario(id);
		u.setTentativasFalhas(0);
		return id;
	}

	public void update(Usuario u) {
		String sql;
		Object[] params;

		Integer idEmpresa = (u.getEmpresa() != null) ? u.getEmpresa().getIdEmpresa() : null;

		if (u.getSenhaHashString() != null && !u.getSenhaHashString().isEmpty()) {
			sql = "UPDATE " + tableName
					+ " SET nome = ?, email = ?, status = ?, id_empresa = ?, senha_hash = ?, is_master = ?, atualizado_em = NOW() WHERE id_usuario = ?";
			params = new Object[] { u.getNome(), u.getEmail(), u.getStatus(), idEmpresa, u.getSenhaHashString(),
					u.isMaster(), u.getIdUsuario() };
		} else {
			sql = "UPDATE " + tableName
					+ " SET nome = ?, email = ?, status = ?, id_empresa = ?, is_master = ?, atualizado_em = NOW() WHERE id_usuario = ?";
			params = new Object[] { u.getNome(), u.getEmail(), u.getStatus(), idEmpresa, u.isMaster(),
					u.getIdUsuario() };
		}
		executeUpdate(sql, params);
	}

	public List<Usuario> listarPorPermissao(String chavePermissao) {
		String sql = "SELECT u.*, e.razao_social AS razao_social_empresa " + "FROM " + tableName + " u "
				+ "INNER JOIN usuario_permissoes up ON u.id_usuario = up.id_usuario "
				+ "INNER JOIN permissoes p ON up.id_permissoes = p.id_permissoes "
				+ "LEFT JOIN empresa e ON u.id_empresa = e.id_empresa "
				+ "WHERE p.chave = ? AND up.ativa = 1 AND u.deletado_em IS NULL";

		return executeQuery(sql, chavePermissao);
	}

	public Usuario buscarPorEmail(String email) {
		String sql = "SELECT * FROM " + tableName + " WHERE email = ? AND deletado_em IS NULL";
		var lista = executeQuery(sql, email);
		return lista.isEmpty() ? null : lista.get(0);
	}

	public void atualizarUltimaAlteracaoSenha(int idUsuario) {
		String sql = "UPDATE " + tableName + " SET ultima_alteracao_senha = NOW(), atualizado_em = NOW() "
				+ " WHERE id_usuario = ?";
		executeUpdate(sql, idUsuario);
	}

	public Usuario buscarMasterUnico() {
		String sql = "SELECT * FROM " + tableName + " WHERE is_master = 1 AND deletado_em IS NULL LIMIT 1";
		var lista = executeQuery(sql);
		return lista.isEmpty() ? null : lista.get(0);
	}

	public void atualizarUltimoLogin(int idUsuario) {
		String sql = "UPDATE " + tableName + " SET ultimo_login = NOW(), tentativas_falhas = 0 WHERE id_usuario = ?";
		executeUpdate(sql, idUsuario);
	}

	public void bloquearUsuario(int idUsuario) {
		String sql = "UPDATE " + tableName + " SET status = 'BLOQUEADO', atualizado_em = NOW() WHERE id_usuario = ?";
		executeUpdate(sql, idUsuario);
	}

	public List<Usuario> listarExcluidos() {
		String sql = "SELECT u.*, e.razao_social AS razao_social_empresa " + "FROM " + tableName + " u "
				+ "LEFT JOIN empresa e ON u.id_empresa = e.id_empresa " + "WHERE u.deletado_em IS NOT NULL";

		return executeQuery(sql);
	}

	public int incrementarERetornarTentativas(String email) {
		String sqlUpdate = "UPDATE " + tableName + " SET tentativas_falhas = tentativas_falhas + 1 WHERE email = ?";
		executeUpdate(sqlUpdate, email);

		String sqlSelect = "SELECT tentativas_falhas FROM " + tableName + " WHERE email = ?";
		try (var ps = getConnection().prepareStatement(sqlSelect)) {
			ps.setString(1, email);
			try (ResultSet rs = ps.executeQuery()) {
				if (rs.next()) {
					return rs.getInt("tentativas_falhas");
				}
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, "Erro ao recuperar tentativas", e);
		}
		return 0;
	}

	public void resetTentativasFalhas(int idUsuario) {
		String sql = "UPDATE " + tableName + " SET tentativas_falhas = 0 WHERE id_usuario = ?";
		executeUpdate(sql, idUsuario);
	}

	public void restaurar(int id) {
		String sql = "UPDATE " + tableName + " SET deletado_em = NULL, status = 'ATIVO' WHERE " + pkName + " = ?";
		executeUpdate(sql, id);
	}

	@Override
	public void softDeleteById(int id) {
		softDeleteMasterProtected(id);
	}

	private void softDeleteMasterProtected(int idUsuario) {
		Usuario usuario = searchById(idUsuario);

		if (usuario != null && usuario.isMaster()) {
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED,
					"ERRO: O USUÁRIO MASTER NÃO PODE SER EXCLUÍDO DO SISTEMA.", null);
		}

		super.softDeleteById(idUsuario);

		String sqlStatus = "UPDATE " + tableName + " SET status = 'INATIVO' WHERE " + pkName + " = ?";
		executeUpdate(sqlStatus, idUsuario);
	}

	public List<Usuario> listAll() {
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
		u.setStatus(valueOf(StatusUsuario.class, rs.getString("status")));

		Empresa emp = new Empresa();
		emp.setIdEmpresa(rs.getInt("id_empresa"));

		try {
			String razaoSocial = rs.getString("razao_social_empresa");
			if (razaoSocial != null)
				emp.setRazaoSocialEmpresa(razaoSocial);
		} catch (SQLException e) {

		}
		u.setEmpresa(emp);

		u.setTentativasFalhas(rs.getInt("tentativas_falhas"));
		Timestamp ts = rs.getTimestamp("ultimo_login");
		if (ts != null)
			u.setUltimoLogin(ts.toLocalDateTime());
		u.setMaster(rs.getBoolean("is_master"));

		return u;
	}

}
