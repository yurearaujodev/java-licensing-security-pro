package com.br.yat.gerenciador.model.enums;
/**
 * Enumeração que representa diferentes códigos CNAE (Classificação Nacional de Atividades Econômicas)
 * relacionados a tecnologia da informação, comércio e serviços.
 * <p>
 * Cada constante possui uma descrição legível para uso em interfaces gráficas,
 * relatórios e logs.
 * </p>
 */
public enum Cnae {
	CNAE_6201("6201-5/01 - Desenvolvimento de software sob encomenda"),
	CNAE_6202("6202-3/00 - Desenvolvimento e licenciamento de programas customizáveis"),
	CNAE_6203("6203-1/00 - Desenvolvimento e licenciamento de programas não-customizáveis"),
	CNAE_6311("6311-9/00 - Tratamento de dados, hospedagem e serviços de aplicação"),
	CNAE_6319("6319-4/00 - Portais, provedores de conteúdo e outros serviços de informação"),
	CNAE_4744_1("4744-0/01 - Comércio varejista de computadores e periféricos"),
	CNAE_4744_2("4744-0/02 - Comércio varejista de equipamentos de comunicação"),
	CNAE_4751("4751-2/01 - Comércio varejista de artigos de informática"),
	CNAE_5821("5821-1/00 - Edição de livros técnicos e digitais"),
	CNAE_5822("5822-0/00 - Edição de jornais e revistas de tecnologia"),
	CNAE_5829("5829-8/00 - Edição de outros materiais de informática"),
	CNAE_6190_1("6190-6/01 - Provedores de acesso à internet"), 
	CNAE_6190_2("6190-6/02 - Serviços de backbone e redes"),
	CNAE_6209("6209-1/00 - Suporte técnico, manutenção e outros serviços em TI"),
	CNAE_6310("6310-2/00 - Serviços de busca e ferramentas online");

	private final String descricao;

	Cnae(String descricao) {
		this.descricao = descricao;
	}

	/**
	 * Retorna a descrição legível do código CNAE.
	 * 
	 * @return descrição do CNAE
	 */
	@Override
	public String toString() {
		return descricao;
	}

}
