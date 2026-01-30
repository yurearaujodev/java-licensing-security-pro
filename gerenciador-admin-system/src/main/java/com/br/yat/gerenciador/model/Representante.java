package com.br.yat.gerenciador.model;

public class Representante extends BaseEntity {
	private int idRepresentante;
	private String nomeRepresentante;
	private String cpfRepresentante;
	private String rgRepresentante;
	private String cargoRepresentante;
	private String nacionalidadeRepresentante;
	private String estadoCivilRepresentante;
	private String telefoneRepresentante;
	private String emailRepresentante;
	private Empresa empresa;

	public Representante() {
	}

	public int getIdRepresentante() {
		return idRepresentante;
	}

	public void setIdRepresentante(int idRepresentante) {
		this.idRepresentante = idRepresentante;
	}

	public String getNomeRepresentante() {
		return nomeRepresentante;
	}

	public void setNomeRepresentante(String nomeRepresentante) {
		this.nomeRepresentante = nomeRepresentante;
	}

	public String getCpfRepresentante() {
		return cpfRepresentante;
	}

	public void setCpfRepresentante(String cpfRepresentante) {
		this.cpfRepresentante = cpfRepresentante;
	}

	public String getRgRepresentante() {
		return rgRepresentante;
	}

	public void setRgRepresentante(String rgRepresentante) {
		this.rgRepresentante = rgRepresentante;
	}

	public String getCargoRepresentante() {
		return cargoRepresentante;
	}

	public void setCargoRepresentante(String cargoRepresentante) {
		this.cargoRepresentante = cargoRepresentante;
	}

	public String getNacionalidadeRepresentante() {
		return nacionalidadeRepresentante;
	}

	public void setNacionalidadeRepresentante(String nacionalidadeRepresentante) {
		this.nacionalidadeRepresentante = nacionalidadeRepresentante;
	}

	public String getEstadoCivilRepresentante() {
		return estadoCivilRepresentante;
	}

	public void setEstadoCivilRepresentante(String estadoCivilRepresentante) {
		this.estadoCivilRepresentante = estadoCivilRepresentante;
	}

	public String getTelefoneRepresentante() {
		return telefoneRepresentante;
	}

	public void setTelefoneRepresentante(String telefoneRepresentante) {
		this.telefoneRepresentante = telefoneRepresentante;
	}

	public String getEmailRepresentante() {
		return emailRepresentante;
	}

	public void setEmailRepresentante(String emailRepresentante) {
		this.emailRepresentante = emailRepresentante;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

}
