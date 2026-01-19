package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

public class Complementar {
	private int idComplementar;
	private String logoTipoComplementar;
	private int numFuncionariosComplementar;
	private String ramoAtividadeComplementar;
	private String obsComplementar;
	private LocalDateTime criadoEmComplementar;
	private LocalDateTime atualizadoEmComplementar;
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

	public LocalDateTime getCriadoEmComplementar() {
		return criadoEmComplementar;
	}

	public void setCriadoEmComplementar(LocalDateTime criadoEmComplementar) {
		this.criadoEmComplementar = criadoEmComplementar;
	}

	public LocalDateTime getAtualizadoEmComplementar() {
		return atualizadoEmComplementar;
	}

	public void setAtualizadoEmComplementar(LocalDateTime atualizadoEmComplementar) {
		this.atualizadoEmComplementar = atualizadoEmComplementar;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
}
