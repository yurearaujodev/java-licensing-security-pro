package com.br.yat.gerenciador.model;

public class ParametroSistema extends BaseEntity {

	private int idParametro;
	private String chave;
	private String valor;
	private String descricao;

	public ParametroSistema() {
	}

	public ParametroSistema(int idParametro, String chave, String valor, String descricao) {
		this.idParametro = idParametro;
		this.chave = chave;
		this.valor = valor;
		this.descricao = descricao;
	}

	public int getIdParametro() {
		return idParametro;
	}

	public void setIdParametro(int idParametro) {
		this.idParametro = idParametro;
	}

	public String getChave() {
		return chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}

	public String getValor() {
		return valor;
	}

	public void setValor(String valor) {
		this.valor = valor;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

}
