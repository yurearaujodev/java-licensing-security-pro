package com.br.yat.gerenciador.util.ui;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Locale;

import javax.swing.JFormattedTextField;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.MaskFormatter;
import javax.swing.text.NumberFormatter;

import com.br.yat.gerenciador.util.ValidationUtils;

/**
 * Classe utilitária para aplicação de máscaras em campos formatados Swing.
 * <p>
 * Esta classe utiliza a biblioteca <b>Swing</b> ({@code javax.swing}) para
 * manipulação de {@link JFormattedTextField}, juntamente com classes de
 * formatação de {@code java.text} como {@link MaskFormatter},
 * {@link NumberFormatter} e {@link DecimalFormat}.
 * </p>
 * 
 * <p>
 * Não deve ser instanciada.
 * </p>
 */
public final class FormatterUtils {

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private FormatterUtils() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Aplica uma máscara genérica a um {@link JFormattedTextField}.
	 * 
	 * @param field             campo formatado a ser configurado
	 * @param mask              padrão da máscara (ex.: "###.###.###-##")
	 * @param literalCharacters define se os caracteres literais devem ser
	 *                          considerados no valor
	 */
	private static void applyMask(JFormattedTextField field, String mask, boolean literalCharacters) {
		if (ValidationUtils.isEmpty(mask)) {
			field.setValue(null);
			field.setText("");
			return;
		}

		try {
			MaskFormatter mf = new MaskFormatter(mask);
			mf.setPlaceholderCharacter('_');
			mf.setValueContainsLiteralCharacters(literalCharacters);

			field.setFormatterFactory(new DefaultFormatterFactory(mf));
			field.setFocusLostBehavior(JFormattedTextField.COMMIT);
			field.setValue(null);
			field.setCaretPosition(0);
		} catch (ParseException e) {
			throw new IllegalArgumentException("MÁSCARA INVÁLIDA: " + mask, e);
		}

	}

	public static void clearMask(JFormattedTextField field) {
		if (field == null)
			return;
		field.setValue(null);
		field.setFormatterFactory(null);
		field.setText("");
	}

	/**
	 * Aplica máscara para documentos (ex.CPF, CNPJ).
	 * 
	 * @param field campo formatado
	 * @param mask  padrão da máscara
	 */
	public static void applyDocumentMask(JFormattedTextField field, String mask) {
		applyMask(field, mask, false);
	}

	/**
	 * Aplica máscara para números de telefone.
	 * 
	 * @param field campo formatado
	 * @param mask  padrão da máscara
	 */
	public static void applyPhoneMask(JFormattedTextField field, String mask) {
		applyMask(field, mask, true);
	}

	/**
	 * Aplica máscara para datas.
	 * 
	 * @param field campo formatado
	 * @param mask  padrão da máscara
	 */
	public static void applyDateMask(JFormattedTextField field, String mask) {
		applyMask(field, mask, true);
	}

	/**
	 * Aplica máscara para CEP (código postal)
	 * 
	 * @param field campo formatado
	 * @param mask  padrão da máscara
	 */
	public static void applyPostalCodeMask(JFormattedTextField field, String mask) {
		applyMask(field, mask, true);
	}

	public static void applyBank(JFormattedTextField field, String mask) {
		applyMask(field, mask, true);
	}

	/**
	 * Aplica formatação numérica para valores monetários ou capital
	 * <p>
	 * Utiliza {@link DecimalFormat} em locale {@code pt-BR}.
	 * </p>
	 * 
	 * @param field   campo formatado
	 * @param pattern padrão de formatação (ex.: "#,##0.00)
	 */
	public static void applyCapitalMask(JFormattedTextField field, String pattern) {
		if (ValidationUtils.isEmpty(pattern)) {
			return;
		}

		DecimalFormat formato = (DecimalFormat) NumberFormat.getNumberInstance(Locale.forLanguageTag("pt-BR"));
		formato.applyPattern(pattern);

		NumberFormatter nf = new NumberFormatter(formato);
		nf.setAllowsInvalid(true);
		nf.setOverwriteMode(false);
		nf.setMinimum(0.00);
		nf.setMaximum(Double.MAX_VALUE);

		field.setFormatterFactory(new DefaultFormatterFactory(nf));
		field.setFocusLostBehavior(JFormattedTextField.COMMIT);
	}
	
	public static String formatValueWithMask(String value, String mask) {
		if(ValidationUtils.isEmpty(value))return "";
		try {
			MaskFormatter mf = new MaskFormatter(mask);
			mf.setValueContainsLiteralCharacters(false);
			return mf.valueToString(value);
		} catch (ParseException e) {
			return value;
		}
	}

}
