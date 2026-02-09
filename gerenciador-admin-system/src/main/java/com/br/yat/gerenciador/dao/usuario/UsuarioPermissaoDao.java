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
				+ "AND (up.expira_em IS NULL OR up.expira_em > NOW())"; // ADICIONE ESTA LINHA
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
		// Ajuste o SELECT para trazer os campos necessários
		String sql = "SELECT p.id_permissoes, p.chave, p.tipo, p.categoria FROM usuario_permissoes up "
				+ "INNER JOIN permissoes p ON up.id_permissoes = p.id_permissoes "
				+ "WHERE up.id_usuario = ? AND up.ativa = 1 AND up.deletado_em IS NULL "
				+ "AND (up.expira_em IS NULL OR up.expira_em > NOW())";

		try (PreparedStatement ps = conn.prepareStatement(sql)) {
			ps.setInt(1, idUsuario);
			try (ResultSet rs = ps.executeQuery()) {
				while (rs.next()) {
					Permissao p = new Permissao();
					p.setIdPermissoes(rs.getInt("id_permissoes")); // Agora o campo existe no SELECT
					p.setChave(rs.getString("chave"));
					p.setTipo(rs.getString("tipo"));
					p.setCategoria(rs.getString("categoria"));
					lista.add(p);
				}
			}
		}
		return lista;
	}

	public void syncByUsuario(int idUsuario, List<UsuarioPermissao> novos) {
		// Busca os atuais para o diff da GenericDao
		List<UsuarioPermissao> atuais = listarPorUsuario(idUsuario);

		// O syncByParentId vai usar o id da permissão como chave de comparação
		syncByParentId(novos, atuais, up -> up.getIdPermissoes(), this::saveOrUpdate, this::saveOrUpdate,
				this::softDelete);
	}

	public List<UsuarioPermissao> listarPorUsuario(int idUsuario) {
		String sql = "SELECT * FROM " + tableName + " WHERE id_usuario = ? AND deletado_em IS NULL";
		return executeQuery(sql, idUsuario);
	}

	public void saveOrUpdate(UsuarioPermissao up) {
		String sql = "INSERT INTO usuario_permissoes (id_usuario, id_permissoes, ativa, expira_em, herdada, criado_em, atualizado_em) "
				+ "VALUES (?, ?, ?, ?, ?, NOW(), NOW()) "
				+ "ON DUPLICATE KEY UPDATE ativa = ?, expira_em = ?, herdada = ?, deletado_em = NULL, atualizado_em = NOW()";

		// Centralizando a lógica de conversão de boolean para int no bind da GenericDao
		// (ou manual aqui)
		executeUpdate(sql, up.getIdUsuario(), up.getIdPermissoes(), up.isAtiva(), up.getExpiraEm(), up.isHerdada(),
				up.isAtiva(), up.getExpiraEm(), up.isHerdada());
	}

	public void softDelete(UsuarioPermissao up) {
		String sql = "UPDATE " + tableName
				+ " SET ativa = 0, deletado_em = NOW() WHERE id_usuario = ? AND id_permissoes = ?";
		executeUpdate(sql, up.getIdUsuario(), up.getIdPermissoes());
	}

	public boolean usuarioPossuiAcessoCompleto(int idUsuario, int idPerfil, String chave, String tipo) {
		String sql = "SELECT COUNT(1) FROM (" + "  SELECT p.id_permissoes FROM permissoes p "
				+ "  INNER JOIN perfil_permissoes pp ON p.id_permissoes = pp.id_permissoes "
				+ "  WHERE pp.id_perfil = ? AND p.chave = ? AND p.tipo = ? "
				+ "  AND pp.deletado_em IS NULL AND p.deletado_em IS NULL " + "  UNION "
				+ "  SELECT p.id_permissoes FROM permissoes p "
				+ "  INNER JOIN usuario_permissoes up ON p.id_permissoes = up.id_permissoes "
				+ "  WHERE up.id_usuario = ? AND p.chave = ? AND p.tipo = ? "
				+ "  AND up.ativa = 1 AND up.deletado_em IS NULL AND p.deletado_em IS NULL "
				+ "  AND (up.expira_em IS NULL OR up.expira_em > NOW())" + // TRAVA DE DATA AQUI
				") as acesso_total";

		return executeScalarInt(sql, idPerfil, chave, tipo, idUsuario, chave, tipo) > 0;
	}

	public void softDeleteGranularesPorUsuario(int idUsuario) {
		// Ajustado para 'usuario_permissoes'
		String sql = "UPDATE usuario_permissoes SET deletado_em = NOW(), ativa = 0 "
				+ "WHERE id_usuario = ? AND herdada = 0 AND deletado_em IS NULL";
		executeUpdate(sql, idUsuario);
	}

	public boolean usuarioPossuiPermissaoEspecifica(Integer idUsuario, String chave, String tipo) {
		String sql = "SELECT COUNT(*) FROM usuario_permissoes up "
				+ "JOIN permissoes p ON up.id_permissoes = p.id_permissoes "
				+ "WHERE up.id_usuario = ? AND p.chave = ? AND p.tipo = ? "
				+ "AND up.ativa = 1 AND up.deletado_em IS NULL " + "AND (up.expira_em IS NULL OR up.expira_em > NOW())"; // VALIDAÇÃO
																															// DE
																															// EXPIRAÇÃO

		return executeScalarInt(sql, idUsuario, chave, tipo) > 0;
	}

	@Override
	protected UsuarioPermissao mapResultSetToEntity(ResultSet rs) throws SQLException {
		UsuarioPermissao up = new UsuarioPermissao();
		up.setIdUsuario(rs.getInt("id_usuario"));
		up.setIdPermissoes(rs.getInt("id_permissoes"));
		up.setAtiva(rs.getBoolean("ativa"));
		up.setHerdada(rs.getBoolean("herdada"));

		Timestamp ts = rs.getTimestamp("expira_em");
		if (ts != null)
			up.setExpiraEm(ts.toLocalDateTime());

		return up;
	}
}
