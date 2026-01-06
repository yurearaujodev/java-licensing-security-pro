package com.br.yat.gerenciador.view.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.util.ui.MenuFactory;

public final class FinanceiroMenuBuilder {

	private FinanceiroMenuBuilder() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	public static JMenu builder() {
		JMenu menuFinanceiro = MenuFactory.createMenu("FINANCEIRO", null);

		JMenuItem itemRegPag = MenuFactory.createMenuItem("REGISTRAR PAGAMENTO", null, null);
		MenuRegistry.register(MenuChave.FINANCEIRO_REGISTRAR_PAGAMENTO, itemRegPag);
		menuFinanceiro.add(itemRegPag);

		JMenuItem itemConPg = MenuFactory.createMenuItem("CONSULTAR PAGAMENTOS", null, null);
		MenuRegistry.register(MenuChave.FINANCEIRO_CONSULTAR_PAGAMENTOS, itemConPg);
		menuFinanceiro.add(itemConPg);

		JMenuItem itemFatRec = MenuFactory.createMenuItem("FATURAMENTO / RECEITA", null, null);
		MenuRegistry.register(MenuChave.FINANCEIRO_FATURAMENTO_RECEITA, itemFatRec);
		menuFinanceiro.add(itemFatRec);

		JMenuItem itemRel = MenuFactory.createMenuItem("RELATÓRIOS", null, null);
		MenuRegistry.register(MenuChave.FINANCEIRO_RELATORIOS, itemRel);
		menuFinanceiro.add(itemRel);

		return menuFinanceiro;
	}
}
