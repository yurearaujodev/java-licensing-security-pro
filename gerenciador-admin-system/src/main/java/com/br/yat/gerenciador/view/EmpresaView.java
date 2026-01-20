package com.br.yat.gerenciador.view;

import javax.swing.JPanel;
import net.miginfocom.swing.MigLayout;

import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.util.ui.ButtonFactory;
import com.br.yat.gerenciador.util.ui.DesktopFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;
import com.br.yat.gerenciador.view.empresa.DadoBancarioPanel;
import com.br.yat.gerenciador.view.empresa.DadoComplementarPanel;
import com.br.yat.gerenciador.view.empresa.DadoContatoPanel;
import com.br.yat.gerenciador.view.empresa.DadoPrincipalPanel;
import com.br.yat.gerenciador.view.empresa.DadoEnderecoPanel;
import com.br.yat.gerenciador.view.empresa.DadoFiscalPanel;
import com.br.yat.gerenciador.view.empresa.DadoRepresentantePanel;

import java.util.HashMap;
import java.util.Map;

import javax.swing.JButton;

import javax.swing.JInternalFrame;
import javax.swing.JTabbedPane;

public class EmpresaView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private DadoPrincipalPanel painelPrincipal;
	private DadoEnderecoPanel painelEndereco;
	private DadoContatoPanel painelContato;
	private DadoFiscalPanel painelFiscal;
	private DadoRepresentantePanel painelRepresentante;
	private DadoBancarioPanel painelBancario;
	private DadoComplementarPanel painelComplementar;
	
	private JTabbedPane tabbedPane;
	private Map<String, JPanel> panels = new HashMap<>();

	private JButton btnSalvar;
	private JButton btnCancelar;
	private JButton btnNovo;

	public EmpresaView() {
		super("Cadastro de Empresa Fornecedor/Cliente", true, true, true, true);
		setLayout(new MigLayout("gapx 15, gapy 15", "[grow,fill]", "[grow][]"));

		JTabbedPane tabbedPane = criarTabbedPane();
		add(tabbedPane, "cell 0 0,grow");
		
		JPanel criarBotoes = criarBotoes();
		add(criarBotoes, "cell 0 1, right");
		setSize(690, 530);
	}
	
	private JTabbedPane criarTabbedPane() {
		tabbedPane = DesktopFactory.createTabbedPane();

		this.painelPrincipal = new DadoPrincipalPanel();
		tabbedPane.addTab("DADOS PRINCIPAIS", null, painelPrincipal, null);
		panels.put("DADOS PRINCIPAIS", painelPrincipal);
		
		this.painelEndereco = new DadoEnderecoPanel();
		tabbedPane.addTab("ENDEREÇO", null, painelEndereco, null);
		panels.put("ENDEREÇO", painelEndereco);
		
		this.painelContato = new DadoContatoPanel();
		tabbedPane.addTab("CONTATOS", null, painelContato, null);
		panels.put("CONTATOS", painelContato);
		
		this.painelFiscal = new DadoFiscalPanel();
		tabbedPane.addTab("DADOS FISCAIS", null, painelFiscal, null);
		panels.put("DADOS FISCAIS", painelFiscal);
		
		this.painelRepresentante = new DadoRepresentantePanel();
		tabbedPane.addTab("REPRESENTANTE LEGAL", null, painelRepresentante, null);
		panels.put("REPRESENTANTE LEGAL", painelRepresentante);
		
		this.painelBancario = new DadoBancarioPanel();
		tabbedPane.addTab("DADOS BANCÁRIOS", null, painelBancario, null);
		panels.put("DADOS BANCÁRIOS", painelBancario);
		
		this.painelComplementar = new DadoComplementarPanel();
		tabbedPane.addTab("INFORMAÇÕES COMPLEMENTARES", null, painelComplementar, null);
		panels.put("INFORMAÇÕES COMPLEMENTARES", painelComplementar);
		
		return tabbedPane;
	}
	
	private JPanel criarBotoes() {
		JPanel panel = PanelFactory.createPanel("insets 5", "[left][grow][right]", "[]");
		
		btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());
		panel.add(btnNovo,"cell 0 0,h 35!,w 140!");
		
		btnCancelar = ButtonFactory.createPrimaryButton("CANCELAR", IconFactory.cancelar());
		panel.add(btnCancelar,"cell 2 0,h 35!,w 140!");
		
		btnSalvar = ButtonFactory.createPrimaryButton("SALVAR", IconFactory.salvar());
		panel.add(btnSalvar,"cell 2 0,h 35!,w 140!");
		return panel;
	}
	
	public DadoPrincipalPanel getDadoPrincipal() {
		return painelPrincipal;
	}
	
	public DadoEnderecoPanel getDadoEndereco() {
		return painelEndereco;
	}
	
	public DadoContatoPanel getDadoContato() {
		return painelContato;
	}
	
	public DadoFiscalPanel getDadoFiscal() {
		return painelFiscal;
	}
	
	public DadoRepresentantePanel getDadoRepresentante() {
		return painelRepresentante;
	}
	
	public DadoBancarioPanel getDadoBancario() {
		return painelBancario;
	}
	
	public DadoComplementarPanel getDadoComplementar() {
		return painelComplementar;
	}
	
	public JButton getBtnSalvar() {
		return btnSalvar;
	}
	public JButton getBtnNovo() {
		return btnNovo;
	}
	public JButton getBtnCancelar() {
		return btnCancelar;
	}
	
	public JPanel getPanelByName(String name) {
		return panels.get(name);
	}
	
	public JTabbedPane getTabbedPane() {
		return tabbedPane;
	}

}
