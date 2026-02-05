package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

import com.br.yat.gerenciador.model.enums.StatusUsuario;

public class Usuario extends BaseEntity {

	private Integer idUsuario;
	private String nome;
	private String email;
	private transient char[] senhaHash; // Usado para entrada segura na View
	private transient String senhaHashString; // Usado para persistência no Banco (BCrypt)
	private StatusUsuario status;
	private transient char[] senhaAntiga; // usado só para validação na troca
	private transient char[] confirmarSenha;
	private int tentativasFalhas;
	private LocalDateTime ultimoLogin;
	private Empresa empresa;
	private boolean master;
	private String tempoDesdeUltimoAcesso;

	public Usuario() {
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public String getNome() {
		return nome;
	}

	public void setNome(String nome) {
		this.nome = nome;
	}

	public String getEmail() {
		return email;
	}

	public void setEmail(String email) {
		this.email = email;
	}

	public char[] getSenhaHash() {
		return senhaHash;
	}

	public void setSenhaHash(char[] senhaHash) {
		this.senhaHash = senhaHash;
	}

	public String getSenhaHashString() {
		return senhaHashString;
	}

	public void setSenhaHashString(String senhaHashString) {
		this.senhaHashString = senhaHashString;
	}

	public char[] getSenhaAntiga() {
		return senhaAntiga;
	}

	public void setSenhaAntiga(char[] senhaAntiga) {
		this.senhaAntiga = senhaAntiga;
	}

	public char[] getConfirmarSenha() {
		return confirmarSenha;
	}

	public void setConfirmarSenha(char[] confirmarSenha) {
		this.confirmarSenha = confirmarSenha;
	}

	public StatusUsuario getStatus() {
		return status;
	}

	public void setStatus(StatusUsuario status) {
		this.status = status;
	}

	public LocalDateTime getUltimoLogin() {
		return ultimoLogin;
	}

	public void setUltimoLogin(LocalDateTime ultimoLogin) {
		this.ultimoLogin = ultimoLogin;
	}

	public Empresa getEmpresa() {
		return empresa;
	}

	public void setEmpresa(Empresa empresa) {
		this.empresa = empresa;
	}

	public int getTentativasFalhas() {
		return tentativasFalhas;
	}

	public void setTentativasFalhas(int tentativasFalhas) {
		this.tentativasFalhas = tentativasFalhas;
	}

	public boolean isMaster() {
		return master;
	}

	public void setMaster(boolean master) {
		this.master = master;
	}

	public String getTempoDesdeUltimoAcesso() {
		return tempoDesdeUltimoAcesso;
	}

	public void setTempoDesdeUltimoAcesso(String tempoDesdeUltimoAcesso) {
		this.tempoDesdeUltimoAcesso = tempoDesdeUltimoAcesso;
	}

	/**
	 * Cria uma cópia parcial do usuário contendo apenas os dados necessários para
	 * validação e auditoria de senha. Não expõe dados sensíveis em memória
	 * desnecessariamente.
	 */
	public static Usuario snapshotParaValidacaoSenha(Usuario origem) {
		if (origem == null) {
			return null;
		}

		Usuario u = new Usuario();
		u.setIdUsuario(origem.getIdUsuario());
		u.setSenhaHashString(origem.getSenhaHashString());
		u.setEmail(origem.getEmail());
		u.setStatus(origem.getStatus());
		u.setMaster(origem.isMaster());
		return u;
	}
}
