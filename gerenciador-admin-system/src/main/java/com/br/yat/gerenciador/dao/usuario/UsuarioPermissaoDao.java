package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;

public class UsuarioPermissaoDao extends GenericDao<UsuarioPermissao> {

	public UsuarioPermissaoDao(Connection conn) {
		super(conn, "usuario_permissoes", "id_usuario");
	}

	public List<MenuChave> buscarChavesAtivasPorUsuario(int idUsuario) {
		List<MenuChave> chaves = new ArrayList<>();
		String sql = "SELECT p.chave FROM usuario_permissoes up "
				+ "INNER JOIN permissoes p ON up.id_permissoes = p.id_permissoes "
				+ "WHERE up.id_usuario = ? AND up.ativa = 1 AND up.deletado_em IS NULL";

		try (var stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idUsuario);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					try {
						chaves.add(MenuChave.valueOf(rs.getString("chave")));
					} catch (Exception e) {
						continue;
					}
				}
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, e.getMessage(), e);
		}
		return chaves;
	}

	public void syncByUsuario(int idUsuario, List<UsuarioPermissao> novos) {
		List<UsuarioPermissao> atuais = listarPorUsuario(idUsuario);

		syncByParentId(novos, atuais, up -> up.getIdPermissoes(), this::saveOrUpdate, this::saveOrUpdate,
				this::softDelete);
	}

	public List<UsuarioPermissao> listarPorUsuario(int idUsuario) {
		String sql = "SELECT * FROM " + tableName + " WHERE id_usuario = ? AND deletado_em IS NULL";
		return executeQuery(sql, idUsuario);
	}

	public void saveOrUpdate(UsuarioPermissao up) {
		String sql = "INSERT INTO usuario_permissoes (id_usuario, id_permissoes, ativa, id_usuario_concedeu, criado_em, atualizado_em) "
				+ "VALUES (?, ?, ?, ?, NOW(), NOW()) "
				+ "ON DUPLICATE KEY UPDATE ativa = ?, deletado_em = NULL, atualizado_em = NOW(), id_usuario_concedeu = ?";

		int idConcedeu = up.getUsuarioConcedeu().getIdUsuario();
		int ativaInt = up.isAtiva() ? 1 : 0;

		executeUpdate(sql, up.getIdUsuario(), up.getIdPermissoes(), ativaInt, idConcedeu, ativaInt, idConcedeu);
	}

	public void softDelete(UsuarioPermissao up) {
		String sql = "UPDATE usuario_permissoes SET ativa = 0, deletado_em = NOW() WHERE id_usuario = ? AND id_permissoes = ?";
		executeUpdate(sql, up.getIdUsuario(), up.getIdPermissoes());
	}

	public void bloquearTemporariamente(int idUsuario, int minutos) {
		String sql = "UPDATE usuario_permissoes " + "SET expira_em = DATE_ADD(NOW(), INTERVAL ? MINUTE) "
				+ "WHERE id_usuario = ? AND deletado_em IS NULL";

		executeUpdate(sql, minutos, idUsuario);
	}

	public boolean estaBloqueado(int idUsuario) {
		String sql = "SELECT 1 FROM usuario_permissoes "
				+ "WHERE id_usuario = ? AND expira_em IS NOT NULL AND expira_em > NOW() LIMIT 1";
		try (var stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idUsuario);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, e.getMessage(), e);
		}
	}

	public void removerBloqueioTemporario(int idUsuario) {
		String sql = "UPDATE usuario_permissoes SET expira_em = NULL WHERE id_usuario = ?";
		executeUpdate(sql, idUsuario);
	}

	public boolean jaTeveBloqueioTemporario(int idUsuario) {
		String sql = "SELECT 1 FROM usuario_permissoes " + "WHERE id_usuario = ? AND expira_em IS NOT NULL";
		try (var stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idUsuario);
			try (ResultSet rs = stmt.executeQuery()) {
				return rs.next();
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, e.getMessage(), e);
		}
	}

	@Override
	protected UsuarioPermissao mapResultSetToEntity(ResultSet rs) throws SQLException {
		UsuarioPermissao up = new UsuarioPermissao();
		up.setIdUsuario(rs.getInt("id_usuario"));
		up.setIdPermissoes(rs.getInt("id_permissoes"));
		up.setAtiva(rs.getBoolean("ativa"));
		return up;
	}
}
