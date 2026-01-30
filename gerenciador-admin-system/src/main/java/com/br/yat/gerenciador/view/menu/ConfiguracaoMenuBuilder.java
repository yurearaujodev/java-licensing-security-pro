package com.br.yat.gerenciador.view.menu;

import javax.swing.JMenu;
import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.view.factory.MenuFactory;

public final class ConfiguracaoMenuBuilder {

	private ConfiguracaoMenuBuilder() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	public static JMenu builder() {
		JMenu menuConfiguracao = MenuFactory.createMenu("CONFIGURAÇÃO", null);

		JMenuItem itemEmpFor = MenuFactory.createMenuItem("EMPRESA FORNECEDORA", null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_EMPRESA_FORNECEDORA, itemEmpFor);
		menuConfiguracao.add(itemEmpFor);

		JMenuItem itemParSis = MenuFactory.createMenuItem("PARÂMETRO DO SISTEMA", null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_PARAMETRO_SISTEMA, itemParSis);
		menuConfiguracao.add(itemParSis);

		JMenuItem itemParLic = MenuFactory.createMenuItem("PARÂMETRO DE LICENÇA", null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_PARAMETRO_SISTEMA, itemParLic);
		menuConfiguracao.add(itemParLic);

		JMenu menuSeguranca = MenuFactory.createMenu("SEGURANÇA", null);

		JMenuItem itemPer = MenuFactory.createMenuItem("PERMISSÕES", null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_PERMISSAO, itemPer);
		menuSeguranca.add(itemPer);

		JMenuItem itemUsuPer = MenuFactory.createMenuItem("USUÁRIOS X PERMISSÕES", null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES, itemUsuPer);
		menuSeguranca.add(itemUsuPer);

		menuConfiguracao.add(menuSeguranca);

		JMenu menuManutencao = MenuFactory.createMenu("MANUTENÇÃO", null);

		JMenuItem itemConBan = MenuFactory.createMenuItem("CONEXÃO COM BANCO", null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_CONEXAO_BANCO_DADOS, itemConBan);
		menuManutencao.add(itemConBan);
		
		JMenuItem itemBacDad = MenuFactory.createMenuItem("BACKUP DE DADOS", null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_BACKUP_DE_DADOS, itemBacDad);
		menuManutencao.add(itemBacDad);

		JMenuItem itemResBac = MenuFactory.createMenuItem("RESTAURAR BACKUP", null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_RESTAURAR_BACKUP, itemResBac);
		menuManutencao.add(itemResBac);

		JMenuItem itemLimLog = MenuFactory.createMenuItem("LIMPEZA DE LOGS", null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_LIMPEZA_DE_LOGS, itemLimLog);
		menuManutencao.add(itemLimLog);

		menuConfiguracao.add(menuManutencao);

		JMenuItem itemVerSis = MenuFactory.createMenuItem("VERSÃO DO SISTEMA", null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_VERSAO_DO_SISTEMA, itemVerSis);
		menuConfiguracao.add(itemVerSis);

		return menuConfiguracao;
	}
}
