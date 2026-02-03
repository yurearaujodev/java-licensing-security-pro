package com.br.yat.gerenciador.view.factory;

import java.util.Objects;

import javax.swing.JComboBox;

import com.br.yat.gerenciador.util.UITheme;
/**
 * Classe utilitária para criação de combo boxes Swing.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para criar
 * {@link JComboBox} configurados com estilos visuais definidos em {@link UITheme}.
 * </p>
 * <p>
 * Não de ser instanciada.
 * </p>
 */
public final class ComboBoxFactory {

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private ComboBoxFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Cria um {@link JComboBox} de {@link String} com os itens fornecidos.
	 * 
	 * @param items itens a serem exibidos no combo box
	 * @return uma instância de {@link JComboBox} configurada
	 */
	public static JComboBox<String> createComboBox(String... items) {
		JComboBox<String> combo = new JComboBox<>(items);
		combo.setFont(UITheme.FONT_FIELD);
		return combo;
	}

	/**
	 * Cria um {@link JComboBox} baseado em um {@link Enum}.
	 * <p>
	 * Todos os valores do enum informado serão exibidos como opções.
	 * </p>
	 * 
	 * @param <E> tipo do enum
	 * @param enumClass classe do enum
	 * @return uma instância de {@link JComboBox} configurada com os valores do enum
	 * @throws NullPointerException se {@code enumClass} for nulo
	 */
	public static <E extends Enum<E>> JComboBox<E> createEnumComboBox(Class<E> enumClass) {
		Objects.requireNonNull(enumClass);
		JComboBox<E> combo = new JComboBox<>(enumClass.getEnumConstants());
		combo.setFont(UITheme.FONT_FIELD);
		return combo;
	}

}