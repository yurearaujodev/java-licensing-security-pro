package com.br.yat.gerenciador.model;

public class Permissao extends BaseEntity {
	private Integer idPermissoes;
	private String chave;
	private String tipo;
	private String categoria;
	private String descricao;
	private Integer nivel; // NOVO CAMPO

	public Permissao() {
	}

	public Integer getIdPermissoes() {
		return idPermissoes;
	}

	public void setIdPermissoes(Integer idPermissoes) {
		this.idPermissoes = idPermissoes;
	}

	public String getChave() {
		return chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getCategoria() {
		return categoria;
	}

	public void setCategoria(String categoria) {
		this.categoria = categoria;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public Integer getNivel() {
		return nivel;
	}

	public void setNivel(Integer nivel) {
		this.nivel = nivel;
	}
}