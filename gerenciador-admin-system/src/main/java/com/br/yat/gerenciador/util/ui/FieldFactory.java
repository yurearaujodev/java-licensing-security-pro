package com.br.yat.gerenciador.util.ui;

import javax.swing.BorderFactory;
import javax.swing.JFormattedTextField;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ScrollPaneConstants;
import javax.swing.SpinnerNumberModel;
import javax.swing.border.EmptyBorder;

import com.br.yat.gerenciador.util.UITheme;
/**
 * Classe utilitária para criação de campos de entrada Swing.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para criar
 * componentes de entrada como {@link JTextField}, {@link JPasswordField}, {@link JTextArea},
 * {@link JScrollPane}, {@link JSpinner}, aplicando estilos definidos em {@link UITheme}.
 * </p> 
 * 
 * <p>
 * Não deve ser instanciada
 * </p>
 */
public final class FieldFactory {

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private FieldFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Cria um {@link JTextField} configurado com número de colunas e fonte padrão.
	 * 
	 * @param columns número de colunas do campo de texto
	 * @return uma instância de {@link JTextField} configurada
	 */
	public static JTextField createTextField(int columns) {
		JTextField field = new JTextField(columns);
		field.setFont(UITheme.FONT_FIELD);
		return field;
	}

	/**
	 * Cria um {@link JFormattedTextField} configurado com fonte padrão.
	 * 
	 * @return uma instância de {@link JFormattedTextField} configurada
	 */
	public static JFormattedTextField createFormattedField() {
		JFormattedTextField field = new JFormattedTextField();
		field.setFont(UITheme.FONT_FIELD);
		return field;

	}

	/**
	 * Cria um {@link JPasswordField} configurada com número de colunas e fonte padrão.
	 * 
	 * @param columns número de colunas do campo de senha
	 * @return uma instância de {@link JPasswordField} configurada
	 */
	public static JPasswordField creadtePasswordField(int columns) {
		JPasswordField field = new JPasswordField(columns);
		field.setFont(UITheme.FONT_FIELD);
		return field;
	}

	/**
	 * Cria um {@link JScrollPane} contendo um {@link JTextArea}.
	 * <p>
	 * O campo de área de texto é configurado com quebra de linha, borda interna e fonte padrão.
	 * </p>
	 * 
	 * @param rows número de linhas visíveis
	 * @param columns número de colunas visíveis
	 * @return uma instância de {@link JScrollPane} contendo a área de texto
	 */
	public static JScrollPane createTextAreaField(int rows, int columns) {
		JTextArea area = new JTextArea(rows, columns);
		area.setFont(UITheme.FONT_FIELD);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setBorder(new EmptyBorder(5, 5, 5, 5));

		JScrollPane scroll = new JScrollPane(area);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		return scroll;
	}
	/**
	 * Cria um {@link JTextArea} configurado com fonte padrão e quebra de linhas.
	 * 
	 * @return uma instância de {@link JTextArea} configurada
	 */
	public static JTextArea createTextArea() {
		JTextArea area = new JTextArea();
		area.setFont(UITheme.FONT_FIELD);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setBorder(new EmptyBorder(5, 5, 5, 5));
		return area;
	}

	/**
	 * Cria um {@link JScrollPane} contendo uma {@link JTextArea}.
	 * 
	 * @param area instância de {@link JTextArea} a ser encapsulada
	 * @return uma instância de {@link JScrollPane} contendo a área de texto
	 */
	public static JScrollPane createTextAreaScroll(JTextArea area) {
		JScrollPane scroll = new JScrollPane(area);
		scroll.setBorder(BorderFactory.createEmptyBorder());
		scroll.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED);

		return scroll;
	}

	/**
	 * Cria um {@link JSpinner} configurado para valores numéricos.
	 * <p>
	 * Utiliza {@link SpinnerNumberModel} para definir valor inicial, mínimo, máximo, incremento.
	 * </p>
	 * 
	 * @param initial valor inicial
	 * @param min valor mínimo
	 * @param max valor máximo
	 * @param step incremento
	 * @return uma instância de {@link JSpinner} configurada
	 */
	public static JSpinner createSpinnerNumber(int initial, int min, int max, int step) {
		SpinnerNumberModel modelo = new SpinnerNumberModel(initial, min, max, step);
		JSpinner spinner = new JSpinner(modelo);
		spinner.setFont(UITheme.FONT_FIELD);

		JSpinner.NumberEditor editor = new JSpinner.NumberEditor(spinner);
		editor.getTextField().setFont(UITheme.FONT_FIELD);
		spinner.setEditor(editor);

		return spinner;
	}

}
