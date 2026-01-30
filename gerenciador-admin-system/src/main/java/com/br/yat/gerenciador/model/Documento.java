package com.br.yat.gerenciador.model;

public class Documento extends BaseEntity {
	private int idDocumento;
	private String tipoDocumento;
	private String arquivoDocumento;
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

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

}
