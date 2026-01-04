package com.br.yat.gerenciador.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public abstract class GenericDao<T> {
	protected Connection conn;

	public GenericDao(Connection conn) throws SQLException{
		if (conn==null||conn.isClosed()) {
			throw new IllegalArgumentException("Conexão inválida!");
		}
		this.conn = conn;
	}

	public abstract void save(T entidade) throws SQLException;

	public abstract void update(T entidade) throws SQLException;

	public abstract void delete(T entidade) throws SQLException;

	public abstract T searchById(int id) throws SQLException;

	public abstract List<T> listAll() throws SQLException;

}
