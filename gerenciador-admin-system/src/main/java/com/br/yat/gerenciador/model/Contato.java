package com.br.yat.gerenciador.model;

import com.br.yat.gerenciador.model.enums.TipoContato;

public class Contato {
	private int idContato;
	private TipoContato tipoContato;
	private String valorContato;
	private Empresa empresa;

	public Contato() {
	}

	public int getIdContato() {
		return idContato;
	}

	public void setIdContato(int idContato) {
		this.idContato = idContato;
	}

	public TipoContato getTipoContato() {
		return tipoContato;
	}

	public void setTipoContato(TipoContato tipoContato) {
		this.tipoContato = tipoContato;
	}

	public String getValorContato() {
		return valorContato;
	}

	public void setValorContato(String valorContato) {
		this.valorContato = valorContato;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

}
