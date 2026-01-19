package com.br.yat.gerenciador.util;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;

import javax.swing.BorderFactory;
import javax.swing.JComboBox;
import javax.swing.JComponent;
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
			case JTextComponent txt -> txt.getText();
			case JComboBox<?> combo -> (combo.getSelectedItem() == null ? "" : combo.getSelectedItem().toString());
			case null, default -> "";
			};

			if (isEmpty(valor)) {
				exibirErro(campo, "CAMPO OBRIGATÓRIO.");
				erro = true;
			} else {
				removerDestaque(campo);
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

}
