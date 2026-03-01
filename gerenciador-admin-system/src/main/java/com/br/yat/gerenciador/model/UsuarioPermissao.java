package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

public class UsuarioPermissao extends BaseEntity {
	private Usuario usuario;
	private Permissao permissao;
	private boolean ativa;
	private LocalDateTime expiraEm;
	private boolean herdada;

	public UsuarioPermissao() {
	}

	public Usuario getUsuario() {
		return usuario;
	}

	public void setUsuario(Usuario usuario) {
		this.usuario = usuario;
	}

	public Permissao getPermissao() {
		return permissao;
	}

	public void setPermissao(Permissao permissao) {
		this.permissao = permissao;
	}

	public boolean isAtiva() {
		return ativa;
	}

	public void setAtiva(boolean ativa) {
		this.ativa = ativa;
	}

	public LocalDateTime getExpiraEm() {
		return expiraEm;
	}

	public void setExpiraEm(LocalDateTime expiraEm) {
		this.expiraEm = expiraEm;
	}

	public boolean isHerdada() {
		return herdada;
	}

	public void setHerdada(boolean herdada) {
		this.herdada = herdada;
	}
}
