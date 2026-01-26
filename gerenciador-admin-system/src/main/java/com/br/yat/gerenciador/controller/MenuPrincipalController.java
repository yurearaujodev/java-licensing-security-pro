package com.br.yat.gerenciador.controller;

import java.awt.event.ActionListener;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.JDesktopPane;
import javax.swing.JMenuItem;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.util.ui.DesktopUtils;
import com.br.yat.gerenciador.util.ui.ViewFactory;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.MenuPrincipal;
import com.br.yat.gerenciador.view.empresa.EmpresaConsultaView;

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
		configurarAcaoMenu(MenuChave.CADASTROS_EMPRESA_CLIENTE, e -> abrirEmpresaCliente());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_EMPRESA_FORNECEDORA, e -> abrirEmpresaFornecedora());
		configurarAcaoMenu(MenuChave.CONSULTAS_EMPRESAS_CLIENTES, e -> abrirEmpresaConsulta());
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
		if (relogioTimer != null) {
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

	private void abrirEmpresaCliente() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, "SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE CLIENTE")) {
			logger.debug("Janela CLiente reutilizada.");
			return;
		}

		EmpresaView frame = ViewFactory.createEmpresaView(TipoCadastro.CLIENTE);
		frame.setTitle("SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE CLIENTE");
		DesktopUtils.showFrame(desk, frame);
	}

	private void abrirEmpresaFornecedora() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, "SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE FORNECEDORA")) {
			logger.debug("Janela EmpresaView reutilizada.");
			return;
		}

		EmpresaView frame = ViewFactory.createEmpresaView(TipoCadastro.FORNECEDORA);
		frame.setTitle("SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE FORNECEDORA");
		DesktopUtils.showFrame(desk, frame);
	}

	private void abrirEmpresaConsulta() {
		JDesktopPane desk = view.getDesktopPane();

		if (DesktopUtils.reuseIfOpen(desk, EmpresaConsultaView.class)) {
			return;
		}

		EmpresaConsultaView frame = ViewFactory.createEmpresaConsultaView();
		DesktopUtils.showFrame(desk, frame);

	}

}
