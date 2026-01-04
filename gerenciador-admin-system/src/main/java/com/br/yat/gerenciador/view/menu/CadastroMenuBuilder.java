package com.br.yat.gerenciador.view.menu;

import java.awt.event.ActionListener;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.util.ui.MenuFactory;

public final class CadastroMenuBuilder {

	private CadastroMenuBuilder() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}
	
	public static JMenu builder(ActionListener abrirEmpresa) {
		JMenu menuCadastro = MenuFactory.createMenu("CADASTROS", null);

		JMenuItem itemEmpCli = MenuFactory.createMenuItem("EMPRESA CLIENTE", null, null, abrirEmpresa);
		MenuRegistry.register(MenuChave.CADASTROS_EMPRESA_CLIENTE, itemEmpCli);
		menuCadastro.add(itemEmpCli);

		JMenuItem itemUsu = MenuFactory.createMenuItem("USUÁRIO", null, null, null);
		MenuRegistry.register(MenuChave.CADASTROS_USUARIO, itemUsu);
		menuCadastro.add(itemUsu);

		JMenuItem itemPlaLic = MenuFactory.createMenuItem("PLANO DE LICENÇA", null, null, null);
		MenuRegistry.register(MenuChave.CADASTROS_PLANO_DE_LICENCA, itemPlaLic);
		menuCadastro.add(itemPlaLic);

		return menuCadastro;
	}

}
