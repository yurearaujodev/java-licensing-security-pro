package com.br.yat.gerenciador.model.enums;
/**
 * Enumeração que representa os diferentes portes de empresa.
 * <p>
 * Cada constante possui uma descrição legível para uso em interfaces gráficas,
 * relatórios e logs.
 * </p>
 */
public enum PorteEmpresa {
	SELECIONE("SELECIONE UMA OPÇÃO"),
	MEI("MEI - Microempreendedor Individual"), 
	ME("ME - Microempresa"), 
	EPP("EPP - Empresa de Pequeno Porte"),
	MP("MP - Médio Porte"), 
	GP("GP - Grande Porte"), 
	MT("MT - Multinacional de Tecnologia"),
	STI_I("STI-I - Startup em fase inicial"),
	STI_C("STI-C - Startup em fase de crescimento"),
	ESB("ESB - Empresa de Software Boutique"), 
	CTI_P("CTI-P - Consultoria de TI de pequeno porte"),
	CTI_M("CTI-M - Consultoria de TI de médio porte"), 
	CTI_G("CTI-G - Consultoria de TI de grande porte"),
	CPTI("CPTI - Cooperativa de Profissionais de TI"), 
	ADTI("ADTI - Associação de Desenvolvedores de TI"),
	FPTI("FPTI - Fundação/Instituto de Pesquisa em TI");

	private final String descricao;

	private PorteEmpresa(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * Retorna a descrição legível do porte da empresa.
	 * 
	 * @return descrição do porte
	 */
	@Override
	public String toString() {
		return descricao;
	}

}
