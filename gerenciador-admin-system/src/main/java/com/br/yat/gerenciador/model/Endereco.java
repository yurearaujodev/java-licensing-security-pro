package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

public class Endereco {

	private int idEndereco;
	private String cepEndereco;
	private String logradouroEndereco;
	private String complementoLogradouro;
	private String bairroEndereco;
	private String numeroEndereco;
	private String cidadeEndereco;
	private String estadoEndereco;
	private String paisEndereco;
	private LocalDateTime criadoEmEndereco;
	private LocalDateTime atualizadoEmEndereco;

	public Endereco() {
		this.paisEndereco = "BRASIL";
	}

	public Endereco(String cepEndereco, String logradouroEndereco, String bairroEndereco, String cidadeEndereco,
			String estadoEndereco) {
		this();
		this.cepEndereco = cepEndereco != null ? cepEndereco.replaceAll("\\D", "") : "";
		this.logradouroEndereco = logradouroEndereco != null ? logradouroEndereco.trim() : "";
		this.bairroEndereco = bairroEndereco != null ? bairroEndereco.trim() : "";
		this.cidadeEndereco = cidadeEndereco != null ? cidadeEndereco.trim() : "";
		this.estadoEndereco = estadoEndereco != null ? estadoEndereco.trim() : "";
		
	}

	public int getIdEndereco() {
		return idEndereco;
	}

	public void setIdEndereco(int idEndereco) {
		this.idEndereco = idEndereco;
	}

	public String getCepEndereco() {
		return cepEndereco;
	}

	public void setCepEndereco(String cepEndereco) {
		this.cepEndereco = cepEndereco;
	}

	public String getLogradouroEndereco() {
		return logradouroEndereco;
	}

	public void setLogradouroEndereco(String logradouroEndereco) {
		this.logradouroEndereco = logradouroEndereco;
	}

	public String getComplementoLogradouro() {
		return complementoLogradouro;
	}

	public void setComplementoLogradouro(String complementoLogradouro) {
		this.complementoLogradouro = complementoLogradouro;
	}

	public String getBairroEndereco() {
		return bairroEndereco;
	}

	public void setBairroEndereco(String bairroEndereco) {
		this.bairroEndereco = bairroEndereco;
	}

	public String getNumeroEndereco() {
		return numeroEndereco;
	}

	public void setNumeroEndereco(String numeroEndereco) {
		this.numeroEndereco = numeroEndereco;
	}

	public String getCidadeEndereco() {
		return cidadeEndereco;
	}

	public void setCidadeEndereco(String cidadeEndereco) {
		this.cidadeEndereco = cidadeEndereco;
	}

	public String getEstadoEndereco() {
		return estadoEndereco;
	}

	public void setEstadoEndereco(String estadoEndereco) {
		this.estadoEndereco = estadoEndereco;
	}

	public String getPaisEndereco() {
		return paisEndereco;
	}

	public void setPaisEndereco(String paisEndereco) {
		this.paisEndereco = paisEndereco;
	}

	public LocalDateTime getCriadoEmEndereco() {
		return criadoEmEndereco;
	}

	public void setCriadoEmEndereco(LocalDateTime criadoEmEndereco) {
		this.criadoEmEndereco = criadoEmEndereco;
	}

	public LocalDateTime getAtualizadoEmEndereco() {
		return atualizadoEmEndereco;
	}

	public void setAtualizadoEmEndereco(LocalDateTime atualizadoEmEndereco) {
		this.atualizadoEmEndereco = atualizadoEmEndereco;
	}

}
