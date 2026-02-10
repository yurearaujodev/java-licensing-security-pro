package com.br.yat.gerenciador.controller;

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
import com.br.yat.gerenciador.view.PermissaoConsultaView;
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
		AppEventManager.subscribeLogoChange(() -> {
			IconFactory.limparCacheLogo();
			carregarLogoSistema();
		});
		configurarMonitorGlobal();
		verificarSequenciaDeAcesso();
	}

	public void registrarAcoes() {
		configurarAcaoMenu(MenuChave.CADASTROS_EMPRESA_CLIENTE, e -> abrirEmpresaCliente());
		configurarAcaoMenu(MenuChave.CADASTROS_PERFIL, e -> abrirPerfil());
		configurarAcaoMenu(MenuChave.CONSULTAS_PERFIL, e -> abrirConsultaPerfil());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_EMPRESA_FORNECEDORA, e -> abrirEmpresaFornecedora());
		configurarAcaoMenu(MenuChave.CONSULTAS_EMPRESAS_CLIENTES, e -> abrirEmpresaConsulta());
		configurarAcaoMenu(MenuChave.CADASTROS_USUARIO, e -> abrirUsuario());
		configurarAcaoMenu(MenuChave.CONSULTAS_USUARIOS, e -> abrirConsultaUsuario());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_CONEXAO_BANCO_DADOS, e -> abrirConfiguracaoBanco());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES, e -> abrirConsultaUsuario());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_PERMISSAO, e -> abrirConsultaPermissoes());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_PARAMETRO_SISTEMA, e -> abrirParametroSistema());
		configurarAcaoMenu(MenuChave.AUDITORIA_LOG_DO_SISTEMA, e -> abrirConsultaLogs());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_LIMPEZA_DE_LOGS, e -> abrirLogManutencao());
		for (var al : view.getBtnLogout().getActionListeners()) {
			view.getBtnLogout().removeActionListener(al);
		}
		view.getBtnLogout().addActionListener(e -> processarLogout());
	}

	private void configurarMonitorGlobal() {
		java.awt.Toolkit.getDefaultToolkit().addAWTEventListener(event -> {
			// Toda vez que o usuário interage, resetamos o cronômetro na Sessão
			if (Sessao.getUsuario() != null) {
				Sessao.registrarAtividade();
			}
		}, java.awt.AWTEvent.MOUSE_EVENT_MASK | java.awt.AWTEvent.KEY_EVENT_MASK);
	}

	// Adicione este método ao seu MenuPrincipalController
	public void executarLogout(boolean pedirConfirmacao) {
	    if (processandoLogout) return;
	    
	    // Se for manual (pedirConfirmacao = true), abre o diálogo.
	    // Se for automático (expiração), ignora a confirmação.
	    if (pedirConfirmacao && !DialogFactory.confirmacao(view, "DESEJA REALMENTE SAIR?")) {
	        return;
	    }

	    processandoLogout = true;
	    try {
	        // 1. Limpeza da Sessão (Memória)
	        Sessao.logout();

	        // 2. UI: Limpeza de Campos e Frames
	        view.setNomeUsuario("SESSÃO ENCERRADA");
	        view.setTempoAcesso("");
	        
	        for (JInternalFrame frame : view.getDesktopPane().getAllFrames()) {
	            frame.dispose();
	        }

	        // 3. UI: Reset de Menus e volta ao Login
	        exibirLogin();
	        
	        logger.info(pedirConfirmacao ? "LOGOUT MANUAL REALIZADO." : "SESSÃO EXPIRADA POR INATIVIDADE.");
	    } finally {
	        processandoLogout = false;
	    }
	}

	// Atualize seu método processarLogout antigo para apenas uma linha:
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
				// 1. Criamos a View e a Controller através da Factory
				// Supondo que sua ViewFactory já injete as Services e a Controller na View
				UsuarioView fUser = ViewFactory.createPrimeiroMasterView();

				// 3. Exibe com o callback para re-verificar quando fechar
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

	private void abrirUsuario() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, "NOVO_USUARIO")) {
			return;
		}
		UsuarioView frame = ViewFactory.createUsuarioView();
		frame.setName("NOVO_USUARIO");
		DesktopUtils.showFrame(desk, frame);
	}

	private void exibirLogin() {
		MenuRegistry.disableAll();
		UsuarioViewLogin login = ViewFactory.createLoginView();
		DesktopUtils.showFrame(view.getDesktopPane(), login);
	}
	
	/**
	 * Finaliza a sessão atual, limpa a interface e força um novo login.
	 * Útil para Logout manual e Expiração de Sessão.
	 */
	public void forcarLogoutExpiracao() {
	    // 1. Garante a limpeza da sessão na memória
	    Sessao.logout();

	    // 2. UI: Limpa o DesktopPane (fecha todas as janelas internas de dados)
	    for (JInternalFrame frame : view.getDesktopPane().getAllFrames()) {
	        frame.dispose();
	    }

	    // 3. UI: Reseta os Menus e informações do usuário na barra de status
	    MenuRegistry.disableAll();
	    view.setNomeUsuario("SESSÃO EXPIRADA");
	    view.setTempoAcesso("");

	    // 4. UI: Abre a tela de login
	    exibirLogin();
	    
	    logger.info("Sessão finalizada por expiração de inatividade.");
	}

	public void atualizarDadosUsuario() {
		if (Sessao.getUsuario() != null) {
			Usuario user = Sessao.getUsuario();
			view.setNomeUsuario(user.getNome());

			if (user.getUltimoLogin() != null) {
				String tempoFormatado = TimeUtils.formatarTempoDecorrido(user.getUltimoLogin());
				view.setTempoAcesso("Último acesso: " + tempoFormatado);
			} else {
				view.setTempoAcesso("Bem-vindo! Este é seu primeiro acesso.");
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

	private void abrirConsultaPermissoes() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, PermissaoConsultaView.class))
			return;

		PermissaoConsultaView frame = ViewFactory.createPermissaoConsultaView();
		DesktopUtils.showFrame(desk, frame);
	}

	private void abrirParametroSistema() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, ParametroSistemaView.class))
			return;

		ParametroSistemaView frame = ViewFactory.createParametroSistemaView();
		DesktopUtils.showFrame(desk, frame);
	}

	private void abrirPerfil() {
		JDesktopPane desk = view.getDesktopPane();
		if (DesktopUtils.reuseIfOpen(desk, "NOVO_PERFIL")) {
			return;
		}
		// Usa a Factory para criar a View + Controller
		var frame = ViewFactory.createPerfilView();
		frame.setName("NOVO_PERFIL");
		DesktopUtils.showFrame(desk, frame);
	}

	private void abrirConsultaPerfil() {
		JDesktopPane desk = view.getDesktopPane();
		// Reutiliza se já estiver aberta para evitar múltiplas instâncias
		if (DesktopUtils.reuseIfOpen(desk, PerfilConsultaView.class)) {
			return;
		}
		// Chama a Factory
		var frame = ViewFactory.createPerfilConsultaView();
		DesktopUtils.showFrame(desk, frame);
	}

	private void abrirLogManutencao() {
		JDesktopPane desk = view.getDesktopPane();
		// Reutiliza se já estiver aberta para evitar múltiplas instâncias
		if (DesktopUtils.reuseIfOpen(desk, LogManutencaoView.class)) {
			return;
		}
		// Chama a Factory
		var frame = ViewFactory.createLogManutencao();
		DesktopUtils.showFrame(desk, frame);
	}

}
