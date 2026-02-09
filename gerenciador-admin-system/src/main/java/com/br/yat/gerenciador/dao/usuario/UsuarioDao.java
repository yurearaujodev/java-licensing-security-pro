package com.br.yat.gerenciador.dao.usuario;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.List;

import com.br.yat.gerenciador.dao.GenericDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.StatusUsuario;

public class UsuarioDao extends GenericDao<Usuario> {

	public UsuarioDao(Connection conn) {
		super(conn, "usuario", "id_usuario");
	}

	public int save(Usuario u) {
		String sql = "INSERT INTO " + tableName
				+ " (nome, email, senha_hash, status, tentativas_falhas, id_empresa, id_perfil, is_master, "
				+ "senha_expira_em, bloqueado_ate, forcar_reset_senha, criado_em, atualizado_em) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?, NOW(), NOW())";

		Integer idEmpresa = (u.getEmpresa() != null) ? u.getEmpresa().getIdEmpresa() : null;
		Integer idPerfil = (u.getPerfil() != null) ? u.getPerfil().getIdPerfil() : null;

		int id = executeInsert(sql, 
				u.getNome(), 
				u.getEmail(), 
				u.getSenhaHashString(), 
				u.getStatus().name(), 
				u.getTentativasFalhas(),
				idEmpresa,
				idPerfil,
				u.isMaster() ? 1 : 0,
				u.getSenhaExpiraEm(),
				u.getBloqueadoAte(),
				u.isForcarResetSenha() ? 1 : 0);

		u.setIdUsuario(id);
		return id;
	}

	public void update(Usuario u) {
		String sql;
		Object[] params;

		Integer idEmpresa = (u.getEmpresa() != null) ? u.getEmpresa().getIdEmpresa() : null;
		Integer idPerfil = (u.getPerfil() != null) ? u.getPerfil().getIdPerfil() : null;

		String baseSql = "UPDATE " + tableName + " SET nome = ?, email = ?, status = ?, id_empresa = ?, id_perfil = ?, "
				+ "is_master = ?, senha_expira_em = ?, bloqueado_ate = ?, forcar_reset_senha = ?, atualizado_em = NOW()";

		if (u.getSenhaHashString() != null && !u.getSenhaHashString().isEmpty()) {
			sql = baseSql + ", senha_hash = ? WHERE id_usuario = ?";
			params = new Object[] { u.getNome(), u.getEmail(), u.getStatus(), idEmpresa, idPerfil,
					u.isMaster() ? 1 : 0, u.getSenhaExpiraEm(), u.getBloqueadoAte(), u.isForcarResetSenha() ? 1 : 0,
					u.getSenhaHashString(), u.getIdUsuario() };
		} else {
			sql = baseSql + " WHERE id_usuario = ?";
			params = new Object[] { u.getNome(), u.getEmail(), u.getStatus(), idEmpresa, idPerfil,
					u.isMaster() ? 1 : 0, u.getSenhaExpiraEm(), u.getBloqueadoAte(), u.isForcarResetSenha() ? 1 : 0,
					u.getIdUsuario() };
		}
		executeUpdate(sql, params);
	}
	public List<Usuario> listarPorPermissao(String chavePermissao) {
		String sql = "SELECT u.*, e.razao_social AS razao_social_empresa, p.nome AS nome_perfil " + "FROM " + tableName + " u "
				+ "INNER JOIN usuario_permissoes up ON u.id_usuario = up.id_usuario "
				+ "INNER JOIN permissoes perm ON up.id_permissoes = perm.id_permissoes "
				+ "LEFT JOIN empresa e ON u.id_empresa = e.id_empresa "
				+ "LEFT JOIN perfil p ON u.id_perfil = p.id_perfil "
				+ "WHERE perm.chave = ? AND up.ativa = 1 AND u.deletado_em IS NULL";

		return executeQuery(sql, chavePermissao);
	}

	public Usuario buscarPorEmail(String email) {
		String sql = "SELECT u.*, e.razao_social AS razao_social_empresa, p.nome AS nome_perfil " 
				+ "FROM " + tableName + " u "
				+ "LEFT JOIN empresa e ON u.id_empresa = e.id_empresa "
				+ "LEFT JOIN perfil p ON u.id_perfil = p.id_perfil "
				+ "WHERE u.email = ? AND u.deletado_em IS NULL";
		var lista = executeQuery(sql, email);
		return lista.isEmpty() ? null : lista.get(0);
	}

	//e pra ser removido banco mudou
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
		String sql = "UPDATE " + tableName + " SET ultimo_login = NOW(), tentativas_falhas = 0, bloqueado_ate = NULL WHERE id_usuario = ?";
		executeUpdate(sql, idUsuario);
	}

	public void bloquearUsuario(int idUsuario) {
		String sql = "UPDATE " + tableName + " SET status = 'BLOQUEADO', atualizado_em = NOW() WHERE id_usuario = ?";
		executeUpdate(sql, idUsuario);
	}

	public List<Usuario> listarExcluidos() {
		String sql = "SELECT u.*, e.razao_social AS razao_social_empresa, p.nome AS nome_perfil " + "FROM " + tableName + " u "
				+ "LEFT JOIN empresa e ON u.id_empresa = e.id_empresa " 
				+ "LEFT JOIN perfil p ON u.id_perfil = p.id_perfil "
				+ "WHERE u.deletado_em IS NOT NULL";

		return executeQuery(sql);
	}

	public int incrementarERetornarTentativas(String email) {
	    // 1. Incrementa no banco
	    String sqlUpdate = "UPDATE " + tableName + " SET tentativas_falhas = COALESCE(tentativas_falhas, 0) + 1 WHERE email = ?";
	    executeUpdate(sqlUpdate, email);

	    // 2. Busca o novo valor usando o método da GenericDao
	    String sqlSelect = "SELECT tentativas_falhas FROM " + tableName + " WHERE email = ?";
	    
	    // USANDO O MÉTODO QUE JÁ EXISTE NA SUA GENERICDAO
	    return executeScalarInt(sqlSelect, email);
	}

	public void resetTentativasFalhas(int idUsuario) {
		String sql = "UPDATE " + tableName + " SET tentativas_falhas = 0, bloqueado_ate = NULL WHERE id_usuario = ?";
		executeUpdate(sql, idUsuario);
	}

	public void restaurar(int id) {
		String sql = "UPDATE " + tableName + " SET deletado_em = NULL, status = 'ATIVO' WHERE " + pkName + " = ?";
		executeUpdate(sql, id);
	}
	
	public void bloquearTemporariamente(int idUsuario, LocalDateTime ate) {
		String sql = "UPDATE " + tableName + " SET bloqueado_ate = ?, atualizado_em = NOW() WHERE id_usuario = ?";
		executeUpdate(sql, ate, idUsuario);
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
	
	@Override
	public Usuario searchById(int id) {
        String sql = "SELECT u.*, e.razao_social AS razao_social_empresa, p.nome AS nome_perfil " 
                + "FROM " + tableName + " u "
                + "LEFT JOIN empresa e ON u.id_empresa = e.id_empresa "
                + "LEFT JOIN perfil p ON u.id_perfil = p.id_perfil "
                + "WHERE u." + pkName + " = ? AND u.deletado_em IS NULL";
        List<Usuario> resultados = executeQuery(sql, id);
        return resultados.isEmpty() ? null : resultados.get(0);
    }

	public List<Usuario> listAll() {
		String sql = "SELECT u.*, e.razao_social AS razao_social_empresa, p.nome AS nome_perfil " + "FROM " + tableName + " u "
				+ "LEFT JOIN empresa e ON u.id_empresa = e.id_empresa " 
				+ "LEFT JOIN perfil p ON u.id_perfil = p.id_perfil "
				+ "WHERE u.deletado_em IS NULL";
		return executeQuery(sql);
	}
	public List<Usuario> listarPorNomeOuEmail(String termo) {
		String sql = "SELECT u.*, e.razao_social AS razao_social_empresa, p.nome AS nome_perfil " + "FROM " + tableName + " u "
				+ "LEFT JOIN empresa e ON u.id_empresa = e.id_empresa " 
				+ "LEFT JOIN perfil p ON u.id_perfil = p.id_perfil "
				+ "WHERE (u.nome LIKE ? OR u.email LIKE ?) "
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
		u.setTentativasFalhas(rs.getInt("tentativas_falhas"));
		u.setMaster(rs.getBoolean("is_master"));
		u.setForcarResetSenha(rs.getBoolean("forcar_reset_senha"));

		// Datas de segurança (LocalDateTime via Timestamp)
        u.setUltimoLogin(getLocalDateTime(rs, "ultimo_login"));
        u.setSenhaExpiraEm(getLocalDateTime(rs, "senha_expira_em"));
        u.setBloqueadoAte(getLocalDateTime(rs, "bloqueado_ate"));
        
		// Empresa (Tratando o ID nulo corretamente)
        int idEmp = rs.getInt("id_empresa");
        if (!rs.wasNull()) {
            Empresa emp = new Empresa();
            emp.setIdEmpresa(idEmp);
            try { emp.setRazaoSocialEmpresa(rs.getString("razao_social_empresa")); } catch (Exception e) {}
            u.setEmpresa(emp);
        }

        // Perfil (Tratando o ID nulo corretamente)
        int idPerf = rs.getInt("id_perfil");
        if (!rs.wasNull()) {
            Perfil perf = new Perfil();
            perf.setIdPerfil(idPerf);
            try { perf.setNome(rs.getString("nome_perfil")); } catch (Exception e) {}
            u.setPerfil(perf);
        }

		return u;
	}
	
	// Helper para evitar repetição de código de data
    private java.time.LocalDateTime getLocalDateTime(ResultSet rs, String column) throws SQLException {
        Timestamp ts = rs.getTimestamp(column);
        return (ts != null) ? ts.toLocalDateTime() : null;
    }
}
