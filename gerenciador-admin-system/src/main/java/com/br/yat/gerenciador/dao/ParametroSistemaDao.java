package com.br.yat.gerenciador.dao;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Optional;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.model.ParametroSistema;

public class ParametroSistemaDao extends GenericDao<ParametroSistema> {

	private static final Logger logger = LoggerFactory.getLogger(ParametroSistemaDao.class);

	public ParametroSistemaDao(Connection conn) {
		super(conn, "parametro_sistema", "id_parametro");
	}

	@Override
	protected ParametroSistema mapResultSetToEntity(ResultSet rs) throws SQLException {
		ParametroSistema p = new ParametroSistema();
		p.setIdParametro(rs.getInt("id_parametro"));
		p.setChave(rs.getString("chave"));
		p.setValor(rs.getString("valor"));
		p.setDescricao(rs.getString("descricao"));
		p.setCriadoEm(rs.getTimestamp("criado_em").toLocalDateTime());
		p.setAtualizadoEm(rs.getTimestamp("atualizado_em").toLocalDateTime());
		return p;
	}

	public Optional<ParametroSistema> getParametro(String chave) {
		String sql = "SELECT * FROM parametro_sistema WHERE chave = ? AND deletado_em IS NULL";
		return Optional.ofNullable(executeQuerySingle(sql, rs -> {
			try {
				return mapResultSetToEntity(rs);
			} catch (SQLException e) {
				logger.error("Erro ao mapear ResultSet para ParametroSistema", e);
				return null;
			}
		}, chave));
	}

	public ParametroSistema salvarOuAtualizar(ParametroSistema p) {
		Optional<ParametroSistema> existenteOpt = getParametro(p.getChave());

		if (existenteOpt.isEmpty()) {
			String sql = "INSERT INTO parametro_sistema (chave, valor, descricao, criado_em, atualizado_em) VALUES (?, ?, ?, NOW(), NOW())";
			int idGerado = executeInsert(sql, p.getChave(), p.getValor(), p.getDescricao());
			p.setIdParametro(idGerado);
			logger.info("ParametroSistema inserido com chave: {}", p.getChave());
		} else {
			ParametroSistema existente = existenteOpt.get();
			String sql = "UPDATE parametro_sistema SET valor = ?, descricao = ?, atualizado_em = NOW() WHERE id_parametro = ?";
			executeUpdate(sql, p.getValor(), p.getDescricao(), existente.getIdParametro());
			p.setIdParametro(existente.getIdParametro());
			logger.info("ParametroSistema atualizado com chave: {}", p.getChave());
		}

		return p;
	}
}