package com.br.yat.gerenciador.view.factory;

import java.awt.KeyboardFocusManager;
import java.awt.Window;

import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JInternalFrame;
import javax.swing.WindowConstants;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;

import com.br.yat.gerenciador.controller.ConfiguracaoBancoController;
import com.br.yat.gerenciador.controller.LogManutencaoController;
import com.br.yat.gerenciador.controller.LogSistemaController;
import com.br.yat.gerenciador.controller.LoginController;
import com.br.yat.gerenciador.controller.MenuPrincipalController;
import com.br.yat.gerenciador.controller.ParametroSistemaController;
import com.br.yat.gerenciador.controller.PerfilConsultaController;
import com.br.yat.gerenciador.controller.PerfilController;
import com.br.yat.gerenciador.controller.PreferenciasSistemaController;
import com.br.yat.gerenciador.controller.TrocaSenhaObrigatoriaController;
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
import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.dao.DaoFactoryImpl;
import com.br.yat.gerenciador.domain.event.DomainEventPublisher;
import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.security.SecurityService;
import com.br.yat.gerenciador.service.AuditLogService;
import com.br.yat.gerenciador.service.AutenticacaoService;
import com.br.yat.gerenciador.service.BootstrapService;
import com.br.yat.gerenciador.service.DatabaseConnectionService;
import com.br.yat.gerenciador.service.DatabaseSetupService;
import com.br.yat.gerenciador.service.EmpresaService;
import com.br.yat.gerenciador.service.LogSistemaService;
import com.br.yat.gerenciador.service.ParametroSistemaService;
import com.br.yat.gerenciador.service.PerfilService;
import com.br.yat.gerenciador.service.PreferenciasSistemaService;
import com.br.yat.gerenciador.service.UsuarioPermissaoService;
import com.br.yat.gerenciador.service.UsuarioService;
import com.br.yat.gerenciador.view.ConfiguracaoBancoView;
import com.br.yat.gerenciador.view.EmpresaView;
import com.br.yat.gerenciador.view.LogManutencaoView;
import com.br.yat.gerenciador.view.LogSistemaView;
import com.br.yat.gerenciador.view.MenuPrincipal;
import com.br.yat.gerenciador.view.ParametroSistemaView;
import com.br.yat.gerenciador.view.PerfilConsultaView;
import com.br.yat.gerenciador.view.PerfilView;
import com.br.yat.gerenciador.view.PreferenciasSistemaView;
import com.br.yat.gerenciador.view.UsuarioConsultaView;
import com.br.yat.gerenciador.view.UsuarioView;
import com.br.yat.gerenciador.view.UsuarioViewLogin;
import com.br.yat.gerenciador.view.UsuarioViewTrocaSenha;
import com.br.yat.gerenciador.view.empresa.EmpresaConsultaView;
import com.br.yat.gerenciador.domain.event.listener.UsuarioAuditListener;
import com.br.yat.gerenciador.domain.event.listener.ErrorEventListener;

public final class ViewFactory {

	private static MenuPrincipalController mainController;
	private static DaoFactory daoFactory;
	private static DomainEventPublisher eventPublisher;

	public static void initializeDependencies() {
	    if (daoFactory == null) {
	        synchronized (ViewFactory.class) {
	            if (daoFactory == null) {
	                daoFactory = new DaoFactoryImpl();
	            }
	        }
	    }

	    if (eventPublisher == null) {
	        synchronized (ViewFactory.class) {
	            if (eventPublisher == null) {
	                eventPublisher = new DomainEventPublisher();
	                
	                SecurityService securityService = new SecurityService();
	                AuditLogService auditLogService = new AuditLogService(daoFactory, eventPublisher, securityService);
	                eventPublisher.register(new UsuarioAuditListener(auditLogService));
	                eventPublisher.register(new ErrorEventListener(daoFactory));
	            }
	        }
	    }
	}

	public static DomainEventPublisher getEventPublisher() {
	    if (eventPublisher == null) {
	        initializeDependencies();
	    }
	    return eventPublisher;
	}

	private static DaoFactory getDaoFactory() {
		if (daoFactory == null) {
			initializeDependencies();
		}
		return daoFactory;
	}

	public static EmpresaView createEmpresaView(TipoCadastro tipoCadastro) {
		EmpresaView view = new EmpresaView();
		DomainEventPublisher dep = getEventPublisher();
		SecurityService securityService = new SecurityService();
		EmpresaService service = new EmpresaService(dep,securityService);

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
		mainController = new MenuPrincipalController(view);
		return view;
	}

	public static void atualizarAcoesMenuPrincipal() {
		if (mainController != null) {
			mainController.registrarAcoes();
			mainController.atualizarDadosUsuario();
		}
	}

	public static EmpresaConsultaView createEmpresaConsultaView() {
		EmpresaConsultaView view = new EmpresaConsultaView();
		DomainEventPublisher dep = getEventPublisher();
		SecurityService securityService = new SecurityService();
		EmpresaService service = new EmpresaService(dep,securityService);
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

		DaoFactory df = getDaoFactory();
		DomainEventPublisher dep = getEventPublisher();
		SecurityService securityService = new SecurityService();

		ParametroSistemaService parametro = new ParametroSistemaService(dep,securityService);

		AutenticacaoService authService = new AutenticacaoService(parametro, df,dep,securityService);
		AuditLogService auditLogService = new AuditLogService(df,dep,securityService);
		UsuarioPermissaoService usuperservice = new UsuarioPermissaoService(df,auditLogService,dep,securityService);
		BootstrapService bootstrapService = new BootstrapService(df,dep,securityService);
		UsuarioService service = new UsuarioService(authService, parametro, usuperservice, df, bootstrapService,dep,securityService);
		UsuarioConsultaController controller = new UsuarioConsultaController(view, service, authService,usuperservice);

		view.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				controller.dispose();
			}
		});
		return view;
	}

	public static UsuarioView createUsuarioViewComController() {
		UsuarioView view = new UsuarioView();
		DaoFactory df = getDaoFactory();
		DomainEventPublisher dep = getEventPublisher();
		SecurityService securityService = new SecurityService();
		EmpresaService empresa = new EmpresaService(dep, securityService);

		ParametroSistemaService parametro = new ParametroSistemaService(dep,securityService);

		AutenticacaoService authService = new AutenticacaoService(parametro, df,dep,securityService);
		AuditLogService auditLogService = new AuditLogService(df,dep,securityService);
		UsuarioPermissaoService usuperservice = new UsuarioPermissaoService(df,auditLogService,dep,securityService);
		BootstrapService bootstrapService = new BootstrapService(df,dep,securityService);
		UsuarioService service = new UsuarioService(authService, parametro, usuperservice, df, bootstrapService,dep,securityService);
		PerfilService perfilService = new PerfilService(usuperservice,df,auditLogService,dep,securityService);

		UsuarioController controller = new UsuarioController(view, service, perfilService, bootstrapService,
				usuperservice,empresa);
		view.putClientProperty("controller", controller);

		view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		view.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				controller.dispose();
				view.dispose();
			}
		});

		return view;
	}

	public static UsuarioViewLogin createLoginView() {
		UsuarioViewLogin view = new UsuarioViewLogin();

		DaoFactory df = getDaoFactory();
		DomainEventPublisher dep = getEventPublisher();
		SecurityService securityService = new SecurityService();
		
		ParametroSistemaService parametro = new ParametroSistemaService(dep,securityService);
		LogSistemaService logSistemaService = new LogSistemaService(dep, securityService, parametro);
		AutenticacaoService authService = new AutenticacaoService(parametro, df,dep,securityService);
		AuditLogService auditLogService = new AuditLogService(df,dep,securityService);
		UsuarioPermissaoService usuperservice = new UsuarioPermissaoService(df,auditLogService,dep,securityService);
		BootstrapService bootstrapService = new BootstrapService(df,dep,securityService);
		new UsuarioService(authService, parametro, usuperservice, df, bootstrapService,dep,securityService);
		new LoginController(view, authService, usuperservice,parametro,logSistemaService);

		view.setClosable(false);
		view.setIconifiable(false);
		view.setMaximizable(false);

		return view;
	}

	public static MenuPrincipalController getMainController() {
		return mainController;
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
		// Cria a estrutura padrão (View + Controller + Service)
		UsuarioView view = createUsuarioViewComController();

		// Recupera a controller injetada
		UsuarioController controller = (UsuarioController) view.getClientProperty("controller");

		// Manda a controller executar a lógica de negócio de primeiro acesso
		controller.prepararComoMaster();

		return view;
	}

	public static LogSistemaView createLogSistemaView() {
		LogSistemaView view = new LogSistemaView();

		new LogSistemaController(view);

		view.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				// Se o controller tivesse recursos para liberar (como um scheduler),
				// chamaríamos o dispose aqui.
			}
		});

		return view;
	}

	public static ParametroSistemaView createParametroSistemaView() {
		ParametroSistemaView view = new ParametroSistemaView();
		DaoFactory df = getDaoFactory();
		DomainEventPublisher dep = getEventPublisher();
		SecurityService securityService = new SecurityService();

		ParametroSistemaService service = new ParametroSistemaService(dep,securityService);
		AutenticacaoService authService = new AutenticacaoService(service, df,dep,securityService);
		new ParametroSistemaController(view, service, authService);
		return view;
	}

	public static PerfilView createPerfilView() {
		PerfilView view = new PerfilView();
		DaoFactory df = getDaoFactory();
		DomainEventPublisher dep = getEventPublisher();
		SecurityService securityService = new SecurityService();
		AuditLogService auditLogService = new AuditLogService(df,dep,securityService);
		UsuarioPermissaoService usuperservice = new UsuarioPermissaoService(df,auditLogService,dep,securityService);
		PerfilService service = new PerfilService(usuperservice,df,auditLogService,dep,securityService);
		PerfilController controller = new PerfilController(view, service,usuperservice);

		view.putClientProperty("controller", controller);

		view.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
		view.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosing(InternalFrameEvent e) {
				view.dispose();
				controller.dispose();
			}
		});
		return view;
	}

	public static PerfilConsultaView createPerfilConsultaView() {
		PerfilConsultaView view = new PerfilConsultaView();
		DaoFactory df = getDaoFactory();
		DomainEventPublisher dep = getEventPublisher();
		SecurityService securityService = new SecurityService();
		AuditLogService auditLogService = new AuditLogService(df,dep,securityService);
		UsuarioPermissaoService usuperservice = new UsuarioPermissaoService(df,auditLogService,dep,securityService);
		PerfilService service = new PerfilService(usuperservice,df,auditLogService,dep,securityService);
		PerfilConsultaController controller = new PerfilConsultaController(view, service,usuperservice);

		view.addInternalFrameListener(new InternalFrameAdapter() {
			@Override
			public void internalFrameClosed(InternalFrameEvent e) {
				controller.dispose();
			}
		});
		return view;
	}

	public static PerfilView createPerfilEdicaoView(Perfil perfil) {
		PerfilView view = createPerfilView();
		PerfilController controller = (PerfilController) view.getClientProperty("controller");
		controller.carregarParaEdicao(perfil);
		return view;
	}

	public static LogManutencaoView createLogManutencao() {
		LogManutencaoView view = new LogManutencaoView();
		DomainEventPublisher dep = getEventPublisher();
		SecurityService securityService = new SecurityService();
		ParametroSistemaService parametro = new ParametroSistemaService(dep,securityService);
		LogSistemaService service = new LogSistemaService(dep,securityService,parametro);

		new LogManutencaoController(view, service, parametro);

		return view;
	}

	public static void abrirTrocaSenhaObrigatoria(Usuario user, Runnable callbackSucesso) {
		Window parent = KeyboardFocusManager.getCurrentKeyboardFocusManager().getActiveWindow();
		UsuarioViewTrocaSenha view = new UsuarioViewTrocaSenha(parent instanceof JFrame ? (JFrame) parent : null);
		DaoFactory df = getDaoFactory();
		DomainEventPublisher dep = getEventPublisher();
		SecurityService securityService = new SecurityService();
		ParametroSistemaService parametro = new ParametroSistemaService(dep,securityService);
		AutenticacaoService authService = new AutenticacaoService(parametro, df,dep,securityService);
		new TrocaSenhaObrigatoriaController(view, user, authService, callbackSucesso);
		view.setVisible(true);
	}

	public static PreferenciasSistemaView createPreferenciasSistemaView() {

		PreferenciasSistemaView view = new PreferenciasSistemaView();
		PreferenciasSistemaService service = new PreferenciasSistemaService();

		new PreferenciasSistemaController(view, service);

		view.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		return view;
	}

}
