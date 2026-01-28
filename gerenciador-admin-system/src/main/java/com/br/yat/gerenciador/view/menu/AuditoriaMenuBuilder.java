package com.br.yat.gerenciador.view.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.view.factory.MenuFactory;

public final class AuditoriaMenuBuilder {

	private AuditoriaMenuBuilder() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}
	
	public static JMenu builder() {
		JMenu menuAuditoria = MenuFactory.createMenu("AUDITORIA", null);

		JMenuItem itemLogSis = MenuFactory.createMenuItem("LOG DO SISTEMA", null, null);
		MenuRegistry.register(MenuChave.AUDITORIA_LOG_DO_SISTEMA, itemLogSis);
		menuAuditoria.add(itemLogSis);

		JMenuItem itemHisLic = MenuFactory.createMenuItem("HISTÓRICO DE LICENÇAS", null, null);
		MenuRegistry.register(MenuChave.AUDITORIA_HISTORICO_DE_LICENCAS, itemHisLic);
		menuAuditoria.add(itemHisLic);

		return menuAuditoria;
	}

}
