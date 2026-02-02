package com.br.yat.gerenciador.model;

public class Complementar extends BaseEntity {
	private int idComplementar;
	private String logoTipoComplementar;
	private int numFuncionariosComplementar;
	private String ramoAtividadeComplementar;
	private String obsComplementar;
	private Empresa empresa;

	public Complementar() {
	}

	public int getIdComplementar() {
		return idComplementar;
	}

	public void setIdComplementar(int idComplementar) {
		this.idComplementar = idComplementar;
	}

	public String getLogoTipoComplementar() {
		return logoTipoComplementar;
	}

	public void setLogoTipoComplementar(String logoTipoComplementar) {
		this.logoTipoComplementar = logoTipoComplementar;
	}

	public int getNumFuncionariosComplementar() {
		return numFuncionariosComplementar;
	}

	public void setNumFuncionariosComplementar(int numFuncionariosComplementar) {
		this.numFuncionariosComplementar = numFuncionariosComplementar;
	}

	public String getRamoAtividadeComplementar() {
		return ramoAtividadeComplementar;
	}

	public void setRamoAtividadeComplementar(String ramoAtividadeComplementar) {
		this.ramoAtividadeComplementar = ramoAtividadeComplementar;
	}

	public String getObsComplementar() {
		return obsComplementar;
	}

	public void setObsComplementar(String obsComplementar) {
		this.obsComplementar = obsComplementar;
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
	    Complementar other = (Complementar) obj;
	    return idComplementar == other.idComplementar &&
	           numFuncionariosComplementar == other.numFuncionariosComplementar &&
	           java.util.Objects.equals(ramoAtividadeComplementar, other.ramoAtividadeComplementar) &&
	           java.util.Objects.equals(logoTipoComplementar, other.logoTipoComplementar) &&
	           java.util.Objects.equals(obsComplementar, other.obsComplementar);
	}

	@Override
	public int hashCode() {
	    return java.util.Objects.hash(idComplementar, ramoAtividadeComplementar);
	}
}
