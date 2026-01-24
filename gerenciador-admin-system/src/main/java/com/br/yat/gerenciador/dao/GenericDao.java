package com.br.yat.gerenciador.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public abstract class GenericDao<T> {
	protected Connection conn;
	protected String tableName;
	protected String pkName;

	public GenericDao(Connection conn, String tableName, String pkName) throws SQLException {
		if (conn == null || conn.isClosed()) {
			throw new IllegalArgumentException("Conexão inválida!");
		}
		this.conn = conn;
		this.tableName = tableName;
		this.pkName = pkName;
	}

	protected abstract T mapResultSetToEntity(ResultSet rs) throws SQLException;

	protected int executeInsert(String sql, Object... params) {
		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {

			bindParameters(stmt, params);
			stmt.executeUpdate();

			try (ResultSet rs = stmt.getGeneratedKeys()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("ERRO EM [" + tableName + "]: " + e.getMessage());
		}
	}

	protected int executeUpdate(String sql, Object... params) {
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {

			bindParameters(stmt, params);
			return stmt.executeUpdate();

		} catch (SQLException e) {
			throw new RuntimeException("ERRO EM [" + tableName + "]: " + e.getMessage());
		}
	}

	protected List<T> executeQuery(String sql, Object... params) {
		List<T> lista = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {

			bindParameters(stmt, params);

			try (ResultSet rs = stmt.executeQuery()) {
				while (rs.next()) {
					lista.add(mapResultSetToEntity(rs));
				}
			}
		} catch (SQLException e) {
			throw new RuntimeException("ERRO AO CONSULTAR [" + tableName + "]: " + e.getMessage());
		}
		return lista;
	}

	public T searchById(int id) {
		String sql = "SELECT * FROM " + tableName + " WHERE " + pkName + " = ?";
		List<T> resultados = executeQuery(sql, id);
		return resultados.isEmpty() ? null : resultados.get(0);
	}

	protected void bindParameters(PreparedStatement stmt, Object... params) throws SQLException {
		for (int i = 0; i < params.length; i++) {
			Object value = params[i];
			int idx = i + 1;

			switch (value) {
			case null -> stmt.setNull(idx, Types.NULL);
			case String s when s.isBlank() -> stmt.setNull(idx, Types.VARCHAR);
			case String s -> stmt.setString(idx, s);
			case Integer iVal -> stmt.setInt(idx, iVal);
			case BigDecimal db -> stmt.setBigDecimal(idx, db);
			case LocalDate ld -> stmt.setDate(idx, Date.valueOf(ld));
			case Enum<?> e -> stmt.setString(idx, e.name());
			case Boolean b -> stmt.setBoolean(idx, b);

			default -> stmt.setObject(idx, value);
			}
		}
	}
}
