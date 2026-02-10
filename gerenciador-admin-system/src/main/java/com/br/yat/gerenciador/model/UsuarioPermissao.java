package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

public class UsuarioPermissao extends BaseEntity {
	private Integer idUsuario;
	private Integer idPermissoes;
	private boolean ativa;
	private LocalDateTime expiraEm;
	private boolean herdada;

	public UsuarioPermissao() {
	}

	public Integer getIdUsuario() {
		return idUsuario;
	}

	public void setIdUsuario(Integer idUsuario) {
		this.idUsuario = idUsuario;
	}

	public Integer getIdPermissoes() {
		return idPermissoes;
	}

	public void setIdPermissoes(Integer idPermissoes) {
		this.idPermissoes = idPermissoes;
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
