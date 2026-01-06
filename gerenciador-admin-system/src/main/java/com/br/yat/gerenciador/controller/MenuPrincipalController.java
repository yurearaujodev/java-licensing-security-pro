package com.br.yat.gerenciador.controller;

import javax.swing.JDesktopPane;
import javax.swing.JMenuItem;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.util.ui.DesktopFactory;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.MenuPrincipal;

public class MenuPrincipalController {

	private final MenuPrincipal view;

	public MenuPrincipalController(MenuPrincipal view) {
		this.view = view;
		
		registrarAcoes();
	}

	private void registrarAcoes() {
		JMenuItem itemEmpresa = MenuRegistry.getItem(MenuChave.CADASTROS_EMPRESA_CLIENTE);
		if (itemEmpresa != null) {
			itemEmpresa.addActionListener(e-> abrirEmpresa());
		}
	}

	private void abrirEmpresa() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopFactory.reuseIfOpen(desk, EmpresaView.class)) {
			return;
		}
		
		EmpresaView frame = new EmpresaView();
		desk.add(frame);
		
		frame.setVisible(true);
		frame.toFront();
		DesktopFactory.centerDesktopPane(desk, frame);
		try {
			frame.setSelected(true);
		} catch (Exception ignored) {
			// TODO: handle exception
		}
		//DialogFactory.informacao(frame, "deu certo");
	}

}
