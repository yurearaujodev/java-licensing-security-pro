package com.br.yat.gerenciador.model.enums;
/**
 * Enumeração que representa as diferentes naturezas jurídicas de empresas.
 * <p>
 * Cada constante possui uma descrição legível para uso em interfaces gráficas,
 * relatórios e logs.
 * </p>
 */
public enum NaturezaJuridica {
	MEI("MEI - Microempreendedor Individual"), 
	EI("EI - Empresário Individual"), 
	LTDA("LTDA - Sociedade Limitada"),
	SLU("SLU - Sociedade Limitada Unipessoal"), 
	SA_ABERTO("SA - Sociedade Anônima de Capital Aberto"),
	SA_FECHADO("SA - SOCIEDADE ANÔNIMA DE CAPITAL FECHADO"), 
	SOC_SIMPLES("Sociedade Simples"),
	SOC_NOME_COLETIVO("Sociedade em Nome Coletivo"), 
	SOC_COMANDITA_SIMPLES("Sociedade em Comandita Simples"),
	SOC_COMANDITA_ACOES("Sociedade em Comandita por Ações"), 
	COOP_TI("Cooperativa de Tecnologia"),
	ASSOC_TI("Associação de Profissionais de TI"), 
	FUNDACAO_TI("Fundação voltada à Pesquisa em TI"),
	CONSORCIO_TI("Consórcio de Empresas de Software");

	private final String descricao;

	private NaturezaJuridica(String descricao) {
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
