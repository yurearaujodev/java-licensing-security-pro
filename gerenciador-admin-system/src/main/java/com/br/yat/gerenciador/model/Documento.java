package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

public class Documento {
	private int idDocumento;
	private String tipoDocumento;
	private String arquivoDocumento;
	private LocalDateTime criadoEmDocumento;
	private LocalDateTime atualizadoEmDocumento;
	private Empresa empresa;

	public Documento() {
	}

	public int getIdDocumento() {
		return idDocumento;
	}

	public void setIdDocumento(int idDocumento) {
		this.idDocumento = idDocumento;
	}

	public String getTipoDocumento() {
		return tipoDocumento;
	}

	public void setTipoDocumento(String tipoDocumento) {
		this.tipoDocumento = tipoDocumento;
	}

	public String getArquivoDocumento() {
		return arquivoDocumento;
	}

	public void setArquivoDocumento(String arquivoDocumento) {
		this.arquivoDocumento = arquivoDocumento;
	}

	public LocalDateTime getCriadoEmDocumento() {
		return criadoEmDocumento;
	}

	public void setCriadoEmDocumento(LocalDateTime criadoEmDocumento) {
		this.criadoEmDocumento = criadoEmDocumento;
	}

	public LocalDateTime getAtualizadoEmDocumento() {
		return atualizadoEmDocumento;
	}

	public void setAtualizadoEmDocumento(LocalDateTime atualizadoEmDocumento) {
		this.atualizadoEmDocumento = atualizadoEmDocumento;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

}
