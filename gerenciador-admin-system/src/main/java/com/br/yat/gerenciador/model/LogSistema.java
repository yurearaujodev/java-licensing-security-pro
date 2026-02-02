package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

public class LogSistema {
	private Integer idLog;
	private String tipo; // Ex: "SEGURANCA", "CADASTRO", "SISTEMA"
	private String acao; // Ex: "LOGIN", "SALVAR_USUARIO", "EXCLUIR_EMPRESA"
	private String entidade; // Nome da tabela afetada
	private Integer idEntidade;
	private String dadosAnteriores; // JSON string
	private String dadosNovos; // JSON string
	private boolean sucesso;
	private String mensagemErro;
	private String ipOrigem;
	private LocalDateTime dataHora;
	private Usuario usuario;

	public LogSistema() {
	}

	public Integer getIdLog() {
		return idLog;
	}

	public void setIdLog(Integer idLog) {
		this.idLog = idLog;
	}

	public String getTipo() {
		return tipo;
	}

	public void setTipo(String tipo) {
		this.tipo = tipo;
	}

	public String getAcao() {
		return acao;
	}

	public void setAcao(String acao) {
		this.acao = acao;
	}

	public String getEntidade() {
		return entidade;
	}

	public void setEntidade(String entidade) {
		this.entidade = entidade;
	}

	public Integer getIdEntidade() {
		return idEntidade;
	}

	public void setIdEntidade(Integer idEntidade) {
		this.idEntidade = idEntidade;
	}

	public String getDadosAnteriores() {
		return dadosAnteriores;
	}

	public void setDadosAnteriores(String dadosAnteriores) {
		this.dadosAnteriores = dadosAnteriores;
	}

	public String getDadosNovos() {
		return dadosNovos;
	}

	public void setDadosNovos(String dadosNovos) {
		this.dadosNovos = dadosNovos;
	}

	public boolean isSucesso() {
		return sucesso;
	}

	public void setSucesso(boolean sucesso) {
		this.sucesso = sucesso;
	}

	public String getMensagemErro() {
		return mensagemErro;
	}

	public void setMensagemErro(String mensagemErro) {
		this.mensagemErro = mensagemErro;
	}

	public String getIpOrigem() {
		return ipOrigem;
	}

	public void setIpOrigem(String ipOrigem) {
		this.ipOrigem = ipOrigem;
	}

	public LocalDateTime getDataHora() {
		return dataHora;
	}

	public void setDataHora(LocalDateTime dataHora) {
		this.dataHora = dataHora;
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

}
