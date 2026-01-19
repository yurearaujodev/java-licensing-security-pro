package com.br.yat.gerenciador.model.enums;

public enum MensagemErro {
	DOCUMENTO_OBRIGATORIO("DOCUMENTO É OBRIGATÓRIO."),
	DOCUMENTO_INVALIDO("DOCUMENTO INVÁLIDO. VERIFIQUE CPF OU CNPJ."),
	INSCRICAO_MUNICIPAL("INSCRIÇÂO MUNICIPAL INVÁLIDA. USE 7 A 15 DÍGITOS OU (ISENTO)"),
	INSCRICAO_ESTADUAL("INSCRIÇÃO ESTADUAL INVÁLIDA. USE 9 A 14 DÍGITOS OU (ISENTO)"),
	CAPITAL_SOCIAL("CAPITAL SOCIAL DEVE SER MAIOR QUE ZERO."),
	DATA_FUNDACAO("DATA DE FUNDAÇÃO INVÁLIDA OU FUTURA.")
	;
	
	
	private final String descricao;

	private MensagemErro(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * Retorna a descrição legível da natureza jurídica.
	 * 
	 * @return descrição da natureza jurídica
	 */
	@Override
	public String toString() {
		return descricao;
	}

}
