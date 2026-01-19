package com.br.yat.gerenciador.model;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public class Empresa {
	private int idEmpresa;
	private String tipoEmpresa;
	private String fantasiaEmpresa;
	private String razaoSocialEmpresa;
	private String tipoDocEmpresa;
	private String documentoEmpresa;
	private String inscEst;
	private String inscMun;
	private String contribuinteIcmsEmpresa;
	private LocalDate fundacaoEmpresa;
	private String cnaeEmpresa;
	private String porteEmpresa;
	private String naturezaJuriEmpresa;
	private int crtEmpresa;
	private String regimeTribEmpresa;
	private BigDecimal capitalEmpresa;
	private String situacaoEmpresa;
	private LocalDateTime criadoEmEmpresa;
	private LocalDateTime atualizadoEmEmpresa;
	private Endereco endereco;

	public Empresa() {
	}

	public int getIdEmpresa() {
		return idEmpresa;
	}

	public void setIdEmpresa(int idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	public String getTipoEmpresa() {
		return tipoEmpresa;
	}

	public void setTipoEmpresa(String tipoEmpresa) {
		this.tipoEmpresa = tipoEmpresa;
	}

	public String getFantasiaEmpresa() {
		return fantasiaEmpresa;
	}

	public void setFantasiaEmpresa(String fantasiaEmpresa) {
		this.fantasiaEmpresa = fantasiaEmpresa;
	}

	public String getRazaoSocialEmpresa() {
		return razaoSocialEmpresa;
	}

	public void setRazaoSocialEmpresa(String razaoSocialEmpresa) {
		this.razaoSocialEmpresa = razaoSocialEmpresa;
	}

	public String getTipoDocEmpresa() {
		return tipoDocEmpresa;
	}

	public void setTipoDocEmpresa(String tipoDocEmpresa) {
		this.tipoDocEmpresa = tipoDocEmpresa;
	}

	public String getDocumentoEmpresa() {
		return documentoEmpresa;
	}

	public void setDocumentoEmpresa(String documentoEmpresa) {
		this.documentoEmpresa = documentoEmpresa;
	}

	public String getInscEst() {
		return inscEst;
	}

	public void setInscEst(String inscEst) {
		this.inscEst = inscEst;
	}

	public String getInscMun() {
		return inscMun;
	}

	public void setInscMun(String inscMun) {
		this.inscMun = inscMun;
	}

	public String getContribuinteIcmsEmpresa() {
		return contribuinteIcmsEmpresa;
	}

	public void setContribuinteIcmsEmpresa(String contribuinteIcmsEmpresa) {
		this.contribuinteIcmsEmpresa = contribuinteIcmsEmpresa;
	}

	public LocalDate getFundacaoEmpresa() {
		return fundacaoEmpresa;
	}

	public void setFundacaoEmpresa(LocalDate fundacaoEmpresa) {
		this.fundacaoEmpresa = fundacaoEmpresa;
	}

	public String getCnaeEmpresa() {
		return cnaeEmpresa;
	}

	public void setCnaeEmpresa(String cnaeEmpresa) {
		this.cnaeEmpresa = cnaeEmpresa;
	}

	public String getPorteEmpresa() {
		return porteEmpresa;
	}

	public void setPorteEmpresa(String porteEmpresa) {
		this.porteEmpresa = porteEmpresa;
	}

	public String getNaturezaJuriEmpresa() {
		return naturezaJuriEmpresa;
	}

	public void setNaturezaJuriEmpresa(String naturezaJuriEmpresa) {
		this.naturezaJuriEmpresa = naturezaJuriEmpresa;
	}

	public int getCrtEmpresa() {
		return crtEmpresa;
	}

	public void setCrtEmpresa(int crtEmpresa) {
		this.crtEmpresa = crtEmpresa;
	}

	public String getRegimeTribEmpresa() {
		return regimeTribEmpresa;
	}

	public void setRegimeTribEmpresa(String regimeTribEmpresa) {
		this.regimeTribEmpresa = regimeTribEmpresa;
	}

	public BigDecimal getCapitalEmpresa() {
		return capitalEmpresa;
	}

	public void setCapitalEmpresa(BigDecimal capitalEmpresa) {
		this.capitalEmpresa = capitalEmpresa;
	}

	public String getSituacaoEmpresa() {
		return situacaoEmpresa;
	}

	public void setSituacaoEmpresa(String situacaoEmpresa) {
		this.situacaoEmpresa = situacaoEmpresa;
	}

	public LocalDateTime getCriadoEmEmpresa() {
		return criadoEmEmpresa;
	}

	public void setCriadoEmEmpresa(LocalDateTime criadoEmEmpresa) {
		this.criadoEmEmpresa = criadoEmEmpresa;
	}

	public LocalDateTime getAtualizadoEmEmpresa() {
		return atualizadoEmEmpresa;
	}

	public void setAtualizadoEmEmpresa(LocalDateTime atualizadoEmEmpresa) {
		this.atualizadoEmEmpresa = atualizadoEmEmpresa;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

}
