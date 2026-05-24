package com.br.yat.gerenciador.service;

import javax.swing.JDesktopPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.view.ConfiguracaoBancoView;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.LogManutencaoView;
import com.br.yat.gerenciador.view.LogSistemaView;
import com.br.yat.gerenciador.view.ParametroSistemaView;
import com.br.yat.gerenciador.view.PerfilConsultaView;
import com.br.yat.gerenciador.view.PreferenciasSistemaView;
import com.br.yat.gerenciador.view.UsuarioConsultaView;
import com.br.yat.gerenciador.view.empresa.EmpresaConsultaView;
import com.br.yat.gerenciador.view.factory.DesktopUtils;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class MenuNavigationService {

	private static final Logger logger = LoggerFactory.getLogger(MenuNavigationService.class);

	private final JDesktopPane desktop;

	public MenuNavigationService(JDesktopPane desktop) {

		this.desktop = desktop;
	}

	public void abrirConfiguracaoBanco() {

		if (DesktopUtils.reuseIfOpen(desktop, ConfiguracaoBancoView.class)) {
			return;
		}

		var frame = ViewFactory.createConfiguracaoBancoView();

		DesktopUtils.showFrame(desktop, frame);
	}

	public void abrirEmpresaCliente() {

		if (DesktopUtils.reuseIfOpen(desktop, "SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE CLIENTE")) {

			logger.debug("JANELA CLIENTE REUTILIZADA.");

			return;
		}

		EmpresaView frame = ViewFactory.createEmpresaView(TipoCadastro.CLIENTE);

		frame.setTitle("SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE CLIENTE");

		DesktopUtils.showFrame(desktop, frame);
	}

	public void abrirEmpresaFornecedora() {

		if (DesktopUtils.reuseIfOpen(desktop, "SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE FORNECEDORA")) {

			logger.debug("Janela EmpresaView reutilizada.");

			return;
		}

		EmpresaView frame = ViewFactory.createEmpresaView(TipoCadastro.FORNECEDORA);

		frame.setTitle("SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE FORNECEDORA");

		DesktopUtils.showFrame(desktop, frame);
	}

	public void abrirEmpresaConsulta() {

		if (DesktopUtils.reuseIfOpen(desktop, EmpresaConsultaView.class)) {
			return;
		}

		var frame = ViewFactory.createEmpresaConsultaView();

		DesktopUtils.showFrame(desktop, frame);
	}

	public void abrirConsultaUsuario() {

		if (DesktopUtils.reuseIfOpen(desktop, UsuarioConsultaView.class)) {
			return;
		}

		var frame = ViewFactory.createUsuarioConsultaView();

		DesktopUtils.showFrame(desktop, frame);
	}

	public void abrirConsultaLogs() {

		if (DesktopUtils.reuseIfOpen(desktop, LogSistemaView.class)) {
			return;
		}

		var frame = ViewFactory.createLogSistemaView();

		DesktopUtils.showFrame(desktop, frame);
	}

	public void abrirParametroSistema() {

		if (DesktopUtils.reuseIfOpen(desktop, ParametroSistemaView.class)) {
			return;
		}

		var frame = ViewFactory.createParametroSistemaView();

		DesktopUtils.showFrame(desktop, frame);
	}

	public void abrirConsultaPerfil() {

		if (DesktopUtils.reuseIfOpen(desktop, PerfilConsultaView.class)) {
			return;
		}

		var frame = ViewFactory.createPerfilConsultaView();

		DesktopUtils.showFrame(desktop, frame);
	}

	public void abrirLogManutencao() {

		if (DesktopUtils.reuseIfOpen(desktop, LogManutencaoView.class)) {
			return;
		}

		var frame = ViewFactory.createLogManutencao();

		DesktopUtils.showFrame(desktop, frame);
	}

	public void abrirConfiguracaoPreferencias() {

		if (DesktopUtils.reuseIfOpen(desktop, PreferenciasSistemaView.class)) {
			return;
		}

		var frame = ViewFactory.createPreferenciasSistemaView();

		DesktopUtils.showFrame(desktop, frame);
	}
}