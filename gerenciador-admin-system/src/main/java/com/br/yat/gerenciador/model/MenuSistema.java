package com.br.yat.gerenciador.model;

public class MenuSistema extends BaseEntity {
	private Integer idMenu;
	private String chave; // Ex: "MNU_CADASTRO_USUARIO"
	private String modulo; // Ex: "CONFIGURACOES"

	public MenuSistema() {
	}

	public Integer getIdMenu() {
		return idMenu;
	}

	public void setIdMenu(Integer idMenu) {
		this.idMenu = idMenu;
	}

	public String getChave() {
		return chave;
	}

	public void setChave(String chave) {
		this.chave = chave;
	}

	public String getModulo() {
		return modulo;
	}

	public void setModulo(String modulo) {
		this.modulo = modulo;
	}

}
