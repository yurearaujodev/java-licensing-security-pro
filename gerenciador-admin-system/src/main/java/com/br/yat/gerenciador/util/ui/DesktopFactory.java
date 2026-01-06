package com.br.yat.gerenciador.util.ui;

import java.awt.Component;
import java.beans.PropertyVetoException;

import javax.swing.BorderFactory;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
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
		JTabbedPane tabs = new JTabbedPane();
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

	/**
	 * Centraliza um {@link JInternalFrame} dentro de um {@link JDesktopPane}.
	 * <p>
	 * Caso o desktop ou o frame sejam nulos, nenhuma ação é realizada.
	 * </p>
	 * 
	 * @param desktopPane painel desktop onde o frame será centralizado
	 * @param frame janela interna a ser posicionada
	 */
	public static void centerDesktopPane(JDesktopPane desktopPane, JInternalFrame frame) {
		if (desktopPane == null || frame == null) {
			return;
		}

		int x = (desktopPane.getWidth() - frame.getWidth()) / 2;
		int y = (desktopPane.getHeight() - frame.getHeight()) / 2;
		frame.setLocation(Math.max(0, x), Math.max(0, y));
	}

	/**
	 * Reutiliza uma {@link JInternalFrame} já aberta dentro de um {@link JDesktopPane}.
	 * <p>
	 * Caso uma instância da classe informada já esteja aberta e não esteja fechada,
	 * ela será trazida para frente, desiconizada (se minimizada) e selecionada.
	 * </p>
	 * 
	 * @param desktopPane painel desktop onde os frames estão abertos
	 * @param frameClass classe da janela interna a ser reutilizada
	 * @return {@code true} se uma instância existente foi reutilizada,{@code false} caso contrário
	 */
	public static boolean reuseIfOpen(JDesktopPane desktopPane, Class<? extends JInternalFrame> frameClass) {
		for (JInternalFrame existing : desktopPane.getAllFrames()) {
			if (frameClass.isInstance(existing) && !existing.isClosed()) {
				try {
					if (existing.isIcon()) {
						existing.setIcon(false);
					}
					existing.setSelected(true);
					existing.moveToFront();
				} catch (PropertyVetoException ignored) {
				}
				return true;
			}
		}
		return false;
	}

}
