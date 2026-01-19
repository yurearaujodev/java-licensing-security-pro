package com.br.yat.gerenciador.controller;

import java.awt.event.ActionListener;
import java.beans.PropertyVetoException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.util.ui.DesktopFactory;
import com.br.yat.gerenciador.util.ui.ViewFactory;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.MenuPrincipal;

public class MenuPrincipalController {

	private static final Logger logger = LoggerFactory.getLogger(MenuPrincipalController.class);
	private final MenuPrincipal view;
	private Timer relogioTimer;

	public MenuPrincipalController(MenuPrincipal view) {
		this.view = view;
		registrarAcoes();
		iniciarRelogio();
	}

	private void registrarAcoes() {
		configurarAcaoMenu(MenuChave.CADASTROS_EMPRESA_CLIENTE, e-> abrirEmpresaCliente());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_EMPRESA_FORNECEDORA, e->abrirEmpresaFornecedora());
	}

	private void iniciarRelogio() {
		relogioTimer = new Timer(1000, e -> atualizarHora());
		relogioTimer.start();
	}

	private void atualizarHora() {
		LocalDateTime agora = LocalDateTime.now();
		DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss", Locale.getDefault());
		view.getHora().setText(agora.format(formatador));
	}
	
	public void stopRelogio() {
		if (relogioTimer!=null) {
			relogioTimer.stop();
		}
	}

	private void configurarAcaoMenu(MenuChave chave, ActionListener acao) {
		JMenuItem item = MenuRegistry.getItem(chave);
		if (item != null) {
			for (var al : item.getActionListeners()) {
				item.removeActionListener(al);
			}
			item.addActionListener(acao);
		} else {
			logger.warn("Item de menu não encontrado no registro: {}", chave);
		}
	}
	
	private void exibirNoDesktop(JDesktopPane desk, JInternalFrame frame) {
		desk.add(frame);
		frame.setVisible(true);
		frame.toFront();
		
		DesktopFactory.centerDesktopPane(desk, frame);
		
		try {
			frame.setSelected(true);
		} catch (PropertyVetoException e) {
			logger.trace("A seleção da janela foi vetada pelo sistema: {}", frame.getTitle());	
		}
	}

	private void abrirEmpresaCliente() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopFactory.reuseIfOpen(desk, "SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE CLIENTE")) {
			logger.debug("Janela CLiente reutilizada.");
			return;
		}

		EmpresaView frame = ViewFactory.createEmpresaView("CLIENTE");
		frame.setTitle("SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE CLIENTE");
		exibirNoDesktop(desk, frame);
	}
	private void abrirEmpresaFornecedora() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopFactory.reuseIfOpen(desk, "SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE FORNECEDORA")) {
			logger.debug("Janela EmpresaView reutilizada.");
			return;
		}
		
		EmpresaView frame = ViewFactory.createEmpresaView("FORNECEDORA");
		frame.setTitle("SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE FORNECEDORA");
		exibirNoDesktop(desk, frame); 
	}

}
