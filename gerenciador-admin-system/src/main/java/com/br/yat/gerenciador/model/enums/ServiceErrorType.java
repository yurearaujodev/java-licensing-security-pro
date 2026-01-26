package com.br.yat.gerenciador.model.enums;

public enum ServiceErrorType {
	INTEGRATION_FAILED("SRV-001", "FALHA NA INTEGRAÇÃO COM SERVIÇO EXTERNO", true),
	BUSINESS_RULE_VIOTATION("SRV-002", "REGRA DE NEGÓCIO IMPEDIU A OPERAÇÃO", false),
	UNAUTHORIZED_OPERATION("SRV-003", "OPERAÇÃO NÃO PERMITIDA PARA O ESTADO ATUAL", false),
	INTERNAL_ERROR("SRV-999", "ERRO INTERNO DE PERSISTÊNCIA", true);

	private final String code;
	private final String message;
	private final boolean critical;

	ServiceErrorType(String code, String message, boolean critical) {
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
