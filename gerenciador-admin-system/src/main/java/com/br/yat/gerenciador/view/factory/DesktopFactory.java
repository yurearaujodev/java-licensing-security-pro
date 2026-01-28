package com.br.yat.gerenciador.view.factory;

import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;

import com.br.yat.gerenciador.util.UITheme;

/**
 * Classe utilitária para criação de componentes de interface Swing relacionados
 * a desktop.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para
 * criar {@link JScrollPane}, {@link JSeparator}, {@link JProgressBar},
 * {@link JTabbedPane} e {@link JDesktopPane}, aplicando estilos definidos em
 * {@link UITheme}.
 * </p>
 * 
 * <p>
 * Não deve ser instanciada.
 * </p>
 */
public final class DesktopFactory {

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private DesktopFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Cria um {@link JScrollPane} contendo o componente informado.
	 * <p>
	 * O scroll é configurado sem borda.
	 * </p>
	 * 
	 * @param comp componente a ser encapsulado
	 * @return uma instância de {@link JScrollPane} configurada
	 */
	public static JScrollPane createScroll(Component comp) {
		JScrollPane scroll = new JScrollPane(comp);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.getVerticalScrollBar().setUnitIncrement(16);
		scroll.getHorizontalScrollBar().setUnitIncrement(16);
		return scroll;
	}

	/**
	 * Cria um {@link JSeparator}.
	 * 
	 * @return uma instância de {@link JSeparator}
	 */
	public static JSeparator createSeparator() {
		return new JSeparator();
	}

	/**
	 * Cria um {@link JProgressBar} indeterminado
	 * 
	 * @return uma instância de {@link JProgressBar} configurada
	 */
	public static JProgressBar createProgressBar() {
		JProgressBar barra = new JProgressBar();
		barra.setIndeterminate(true);
		return barra;
	}

	/**
	 * Cria um {@link JTabbedPane} configurado com fonte padrão.
	 * 
	 * @return uma instância de {@link JTabbedPane} configurada
	 */
	public static JTabbedPane createTabbedPane() {
		JTabbedPane tabs = new JTabbedPane(JTabbedPane.TOP);
//		tabs.setTabLayoutPolicy(JTabbedPane.SCROLL_TAB_LAYOUT);
		tabs.setTabLayoutPolicy(JTabbedPane.WRAP_TAB_LAYOUT);
		tabs.setFont(UITheme.FONT_FIELD);
		return tabs;
	}

	/**
	 * Cria um {@link JDesktopPane} configurado com cor de fundo definida em
	 * {@link UITheme}.
	 * 
	 * 
	 * @return uma instância de {@link JDesktopPane} configurada
	 */
	public static JDesktopPane createDesktopPane() {
		JDesktopPane desktopPane = new JDesktopPane();
		desktopPane.setOpaque(true);
		desktopPane.setBackground(UITheme.DESKTOP_BG);
		return desktopPane;
	}

}
