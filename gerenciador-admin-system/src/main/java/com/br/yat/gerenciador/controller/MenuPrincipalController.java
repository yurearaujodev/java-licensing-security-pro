package com.br.yat.gerenciador.controller;

import java.awt.event.ActionListener;

import javax.swing.Icon;
import javax.swing.JMenuItem;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.BootstrapEtapa;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.service.BootstrapService;
import com.br.yat.gerenciador.service.ConexaoMonitorService;
import com.br.yat.gerenciador.service.MenuNavigationService;
import com.br.yat.gerenciador.service.RelogioService;
import com.br.yat.gerenciador.service.SessaoService;
import com.br.yat.gerenciador.util.AppEventManager;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.util.TimeUtils;
import com.br.yat.gerenciador.view.ConfiguracaoBancoView;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.MenuPrincipal;
import com.br.yat.gerenciador.view.UsuarioView;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class MenuPrincipalController extends BaseController {

	private static final Logger logger = LoggerFactory.getLogger(MenuPrincipalController.class);
	private final MenuPrincipal view;
	private final BootstrapService bootstrapService;
	private final RelogioService relogioService;
	private final ConexaoMonitorService conexaoMonitorService;
	private final SessaoService sessaoService;
	private final MenuNavigationService navigationService;

	public MenuPrincipalController(MenuPrincipal view, BootstrapService bootstrapService) {
		this.view = view;
		this.relogioService = new RelogioService(view.getHora());
		this.conexaoMonitorService = new ConexaoMonitorService(scheduler, bootstrapService.getEventPublisher(),
				bootstrapService.getSecurityService());
		this.navigationService = new MenuNavigationService(view.getDesktopPane());
		this.sessaoService = new SessaoService(view);
		this.bootstrapService = bootstrapService;
		registrarAcoes();
		relogioService.iniciar();
		conexaoMonitorService.iniciarMonitor(status -> view.atualizarStatusBanco(status));
		carregarLogoCache();
		sessaoService.configurarMonitorGlobal();
		verificarSequenciaDeAcesso();
	}

	public void iniciarMonitorSessao() {
		sessaoService.iniciarMonitorSessao();
	}

	public void pararMonitorSessao() {
		sessaoService.pararMonitorSessao();
	}

	public void executarLogout(boolean pedirConfirmacao) {
		sessaoService.executarLogout(pedirConfirmacao);
	}

	public void forcarLogoutExpiracao() {
		sessaoService.forcarLogoutExpiracao();
	}

	private void processarLogout() {
		sessaoService.executarLogout(true);
	}

	private void carregarLogoCache() {
		AppEventManager.subscribeLogoChange(() -> {
			IconFactory.limparCacheLogo();
			carregarLogoSistema();
		});
	}

	private void verificarSequenciaDeAcesso() {

		if (!bootstrapService.existeConfiguracaoBanco()) {

			ConfiguracaoBancoView frame = ViewFactory.createConfiguracaoBancoView();

			ViewFactory.showFrameWithCallback(view.getDesktopPane(), frame, this::verificarSequenciaDeAcesso);

			return;
		}

		runAsyncSilent(null, bootstrapService::verificarEtapaInicial, this::processarBootstrap);
	}

	private void processarBootstrap(BootstrapEtapa etapa) {
		switch (etapa) {
		case CADASTRAR_FORNECEDORA -> {
			EmpresaView f = ViewFactory.createEmpresaView(TipoCadastro.FORNECEDORA);
			f.setTitle("CADASTRO DA EMPRESA DETENTORA DO SISTEMA");
			ViewFactory.showFrameWithCallback(view.getDesktopPane(), f, this::verificarSequenciaDeAcesso);
		}

		case CADASTRAR_MASTER -> {
			UsuarioView fUser = ViewFactory.createPrimeiroMasterView();
			ViewFactory.showFrameWithCallback(view.getDesktopPane(), fUser, this::verificarSequenciaDeAcesso);
		}

		case LOGIN -> {
			carregarLogoSistema();
			sessaoService.exibirLogin();
		}

		default -> {
		}
		}
	}

	private void carregarLogoSistema() {

		runAsyncSilent(null, bootstrapService::buscarLogoSistema, this::aplicarLogoSistema);
	}

	private void aplicarLogoSistema(String caminhoLogo) {
		if (caminhoLogo != null && !caminhoLogo.isBlank()) {
			Icon iconePersonalizado = IconFactory.externalIcon(caminhoLogo, 160, 160);
			view.getLblLogo().setIcon(iconePersonalizado);

		} else {
			view.getLblLogo().setIcon(IconFactory.logo());
		}

		if (view.getLblLogo().getParent() != null) {
			view.getLblLogo().getParent().repaint();
		}
	}

	public void registrarAcoes() {
		configurarAcaoMenu(MenuChave.CADASTROS_EMPRESA_CLIENTE, e -> navigationService.abrirEmpresaCliente());
		configurarAcaoMenu(MenuChave.CONSULTAS_EMPRESAS_CLIENTES, e -> navigationService.abrirEmpresaConsulta());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_EMPRESA_FORNECEDORA,
				e -> navigationService.abrirEmpresaFornecedora());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_PREFERENCIAS_DO_SISTEMA,
				e -> navigationService.abrirConfiguracaoPreferencias());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_PARAMETRO_SISTEMA, e -> navigationService.abrirParametroSistema());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_PERMISSAO, e -> navigationService.abrirConsultaPerfil());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES, e -> navigationService.abrirConsultaUsuario());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_CONEXAO_BANCO_DADOS, e -> navigationService.abrirConfiguracaoBanco());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_LIMPEZA_DE_LOGS, e -> navigationService.abrirLogManutencao());
		configurarAcaoMenu(MenuChave.AUDITORIA_LOG_DO_SISTEMA, e -> navigationService.abrirConsultaLogs());
		
		for (var al : view.getBtnLogout().getActionListeners()) {
			view.getBtnLogout().removeActionListener(al);
		}
		view.getBtnLogout().addActionListener(e -> processarLogout());
	}

	public void stopRelogio() {
		relogioService.parar();
	}

	@Override
	public void dispose() {

		relogioService.parar();

		sessaoService.pararMonitorSessao();

		super.dispose();
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

}
