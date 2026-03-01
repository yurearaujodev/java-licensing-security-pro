package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.UsuarioPermissao;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.MenuChave;

public class UsuarioPermissaoDao extends GenericDao<UsuarioPermissao> {

	public UsuarioPermissaoDao(Connection conn) {
		super(conn, "usuario_permissoes", "id_usuario");
	}

	public List<MenuChave> buscarChavesAtivasPorUsuario(int idUsuario) {
		List<MenuChave> chaves = new ArrayList<>();
		String sql = "SELECT DISTINCT p.chave FROM usuario_permissoes up "
				+ "INNER JOIN permissoes p ON up.id_permissoes = p.id_permissoes "
				+ "WHERE up.id_usuario = ? AND up.ativa = 1 AND up.deletado_em IS NULL "
				+ "AND (up.expira_em IS NULL OR up.expira_em > NOW())";
		try (var stmt = conn.prepareStatement(sql)) {
			stmt.setInt(1, idUsuario);
			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					try {
						String chaveStr = rs.getString("chave");
						if (chaveStr != null)
							chaves.add(MenuChave.valueOf(chaveStr));
					} catch (IllegalArgumentException e) {
						continue;
					}
				}
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, e.getMessage(), e);
		}
		return chaves;
	}

	public List<Permissao> buscarPermissoesAtivasDoUsuario(Integer idUsuario) throws SQLException {
		List<Permissao> lista = new ArrayList<>();
		String sql = "SELECT p.id_permissoes, p.chave, p.tipo, p.categoria FROM usuario_permissoes up "
				+ "INNER JOIN permissoes p ON up.id_permissoes = p.id_permissoes "
				+ "WHERE up.id_usuario = ? AND up.ativa = 1 AND up.deletado_em IS NULL "
				+ "AND (up.expira_em IS NULL OR up.expira_em > NOW())";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, idUsuario);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Permissao p = new Permissao();
					p.setIdPermissoes(rs.getInt("id_permissoes"));
					p.setChave(rs.getString("chave"));
					p.setTipo(rs.getString("tipo"));
					p.setCategoria(rs.getString("categoria"));
					lista.add(p);
				}
			}
		}
		return lista;
	}

	public List<UsuarioPermissao> listarDiretasPorUsuario(int idUsuario) {
		String sql = "SELECT up.*, p.chave, p.tipo, p.categoria " + "FROM " + tableName + " up "
				+ "INNER JOIN permissoes p ON up.id_permissoes = p.id_permissoes "
				+ "WHERE up.id_usuario = ? AND up.herdada = 0 AND up.deletado_em IS NULL "
				+ "AND (up.expira_em IS NULL OR up.expira_em > NOW())";

		return executeQuery(sql, idUsuario);
	}

	public void syncByUsuario(int idUsuario, List<UsuarioPermissao> novos) {
		List<UsuarioPermissao> atuais = listarPorUsuario(idUsuario);

		syncByParentId(novos, atuais, up -> up.getPermissao().getIdPermissoes(), this::saveOrUpdate, this::saveOrUpdate,
				this::softDelete);
	}

	public List<UsuarioPermissao> listarPorUsuario(int idUsuario) {
		String sql = "SELECT up.*, p.chave, p.tipo, p.categoria " + "FROM " + tableName + " up "
				+ "JOIN permissoes p ON up.id_permissoes = p.id_permissoes "
				+ "WHERE up.id_usuario = ? AND up.deletado_em IS NULL "
				+ "AND (up.expira_em IS NULL OR up.expira_em > NOW())";
		return executeQuery(sql, idUsuario);
	}

	public void saveOrUpdate(UsuarioPermissao up) {
		String sql = "INSERT INTO usuario_permissoes (id_usuario, id_permissoes, ativa, expira_em, herdada, criado_em, atualizado_em) "
				+ "VALUES (?, ?, ?, ?, ?, NOW(), NOW()) "
				+ "ON DUPLICATE KEY UPDATE ativa = ?, expira_em = ?, herdada = ?, deletado_em = NULL, atualizado_em = NOW()";

		executeUpdate(sql, up.getUsuario().getIdUsuario(), up.getPermissao().getIdPermissoes(), up.isAtiva(),
				up.getExpiraEm(), up.isHerdada(), up.isAtiva(), up.getExpiraEm(), up.isHerdada());
	}

	public void softDelete(UsuarioPermissao up) {
		String sql = "UPDATE " + tableName
				+ " SET ativa = 0, deletado_em = NOW() WHERE id_usuario = ? AND id_permissoes = ?";
		executeUpdate(sql, up.getUsuario().getIdUsuario(), up.getPermissao().getIdPermissoes());
	}

	public boolean usuarioPossuiAcessoCompleto(int idUsuario, int idPerfil, String chave, String tipo) {
		String sql = "SELECT EXISTS ( " + "  SELECT 1 FROM permissoes p "
				+ "  LEFT JOIN perfil_permissoes pp ON p.id_permissoes = pp.id_permissoes AND pp.id_perfil = ? "
				+ "  LEFT JOIN usuario_permissoes up ON p.id_permissoes = up.id_permissoes AND up.id_usuario = ? "
				+ "  WHERE p.chave = ? AND p.tipo = ? " + "  AND ( "
				+ "    (pp.id_permissoes IS NOT NULL AND pp.deletado_em IS NULL) "
				+ "    OR (up.ativa = 1 AND up.deletado_em IS NULL AND (up.expira_em IS NULL OR up.expira_em > NOW())) "
				+ "  ) " + ")";
		return executeScalarInt(sql, idPerfil, idUsuario, chave, tipo) > 0;
	}

	public void softDeleteGranularesPorUsuario(int idUsuario) {
		String sql = "UPDATE usuario_permissoes SET deletado_em = NOW(), ativa = 0 "
				+ "WHERE id_usuario = ? AND herdada = 0 AND deletado_em IS NULL";
		executeUpdate(sql, idUsuario);
	}

	public boolean usuarioPossuiPermissaoEspecifica(Integer idUsuario, String chave, String tipo) {
		String sql = "SELECT COUNT(*) FROM usuario_permissoes up "
				+ "JOIN permissoes p ON up.id_permissoes = p.id_permissoes "
				+ "WHERE up.id_usuario = ? AND p.chave = ? AND p.tipo = ? "
				+ "AND up.ativa = 1 AND up.deletado_em IS NULL " + "AND (up.expira_em IS NULL OR up.expira_em > NOW())";

		return executeScalarInt(sql, idUsuario, chave, tipo) > 0;
	}

	@Override
	protected UsuarioPermissao mapResultSetToEntity(ResultSet rs) throws SQLException {
		UsuarioPermissao up = new UsuarioPermissao();

		Usuario usuario = new Usuario();
		usuario.setIdUsuario(rs.getInt("id_usuario"));
		up.setUsuario(usuario);

		Permissao permissao = new Permissao();
		permissao.setIdPermissoes(rs.getInt("id_permissoes"));
		permissao.setChave(rs.getString("chave"));
		permissao.setTipo(rs.getString("tipo"));
		permissao.setCategoria(rs.getString("categoria"));
		up.setPermissao(permissao);

		up.setAtiva(rs.getBoolean("ativa"));
		up.setHerdada(rs.getBoolean("herdada"));

		Timestamp ts = rs.getTimestamp("expira_em");
		if (ts != null)
			up.setExpiraEm(ts.toLocalDateTime());

		return up;
	}
}
