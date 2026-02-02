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
	
	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;
	    Contato other = (Contato) obj;
	    return idContato == other.idContato &&
	           tipoContato == other.tipoContato &&
	           java.util.Objects.equals(valorContato, other.valorContato);
	}

	@Override
	public int hashCode() {
	    return java.util.Objects.hash(idContato, tipoContato, valorContato);
	}

}
