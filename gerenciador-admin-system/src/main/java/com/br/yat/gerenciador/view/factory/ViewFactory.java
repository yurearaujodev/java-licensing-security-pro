package com.br.yat.gerenciador.view.factory;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.br.yat.gerenciador.controller.ConfiguracaoBancoController;
import com.br.yat.gerenciador.controller.LoginController;
import com.br.yat.gerenciador.controller.MenuPrincipalController;
import com.br.yat.gerenciador.controller.UsuarioController;
import com.br.yat.gerenciador.controller.empresa.DadoBancarioController;
import com.br.yat.gerenciador.controller.empresa.DadoComplementarController;
import com.br.yat.gerenciador.controller.empresa.DadoContatoController;
import com.br.yat.gerenciador.controller.empresa.DadoEnderecoController;
import com.br.yat.gerenciador.controller.empresa.DadoFiscalController;
import com.br.yat.gerenciador.controller.empresa.DadoPrincipalController;
import com.br.yat.gerenciador.controller.empresa.DadoRepresentanteController;
import com.br.yat.gerenciador.controller.empresa.EmpresaConsultaController;
import com.br.yat.gerenciador.controller.empresa.EmpresaController;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.service.DatabaseConnectionService;
import com.br.yat.gerenciador.service.DatabaseSetupService;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.view.ConfiguracaoBancoView;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.MenuPrincipal;
import com.br.yat.gerenciador.view.UsuarioConsultaView;
import com.br.yat.gerenciador.view.UsuarioView;
import com.br.yat.gerenciador.view.UsuarioViewLogin;
import com.br.yat.gerenciador.view.empresa.EmpresaConsultaView;

public final class ViewFactory {
	
	private static MenuPrincipalController mainController;

	public static EmpresaView createEmpresaView(TipoCadastro tipoCadastro) {
		EmpresaView view = new EmpresaView();
		EmpresaService service = new EmpresaService();

		var ePrincipal = new DadoPrincipalController(view.getDadoPrincipal());
		var eEndereco = new DadoEnderecoController(view.getDadoEndereco());
		var eContato = new DadoContatoController(view.getDadoContato());
		var eFiscal = new DadoFiscalController(view.getDadoFiscal());
		var eRepresentante = new DadoRepresentanteController(view.getDadoRepresentante());
		var eBancario = new DadoBancarioController(view.getDadoBancario());
		var eComplementar = new DadoComplementarController(view.getDadoComplementar());

		EmpresaController controller = new EmpresaController(view, service, ePrincipal, eEndereco, eContato, eFiscal,
				eRepresentante, eBancario, eComplementar, tipoCadastro);

		view.putClientProperty("controller", controller);

		view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

		view.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				if (controller.podeFechar()) {
					controller.dispose();
					view.dispose();
				}
			}
		});

		return view;
	}

	public static MenuPrincipal createMenuPrincipal() {
		MenuPrincipal view = new MenuPrincipal();
		mainController= new MenuPrincipalController(view);
		return view;
	}
	
	public static void atualizarAcoesMenuPrincipal() {
        if (mainController != null) {
        	mainController.registrarAcoes();       // Reativa os menus
            mainController.atualizarDadosUsuario(); // ATUALIZA O NOME NA TELA
        }
    }

	public static EmpresaConsultaView createEmpresaConsultaView() {
		EmpresaConsultaView view = new EmpresaConsultaView();
		EmpresaService service = new EmpresaService();
		EmpresaConsultaController controller = new EmpresaConsultaController(view, service);

		view.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				controller.dispose();
			}
		});
		return view;
	}

	public static EmpresaView createEmpresaEdicaoView(int id) {
		EmpresaView view = createEmpresaView(TipoCadastro.CLIENTE);
		view.setTitle("CARREGANDO DADOS... POR FAVOR, AGUARDE.");

		EmpresaController controller = (EmpresaController) view.getClientProperty("controller");
		controller.carregarDados(id);
		return view;
	}

	public static ConfiguracaoBancoView createConfiguracaoBancoView() {
		ConfiguracaoBancoView view = new ConfiguracaoBancoView();
		DatabaseSetupService service = new DatabaseSetupService();
		DatabaseConnectionService connectionService = new DatabaseConnectionService(service);
		ConfiguracaoBancoController controller = new ConfiguracaoBancoController(view, service, connectionService);

		view.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				controller.dispose();
			}
		});
		return view;
	}

	public static UsuarioView createUsuarioView() {
		UsuarioView view = new UsuarioView();
		new UsuarioController(view);
		return view;
	}

	public static UsuarioViewLogin createLoginView() {
		UsuarioViewLogin view = new UsuarioViewLogin();

		// Agora o Controller não pede mais Connection no construtor
		new LoginController(view);

		view.setClosable(false);
		view.setIconifiable(false);
		view.setMaximizable(false);

		return view;
	}

	public static void showFrameWithCallback(JDesktopPane desk, JInternalFrame frame, Runnable onSchemaChange) {
		frame.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				if (onSchemaChange != null) {
					onSchemaChange.run();
				}
			}
		});
		DesktopUtils.showFrame(desk, frame);
	}
	
	// Use este método no seu MenuPrincipalController para a sequência de telas
    public static void abrirUsuarioComCallback(JDesktopPane desk, Runnable callback) {
        UsuarioView view = new UsuarioView();
        new UsuarioController(view);
        
        view.addInternalFrameListener(new javax.swing.event.InternalFrameAdapter() {
            @Override
            public void internalFrameClosed(javax.swing.event.InternalFrameEvent e) {
                if (callback != null) callback.run();
            }
        });
        
        com.br.yat.gerenciador.view.factory.DesktopUtils.showFrame(desk, view);
    }
    
    public static UsuarioConsultaView createUsuarioConsultaView() {
        // 1. Criamos as duas Views
        UsuarioConsultaView consultaView = new UsuarioConsultaView();
        UsuarioView cadastroView = new UsuarioView(); 

        // 2. O Controller assume o comando de ambas
        UsuarioController controller = new UsuarioController(cadastroView);
        
        // 3. Chamamos o método que você já criou no Controller para vincular os eventos da consulta
        controller.vincularAcoesConsulta(consultaView); 

        // Guardamos o cadastroView dentro da consulta para fácil acesso se necessário
        consultaView.putClientProperty("cadastroView", cadastroView);

        return consultaView;
    }

    // Abre a tela de cadastro carregando um usuário específico
    public static UsuarioView createUsuarioEdicaoView(Usuario usuario) {
        UsuarioView view = new UsuarioView();
        UsuarioController controller = new UsuarioController(view);
        
        // Carrega os dados no controller
        controller.carregarUsuarioParaEdicao(usuario);
        
        return view;
    }

}
