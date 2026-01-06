package com.br.yat.gerenciador.view.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.util.ui.MenuFactory;

public final class RelatorioMenuBuilder {

	private RelatorioMenuBuilder() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	public static JMenu builder() {
		JMenu menuRelatorios = MenuFactory.createMenu("RELATÓRIOS GERENCIAIS", null);

		JMenuItem itemLicAtiExp = MenuFactory.createMenuItem("LICENÇAS ATIVAS / EXPIRADAS", null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_LICENCAS_ATIVAS_EXPERIDAS, itemLicAtiExp);
		menuRelatorios.add(itemLicAtiExp);

		JMenuItem itemLicVen = MenuFactory.createMenuItem("LICENÇAS A VENCER", null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_LICENCAS_A_VENCER, itemLicVen);
		menuRelatorios.add(itemLicVen);

		JMenuItem itemRecPer = MenuFactory.createMenuItem("RECEITA POR PERIODO", null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_RECEITA_POR_PERIODO, itemRecPer);
		menuRelatorios.add(itemRecPer);

		JMenuItem itemRecPla = MenuFactory.createMenuItem("RECEITA POR PLANO", null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_RECEITA_POR_PLANO, itemRecPla);
		menuRelatorios.add(itemRecPla);

		JMenuItem itemEmpPla = MenuFactory.createMenuItem("EMPRESAS POR PLANO", null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_EMPRESAS_POR_PLANO, itemEmpPla);
		menuRelatorios.add(itemEmpPla);

		JMenuItem itemDisLic = MenuFactory.createMenuItem("DISPOSITIVOS POR LICENÇA", null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_DISPOSITIVOS_POR_LICENCA, itemDisLic);
		menuRelatorios.add(itemDisLic);

		JMenuItem itemUsuEmp = MenuFactory.createMenuItem("USUÁRIOS POR EMPRESA", null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_USUARIOS_POR_EMPRESA, itemUsuEmp);
		menuRelatorios.add(itemUsuEmp);

		return menuRelatorios;
	}
}
