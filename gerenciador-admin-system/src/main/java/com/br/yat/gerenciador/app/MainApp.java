package com.br.yat.gerenciador.app;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.MenuPrincipal;
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
//			JFrame frame = new JFrame("SISTEMA DE GERENCIAMENTO DE LICENÇA - DADOS CADASTRAIS DA EMPRESA");
//			frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//			frame.setContentPane(new EmpresaView());
//			frame.setSize(650, 550);
//			frame.setLocationRelativeTo(null);
//			frame.setVisible(true);
			try {
				MenuPrincipal menu = new MenuPrincipal();
				menu.setLocationRelativeTo(null);
				menu.setVisible(true);
			} catch (Exception e) {
				DialogFactory.erro(null, "ERRO AO ABRIR O MENU: " + e.getMessage());

			}
		});

	}

}
