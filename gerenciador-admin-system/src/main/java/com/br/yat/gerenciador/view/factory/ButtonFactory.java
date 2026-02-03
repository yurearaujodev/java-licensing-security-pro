
package com.br.yat.gerenciador.view.factory;

import java.awt.Cursor;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JRadioButton;
import javax.swing.SwingConstants;

import com.br.yat.gerenciador.util.UITheme;

/**
 * Classe utilitária para criação de botões Swing.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para
 * criar {@link JButton}, {@link JCheckBox},{@link JRadioButton} e
 * {@link ButtonGroup}, aplicando estilos visuais definidos em {@link UITheme}.
 * </p>
 * 
 * <p>
 * Não deve ser instanciada.
 * </p>
 */
public final class ButtonFactory {

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private ButtonFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Cria um botão primário com texto e ação.
	 * 
	 * @param text  texto exibido no botão
	 * @return uma instância de {@link JButton} configurada
	 */
	public static JButton createPrimaryButton(String text) {
		return createPrimaryButton(text, null);
	}

	/**
	 * Cria um botão primário com texto, ícone e ação.
	 * 
	 * @param text  texto exibido no botão
	 * @param icon  ícone exibido no botão (pode ser {@code null})
	 * @return uma instância de {@link JButton} configurada
	 */
	public static JButton createPrimaryButton(String text, Icon icon) {
		JButton button = new JButton(text);
		button.setFont(UITheme.FONT_BUTTON);
		button.setFocusPainted(false);
		button.setHorizontalTextPosition(SwingConstants.RIGHT);
		button.setVerticalTextPosition(SwingConstants.CENTER);
		button.setIconTextGap(8);
		button.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
		if (icon != null) {
			button.setIcon(icon);
		}
		return button;
	}

	/**
	 * Cria um {@link JCheckBox} configurado com fonte padrão.
	 * 
	 * @param text texto exibido no checkbox
	 * @return uma instância de {@link JCheckBox} configurada
	 */
	public static JCheckBox createCheckBox(String text) {
		JCheckBox jcb = new JCheckBox(text);
		jcb.setFont(UITheme.FONT_FIELD);
		jcb.setOpaque(false);
		return jcb;
	}

	/**
	 * Cria um {@link JRadioButton} configurado com fonte padrão.
	 * 
	 * @param text texto exibido no radio button
	 * @return uma instância de {@link JRadioButton} configurada
	 */
	public static JRadioButton createRadioButton(String text) {
		JRadioButton rb = new JRadioButton(text);
		rb.setFont(UITheme.FONT_FIELD);
		rb.setOpaque(false);
		return rb;
	}

	/**
	 * Cria um {@link ButtonGroup} contendo os radio buttons fornecidos.
	 * 
	 * @param radios radio buttons a serem agrupados
	 * @return uma instância de {{@link ButtonGroup} configurada
	 */
	public static ButtonGroup createRadioGrup(JRadioButton... radios) {
		ButtonGroup group = new ButtonGroup();
		for (JRadioButton rb : radios) {
			group.add(rb);
		}
		return group;
	}

}
