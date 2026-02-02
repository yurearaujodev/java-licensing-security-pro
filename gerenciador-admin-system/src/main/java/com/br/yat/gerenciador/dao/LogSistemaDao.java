package com.br.yat.gerenciador.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import com.br.yat.gerenciador.model.LogSistema;
import com.br.yat.gerenciador.model.Usuario;

public class LogSistemaDao extends GenericDao<LogSistema> {

	public LogSistemaDao(Connection conn) {
		super(conn, "log_sistema", "id_log");
	}

	public int save(LogSistema log) {
		String sql = "INSERT INTO " + tableName 
				+ " (tipo, acao, entidade, id_entidade, dados_anteriores, dados_novos, "
				+ "sucesso, mensagem_erro, ip_origem, data_hora, id_usuario) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		// Seguindo seu padrão de extrair IDs de objetos complexos
		Integer idUsuario = (log.getUsuario() != null) ? log.getUsuario().getIdUsuario() : null;

		// O bindParameters da sua GenericDao já trata LocalDateTime automaticamente
		int id = executeInsert(sql, 
				log.getTipo(), 
				log.getAcao(), 
				log.getEntidade(), 
				log.getIdEntidade(),
				log.getDadosAnteriores(), 
				log.getDadosNovos(), 
				log.isSucesso(), 
				log.getMensagemErro(),
				log.getIpOrigem(), 
				log.getDataHora(), 
				idUsuario);

		log.setIdLog(id);
		return id;
	}

	public List<LogSistema> listarComFiltros(String tipo, String acao, String usuarioNome) {
		StringBuilder sql = new StringBuilder(
				"SELECT l.*, u.nome AS nome_usuario " + 
				"FROM " + tableName + " l " +
				"LEFT JOIN usuario u ON l.id_usuario = u.id_usuario WHERE 1=1 ");

		List<Object> params = new ArrayList<>();

		if (tipo != null && !tipo.isEmpty()) {
			sql.append(" AND l.tipo = ? ");
			params.add(tipo);
		}
		if (acao != null && !acao.isEmpty()) {
			sql.append(" AND l.acao LIKE ? ");
			params.add("%" + acao + "%");
		}
		if (usuarioNome != null && !usuarioNome.isEmpty()) {
			sql.append(" AND u.nome LIKE ? ");
			params.add("%" + usuarioNome + "%");
		}

		sql.append(" ORDER BY l.data_hora DESC LIMIT 500");

		return executeQuery(sql.toString(), params.toArray());
	}

	@Override
	protected LogSistema mapResultSetToEntity(ResultSet rs) throws SQLException {
		LogSistema log = new LogSistema();
		log.setIdLog(rs.getInt(pkName));
		log.setTipo(rs.getString("tipo"));
		log.setAcao(rs.getString("acao"));
		log.setEntidade(rs.getString("entidade"));
		log.setIdEntidade(rs.getInt("id_entidade"));
		log.setDadosAnteriores(rs.getString("dados_anteriores"));
		log.setDadosNovos(rs.getString("dados_novos"));
		log.setSucesso(rs.getBoolean("sucesso"));
		log.setMensagemErro(rs.getString("mensagem_erro"));
		log.setIpOrigem(rs.getString("ip_origem"));
		
		// Tratamento de LocalDateTime seguindo seu padrão da UsuarioDao
		Timestamp ts = rs.getTimestamp("data_hora");
		if (ts != null) {
			log.setDataHora(ts.toLocalDateTime());
		}

		// Mapeamento do usuário (id_usuario) seguindo a lógica do id_empresa na sua UsuarioDao
		Usuario u = new Usuario();
		u.setIdUsuario(rs.getInt("id_usuario"));
		
		try {
			// Tenta pegar o nome se o JOIN foi realizado
			u.setNome(rs.getString("nome_usuario"));
		} catch (SQLException e) {
			// Sem join, o usuário terá apenas o ID
		}
		
		log.setUsuario(u);

		return log;
	}
}
