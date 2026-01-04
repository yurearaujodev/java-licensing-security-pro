package com.br.yat.gerenciador.util.ui;

import javax.swing.Icon;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

import com.br.yat.gerenciador.util.UITheme;
/**
 * Classe utilitária para criação de labels Swing.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para criar
 * {@link JLabel} com diferentes estilos visuais, aplicando fontes e cores definidos em {@link UIThme}.
 * </p>
 * 
 * <p>
 * Não pode ser instanciada.
 * </p>
 */
public final class LabelFactory {

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private LabelFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Cria um {@link JLabel} padrão com fonte e cor definidas em {@link UITheme}.
	 * 
	 * @param text texto exibido no label
	 * @return uma instância de {@link JLabel} configurada
	 */
	public static JLabel createLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(UITheme.FONT_DEFAULT);
		label.setForeground(UITheme.FG_DEFAULT);
		return label;
	}

	/**
	 * Cria um {@link JLabel} estilizado com título.
	 * <p>
	 * Aplica fonte e cor de título definidos em {@link UITheme}.
	 * </p>
	 * 
	 * @param text texto exibido no label
	 * @return uma instância de {@link JLabel} configurada com título
	 */
	public static JLabel createTitleLabel(String text) {
		JLabel label = new JLabel(text);
		label.setFont(UITheme.FONT_TITLE);
		label.setForeground(UITheme.FG_TITLE);
		return label;
	}

	/**
	 * Cria um {@link JLabel} com texto e ícone.
	 * <p>
	 * O texto é exibido abaixo do ícone, centralizado horizontalmente.
	 * </p>
	 * 
	 * @param text texto exibido no label
	 * @param icon ícone exibido no label (pode ser {@code null})
	 * @return uma instância de {@link JLabel} configurada com ícone e texto
	 */
	public static JLabel createImageLabel(String text, Icon icon) {
		JLabel label = createLabel(text);
		if (icon != null) {
			label.setIcon(icon);
		}
		label.setHorizontalTextPosition(SwingConstants.CENTER);
		label.setVerticalTextPosition(SwingConstants.BOTTOM);
		label.setIconTextGap(15);
		return label;
	}

}
