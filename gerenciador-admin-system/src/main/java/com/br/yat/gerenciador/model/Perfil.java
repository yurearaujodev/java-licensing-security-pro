package com.br.yat.gerenciador.model;

import java.util.ArrayList;
import java.util.List;

public class Perfil extends BaseEntity {
	private Integer idPerfil;
	private String nome;
	private String descricao;
	private List<Permissao> permissoes = new ArrayList<>();

	public Perfil() {
	}

	public Perfil(Integer idPerfil) {
		this.idPerfil = idPerfil;
	}

	public Integer getIdPerfil() {
		return idPerfil;
	}

	public void setIdPerfil(Integer idPerfil) {
		this.idPerfil = idPerfil;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getDescricao() {
		return descricao;
	}

	public void setDescricao(String descricao) {
		this.descricao = descricao;
	}

	public List<Permissao> getPermissoes() {
		return permissoes;
	}

	public void setPermissoes(List<Permissao> permissoes) {
		this.permissoes = permissoes;
	}
	
	@Override
	public String toString() {
	    return this.nome != null ? this.nome : "Selecione...";
	}
}
