package com.br.yat.gerenciador.view.factory;

import javax.swing.JDesktopPane;
import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.br.yat.gerenciador.controller.ConfiguracaoBancoController;
import com.br.yat.gerenciador.controller.LoginController;
import com.br.yat.gerenciador.controller.MenuPrincipalController;
import com.br.yat.gerenciador.controller.PermissaoConsultaController;
import com.br.yat.gerenciador.controller.UsuarioConsultaController;
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
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.service.DatabaseConnectionService;
import com.br.yat.gerenciador.service.DatabaseSetupService;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.view.ConfiguracaoBancoView;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.LogSistemaView;
import com.br.yat.gerenciador.view.MenuPrincipal;
import com.br.yat.gerenciador.view.PermissaoConsultaView;
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

	public static UsuarioConsultaView createUsuarioConsultaView() {
	    UsuarioConsultaView view = new UsuarioConsultaView();
	    UsuarioService service = new UsuarioService();
	    UsuarioConsultaController controller = new UsuarioConsultaController(view, service);

	    // Garante que o scheduler e recursos do controller sejam liberados
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
	    UsuarioService service = new UsuarioService();
	    UsuarioController controller = new UsuarioController(view, service);
	    view.putClientProperty("controller", controller);

	    // Evita que a janela feche sem validar se há dados pendentes
	    view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
	    view.addInternalFrameListener(new InternalFrameAdapter() {
	        @Override
	        public void internalFrameClosing(InternalFrameEvent e) {
	            // Se você quiser implementar o 'podeFechar' no UsuarioController depois
	            view.dispose(); 
	            controller.dispose();
	        }
	    });
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

	public static UsuarioView createPrimeiroMasterView() {
	    UsuarioView view = createUsuarioView();
	    view.setTitle("CONFIGURAÇÃO INICIAL: CADASTRAR ADMINISTRADOR MASTER");
	    
	    // Força ser Master e impede desmarcar
	    view.setMaster(true);
	    view.getChkMaster().setEnabled(false);
	    
	    // No primeiro acesso, o status deve ser ATIVO obrigatoriamente
	    view.setStatus("ATIVO");
	    view.bloquearStatus(false);
	    
	    view.getPermissoes().values().forEach(chk -> chk.setSelected(true));
	    view.bloquearGradePermissoes(false); // Master não mexe nas próprias permissões
	    
	    return view;
	}
	
	// Adicione este método na sua ViewFactory
	public static LogSistemaView createLogSistemaView() {
	    LogSistemaView view = new LogSistemaView();
	    
	    // O Controller assume o controle da View aqui
	    new com.br.yat.gerenciador.controller.LogSistemaController(view);
	    
	    view.addInternalFrameListener(new InternalFrameAdapter() {
	        @Override
	        public void internalFrameClosed(InternalFrameEvent e) {
	            // Se o controller tivesse recursos para liberar (como um scheduler),
	            // chamaríamos o dispose aqui.
	        }
	    });
	    
	    return view;
	}

	public static PermissaoConsultaView createPermissaoConsultaView() {
	    var view = new PermissaoConsultaView();
	    UsuarioService service = new UsuarioService();
	    
	    // Instancia o controller que criamos anteriormente
	    new PermissaoConsultaController(view, service);
	    
	    return view;
	}
	
}
