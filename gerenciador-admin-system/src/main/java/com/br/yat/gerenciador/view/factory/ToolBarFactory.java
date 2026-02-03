package com.br.yat.gerenciador.view.factory;

import java.awt.Cursor;
import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JToolBar;

import com.br.yat.gerenciador.util.UITheme;
/**
 * Classe utilitária para criação de componentes de barra de ferramentas (toolbar) em Swing.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para criar e configurar
 * {@link JToolBar} e {@link JButton}, aplicando estilos definidos em {@link UITheme}.
 * </p>
 * 
 * <p>
 * Não deve ser instanciada
 * </p>
 */
public final class ToolBarFactory {
	/**
	 * Construtor privado para evitar instanciação.
	 */
	private ToolBarFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}
	/**
	 * Cria uma {@link JToolBar} configurada com estilo padrão.
	 * <p>
	 * A barra não é flutuante e recebe a cor de fundo definida em {@link UIThme}.
	 * </p>
	 * 
	 * @return uma instância de {@link JToolBar} configurada
	 */
	public static JToolBar createToolBar() {
		JToolBar toolBar = new JToolBar();
		toolBar.setFloatable(false);
		toolBar.setBackground(UITheme.TOOLBAR_BG);
		return toolBar;
	}
	/**
	 * Cria um {@link JButton} para ser utilizada em uma {@link JToolBar}.
	 * <p>
	 * O botão recebe ícone, tooltip, cursor em formato de mão e ação opcional.
	 * </p>
	 * 
	 * @param tooltip texto de ajuda exibido ao passar o mouse
	 * @param icon ícone exibido no botão
	 * @param action ação a ser executada ao clicar no botão (pode ser {@code null})
	 * @return uma instância de {@link JButton} configurada
	 */
	public static JButton createToolBarButton(String tooltip, Icon icon, ActionListener action) {
		JButton button = new JButton(icon);
		button.setToolTipText(tooltip);
		button.setFocusPainted(false);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		if (action != null) {
			button.addActionListener(action);
		}
		return button;
	}

}