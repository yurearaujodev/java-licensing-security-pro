package com.br.yat.gerenciador.util;

import java.awt.Component;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
/**
 * Classe utilitária para exibição de diálogos Swing com integração de logging.
 * <p>
 * Responsável por:
 * <ul>
 * <li>Exibir mensagens de informação, aviso e erro.</li>
 * <li>Registrar logs correspondentes via SLF#J.</li>
 * <li>Exibir diálogos de confirmação com retorno booleano.</li>
 * </ul>
 * </p>
 * 
 * <p>Não deve ser instanciada.</p>
 */
public final class DialogFactory {

	private static final Logger logger = LoggerFactory.getLogger(DialogFactory.class);

	/**
	 * Construtor privado para evitar instanciação.
	 */
	private DialogFactory() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Exibe um diálogo de informação.
	 * 
	 * @param parent componente pai da janela
	 * @param msg mensagem a ser exibida
	 */
	public static void informacao(Component parent, String msg) {
		JOptionPane.showMessageDialog(parent, msg, "INFORMAÇÃO", JOptionPane.INFORMATION_MESSAGE);
		logger.info(msg);
	}
	
	/**
	 * Exibe um diálogo de aviso
	 * 
	 * @param parent componente pai da janela
	 * @param msg mensagem a ser exibida
	 */
	public static void aviso(Component parent, String msg) {
		JOptionPane.showMessageDialog(parent, msg, "AVISO", JOptionPane.WARNING_MESSAGE);
		logger.warn(msg);
	}

	/**
	 * Exibe um diálogo de erro.
	 * 
	 * @param parent componente pai da janela
	 * @param msg mensagem a ser exibida
	 */
	public static void erro(Component parent, String msg) {
		JOptionPane.showMessageDialog(parent, msg, "ERRO", JOptionPane.ERROR_MESSAGE);
		logger.error(msg);
	}

	/**
	 * Exibe um diálogo de erro com exceção associada.
	 * 
	 * @param parent componente pai da janela
	 * @param msg mensagem a ser exibida
	 * @param t exceção associada
	 */
	public static void erro(Component parent, String msg, Throwable t) {
		JOptionPane.showMessageDialog(parent, msg, "ERRO", JOptionPane.ERROR_MESSAGE);
		logger.error(msg, t);
	}

	/**
	 * Exibe um diálogo de confirmação (Sim/Não).
	 * 
	 * @param parent componente pai da janela
	 * @param msg mensagem a ser exibida
	 * @return {@code true} se usuário confirmar (Sim), {@code false} caso contrário
	 */
	public static boolean confirmacao(Component parent, String msg) {
		boolean result = JOptionPane.showConfirmDialog(parent, msg, "CONFIRMAÇÃO",
				JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION;
		logger.debug("CONFIRMAÇÃO EXIBIDA: " + msg + " | RESPOSTA: " + result);
		return result;
	}

}
