package com.br.yat.gerenciador.controller;

import java.awt.AWTEvent;
import java.awt.Toolkit;
import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.swing.Icon;
import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.JMenuItem;
import javax.swing.SwingUtilities;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.empresa.ComplementarDao;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.util.AppEventManager;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.util.TimeUtils;
import com.br.yat.gerenciador.view.ConfiguracaoBancoView;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.LogManutencaoView;
import com.br.yat.gerenciador.view.LogSistemaView;
import com.br.yat.gerenciador.view.MenuPrincipal;
import com.br.yat.gerenciador.view.ParametroSistemaView;
import com.br.yat.gerenciador.view.PerfilConsultaView;
import com.br.yat.gerenciador.view.PreferenciasSistemaView;
import com.br.yat.gerenciador.view.UsuarioConsultaView;
import com.br.yat.gerenciador.view.UsuarioView;
import com.br.yat.gerenciador.view.UsuarioViewLogin;
import com.br.yat.gerenciador.view.empresa.EmpresaConsultaView;
import com.br.yat.gerenciador.view.factory.DesktopUtils;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class MenuPrincipalController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(MenuPrincipalController.class);
	private final MenuPrincipal view;
	private Timer relogioTimer;
	private boolean processandoLogout = false;

	public MenuPrincipalController(MenuPrincipal view) {
		this.view = view;
		registrarAcoes();
		iniciarRelogio();
		iniciarMonitorDeConexao();
		carregarLogoCache();
		configurarMonitorGlobal();
		verificarSequenciaDeAcesso();
	}
	
	private void carregarLogoCache() {
		AppEventManager.subscribeLogoChange(() -> {
			IconFactory.limparCacheLogo();
			carregarLogoSistema();
		});
	}

	public void registrarAcoes() {
		configurarAcaoMenu(MenuChave.CADASTROS_EMPRESA_CLIENTE, e -> abrirEmpresaCliente());
	
		configurarAcaoMenu(MenuChave.CONSULTAS_EMPRESAS_CLIENTES, e -> abrirEmpresaConsulta());

		configurarAcaoMenu(MenuChave.CONFIGURACAO_EMPRESA_FORNECEDORA, e -> abrirEmpresaFornecedora());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_PREFERENCIAS_DO_SISTEMA, e -> abrirConfiguracaoPreferencias());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_PARAMETRO_SISTEMA, e -> abrirParametroSistema());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_PERMISSAO, e -> abrirConsultaPerfil());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES, e -> abrirConsultaUsuario());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_CONEXAO_BANCO_DADOS, e -> abrirConfiguracaoBanco());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_LIMPEZA_DE_LOGS, e -> abrirLogManutencao());

		configurarAcaoMenu(MenuChave.AUDITORIA_LOG_DO_SISTEMA, e -> abrirConsultaLogs());
		for (var al : view.getBtnLogout().getActionListeners()) {
			view.getBtnLogout().removeActionListener(al);
		}
		view.getBtnLogout().addActionListener(e -> processarLogout());
	}

	private void configurarMonitorGlobal() {
		Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
			if (Sessao.getUsuario() != null) {
				Sessao.registrarAtividade();
			}
		}, AWTEvent.MOUSE_EVENT_MASK | AWTEvent.KEY_EVENT_MASK);
	}

	public void executarLogout(boolean pedirConfirmacao) {
		if (processandoLogout)
			return;

		if (pedirConfirmacao && !DialogFactory.confirmacao(view, "DESEJA REALMENTE SAIR?")) {
			return;
		}

		processandoLogout = true;
		try {
			pararMonitorSessao();
			Sessao.logout();

			view.setNomeUsuario("SESSÃO ENCERRADA");
			view.setTempoAcesso("");

			for (JInternalFrame frame : view.getDesktopPane().getAllFrames()) {
				frame.dispose();
			}

			exibirLogin();
			logger.info(pedirConfirmacao ? "LOGOUT MANUAL REALIZADO." : "SESSÃO EXPIRADA POR INATIVIDADE.");
		} finally {
			processandoLogout = false;
		}
	}

	private void processarLogout() {
		executarLogout(true);
	}

	private void carregarLogoSistema() {
		runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
			try (Connection conn = ConnectionFactory.getConnection()) {
				EmpresaDao empDao = new EmpresaDao(conn);
				var fornecedora = empDao.buscarPorFornecedora();

				if (fornecedora != null) {
					var compDao = new ComplementarDao(conn);
					var complementar = compDao.buscarPorEmpresa(fornecedora.getIdEmpresa());
					return (complementar != null) ? complementar.getLogoTipoComplementar() : null;
				}
			} catch (Exception e) {
				logger.error("ERRO AO BUSCAR LOGO: ", e);
			}
			return null;

		}, (String caminhoLogo) -> {
			if (caminhoLogo != null && !caminhoLogo.isBlank()) {
				Icon iconePersonalizado = IconFactory.externalIcon(caminhoLogo, 160, 160);
				view.getLblLogo().setIcon(iconePersonalizado);
			} else {
				view.getLblLogo().setIcon(IconFactory.logo());
			}

			if (view.getLblLogo().getParent() != null) {
				view.getLblLogo().getParent().repaint();
			}
		});
	}

	private Timer monitorInatividade;

	public void iniciarMonitorSessao() {
		if (monitorInatividade != null && monitorInatividade.isRunning()) {
			monitorInatividade.stop();
		}

		monitorInatividade = new Timer(60000, e -> {
			if (Sessao.getUsuario() != null) {
				if (Sessao.isExpirada()) {
					logger.warn("SESSÃO EXPIRADA PARA O USUÁRIO: {}", Sessao.getUsuario().getNome());
					pararMonitorSessao();
					executarLogout(false);
					DialogFactory.aviso(null, "SUA SESSÃO EXPIROU POR INATIVIDADE.");
				}
			}
		});
		monitorInatividade.start();
		logger.info("MONITOR DE INATIVIDADE INICIADO.");
	}

	public void pararMonitorSessao() {
		if (monitorInatividade != null) {
			monitorInatividade.stop();
		}
	}

	private void iniciarMonitorDeConexao() {
		scheduler.scheduleAtFixedRate(() -> {
			boolean estaValida = false;
			try (Connection conn = ConnectionFactory.getConnection()) {
				estaValida = (conn != null && conn.isValid(5));
			} catch (Exception e) {
				estaValida = false;
			}

			final boolean status = estaValida;
			SwingUtilities.invokeLater(() -> view.atualizarStatusBanco(status));

			if (!status) {
				dispararAlertaConexao();
			}
		}, 10, 30, TimeUnit.SECONDS);
	}

	private void dispararAlertaConexao() {
		logger.error("CONEXÃO COM O BANCO DE DADOS PERDIDA!");
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

	private void verificarSequenciaDeAcesso() {
		if (!Files.exists(Paths.get("config", "database", "db.properties"))) {
			ConfiguracaoBancoView frame = ViewFactory.createConfiguracaoBancoView();
			ViewFactory.showFrameWithCallback(view.getDesktopPane(), frame, this::verificarSequenciaDeAcesso);
			return;
		}

		runAsyncSilent(null, () -> {
			try (Connection conn = ConnectionFactory.getConnection()) {
				EmpresaDao empDao = new EmpresaDao(conn);
				UsuarioDao userDao = new UsuarioDao(conn);

				if (empDao.buscarPorFornecedora() == null)
					return 1;
				if (userDao.listAll().isEmpty())
					return 2;
				return 3;
			}
		}, (Integer degrau) -> {
			switch (degrau) {
			case 1 -> {
				EmpresaView f = ViewFactory.createEmpresaView(TipoCadastro.FORNECEDORA);
				f.setTitle("CADASTRO DA EMPRESA DETENTORA DO SISTEMA");
				ViewFactory.showFrameWithCallback(view.getDesktopPane(), f, this::verificarSequenciaDeAcesso);
			}
			case 2 -> {
				UsuarioView fUser = ViewFactory.createPrimeiroMasterView();

				ViewFactory.showFrameWithCallback(view.getDesktopPane(), fUser, this::verificarSequenciaDeAcesso);
			}
			case 3 -> {
				carregarLogoSistema();
				exibirLogin();
			}
			}
		});
	}

	private void abrirConfiguracaoBanco() {
		JDesktopPane desk = view.getDesktopPane();

		if (DesktopUtils.reuseIfOpen(desk, ConfiguracaoBancoView.class)) {
			return;
		}

		ConfiguracaoBancoView frame = ViewFactory.createConfiguracaoBancoView();
		DesktopUtils.showFrame(desk, frame);
	}

	private void configurarAcaoMenu(MenuChave chave, ActionListener acao) {
		JMenuItem item = MenuRegistry.getItem(chave);
		if (item != null) {
			for (var al : item.getActionListeners()) {
				item.removeActionListener(al);
			}
			item.addActionListener(acao);
		} else {
			logger.warn("ITEM DE MENU NÃO ENCONTRADO NO REGISTRO: {}", chave);
		}
	}

	private void abrirEmpresaCliente() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, "SISTEMA DE GERENCIAMENTO DE LICENÇA - CADASTRO DE CLIENTE")) {
			logger.debug("JANELA CLIENTE REUTILIZADA.");
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

	private void exibirLogin() {
		MenuRegistry.disableAll();
		UsuarioViewLogin login = ViewFactory.createLoginView();
		DesktopUtils.showFrame(view.getDesktopPane(), login);
	}

	public void forcarLogoutExpiracao() {
		Sessao.logout();

		for (JInternalFrame frame : view.getDesktopPane().getAllFrames()) {
			frame.dispose();
		}

		MenuRegistry.disableAll();
		view.setNomeUsuario("SESSÃO EXPIRADA");
		view.setTempoAcesso("");

		exibirLogin();

		logger.info("SESSÃO FINALIZADA POR EXPIRAÇÃO DE INATIVIDADE.");
	}

	public void atualizarDadosUsuario() {
		if (Sessao.getUsuario() != null) {
			Usuario user = Sessao.getUsuario();
			view.setNomeUsuario(user.getNome());

			if (user.getUltimoLogin() != null) {
				String tempoFormatado = TimeUtils.formatarTempoDecorrido(user.getUltimoLogin());
				view.setTempoAcesso("ÚLTIMO ACESSO: " + tempoFormatado);
			} else {
				view.setTempoAcesso("BEM-VINDO! ESTE É SEU PRIMEIRO ACESSO.");
			}
		}
	}

	public void abrirConsultaUsuario() {
		JDesktopPane desk = view.getDesktopPane();

		if (DesktopUtils.reuseIfOpen(desk, UsuarioConsultaView.class)) {
			return;
		}

		UsuarioConsultaView consultaView = ViewFactory.createUsuarioConsultaView();
		DesktopUtils.showFrame(desk, consultaView);
	}

	private void abrirConsultaLogs() {
		JDesktopPane desk = view.getDesktopPane();

		if (DesktopUtils.reuseIfOpen(desk, LogSistemaView.class)) {
			return;
		}

		var frame = ViewFactory.createLogSistemaView();
		DesktopUtils.showFrame(desk, frame);
	}

	private void abrirParametroSistema() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, ParametroSistemaView.class))
			return;

		ParametroSistemaView frame = ViewFactory.createParametroSistemaView();
		DesktopUtils.showFrame(desk, frame);
	}

	private void abrirConsultaPerfil() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, PerfilConsultaView.class)) {
			return;
		}
		var frame = ViewFactory.createPerfilConsultaView();
		DesktopUtils.showFrame(desk, frame);
	}

	private void abrirLogManutencao() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, LogManutencaoView.class)) {
			return;
		}
		var frame = ViewFactory.createLogManutencao();
		DesktopUtils.showFrame(desk, frame);
	}
	
	private void abrirConfiguracaoPreferencias() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, PreferenciasSistemaView.class)) {
			return;
		}
		var frame = ViewFactory.createPreferenciasSistemaView();
		DesktopUtils.showFrame(desk, frame);
	}

}
