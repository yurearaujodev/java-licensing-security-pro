package com.br.yat.gerenciador.view.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.util.ui.MenuFactory;

public final class AjudaMenuBuilder {

	private AjudaMenuBuilder() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	public static JMenu builder() {
		JMenu menuAjuda = MenuFactory.createMenu("AJUDA", null);

		JMenuItem itemManSis = MenuFactory.createMenuItem("MANUAL DO SISTEMA", null, null);
		MenuRegistry.register(MenuChave.AJUDA_MANUAL_DO_SISTEMA, itemManSis);
		menuAjuda.add(itemManSis);

		JMenuItem itemSupTec = MenuFactory.createMenuItem("SUPORTE TÉCNICO", null, null);
		MenuRegistry.register(MenuChave.AJUDA_SUPORTE_TECNICO, itemSupTec);
		menuAjuda.add(itemSupTec);

		JMenuItem itemSobSis = MenuFactory.createMenuItem("SOBRE O SISTEMA", null, null);
		MenuRegistry.register(MenuChave.AJUDA_SOBRE_O_SISTEMA, itemSobSis);
		menuAjuda.add(itemSobSis);

		JMenuItem itemVerAtu = MenuFactory.createMenuItem("VERIFICAR ATUALIZAÇÕES", null, null);
		MenuRegistry.register(MenuChave.AJUDA_VERIFICAR_ATUALIZACOES, itemVerAtu);
		menuAjuda.add(itemVerAtu);

		return menuAjuda;
	}
}
