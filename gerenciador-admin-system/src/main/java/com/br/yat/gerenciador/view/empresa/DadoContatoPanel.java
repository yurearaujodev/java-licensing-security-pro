package com.br.yat.gerenciador.view.empresa;

import java.awt.FlowLayout;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.br.yat.gerenciador.controller.DadoContatoController;
import com.br.yat.gerenciador.util.ValidationUtils;
import com.br.yat.gerenciador.util.ui.ComboBoxFactory;
import com.br.yat.gerenciador.util.ui.FieldFactory;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;
import com.br.yat.gerenciador.util.ui.TableFactory;

import net.miginfocom.swing.MigLayout;

public class DadoContatoPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField txtGenerico;
	private JFormattedTextField ftxtTelefone;
	private JComboBox<String> cbTipoCont;
	private JPanel painelCampo;
	private JTable tabela;
	private JButton adicionar;
	private JButton remover;
	
	public DadoContatoPanel() {
		setLayout(new MigLayout("fill", "[grow]", "[][][grow][]"));
		
		cbTipoCont = ComboBoxFactory.createComboBox("SELECIONE","FIXO","CELULAR","WHATSAPP","E-MAIL","REDE SOCIAL","SITE");
		add(LabelFactory.createLabel("TIPO CONTATO: "), "split 2");
		add(cbTipoCont,"grow,wrap");
		
		painelCampo = PanelFactory.createPanel("", "[grow]","[]");
		add(painelCampo,"grow,wrap");
		
		txtGenerico = FieldFactory.createTextField(20);
		ftxtTelefone = FieldFactory.createFormattedField();
		
		tabela = TableFactory.createDefaultTable(new String[] {"TIPO","VALOR"});
		add(TableFactory.createTableScrolling(tabela),"grow,wrap");
		
		JPanel painelBotoes = new JPanel(new FlowLayout(FlowLayout.RIGHT));
		adicionar = new JButton("Adicionar");
		remover = new JButton("Remover");
		painelBotoes.add(adicionar);
		painelBotoes.add(remover);
		add(painelBotoes,"growx,wrap");
		
		new DadoContatoController(this);
	}
	
	public JComboBox<String> getCbTipo(){
		return cbTipoCont;
	}

	public JPanel getPainelCampo() {
		return painelCampo;
	}
	
	public JTextField getTxtGenerico() {
		return txtGenerico;
	}
	
	public JFormattedTextField getftxtTelefone() {
		return ftxtTelefone;
	}
	
	public JTable getTabela() {
		return tabela;
	}
	
	public JButton getAdicionar() {
		return adicionar;
	}
	public JButton getRemoverr() {
		return remover;
	}
	
	
	

	private void montarCampos(JPanel panel) {
		panel.add(LabelFactory.createLabel("TIPO CONTATO: "), "cell 0 0,alignx trailing");
		cbTipoCont = ComboBoxFactory.createComboBox("SELECIONE","TELEFONE FIXO","TELEFONE CELULAR","WHATSAPP","E-MAIL","REDE SOCIAL","SITE");
		panel.add(cbTipoCont,"cell 1 0, alignx trailing");
		
		
		panel.add(txtGenerico, "cell 1 1 3 1,growx, h 25!,wmin 250, wmax 850");

		
		panel.add(ftxtTelefone, "cell 1 2,h 25!,wmin 120,wmax 500");

	}
}
