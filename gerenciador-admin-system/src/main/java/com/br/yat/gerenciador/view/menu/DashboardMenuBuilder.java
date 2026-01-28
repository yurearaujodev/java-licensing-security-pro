package com.br.yat.gerenciador.view.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.view.factory.MenuFactory;

public final class DashboardMenuBuilder {
	
	private DashboardMenuBuilder() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}
	
	public static JMenu builder() {
		JMenu menuDashboard = MenuFactory.createMenu("DASHBOARD", null);

		JMenuItem itemGeral = MenuFactory.createMenuItem("VISÃO GERAL", null, null);
		MenuRegistry.register(MenuChave.DASHBOARD_GERAL, itemGeral);
		menuDashboard.add(itemGeral);

		JMenu menuAlertas = MenuFactory.createMenu("ALERTAS", null);

		JMenuItem itemLicVenc = MenuFactory.createMenuItem("LICENÇAS A VENCER", null, null);
		MenuRegistry.register(MenuChave.DASHBOARD_LICENCAS_A_VENCER, itemLicVenc);
		menuAlertas.add(itemLicVenc);

		JMenuItem itemLicBlo = MenuFactory.createMenuItem("LICENÇAS BLOQUEADAS", null, null);
		MenuRegistry.register(MenuChave.DASHBOARD_LICENCAS_BLOQUEADAS, itemLicBlo);
		menuAlertas.add(itemLicBlo);

		JMenuItem itemPagPen = MenuFactory.createMenuItem("PAGAMENTOS PENDENTES", null, null);
		MenuRegistry.register(MenuChave.DASHBOARD_PAGAMENTOS_PENDENTES, itemPagPen);
		menuAlertas.add(itemPagPen);

		menuDashboard.add(menuAlertas);

		return menuDashboard;
	}

}
