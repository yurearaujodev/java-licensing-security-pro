package com.br.yat.gerenciador.view;

import java.util.EnumMap;
import java.util.Map;

import javax.swing.JCheckBox;
import javax.swing.JFormattedTextField;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoPermissao;

public class PermissaoViewItem {

	private final MenuChave menu;
	private final Map<TipoPermissao, JCheckBox> checks;
	private final JFormattedTextField dataExpiracao;

	public PermissaoViewItem(MenuChave menu, JFormattedTextField dataExpiracao) {
		this.menu = menu;
		this.dataExpiracao = dataExpiracao;
		this.checks = new EnumMap<>(TipoPermissao.class);
	}

	public MenuChave getMenu() {
		return menu;
	}

	public void addCheck(TipoPermissao tipo, JCheckBox check) {
		checks.put(tipo, check);
	}

	public JCheckBox getCheck(TipoPermissao tipo) {
		return checks.get(tipo);
	}

	public Map<TipoPermissao, JCheckBox> getChecks() {
		return checks;
	}

	public String getDataExpiracaoTexto() {
		return dataExpiracao.getText();
	}

	public void setEnabled(boolean enabled) {
		checks.values().forEach(c -> c.setEnabled(enabled));
		dataExpiracao.setEnabled(enabled);
	}

	public void limpar() {
		checks.values().forEach(c -> c.setSelected(false));
		dataExpiracao.setText("");
	}
}
