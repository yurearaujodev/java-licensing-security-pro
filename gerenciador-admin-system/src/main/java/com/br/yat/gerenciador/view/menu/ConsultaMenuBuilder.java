package com.br.yat.gerenciador.view.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.view.factory.MenuFactory;

public final class ConsultaMenuBuilder {

	private ConsultaMenuBuilder() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	public static JMenu builder() {
		JMenu menuConsulta = MenuFactory.createMenu("CONSULTAS", null);

		JMenuItem itemEmpCliCon = MenuFactory.createMenuItem("EMPRESAS CLIENTES", null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_EMPRESAS_CLIENTES, itemEmpCliCon);
		menuConsulta.add(itemEmpCliCon);

		JMenuItem itemUsuCon = MenuFactory.createMenuItem("USUÁRIOS", null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_USUARIOS, itemUsuCon);
		menuConsulta.add(itemUsuCon);

		JMenuItem itemPlaLicCon = MenuFactory.createMenuItem("PLANO DE LICENÇA", null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_PLANO_DE_LICENCA, itemPlaLicCon);
		menuConsulta.add(itemPlaLicCon);

		JMenuItem itemLicCon = MenuFactory.createMenuItem("LICENÇAS", null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_LICENCAS, itemLicCon);
		menuConsulta.add(itemLicCon);

		JMenuItem itemDisCon = MenuFactory.createMenuItem("DISPOSITIVOS", null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_DISPOSITIVOS, itemDisCon);
		menuConsulta.add(itemDisCon);

		JMenuItem itemPagCon = MenuFactory.createMenuItem("PAGAMENTOS", null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_PAGAMENTOS, itemPagCon);
		menuConsulta.add(itemPagCon);

		return menuConsulta;
	}

}
