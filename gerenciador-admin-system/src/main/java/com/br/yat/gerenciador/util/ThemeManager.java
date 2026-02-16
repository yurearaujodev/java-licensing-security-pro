package com.br.yat.gerenciador.util;

import java.awt.Color;
import java.awt.Font;
import java.awt.Window;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.br.yat.gerenciador.model.enums.Tema;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class ThemeManager {

	private ThemeManager() {
	}

	public static void aplicarTema(Tema tema, String nomeFonte, int tamanhoFonte, Color corTexto, Color corFundo,
			Color corDestaque) {
		try {
			if (tema == Tema.ESCURO)
				FlatDarkLaf.setup();
			else
				FlatLightLaf.setup();

			if (nomeFonte != null) {
				Font fonte = new Font(nomeFonte, Font.PLAIN, tamanhoFonte);
				UIManager.put("defaultFont", fonte);
			}

			if (corFundo != null) {
				UIManager.put("Panel.background", corFundo);
				UIManager.put("MenuBar.background", corFundo);
				UIManager.put("Menu.background", corFundo);
				UIManager.put("Desktop.background", corFundo);
				UIManager.put("ToolBar.background", corFundo);
			}

			if (corTexto != null) {
				UIManager.put("Label.foreground", corTexto);
				UIManager.put("Menu.foreground", corTexto);
				UIManager.put("MenuItem.foreground", corTexto);
				UIManager.put("MenuBar.foreground", corTexto);
			}

			// Atualiza todas as janelas abertas
			for (Window w : Window.getWindows()) {
				SwingUtilities.updateComponentTreeUI(w);
			}

		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
