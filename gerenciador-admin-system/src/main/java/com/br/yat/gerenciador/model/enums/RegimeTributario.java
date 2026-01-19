package com.br.yat.gerenciador.model.enums;

public enum RegimeTributario {
	SELECIONE(0, "SELECIONE UMA OPÇÃO"),
	SIMPLES_NACIONAL(1, "Simples Nacional"), 
	SIMPLES_SUBLIMITE(2, "Simples Nacional - Excesso de Sublimite"),
	REGIME_NORMAL(3, "Regime Normal (Lucro Presumido/Real)"),
	MEI(4, "Microempreendedor Individual (MEI)");

	private final int crt;
	private final String descricao;

	private RegimeTributario(int crt, String descricao) {
		this.crt = crt;
		this.descricao = descricao;
	}

	public int getCrt() {
		return crt;
	}

	public String getDescricao() {
		return descricao;
	}

	@Override
	public String toString() {
		return descricao;
	}

}
