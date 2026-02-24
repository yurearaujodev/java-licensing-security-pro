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
		String sql = "INSERT INTO " + tableName + " (tipo, acao, entidade, id_entidade, dados_anteriores, dados_novos, "
				+ "sucesso, mensagem_erro, ip_origem, data_hora, id_usuario) "
				+ "VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?)";

		Integer idUsuario = (log.getUsuario() != null) ? log.getUsuario().getIdUsuario() : null;

		int id = executeInsert(sql, log.getTipo(), log.getAcao(), log.getEntidade(), log.getIdEntidade(),
				log.getDadosAnteriores(), log.getDadosNovos(), log.isSucesso(), log.getMensagemErro(),
				log.getIpOrigem(), log.getDataHora(), idUsuario);

		log.setIdLog(id);
		return id;
	}

	public int contarLogsBloqueioRecentes(int idUsuario, int horas) {
		String sql = "SELECT COUNT(*) FROM " + tableName + " WHERE entidade = 'usuario' AND id_entidade = ? "
				+ " AND acao = 'BLOQUEIO_TEMPORARIO' " + " AND data_hora > DATE_SUB(NOW(), INTERVAL ? HOUR)";

		return executeScalarInt(sql, idUsuario, horas);
	}

	public List<LogSistema> listarComFiltros(String tipo, String acao, String usuarioNome) {
		StringBuilder sql = new StringBuilder("SELECT l.*, IFNULL(u.nome, 'SISTEMA') AS nome_usuario " + "FROM "
				+ tableName + " l " + "LEFT JOIN usuario u ON l.id_usuario = u.id_usuario WHERE 1=1 ");
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

	public int limparLogsAntigos(int diasRetencao) {
		String sql = "DELETE FROM " + tableName + " WHERE data_hora < DATE_SUB(NOW(), INTERVAL ? DAY)";

		try {
			return executeUpdate(sql, diasRetencao);
		} catch (Exception e) {
			System.err.println("Erro ao realizar manutenção de logs: " + e.getMessage());
			return 0;
		}
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

		Timestamp ts = rs.getTimestamp("data_hora");
		if (ts != null) {
			log.setDataHora(ts.toLocalDateTime());
		}

		Usuario u = new Usuario();
		u.setIdUsuario(rs.getInt("id_usuario"));

		try {
			u.setNome(rs.getString("nome_usuario"));
		} catch (SQLException e) {
		}

		log.setUsuario(u);

		return log;
	}
}
