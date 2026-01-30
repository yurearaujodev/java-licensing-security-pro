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
}
