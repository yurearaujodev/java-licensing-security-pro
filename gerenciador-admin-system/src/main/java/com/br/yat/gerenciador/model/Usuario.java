package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

public class Usuario extends BaseEntity {

    private Integer idUsuario;
    private String nome;
    private String email;
    private char[] senhaHash; // Usado para entrada segura na View
    private String senhaHashString; // Usado para persistência no Banco (BCrypt)
    private String status;
    private int tentativasFalhas;
    private LocalDateTime ultimoLogin;
    private Empresa idEmpresa;

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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public LocalDateTime getUltimoLogin() {
        return ultimoLogin;
    }

    public void setUltimoLogin(LocalDateTime ultimoLogin) {
        this.ultimoLogin = ultimoLogin;
    }

    public Empresa getIdEmpresa() {
        return idEmpresa;
    }

    public void setIdEmpresa(Empresa idEmpresa) {
        this.idEmpresa = idEmpresa;
	}

	public int getTentativasFalhas() {
		return tentativasFalhas;
	}

	public void setTentativasFalhas(int tentativasFalhas) {
		this.tentativasFalhas = tentativasFalhas;
	}
}
