package com.br.yat.gerenciador.model;

import java.math.BigDecimal;
import java.time.LocalDate;

import com.br.yat.gerenciador.model.enums.Cnae;
import com.br.yat.gerenciador.model.enums.NaturezaJuridica;
import com.br.yat.gerenciador.model.enums.PorteEmpresa;
import com.br.yat.gerenciador.model.enums.RegimeTributario;
import com.br.yat.gerenciador.model.enums.SituacaoEmpresa;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.model.enums.TipoDocumento;

public class Empresa extends BaseEntity{
	private int idEmpresa;
	private TipoCadastro tipoEmpresa;
	private String fantasiaEmpresa;
	private String razaoSocialEmpresa;
	private TipoDocumento tipoDocEmpresa;
	private String documentoEmpresa;
	private String inscEst;
	private String inscMun;
	private String contribuinteIcmsEmpresa;
	private LocalDate fundacaoEmpresa;
	private Cnae cnaeEmpresa;
	private PorteEmpresa porteEmpresa;
	private NaturezaJuridica naturezaJuriEmpresa;
	private int crtEmpresa;
	private RegimeTributario regimeTribEmpresa;
	private BigDecimal capitalEmpresa;
	private SituacaoEmpresa situacaoEmpresa;
	private Endereco endereco;

	public Empresa() {
	}

	public int getIdEmpresa() {
		return idEmpresa;
	}

	public void setIdEmpresa(int idEmpresa) {
		this.idEmpresa = idEmpresa;
	}

	public TipoCadastro getTipoEmpresa() {
		return tipoEmpresa;
	}

	public void setTipoEmpresa(TipoCadastro tipoEmpresa) {
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

	public TipoDocumento getTipoDocEmpresa() {
		return tipoDocEmpresa;
	}

	public void setTipoDocEmpresa(TipoDocumento tipoDocEmpresa) {
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

	public Cnae getCnaeEmpresa() {
		return cnaeEmpresa;
	}

	public void setCnaeEmpresa(Cnae cnaeEmpresa) {
		this.cnaeEmpresa = cnaeEmpresa;
	}

	public PorteEmpresa getPorteEmpresa() {
		return porteEmpresa;
	}

	public void setPorteEmpresa(PorteEmpresa porteEmpresa) {
		this.porteEmpresa = porteEmpresa;
	}

	public NaturezaJuridica getNaturezaJuriEmpresa() {
		return naturezaJuriEmpresa;
	}

	public void setNaturezaJuriEmpresa(NaturezaJuridica naturezaJuriEmpresa) {
		this.naturezaJuriEmpresa = naturezaJuriEmpresa;
	}

	public int getCrtEmpresa() {
		return crtEmpresa;
	}

	public void setCrtEmpresa(int crtEmpresa) {
		this.crtEmpresa = crtEmpresa;
	}

	public RegimeTributario getRegimeTribEmpresa() {
		return regimeTribEmpresa;
	}

	public void setRegimeTribEmpresa(RegimeTributario regimeTribEmpresa) {
		this.regimeTribEmpresa = regimeTribEmpresa;
	}

	public BigDecimal getCapitalEmpresa() {
		return capitalEmpresa;
	}

	public void setCapitalEmpresa(BigDecimal capitalEmpresa) {
		this.capitalEmpresa = capitalEmpresa;
	}

	public SituacaoEmpresa getSituacaoEmpresa() {
		return situacaoEmpresa;
	}

	public void setSituacaoEmpresa(SituacaoEmpresa situacaoEmpresa) {
		this.situacaoEmpresa = situacaoEmpresa;
	}

	public Endereco getEndereco() {
		return endereco;
	}

	public void setEndereco(Endereco endereco) {
		this.endereco = endereco;
	}

}
