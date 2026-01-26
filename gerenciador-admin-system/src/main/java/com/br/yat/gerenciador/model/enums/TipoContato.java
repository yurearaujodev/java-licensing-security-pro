package com.br.yat.gerenciador.model.enums;

public enum TipoContato {
	SELECIONE("SELECIONE"),
	FIXO("TELEFONE FIXO"),
	CELULAR("TELEFONE CELULAR"),
	WHATSAPP("TELEFONE WHATSAPP"),
	EMAIL("E-MAIL"),
	REDESOCIAL("REDE SOCIAL"),
	SITE("SITE");
	
	private final String descricao;
	
	TipoContato(String descricao) {
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
