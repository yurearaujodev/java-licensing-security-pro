package com.br.yat.gerenciador.app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.factory.ViewFactory;
import com.formdev.flatlaf.FlatIntelliJLaf;

public class MainApp {

	public static void main(String[] args) {
		// FlatLightLaf.setup();
		// FlatDarkLaf.setup();
		// FlatIntelliJLaf.setup();
		// FlatDarculaLaf.setup();

		FlatIntelliJLaf.setup();
		UIManager.put("TitlePane.menuBarEmbedded", false);
		JFrame.setDefaultLookAndFeelDecorated(true);

		SwingUtilities.invokeLater(() -> {
			try {
				
				ViewFactory.createMenuPrincipal();
			} catch (Exception e) {
				DialogFactory.erro(null, "ERRO AO ABRIR O MENU: " + e.getMessage());

			}
		});

	}


}
