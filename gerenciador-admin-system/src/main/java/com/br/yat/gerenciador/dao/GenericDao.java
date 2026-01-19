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

	protected int executeUpdate(String sql, Object... params) {
		try (PreparedStatement stmt = conn.prepareStatement(sql, Statement.RETURN_GENERATED_KEYS)) {
			for (int i = 0; i < params.length; i++) {
				setSafeParameter(stmt, i + 1, params[i], Types.NULL);
			}
			stmt.executeUpdate();
			try (ResultSet rs = stmt.getGeneratedKeys()) {
				return rs.next() ? rs.getInt(1) : 0;
			}
		} catch (SQLException e) {
			throw new RuntimeException("ERRO EM [" + tableName + "]: " + e.getMessage());
		}
	}

	protected List<T> executeQuery(String sql, Object... params) {
		List<T> lista = new ArrayList<>();
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {
			for (int i = 0; i < params.length; i++) {
				setSafeParameter(stmt, i + 1, params[i], Types.NULL);
			}
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
	
	protected List<T> listByForeignKey(String column,int value){
		String sql = "SELECT * FROM "+tableName+"WHERE "+column+" = ?";
		return executeQuery(sql, value);
	}

	protected void setSafeParameter(PreparedStatement stmt, int index, Object value, int sqlType) throws SQLException {
		switch (value) {
		case null -> stmt.setNull(index, sqlType == Types.NULL ? Types.VARCHAR : sqlType);
		case String s when s.isBlank() -> stmt.setNull(index, Types.VARCHAR);
		case String s -> stmt.setString(index, s);
		case Integer i -> stmt.setInt(index, i);
		case BigDecimal db -> stmt.setBigDecimal(index, db);
		case LocalDate ld -> stmt.setDate(index, Date.valueOf(ld));
		case Enum<?> e -> stmt.setString(index, e.name());
		case Boolean b -> stmt.setBoolean(index, b);

		default -> stmt.setObject(index, value, sqlType);
		}
	}
}
