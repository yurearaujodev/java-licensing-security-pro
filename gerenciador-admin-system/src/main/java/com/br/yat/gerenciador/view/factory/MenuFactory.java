package com.br.yat.gerenciador.view.factory;

import javax.swing.Icon;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.KeyStroke;

import com.br.yat.gerenciador.util.UITheme;

import net.miginfocom.swing.MigLayout;
/**
 * Classe utilitária para criação de menus Swing.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para criar
 * {@link JMenuBar}, {@link JMenu}, {@link JMenuItem} e {@link JPanel}, aplicando estilos
 * definidos em {@link UITheme}. Também utiliza a biblioteca <b>MigLayout</b> ({@code net.miginfocom.swing.MigLayout})
 * para organizar o painel que contém a barra de menus.
 * </p>
 * 
 * <p>
 * Não pode ser instanciada.
 * </p>
 */
public final class MenuFactory {
	/**
	 * Construtor privado para evitar instanciação.
	 */
	private MenuFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Cria uma {@link JMenuBar} configurada com estilo padrão.
	 * 
	 * @return uma instância de {@link JMenuBar} configurada
	 */
	public static JMenuBar createMenuBar() {
		JMenuBar menuBar = new JMenuBar();
		menuBar.setOpaque(true);
		menuBar.setBackground(UITheme.MENU_BG);
		menuBar.setForeground(UITheme.MENU_FG);
		return menuBar;
	}

	/**
	 * Cria um {@link JMenu} configurado com texto e ícone opcionais.
	 * 
	 * @param text texto exibido no menu
	 * @param icon ícone exibido no menu (pode ser {@code null})
	 * @return uma instância de {@link JMenu} configurada
	 */
	public static JMenu createMenu(String text, Icon icon) {
		JMenu menu = new JMenu(text);
		menu.setFont(UITheme.FONT_MENU);
		menu.setForeground(UITheme.MENU_FG);
		if (icon != null) {
			menu.setIcon(icon);
		}
		return menu;
	}

	/**
	 * Cria um {@link JMenuItem} configurada com texto, ícone, atalho.
	 * 
	 * @param text texto exibido no item de menu
	 * @param icon ícone exibido no item de menu (pode ser {@code null})
	 * @param key atalho a ser executada ao clicar no item (pode ser {@code null})
	 * @return uma instância de {@link JMenuItem} configurada
	 */
	public static JMenuItem createMenuItem(String text, Icon icon, KeyStroke key) {
		JMenuItem item = new JMenuItem(text);
		item.setFont(UITheme.FONT_MENU_ITEM);
		if (icon != null) {
			item.setIcon(icon);
		}
		if (key != null) {
			item.setAccelerator(key);
		}
		return item;
	}

	/**
	 * Cria um {@link JPanel} contendo uma {@link JMenuBar}, utilizando {@link MigLayout}.
	 * 
	 * @param menuBar instância de {@link JMenuBar} a ser adicionada ao painel
	 * @return uma instância de {@link JPanel} contendo a barra de menus
	 */
	public static JPanel createMenuPanel(JMenuBar menuBar) {
		JPanel painel = new JPanel(new MigLayout("fill,insets 0", "[grow]", "[]"));
		painel.add(menuBar, "growx");
		return painel;
	}

}
