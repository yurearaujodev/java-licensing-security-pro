package com.br.yat.gerenciador.util;

import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.UIManager;
import javax.swing.border.Border;
import javax.swing.text.JTextComponent;

public final class ValidationUtils {

	private static final Border BORDA_PADRAO = UIManager.getBorder("TextField.border");
	private static final Border BORDA_ERRO = BorderFactory.createLineBorder(Color.RED, 2);

	private ValidationUtils() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	public static void exibirErro(JTextComponent campo, Component parent, String mensagem) {
		campo.setBorder(BORDA_ERRO);
		DialogFactory.aviso(parent, mensagem);
		campo.requestFocusInWindow();
	}

	public static void removerDestaque(JTextComponent campo) {
		campo.setBorder(BORDA_PADRAO);
	}

	public static boolean hasErroVisual(JTextComponent... campos) {
		for (JTextComponent campo : campos) {
			if (campo.getBorder() == BORDA_ERRO) {
				return true;
			}
		}
		return false;
	}
}
