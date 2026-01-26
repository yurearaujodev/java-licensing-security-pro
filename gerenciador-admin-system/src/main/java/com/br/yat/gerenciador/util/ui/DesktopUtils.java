package com.br.yat.gerenciador.util.ui;

import java.beans.PropertyVetoException;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public final class DesktopUtils {

	private static final Logger logger = LoggerFactory.getLogger(DesktopUtils.class);

	private DesktopUtils() {
		throw new AssertionError("Classe Utilitária não deve ser instanciada");
	}

	/**
	 * Centraliza um {@link JInternalFrame} dentro de um {@link JDesktopPane}.
	 * <p>
	 * Caso o desktop ou o frame sejam nulos, nenhuma ação é realizada.
	 * </p>
	 * 
	 * @param desktopPane painel desktop onde o frame será centralizado
	 * @param frame       janela interna a ser posicionada
	 */
	public static void centerDesktopPane(JDesktopPane desktopPane, JInternalFrame frame) {
		if (desktopPane == null || frame == null)
			return;

		int x = (desktopPane.getWidth() - frame.getWidth()) / 2;
		int y = (desktopPane.getHeight() - frame.getHeight()) / 2;
		frame.setLocation(Math.max(0, x), Math.max(0, y));
	}

	public static void showFrame(JDesktopPane desktop, JInternalFrame frame) {
		if (desktop == null || frame == null)
			return;

		desktop.add(frame);
		frame.setVisible(true);
		frame.toFront();

		centerDesktopPane(desktop, frame);

		try {
			frame.setSelected(true);
		} catch (PropertyVetoException e) {
			logger.trace("A seleção da janela foi vetada pelo sistema: {}", frame.getTitle());
		}
	}

	/**
	 * Reutiliza uma {@link JInternalFrame} já aberta dentro de um
	 * {@link JDesktopPane}.
	 * <p>
	 * Caso uma instância da classe informada já esteja aberta e não esteja fechada,
	 * ela será trazida para frente, desiconizada (se minimizada) e selecionada.
	 * </p>
	 * 
	 * @param desktopPane painel desktop onde os frames estão abertos
	 * @param frameClass  classe da janela interna a ser reutilizada
	 * @return {@code true} se uma instância existente foi reutilizada,{@code false}
	 *         caso contrário
	 */
	public static boolean reuseIfOpen(JDesktopPane desktopPane, Class<? extends JInternalFrame> frameClass) {
		for (JInternalFrame existing : desktopPane.getAllFrames()) {
			if (frameClass.isInstance(existing) && !existing.isClosed()) {
				try {
					if (existing.isIcon()) {
						existing.setIcon(false);
					}
					existing.setSelected(true);
					existing.moveToFront();
				} catch (PropertyVetoException ignored) {
				}
				return true;
			}
		}
		return false;
	}

	public static boolean reuseIfOpen(JDesktopPane desktopPane, String identifier) {
		for (JInternalFrame existing : desktopPane.getAllFrames()) {
			boolean matches = identifier.equals(existing.getTitle()) || identifier.equals(existing.getName());
			if (!existing.isClosed() && matches) {
				try {
					if (existing.isIcon())
						existing.setIcon(false);
					existing.setSelected(true);
					existing.moveToFront();
					return true;
				} catch (PropertyVetoException ignored) {
				}
			}
		}
		return false;
	}

}
