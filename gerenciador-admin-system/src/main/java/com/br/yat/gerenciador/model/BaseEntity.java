package com.br.yat.gerenciador.model;

import java.time.LocalDateTime;

public abstract class BaseEntity {

	private LocalDateTime criadoEm;
	private LocalDateTime atualizadoEm;
	private LocalDateTime deletadoEm;

	public LocalDateTime getCriadoEm() {
		return criadoEm;
	}

	public void setCriadoEm(LocalDateTime criadoEm) {
		this.criadoEm = criadoEm;
	}

	public LocalDateTime getAtualizadoEm() {
		return atualizadoEm;
	}

	public void setAtualizadoEm(LocalDateTime atualizadoEm) {
		this.atualizadoEm = atualizadoEm;
	}

	public LocalDateTime getDeletadoEm() {
		return deletadoEm;
	}

	public void setDeletadoEm(LocalDateTime deletadoEm) {
		this.deletadoEm = deletadoEm;
	}

	public boolean isAtivo() {
		return deletadoEm == null;
	}

}
