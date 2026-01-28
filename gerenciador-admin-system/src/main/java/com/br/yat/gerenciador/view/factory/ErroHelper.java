package com.br.yat.gerenciador.view.factory;

import java.awt.Component;

import javax.swing.text.JTextComponent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.security.PasswordUtils;
import com.br.yat.gerenciador.util.ValidationUtils;

public final class ErroHelper {
	
	private static final Logger logger = LoggerFactory.getLogger(PasswordUtils.class);
	
	private ErroHelper() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
		}
	
	public static void mostrarErro(JTextComponent campo, Component view, String mensagem) {
		if (!ValidationUtils.isHighLighted(campo)) {
	//		ValidationUtils.exibirErro(campo, view, mensagem);
			logger.warn("ERRO EXIBIDO NO CAMPO "+ campo.getName());
		}
	}
	
	public static void limparErro(JTextComponent campo) {
		ValidationUtils.removerDestaque(campo);
		logger.info("ERRO REMOVIDO DO CAMPO: "+campo.getName());
	}

}
