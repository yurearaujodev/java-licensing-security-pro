package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

public class Contato {
	private int idContato;
	private String tipoContato;
	private String valorContato;
	private LocalDateTime criadoEmContato;
	private LocalDateTime atualizadoEmContato;
	private Empresa empresa;

	public Contato() {
	}

	public int getIdContato() {
		return idContato;
	}

	public void setIdContato(int idContato) {
		this.idContato = idContato;
	}

	public String getTipoContato() {
		return tipoContato;
	}

	public void setTipoContato(String tipoContato) {
		this.tipoContato = tipoContato;
	}

	public String getValorContato() {
		return valorContato;
	}

	public void setValorContato(String valorContato) {
		this.valorContato = valorContato;
	}

	public LocalDateTime getCriadoEmContato() {
		return criadoEmContato;
	}

	public void setCriadoEmContato(LocalDateTime criadoEmContato) {
		this.criadoEmContato = criadoEmContato;
	}

	public LocalDateTime getAtualizadoEmContato() {
		return atualizadoEmContato;
	}

	public void setAtualizadoEmContato(LocalDateTime atualizadoEmContato) {
		this.atualizadoEmContato = atualizadoEmContato;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

}
