package com.br.yat.gerenciador.controller;

import java.awt.event.ActionListener;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Connection;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

import javax.swing.JDesktopPane;
import javax.swing.JMenuItem;
import javax.swing.Timer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.empresa.EmpresaDao;
import com.br.yat.gerenciador.dao.usuario.UsuarioDao;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.view.ConfiguracaoBancoView;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.MenuPrincipal;
import com.br.yat.gerenciador.view.UsuarioConsultaView;
import com.br.yat.gerenciador.view.UsuarioView;
import com.br.yat.gerenciador.view.UsuarioViewLogin;
import com.br.yat.gerenciador.view.empresa.EmpresaConsultaView;
import com.br.yat.gerenciador.view.factory.DesktopUtils;
import com.br.yat.gerenciador.view.factory.ViewFactory;

public class MenuPrincipalController extends BaseController{

	private static final Logger logger = LoggerFactory.getLogger(MenuPrincipalController.class);
	private final MenuPrincipal view;
	private Timer relogioTimer;

	public MenuPrincipalController(MenuPrincipal view) {
		this.view = view;
		registrarAcoes();
		iniciarRelogio();
		verificarSequenciaDeAcesso();
	}

	public void registrarAcoes() {
		configurarAcaoMenu(MenuChave.CADASTROS_EMPRESA_CLIENTE, e -> abrirEmpresaCliente());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_EMPRESA_FORNECEDORA, e -> abrirEmpresaFornecedora());
		configurarAcaoMenu(MenuChave.CONSULTAS_EMPRESAS_CLIENTES, e -> abrirEmpresaConsulta());
		configurarAcaoMenu(MenuChave.CADASTROS_USUARIO, e -> abrirUsuario());
		configurarAcaoMenu(MenuChave.CONSULTAS_USUARIOS, e->abrirConsultaUsuario());
		configurarAcaoMenu(MenuChave.CONFIGURACAO_CONEXAO_BANCO_DADOS, e -> abrirConfiguracaoBanco());
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

//	private void verificarPrimeiroAcesso() {
//		Path path = Paths.get("config", "database", "db.properties");
//
//		if (!Files.exists(path)) {
//			abrirConfiguracaoBanco();
//			return;
//		}
//	}

	private void verificarSequenciaDeAcesso() {
	    if (!Files.exists(Paths.get("config", "database", "db.properties"))) {
	        ConfiguracaoBancoView frame = ViewFactory.createConfiguracaoBancoView();
	        ViewFactory.showFrameWithCallback(view.getDesktopPane(), frame, this::verificarSequenciaDeAcesso);
	        return;
	    }

	    // runAsyncSilent geralmente não precisa de Window (null)
	    runAsyncSilent(null, () -> {
	        try (Connection conn = ConnectionFactory.getConnection()) {
	            EmpresaDao empDao = new EmpresaDao(conn);
	            UsuarioDao userDao = new UsuarioDao(conn);
	            
	            if (empDao.buscarPorFornecedora() == null) return Integer.valueOf(1);
	            if (userDao.listAll().isEmpty()) return Integer.valueOf(2);
	            return Integer.valueOf(3);
	        }
	    }, (Integer degrau) -> { // Tipagem explícita no Consumer
	        switch (degrau.intValue()) {
	            case 1 -> {
	                EmpresaView f = ViewFactory.createEmpresaView(TipoCadastro.FORNECEDORA);
	                ViewFactory.showFrameWithCallback(view.getDesktopPane(), f, this::verificarSequenciaDeAcesso);
	            }
	            case 2 -> ViewFactory.abrirUsuarioComCallback(view.getDesktopPane(), this::verificarSequenciaDeAcesso);
	            case 3 -> exibirLogin();
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
		if (DesktopUtils.reuseIfOpen(desk, EmpresaConsultaView.class)) {
			return;
		}
		UsuarioView frame = ViewFactory.createUsuarioView();
		DesktopUtils.showFrame(desk, frame);
	}
	
	private void exibirLogin() {
        MenuRegistry.disableAll();
        UsuarioViewLogin login = ViewFactory.createLoginView();
        DesktopUtils.showFrame(view.getDesktopPane(), login);
    }
	
	public void atualizarDadosUsuario() {
	    if (Sessao.getUsuario() != null) {
	        String nome = Sessao.getUsuario().getNome();
	        view.setNomeUsuario(nome);
	    }
	}
	
	// Exemplo de como abrir a consulta de usuários
	public void abrirConsultaUsuario() {
	    UsuarioConsultaView consultaView = ViewFactory.createUsuarioConsultaView();
	    
	    // Precisamos de um controller para a consulta ou vincular as ações
	    // Se você seguiu o código anterior, a lógica de "Editar" da ConsultaView 
	    // vai chamar a ViewFactory para abrir a UsuarioView.
	    
	    DesktopUtils.showFrame(view.getDesktopPane(), consultaView);
	}

}
