package com.br.yat.gerenciador.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.math.BigDecimal;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.time.format.ResolverStyle;
import java.util.Base64;
import java.util.Locale;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JSpinner;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DocumentFilter;
import javax.swing.text.JTextComponent;

public final class ValidationUtils {

	private static final Border BORDA_PADRAO = UIManager.getBorder("TextField.border");
	private static final Border BORDA_ERRO = BorderFactory.createLineBorder(Color.RED, 2);
	private static final DateTimeFormatter DATE_FORMATTER = new DateTimeFormatterBuilder().appendPattern("dd/MM/uuuu")
			.toFormatter().withResolverStyle(ResolverStyle.STRICT);
	private static final Locale LOCALE_BR = Locale.forLanguageTag("pt-BR");
	private static final DecimalFormat DECIMAL_FORMATTER;

	static {
		var symbols = DecimalFormatSymbols.getInstance(LOCALE_BR);
		DECIMAL_FORMATTER = new DecimalFormat("#,##0.00", symbols);
		DECIMAL_FORMATTER.setParseBigDecimal(true);
	}

	private ValidationUtils() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	public static boolean isEmpty(String value) {
		return value == null || value.isBlank();
	}

	public static String onlyNumbers(String value) {
		if (isEmpty(value))
			return "";
		return value.replaceAll("\\D", "");
	}

	public static void resetForm(JComponent... campos) {
		for (JComponent campo : campos) {
			if (campo == null)
				continue;

			removerDestaque(campo);
			campo.setToolTipText(null);

			switch (campo) {
			case JTextComponent txt -> txt.setText("");
			case JComboBox<?> combo -> combo.setSelectedIndex(-1);
			default -> {
				/* Outros componenetes se necessário. */}
			}
		}
	}

	public static boolean temCamposVazios(JComponent... campos) {
		boolean erro = false;
		for (JComponent campo : campos) {
			String valor = switch (campo) {
			case JFormattedTextField ftxt -> ftxt.getText();
			case JTextComponent txt -> txt.getText().trim();
			case JComboBox<?> combo -> {
				Object selected = combo.getSelectedItem();
				if (selected == null || "SELECIONE".equals(selected.toString().toUpperCase())) {
					yield "";
				}
				yield selected.toString().trim();
			}
			case JSpinner spinner -> {
				if (spinner.getEditor() instanceof JSpinner.DefaultEditor def) {
					yield def.getTextField().getText().trim();
				}
				yield "";
			}
			case null, default -> "";
			};

			if (isEmpty(valor)) {
				exibirErro(campo, "CAMPO OBRIGATÓRIO.");
				erro = true;
			} else {
				if (!isHighLighted(campo)) {
					removerDestaque(campo);
				}
			}

		}
		return erro;
	}

	public static void exibirErro(JComponent campo, String msg) {
		if (campo == null)
			return;
		campo.setBorder(BORDA_ERRO);
		if (campo instanceof JTextComponent txt) {
			txt.setToolTipText(msg);
		}
	}

	public static void removerDestaque(JComponent campo) {
		if (campo == null)
			return;
		campo.setBorder(BORDA_PADRAO);
		if (campo instanceof JTextComponent txt) {
			txt.setToolTipText("");
		}
	}

	public static void removerDestaque(JComponent... campos) {
		if (campos == null)
			return;
		for (JComponent campo : campos) {
			if (campo == null)
				continue;
			campo.setBorder(BORDA_PADRAO);

			if (campo instanceof JTextComponent txt) {
				txt.setToolTipText("");
			}
		}
	}

	public static boolean isHighLighted(JComponent campo) {
		return campo.getBorder() == BORDA_ERRO;
	}

	public static JComponent hasErroVisual(JComponent... campos) {
		for (JComponent campo : campos) {
			if (isHighLighted(campo)) {
				campo.requestFocusInWindow();
				return campo;
			}
		}
		return null;
	}

	public static FocusAdapter createValidationListener(JTextComponent campo, Runnable validator) {
		if (campo == null || validator == null) {
			throw new IllegalArgumentException("CAMPO E VALIDATOR NÂO PODE SER NULL");
		}

		return new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				removerDestaque(campo);
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (e.isTemporary())
					return;
				if (campo.getText() != null) {
					campo.setText(campo.getText().trim());
				}
				validator.run();
			}
		};
	}

	public static FocusAdapter createValidationListener(JComponent campo, Runnable validator) {
		if (campo == null || validator == null) {
			throw new IllegalArgumentException("CAMPO E VALIDATOR NÂO PODE SER NULL");
		}

		return new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e) {
				removerDestaque(campo);
			}

			@Override
			public void focusLost(FocusEvent e) {
				if (e.isTemporary())
					return;
				if (campo instanceof JComboBox<?> combo) {
					Component editor = combo.getEditor().getEditorComponent();
					if (editor instanceof JTextComponent txt) {
						txt.setText(txt.getText().trim());
					}
				}
				validator.run();
			}
		};
	}

	public static DocumentListener createDocumentListener(JTextComponent campo, Runnable validator) {
		if (campo == null || validator == null) {
			throw new IllegalArgumentException("CAMPO E VALIDATOR NÂO PODE SER NULL");
		}

		return new DocumentListener() {

			@Override
			public void removeUpdate(DocumentEvent e) {
				validator.run();
			}

			@Override
			public void insertUpdate(DocumentEvent e) {
				validator.run();
			}

			@Override
			public void changedUpdate(DocumentEvent e) {
				validator.run();
			}
		};
	}
	
	public static DocumentListener createDocumentListener(Runnable validator) {
	    if (validator == null) {
	        throw new IllegalArgumentException("VALIDATOR NÃO PODE SER NULL");
	    }

	    return new DocumentListener() {
	        @Override
	        public void removeUpdate(DocumentEvent e) { validator.run(); }
	        @Override
	        public void insertUpdate(DocumentEvent e) { validator.run(); }
	        @Override
	        public void changedUpdate(DocumentEvent e) { validator.run(); }
	    };
	}

	public static void createDocumentFilter(JTextComponent... campos) {
		if (campos == null)
			return;
		DocumentFilter filtro = new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
					throws BadLocationException {
				if (string != null) {
					fb.insertString(offset, string.toUpperCase(), attr);
				}
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				if (text != null) {
					fb.replace(offset, length, text.toUpperCase(), attrs);
				}
			}

		};
		for (JTextComponent campo : campos) {
			if (campo != null) {
				((AbstractDocument) campo.getDocument()).setDocumentFilter(filtro);
			}
		}
	}

	public static void createDocumentFilterNumeric(JTextComponent... campos) {
		if (campos == null)
			return;
		DocumentFilter filtro = new DocumentFilter() {
			@Override
			public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
					throws BadLocationException {
				if (string != null) {
					String somenteNumeros = string.replaceAll("\\D", "");
					if (!somenteNumeros.isBlank()) {
						super.insertString(fb, offset, somenteNumeros, attr);
					}
				}
			}

			@Override
			public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
					throws BadLocationException {
				if (text != null) {
					String somenteNumeros = text.replaceAll("\\D", "");
					if (!somenteNumeros.isBlank()) {
						super.replace(fb, offset, length, somenteNumeros, attrs);
					}
				}
			}

		};
		for (JTextComponent campo : campos) {
			if (campo != null) {
				((AbstractDocument) campo.getDocument()).setDocumentFilter(filtro);
			}
		}
	}

	public static int parseInt(String valor) {
		if (isEmpty(valor))
			return 0;
		try {
			return Integer.parseInt(valor.replaceAll("\\D", ""));
		} catch (NumberFormatException e) {
			return 0;
		}
	}

	public static LocalDate parseDate(String valor) {
		if (isEmpty(valor) || valor.contains("_"))
			return null;
		try {
			return LocalDate.parse(valor, DATE_FORMATTER);
		} catch (Exception e) {
			return null;
		}
	}

	public static BigDecimal parseBigDecimal(String valor) {
		if (isEmpty(valor))
			return null;
		try {
			return (BigDecimal) DECIMAL_FORMATTER.parse(valor.trim());
		} catch (Exception e) {
			return null;
		}
	}

	public static String formatBigDecimal(BigDecimal valor) {
		return (valor != null) ? DECIMAL_FORMATTER.format(valor) : "";
	}

	public static String formatDate(LocalDate data) {
		return (data != null) ? data.format(DATE_FORMATTER) : "";
	}
	
	public static boolean isBase64(String value) {
	    if (isEmpty(value)) {
	        return false;
	    }
	    try {
	        Base64.getDecoder().decode(value.trim());
	        return true;
	    } catch (IllegalArgumentException e) {
	        return false;
	    }
	}
	
	public static int calcularForcaSenha(char[] senha) {
	    if (senha == null || senha.length == 0) return 0;

	    int score = senha.length >= 6 ? 1 : 0;

	    boolean temMaiuscula = false;
	    boolean temNumero = false;
	    boolean temEspecial = false;

	    for (char c : senha) {
	        temMaiuscula |= Character.isUpperCase(c);
	        temNumero    |= Character.isDigit(c);
	        temEspecial  |= isEspecial(c);

	        if (temMaiuscula && temNumero && temEspecial) {
	            break; // otimização
	        }
	    }

	    score += (temMaiuscula ? 1 : 0)
	           + (temNumero ? 1 : 0)
	           + (temEspecial ? 1 : 0);

	    return score; // 0 a 4
	}

	private static boolean isEspecial(char c) {
	    return "!@#$%^&*(),.?\":{}|<>".indexOf(c) >= 0;
	}

}
