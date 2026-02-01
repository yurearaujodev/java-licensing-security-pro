package com.br.yat.gerenciador.view;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JRootPane;

import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.DesktopFactory;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import com.br.yat.gerenciador.view.factory.MenuFactory;
import com.br.yat.gerenciador.view.factory.PanelFactory;
import com.br.yat.gerenciador.view.menu.AjudaMenuBuilder;
import com.br.yat.gerenciador.view.menu.AuditoriaMenuBuilder;
import com.br.yat.gerenciador.view.menu.CadastroMenuBuilder;
import com.br.yat.gerenciador.view.menu.ConfiguracaoMenuBuilder;
import com.br.yat.gerenciador.view.menu.ConsultaMenuBuilder;
import com.br.yat.gerenciador.view.menu.DashboardMenuBuilder;
import com.br.yat.gerenciador.view.menu.FinanceiroMenuBuilder;
import com.br.yat.gerenciador.view.menu.LicencaMenuBuilder;
import com.br.yat.gerenciador.view.menu.RelatorioMenuBuilder;

import javax.swing.JDesktopPane;
import javax.swing.JMenuBar;
import javax.swing.JLabel;

public class MenuPrincipal extends JFrame {

	private static final long serialVersionUID = 1L;
	private JPanel painelPrincipal;
	private JLabel lblHora;
	private JLabel lblUsuarioLogado;
	private JDesktopPane desktopPane;

	public MenuPrincipal() {
		configurarFrame();
		montarTela();
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
		painel.add(LabelFactory.createImageLabel("", IconFactory.logo()), "cell 0 2, alignx center");
		return painel;

	}

	private JPanel criarPainelRodape() {
		JLabel lblAutor = LabelFactory.createLabel("SISTEMA DESENVOLVIDO POR XXXXXXXXXXXXXXXXXXXXXXXXX - VERSÃO 1.0.0");
		JPanel painel = PanelFactory.createPanel("fill,insets 0", "[grow]", "[]");
		painel.add(lblAutor, "center");
		return painel;
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
