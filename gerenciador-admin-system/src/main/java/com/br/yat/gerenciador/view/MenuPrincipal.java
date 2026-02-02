package com.br.yat.gerenciador.view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JToolBar;
import javax.swing.SwingConstants;

import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.DesktopFactory;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import com.br.yat.gerenciador.view.factory.MenuFactory;
import com.br.yat.gerenciador.view.factory.PanelFactory;
import com.br.yat.gerenciador.view.factory.ToolBarFactory;
import com.br.yat.gerenciador.view.menu.AjudaMenuBuilder;
import com.br.yat.gerenciador.view.menu.AuditoriaMenuBuilder;
import com.br.yat.gerenciador.view.menu.CadastroMenuBuilder;
import com.br.yat.gerenciador.view.menu.ConfiguracaoMenuBuilder;
import com.br.yat.gerenciador.view.menu.ConsultaMenuBuilder;
import com.br.yat.gerenciador.view.menu.DashboardMenuBuilder;
import com.br.yat.gerenciador.view.menu.FinanceiroMenuBuilder;
import com.br.yat.gerenciador.view.menu.LicencaMenuBuilder;
import com.br.yat.gerenciador.view.menu.RelatorioMenuBuilder;

import java.awt.Color;

import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JMenuBar;
import javax.swing.JLabel;

public class MenuPrincipal extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel painelPrincipal;
	private JLabel lblHora;
	private JLabel lblLogo;
	private JLabel lblUsuarioLogado;
	private JDesktopPane desktopPane;
	private JButton btnLogout;
	private JLabel lblStatusBanco;

	public MenuPrincipal() {
		configurarFrame();
		montarTela();
	}

	private void montarTela() {
		painelPrincipal = PanelFactory.createPanel("fill,insets 0,gap 0", "[grow][200!]", "[][][grow][30!]");
		setContentPane(painelPrincipal);

		painelPrincipal.add(criarPainelMenu(), "cell 0 0 2 1, growx");

		painelPrincipal.add(criarBarraFerramentas(), "cell 0 1 2 1, growx");

		painelPrincipal.add(criarPainelCentro(), "cell 0 2,grow");
		painelPrincipal.add(criarPainelLateral(), "cell 1 2,grow");
		painelPrincipal.add(criarPainelRodape(), "cell 0 3 2 1, grow");
	}

	private void configurarFrame() {
		setTitle("SISTEMA DE GERENCIAMENTO DE LICENÇA - MENU PRINCIPAL");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setSize(1300, 800);
		setLocationRelativeTo(null);
		setExtendedState(JFrame.MAXIMIZED_BOTH);
		getRootPane().setWindowDecorationStyle(JRootPane.FRAME);
		setVisible(true);

	}

	private JPanel criarPainelMenu() {
		JMenuBar menuBar = MenuFactory.createMenuBar();

		menuBar.add(DashboardMenuBuilder.builder());
		menuBar.add(CadastroMenuBuilder.builder());
		menuBar.add(ConsultaMenuBuilder.builder());
		menuBar.add(LicencaMenuBuilder.builder());
		menuBar.add(FinanceiroMenuBuilder.builder());
		menuBar.add(RelatorioMenuBuilder.builder());
		menuBar.add(AuditoriaMenuBuilder.builder());
		menuBar.add(ConfiguracaoMenuBuilder.builder());
		menuBar.add(AjudaMenuBuilder.builder());

		return MenuFactory.createMenuPanel(menuBar);
	}

	private JPanel criarPainelCentro() {
		JPanel painel = PanelFactory.createPanel("fill, insets 0, gap 0", "[grow]", "[grow]");
		desktopPane = DesktopFactory.createDesktopPane();
		painel.add(desktopPane, "grow");
		return painel;
	}

	private JPanel criarPainelLateral() {
		JPanel painel = PanelFactory.createPanel("fill, insets 0", "[grow]", "[]10[]10[]30[]");
		lblHora = LabelFactory.createImageLabel("", IconFactory.data());
		lblUsuarioLogado = LabelFactory.createImageLabel("CONECTANDO...", IconFactory.usuario());
		painel.add(lblUsuarioLogado, "cell 0 0, alignx center");
		painel.add(lblHora, "cell 0 1, alignx center");
		lblLogo = LabelFactory.createImageLabel("", IconFactory.logo());
		lblLogo.setOpaque(false);
		lblLogo.setBackground(new Color(0,0,0,0));
		painel.add(lblLogo, "cell 0 2, alignx center");
		return painel;

	}

	private JPanel criarPainelRodape() {
		JLabel lblAutor = LabelFactory.createLabel("SISTEMA DESENVOLVIDO POR YURE ARAUJO TORRES - VERSÃO 1.0.0");

		lblStatusBanco = LabelFactory.createImageLabel("CONECTANDO...", null);
		lblStatusBanco.setHorizontalTextPosition(SwingConstants.RIGHT);
		lblStatusBanco.setIconTextGap(8);

		JPanel painel = PanelFactory.createPanel("fill, insets 2 10 2 10", "[200!][grow][200!]", "[]");

		painel.add(lblStatusBanco, "cell 0 0, left");

		painel.add(lblAutor, "cell 1 0, center");

		painel.add(new JLabel(""), "cell 2 0");

		return painel;
	}

	public void atualizarStatusBanco(boolean online) {
		lblStatusBanco.setHorizontalTextPosition(javax.swing.SwingConstants.RIGHT);

		if (online) {
			lblStatusBanco.setText("ONLINE");
			lblStatusBanco.setForeground(new Color(0, 150, 0));
			lblStatusBanco.setIcon(IconFactory.bancoOk());
		} else {
			lblStatusBanco.setText("OFFLINE");
			lblStatusBanco.setForeground(Color.RED);
			lblStatusBanco.setIcon(IconFactory.bancoErro());
		}
		lblStatusBanco.setFont(lblStatusBanco.getFont().deriveFont(java.awt.Font.BOLD));
	}

	private JToolBar criarBarraFerramentas() {
		JToolBar toolBar = ToolBarFactory.createToolBar();

		btnLogout = ToolBarFactory.createToolBarButton("SAIR DO SISTEMA", IconFactory.logout(), null);

		toolBar.add(btnLogout);
		return toolBar;
	}

	public JLabel getLblLogo() {
		return lblLogo;
	}

	public JButton getBtnLogout() {
		return btnLogout;
	}

	public JLabel getHora() {
		return lblHora;
	}

	public void setNomeUsuario(String nome) {
		lblUsuarioLogado.setText(nome.toUpperCase());
	}

	public JDesktopPane getDesktopPane() {
		return desktopPane;
	}
}
