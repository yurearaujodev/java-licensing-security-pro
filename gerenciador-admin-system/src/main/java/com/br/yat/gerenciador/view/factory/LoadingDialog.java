package com.br.yat.gerenciador.view.factory;

import java.awt.BorderLayout;
import java.awt.Dialog;
import java.awt.Window;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
import javax.swing.JPanel;

public class LoadingDialog {

	private final JDialog dialog;

	public LoadingDialog(Window parent) {
		dialog = new JDialog(parent, "PROCESSANDO", Dialog.ModalityType.APPLICATION_MODAL);
		initComponents();
	}

	private void initComponents() {
		var panel = new JPanel(new BorderLayout(10, 10));
		panel.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

		var label = LabelFactory.createLabel("AGUARDE, PROCESSANDO INFORMAÇÕES...");
		var progressBar = DesktopFactory.createProgressBar();
		progressBar.setIndeterminate(true);

		panel.add(label, BorderLayout.NORTH);
		panel.add(progressBar, BorderLayout.CENTER);

		dialog.add(panel);
		dialog.pack();
		dialog.setLocationRelativeTo(dialog.getParent());
		dialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
	}

	public void show() {
		Thread.ofVirtual().start(() -> dialog.setVisible(true));
	}

	public void hide() {
		dialog.setVisible(false);
	}
}
