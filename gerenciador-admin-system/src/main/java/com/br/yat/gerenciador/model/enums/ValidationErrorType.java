package com.br.yat.gerenciador.model.enums;

public enum ValidationErrorType {
	INVALID_FIELD("VAL-001", "CAMPO COM FORMATO INVÁLIDO", false),
	REQUIRED_FIELD_MISSING("VAL-002", "CAMPO OBRIGATÓRIO NÃO PREENCHIDO", false), 
	DUPLICATE_ENTRY("VAL-003", "ENTRADA DUPLICADA DETECTADA", false),
	GENERIC_VALIDATION_ERROR("VAL-999", "ERRO DE VALIDAÇÃO DE DADOS", false);

	private final String code;
	private final String message;
	private final boolean critical;

	ValidationErrorType(String code, String message, boolean critical) {
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
