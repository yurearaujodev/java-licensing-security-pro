package com.br.yat.gerenciador.model.enums;

public enum DataAccessErrorType {
	CONNECTION_ERROR("DATA-001", "ERRO AO CONECTAR COM O BANCO DE DADOS", true),
	QUERY_FAILED("DATA-002", "FALHA AO EXECUTAR CONSULTA", true),
	CONSTRAINT_VIOLATION("DATA-003", "VIOLAÇÃO DE RESTRIÇÃO DE DADOS", false),
	NOT_FOUND("DATA-004", "REGISTRO NÃO ENCONTRADO", false),
	INTERNAL_ERROR("DATA-999", "ERRO INTERNO DE PERSISTÊNCIA", true);

	private final String code;
	private final String message;
	private final boolean critical;

	DataAccessErrorType(String code, String message, boolean critical) {
		this.code = code;
		this.message = message;
		this.critical = critical;
	}

	public String getCode() {
		return code;
	}

	public String getMessage() {
		return message;
	}

	public boolean isCritical() {
		return critical;
	}

	@Override
	public String toString() {
		return code + " - " + message;
	}
}
