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

	@Override
	public boolean equals(Object obj) {
	    if (this == obj) return true;
	    if (obj == null || getClass() != obj.getClass()) return false;
	    Documento other = (Documento) obj;
	    return idDocumento == other.idDocumento &&
	           java.util.Objects.equals(tipoDocumento, other.tipoDocumento) &&
	           java.util.Objects.equals(arquivoDocumento, other.arquivoDocumento);
	}

	@Override
	public int hashCode() {
	    return java.util.Objects.hash(idDocumento, tipoDocumento, arquivoDocumento);
	}
}
