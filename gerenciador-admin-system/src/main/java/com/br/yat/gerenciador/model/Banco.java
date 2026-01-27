package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

import com.br.yat.gerenciador.model.enums.TipoConta;

public class Banco {
	private int idBanco;
	private String nomeBanco;
	private int codBanco;
	private String agenciaBanco;
	private String contaBanco;
	private TipoConta tipoBanco;
	private LocalDateTime criadoEmBanco;
	private LocalDateTime atualizadoEmBanco;
	private Empresa empresa;

	public Banco() {
	}

	public int getIdBanco() {
		return idBanco;
	}

	public void setIdBanco(int idBanco) {
		this.idBanco = idBanco;
	}

	public String getNomeBanco() {
		return nomeBanco;
	}

	public void setNomeBanco(String nomeBanco) {
		this.nomeBanco = nomeBanco;
	}

	public int getCodBanco() {
		return codBanco;
	}

	public void setCodBanco(int codBanco) {
		this.codBanco = codBanco;
	}

	public String getAgenciaBanco() {
		return agenciaBanco;
	}

	public void setAgenciaBanco(String agenciaBanco) {
		this.agenciaBanco = agenciaBanco;
	}

	public String getContaBanco() {
		return contaBanco;
	}

	public void setContaBanco(String contaBanco) {
		this.contaBanco = contaBanco;
	}

	public TipoConta getTipoBanco() {
		return tipoBanco;
	}

	public void setTipoBanco(TipoConta tipoBanco) {
		this.tipoBanco = tipoBanco;
	}

	public LocalDateTime getCriadoEmBanco() {
		return criadoEmBanco;
	}

	public void setCriadoEmBanco(LocalDateTime criadoEmBanco) {
		this.criadoEmBanco = criadoEmBanco;
	}

	public LocalDateTime getAtualizadoEmBanco() {
		return atualizadoEmBanco;
	}

	public void setAtualizadoEmBanco(LocalDateTime atualizadoEmBanco) {
		this.atualizadoEmBanco = atualizadoEmBanco;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}
}
