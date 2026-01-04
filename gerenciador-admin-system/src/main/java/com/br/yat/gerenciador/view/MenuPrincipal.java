package com.br.yat.gerenciador.view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.util.MenuRegistry;
import com.br.yat.gerenciador.util.ui.DesktopFactory;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.MenuFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;

import javax.swing.JDesktopPane;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JLabel;
import javax.swing.JMenu;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import javax.swing.Timer;

public class MenuPrincipal extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel painelPrincipal;
	private JLabel lblHora;
	private JDesktopPane desktopPane;

	public MenuPrincipal() {
		configurarFrame();
		montarTela();
		iniciarRelogio();
	}

	private void montarTela() {
		painelPrincipal = PanelFactory.createPanel("fill,insets 0,gap 0", "[grow][200!]", "[][grow][30!]");
		setContentPane(painelPrincipal);

		painelPrincipal.add(criarPainelMenu(), "cell 0 0 2 1, growx");
		painelPrincipal.add(criarPainelCentro(), "cell 0 1,grow");
		painelPrincipal.add(criarPainelLateral(), "cell 1 1,grow");
		painelPrincipal.add(criarPainelRodape(), "cell 0 2 2 1, grow");
	}

	private void configurarFrame() {
		setTitle("SISTEMA DE GERENCIAMENTO DE LICENÇA - MENU PRINCIPAL");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(950, 600);
		setUndecorated(true);
		getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
	}

	private JPanel criarPainelMenu() {
		JMenuBar menuBar = MenuFactory.createMenuBar();

		menuBar.add(criarMenuDashboard());
		menuBar.add(criarMenuCadastro());
		menuBar.add(criarMenuConsulta());
		menuBar.add(criarMenuLicenca());
		menuBar.add(criarMenuFinanceiro());
		menuBar.add(criarMenuRelatorio());
		menuBar.add(criarMenuAuditoria());
		menuBar.add(criarMenuConfiguração());
		menuBar.add(criarMenuAjuda());

		return MenuFactory.criarPainelBarraMenu(menuBar);
	}

	private JMenu criarMenuDashboard() {
		JMenu menuDashboard = MenuFactory.createMenu("DASHBOARD", null);

		JMenuItem itemGeral = MenuFactory.createMenuItem("VISÃO GERAL", null, null, null);
		MenuRegistry.register(MenuChave.DASHBOARD_GERAL, itemGeral);
		menuDashboard.add(itemGeral);

		JMenu menuAlertas = MenuFactory.createMenu("ALERTAS", null);

		JMenuItem itemLicVenc = MenuFactory.createMenuItem("LICENÇAS A VENCER", null, null, null);
		MenuRegistry.register(MenuChave.DASHBOARD_LICENCAS_A_VENCER, itemLicVenc);
		menuAlertas.add(itemLicVenc);

		JMenuItem itemLicBlo = MenuFactory.createMenuItem("LICENÇAS BLOQUEADAS", null, null, null);
		MenuRegistry.register(MenuChave.DASHBOARD_LICENCAS_BLOQUEADAS, itemLicBlo);
		menuAlertas.add(itemLicBlo);

		JMenuItem itemPagPen = MenuFactory.createMenuItem("PAGAMENTOS PENDENTES", null, null, null);
		MenuRegistry.register(MenuChave.DASHBOARD_PAGAMENTOS_PENDENTES, itemPagPen);
		menuAlertas.add(itemPagPen);

		menuDashboard.add(menuAlertas);

		return menuDashboard;
	}

	private JMenu criarMenuCadastro() {
		JMenu menuCadastro = MenuFactory.createMenu("CADASTROS", null);

		JMenuItem itemEmpCli = MenuFactory.createMenuItem("EMPRESA CLIENTE", null, null, null);
		MenuRegistry.register(MenuChave.CADASTROS_EMPRESA_CLIENTE, itemEmpCli);
		menuCadastro.add(itemEmpCli);

		JMenuItem itemUsu = MenuFactory.createMenuItem("USUÁRIO", null, null, null);
		MenuRegistry.register(MenuChave.CADASTROS_USUARIO, itemUsu);
		menuCadastro.add(itemUsu);

		JMenuItem itemPlaLic = MenuFactory.createMenuItem("PLANO DE LICENÇA", null, null, null);
		MenuRegistry.register(MenuChave.CADASTROS_PLANO_DE_LICENCA, itemPlaLic);
		menuCadastro.add(itemPlaLic);

		return menuCadastro;
	}

	private JMenu criarMenuConsulta() {
		JMenu menuConsulta = MenuFactory.createMenu("CONSULTAS", null);

		JMenuItem itemEmpCliCon = MenuFactory.createMenuItem("EMPRESAS CLIENTES", null, null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_EMPRESAS_CLIENTES, itemEmpCliCon);
		menuConsulta.add(itemEmpCliCon);

		JMenuItem itemUsuCon = MenuFactory.createMenuItem("USUÁRIOS", null, null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_USUARIOS, itemUsuCon);
		menuConsulta.add(itemUsuCon);

		JMenuItem itemPlaLicCon = MenuFactory.createMenuItem("PLANO DE LICENÇA", null, null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_PLANO_DE_LICENCA, itemPlaLicCon);
		menuConsulta.add(itemPlaLicCon);

		JMenuItem itemLicCon = MenuFactory.createMenuItem("LICENÇAS", null, null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_LICENCAS, itemLicCon);
		menuConsulta.add(itemLicCon);

		JMenuItem itemDisCon = MenuFactory.createMenuItem("DISPOSITIVOS", null, null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_DISPOSITIVOS, itemDisCon);
		menuConsulta.add(itemDisCon);

		JMenuItem itemPagCon = MenuFactory.createMenuItem("PAGAMENTOS", null, null, null);
		MenuRegistry.register(MenuChave.CONSULTAS_PAGAMENTOS, itemPagCon);
		menuConsulta.add(itemPagCon);

		return menuConsulta;
	}

	private JMenu criarMenuLicenca() {
		JMenu menuLicenca = MenuFactory.createMenu("LICENÇAS", null);

		JMenuItem itemGerLic = MenuFactory.createMenuItem("GERAR LICENÇA", null, null, null);
		MenuRegistry.register(MenuChave.LICENCAS_GERAR_LICENCA, itemGerLic);
		menuLicenca.add(itemGerLic);

		JMenuItem itemRenLic = MenuFactory.createMenuItem("RENOVAR LICENÇA", null, null, null);
		MenuRegistry.register(MenuChave.LICENCAS_RENOVAR_LICENCA, itemRenLic);
		menuLicenca.add(itemRenLic);

		JMenuItem itemAtiBlo = MenuFactory.createMenuItem("ATIVAR / BLOQUEAR LICENÇA", null, null, null);
		MenuRegistry.register(MenuChave.LICENCAS_ATIVAR_BLOQUEAR_LICENCA, itemAtiBlo);
		menuLicenca.add(itemAtiBlo);

		JMenuItem itemDisVin = MenuFactory.createMenuItem("DISPOSITIVOS VINCULADOS", null, null, null);
		MenuRegistry.register(MenuChave.LICENCAS_DISPOSITIVOS_VINCULADOS, itemDisVin);
		menuLicenca.add(itemDisVin);

		JMenuItem itemHisLic = MenuFactory.createMenuItem("HISTÓRICO DA LICENÇA", null, null, null);
		MenuRegistry.register(MenuChave.LICENCAS_HISTORICO_DA_LICENCA, itemHisLic);
		menuLicenca.add(itemHisLic);

		return menuLicenca;
	}

	private JMenu criarMenuFinanceiro() {
		JMenu menuFinanceiro = MenuFactory.createMenu("FINANCEIRO", null);

		JMenuItem itemRegPag = MenuFactory.createMenuItem("REGISTRAR PAGAMENTO", null, null, null);
		MenuRegistry.register(MenuChave.FINANCEIRO_REGISTRAR_PAGAMENTO, itemRegPag);
		menuFinanceiro.add(itemRegPag);

		JMenuItem itemConPg = MenuFactory.createMenuItem("CONSULTAR PAGAMENTOS", null, null, null);
		MenuRegistry.register(MenuChave.FINANCEIRO_CONSULTAR_PAGAMENTOS, itemConPg);
		menuFinanceiro.add(itemConPg);

		JMenuItem itemFatRec = MenuFactory.createMenuItem("FATURAMENTO / RECEITA", null, null, null);
		MenuRegistry.register(MenuChave.FINANCEIRO_FATURAMENTO_RECEITA, itemFatRec);
		menuFinanceiro.add(itemFatRec);

		JMenuItem itemRel = MenuFactory.createMenuItem("RELATÓRIOS", null, null, null);
		MenuRegistry.register(MenuChave.FINANCEIRO_RELATORIOS, itemRel);
		menuFinanceiro.add(itemRel);

		return menuFinanceiro;
	}

	private JMenu criarMenuRelatorio() {
		JMenu menuRelatorios = MenuFactory.createMenu("RELATÓRIOS GERENCIAIS", null);

		JMenuItem itemLicAtiExp = MenuFactory.createMenuItem("LICENÇAS ATIVAS / EXPIRADAS", null, null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_LICENCAS_ATIVAS_EXPERIDAS, itemLicAtiExp);
		menuRelatorios.add(itemLicAtiExp);

		JMenuItem itemLicVen = MenuFactory.createMenuItem("LICENÇAS A VENCER", null, null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_LICENCAS_A_VENCER, itemLicVen);
		menuRelatorios.add(itemLicVen);

		JMenuItem itemRecPer = MenuFactory.createMenuItem("RECEITA POR PERIODO", null, null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_RECEITA_POR_PERIODO, itemRecPer);
		menuRelatorios.add(itemRecPer);

		JMenuItem itemRecPla = MenuFactory.createMenuItem("RECEITA POR PLANO", null, null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_RECEITA_POR_PLANO, itemRecPla);
		menuRelatorios.add(itemRecPla);

		JMenuItem itemEmpPla = MenuFactory.createMenuItem("EMPRESAS POR PLANO", null, null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_EMPRESAS_POR_PLANO, itemEmpPla);
		menuRelatorios.add(itemEmpPla);

		JMenuItem itemDisLic = MenuFactory.createMenuItem("DISPOSITIVOS POR LICENÇA", null, null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_DISPOSITIVOS_POR_LICENCA, itemDisLic);
		menuRelatorios.add(itemDisLic);

		JMenuItem itemUsuEmp = MenuFactory.createMenuItem("USUÁRIOS POR EMPRESA", null, null, null);
		MenuRegistry.register(MenuChave.RELATORIOS_GERENCIAIS_USUARIOS_POR_EMPRESA, itemUsuEmp);
		menuRelatorios.add(itemUsuEmp);

		return menuRelatorios;
	}

	private JMenu criarMenuAuditoria() {
		JMenu menuAuditoria = MenuFactory.createMenu("AUDITORIA", null);

		JMenuItem itemLogSis = MenuFactory.createMenuItem("LOG DO SISTEMA", null, null, null);
		MenuRegistry.register(MenuChave.AUDITORIA_LOG_DO_SISTEMA, itemLogSis);
		menuAuditoria.add(itemLogSis);

		JMenuItem itemHisLic = MenuFactory.createMenuItem("HISTÓRICO DE LICENÇAS", null, null, null);
		MenuRegistry.register(MenuChave.AUDITORIA_HISTORICO_DE_LICENCAS, itemHisLic);
		menuAuditoria.add(itemHisLic);

		return menuAuditoria;
	}

	private JMenu criarMenuConfiguração() {
		JMenu menuConfiguracao = MenuFactory.createMenu("CONFIGURAÇÃO", null);

		JMenuItem itemEmpFor = MenuFactory.createMenuItem("EMPRESA FORNECEDORA", null, null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_EMPRESA_FORNECEDORA, itemEmpFor);
		menuConfiguracao.add(itemEmpFor);

		JMenuItem itemParSis = MenuFactory.createMenuItem("PARÂMETRO DO SISTEMA", null, null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_PARAMETRO_SISTEMA, itemParSis);
		menuConfiguracao.add(itemParSis);

		JMenuItem itemParLic = MenuFactory.createMenuItem("PARÂMETRO DE LICENÇA", null, null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_PARAMETRO_SISTEMA, itemParLic);
		menuConfiguracao.add(itemParLic);

		JMenu menuSeguranca = MenuFactory.createMenu("SEGURANÇA", null);

		JMenuItem itemPer = MenuFactory.createMenuItem("PERMISSÕES", null, null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_PERMISSAO, itemPer);
		menuSeguranca.add(itemPer);

		JMenuItem itemUsuPer = MenuFactory.createMenuItem("USUÁRIOS X PERMISSÕES", null, null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_USUARIOS_PERMISSOES, itemUsuPer);
		menuSeguranca.add(itemUsuPer);

		menuConfiguracao.add(menuSeguranca);

		JMenu menuManutencao = MenuFactory.createMenu("MANUTENÇÃO", null);

		JMenuItem itemBacDad = MenuFactory.createMenuItem("BACKUP DE DADOS", null, null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_BACKUP_DE_DADOS, itemBacDad);
		menuManutencao.add(itemBacDad);

		JMenuItem itemResBac = MenuFactory.createMenuItem("RESTAURAR BACKUP", null, null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_RESTAURAR_BACKUP, itemResBac);
		menuManutencao.add(itemResBac);

		JMenuItem itemLimLog = MenuFactory.createMenuItem("LIMPEZA DE LOGS", null, null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_LIMPEZA_DE_LOGS, itemLimLog);
		menuManutencao.add(itemLimLog);

		menuConfiguracao.add(menuManutencao);

		JMenuItem itemVerSis = MenuFactory.createMenuItem("VERSÃO DO SISTEMA", null, null, null);
		MenuRegistry.register(MenuChave.CONFIGURACAO_VERSAO_DO_SISTEMA, itemVerSis);
		menuConfiguracao.add(itemVerSis);

		return menuConfiguracao;
	}

	private JMenu criarMenuAjuda() {
		JMenu menuAjuda = MenuFactory.createMenu("AJUDA", null);

		JMenuItem itemManSis = MenuFactory.createMenuItem("MANUAL DO SISTEMA", null, null, null);
		MenuRegistry.register(MenuChave.AJUDA_MANUAL_DO_SISTEMA, itemManSis);
		menuAjuda.add(itemManSis);

		JMenuItem itemSupTec = MenuFactory.createMenuItem("SUPORTE TÉCNICO", null, null, null);
		MenuRegistry.register(MenuChave.AJUDA_SUPORTE_TECNICO, itemSupTec);
		menuAjuda.add(itemSupTec);

		JMenuItem itemSobSis = MenuFactory.createMenuItem("SOBRE O SISTEMA", null, null, null);
		MenuRegistry.register(MenuChave.AJUDA_SOBRE_O_SISTEMA, itemSobSis);
		menuAjuda.add(itemSobSis);

		JMenuItem itemVerAtu = MenuFactory.createMenuItem("VERIFICAR ATUALIZAÇÕES", null, null, null);
		MenuRegistry.register(MenuChave.AJUDA_VERIFICAR_ATUALIZACOES, itemVerAtu);
		menuAjuda.add(itemVerAtu);

		return menuAjuda;
	}

	private JPanel criarPainelCentro() {
		JPanel painel = PanelFactory.createPanel("fill, insets 0, gap 0", "[grow]", "[grow]");
		desktopPane = DesktopFactory.criarDesktopPane();
		painel.add(desktopPane, "grow");
		return painel;
	}

	private JPanel criarPainelLateral() {
		JPanel painel = PanelFactory.createPanel("fill, insets 0", "[grow]", "[]10[]10[]30[]");
		lblHora = LabelFactory.createImageLabel("", IconFactory.data());
		painel.add(LabelFactory.createImageLabel("ADMINISTRADOR", IconFactory.usuario()), "cell 0 0, alignx center");
		painel.add(lblHora, "cell 0 1, alignx center");
		painel.add(LabelFactory.createImageLabel("", IconFactory.logo()), "cell 0 2, alignx center");
		return painel;

	}

	private JPanel criarPainelRodape() {
		JLabel lblAutor = LabelFactory.createLabel("SISTEMA DESENVOLVIDO POR XXXXXXXXXXXXXXXXXXXXXXXXX - VERSÃO 1.0.0");
		JPanel painel = PanelFactory.createPanel("fill,insets 0", "[grow]", "[]");
		painel.add(lblAutor, "center");
		return painel;
	}

	private void iniciarRelogio() {
		Timer timer = new Timer(1000, e -> {
			LocalDateTime agora = LocalDateTime.now();
			DateTimeFormatter formatador = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
			lblHora.setText(agora.format(formatador));
		});
		timer.start();
	}
}
