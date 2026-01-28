package com.br.yat.gerenciador.view.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.view.factory.MenuFactory;

public final class LicencaMenuBuilder {

	private LicencaMenuBuilder() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	public static JMenu builder() {
		JMenu menuLicenca = MenuFactory.createMenu("LICENÇAS", null);

		JMenuItem itemGerLic = MenuFactory.createMenuItem("GERAR LICENÇA", null, null);
		MenuRegistry.register(MenuChave.LICENCAS_GERAR_LICENCA, itemGerLic);
		menuLicenca.add(itemGerLic);

		JMenuItem itemRenLic = MenuFactory.createMenuItem("RENOVAR LICENÇA", null, null);
		MenuRegistry.register(MenuChave.LICENCAS_RENOVAR_LICENCA, itemRenLic);
		menuLicenca.add(itemRenLic);

		JMenuItem itemAtiBlo = MenuFactory.createMenuItem("ATIVAR / BLOQUEAR LICENÇA", null, null);
		MenuRegistry.register(MenuChave.LICENCAS_ATIVAR_BLOQUEAR_LICENCA, itemAtiBlo);
		menuLicenca.add(itemAtiBlo);

		JMenuItem itemDisVin = MenuFactory.createMenuItem("DISPOSITIVOS VINCULADOS", null, null);
		MenuRegistry.register(MenuChave.LICENCAS_DISPOSITIVOS_VINCULADOS, itemDisVin);
		menuLicenca.add(itemDisVin);

		JMenuItem itemHisLic = MenuFactory.createMenuItem("HISTÓRICO DA LICENÇA", null, null);
		MenuRegistry.register(MenuChave.LICENCAS_HISTORICO_DA_LICENCA, itemHisLic);
		menuLicenca.add(itemHisLic);

		return menuLicenca;
	}
}
