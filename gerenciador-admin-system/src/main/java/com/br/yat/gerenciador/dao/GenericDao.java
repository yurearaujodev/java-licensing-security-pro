package com.br.yat.gerenciador.dao;

import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.sql.Types;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.util.ValidationUtils;

public abstract class GenericDao<T> {
	protected Connection conn;
	protected String tableName;
	protected String pkName;

	public GenericDao(Connection conn, String tableName, String pkName) {
		try {
			if (conn == null || conn.isClosed()) {
				throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "CONEXÃO INVÁLIDA OU FECHADA.",
						null);
			}
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.CONNECTION_ERROR, "FALHA AO VERIFICAR STATUS DA CONEXÃO",
					e);
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
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED,
					"ERRO AO INSERIR EM [" + tableName + "]: " + e.getMessage(), e);
		}
	}

	protected int executeUpdate(String sql, Object... params) {
		try (PreparedStatement stmt = conn.prepareStatement(sql)) {

			bindParameters(stmt, params);
			return stmt.executeUpdate();

		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED,
					"ERRO AO ATUALIZAR EM [" + tableName + "]: " + e.getMessage(), e);
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
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED,
					"ERRO AO CONSULTAR [" + tableName + "]: " + e.getMessage(), e);
		}
		return lista;
	}
	
	protected <R> R executeQuerySingle(String sql, Function<ResultSet, R> mapper, Object... params) {
	    try (PreparedStatement stmt = conn.prepareStatement(sql)) {
	        bindParameters(stmt, params);
	        try (ResultSet rs = stmt.executeQuery()) {
	            if (rs.next()) {
	                return mapper.apply(rs);
	            }
	        }
	    } catch (SQLException e) {
	        throw new DataAccessException(DataAccessErrorType.QUERY_FAILED,
	                "ERRO AO EXECUTAR QUERY SINGLE EM [" + tableName + "]: " + e.getMessage(), e);
	    }
	    return null;
	}

	public T searchById(int id) {
		String sql = "SELECT * FROM " + tableName + " WHERE " + pkName + " = ? AND deletado_em IS NULL";
		List<T> resultados = executeQuery(sql, id);
		return resultados.isEmpty() ? null : resultados.get(0);
	}
	
	public void softDeleteById(int id) {
		String sql = "UPDATE "+tableName+" SET deletado_em = NOW() WHERE "+pkName+" =? AND deletado_em IS NULL";
		executeUpdate(sql, id);
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
			case LocalDateTime ldt -> stmt.setTimestamp(idx, Timestamp.valueOf(ldt));
			case Enum<?> e -> stmt.setString(idx, e.name());
			case Boolean b -> stmt.setInt(idx, b ? 1 : 0);
			case Long l -> stmt.setLong(idx, l);
			case Double d -> stmt.setDouble(idx, d);
			
			default -> stmt.setObject(idx, value);
			}
		}
	}

	protected <E extends Enum<E>> E valueOf(Class<E> enumClass, String value) {
		if (ValidationUtils.isEmpty(value))
			return null;
		try {
			return Enum.valueOf(enumClass, value);
		} catch (IllegalArgumentException e) {
			return null;
		}
	}

	protected <E> void syncByParentId(List<E> novos, List<E> atuais, Function<E, Integer> getId, Consumer<E> inserir,
			Consumer<E> alterar, Consumer<E> softDelete) {
		var mapaAtuais = atuais.stream().collect(Collectors.toMap(getId, e -> e));

		for (E novo : novos) {
			Integer id = getId.apply(novo);

			if (id == null || id == 0) {
				inserir.accept(novo);
			} else {
				alterar.accept(novo);
				mapaAtuais.remove(id);
			}
		}
		mapaAtuais.values().forEach(softDelete);
	}
	
	public Connection getConnection() {
	    return this.conn;
	}

}
