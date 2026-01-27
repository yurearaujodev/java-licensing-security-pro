package com.br.yat.gerenciador.model.enums;

public enum TipoConta {
	SELECIONE("SELECIONE"),
	CORRENTE("CONTA CORRENTE"),
	POUPANCA("CONTA POUPANÇA"),
	SALARIO("CONTA SALÁRIO"),
	DIGITAL("CONTA DIGITAL/PAGAMENTO");
	
	private final String descricao;
	
	TipoConta(String descricao) {
		this.descricao=descricao;
	}
	
	public String getDescricao() {
		return descricao;
	}
	
	@Override
	public String toString() {
		return descricao;
	}

}
