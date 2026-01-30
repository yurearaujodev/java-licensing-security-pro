package com.br.yat.gerenciador.view.empresa;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.br.yat.gerenciador.view.factory.ButtonFactory;
import com.br.yat.gerenciador.view.factory.ComboBoxFactory;
import com.br.yat.gerenciador.view.factory.FieldFactory;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import com.br.yat.gerenciador.view.factory.PanelFactory;
import com.br.yat.gerenciador.view.factory.TableFactory;

import net.miginfocom.swing.MigLayout;

public class DadoComplementarPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField txtLogo;
	private JTextField txtRamo;
	private JSpinner spinnerNum;
	private JButton btnLogo;
	private JButton btnDoc;
	private JTextArea jtaobs;
	private JScrollPane jsobs;
	private JComboBox<String> cbTipo;
	private JButton btnAdicionar;
	private JButton btnRemover;
	private JTable tabela;


	public DadoComplementarPanel() {
		setLayout(new MigLayout("fill", "[grow]", "[grow]"));
		montarTela();
	}

	private void montarTela() {
		JPanel panel = PanelFactory.createPanel("gapx 10, gapy 10", "[right][grow,fill][right][grow,fill]",
				"[][][][][grow]");
		montarCampos(panel);
		add(panel, "grow");
	}

	private void montarCampos(JPanel panel) {
		panel.add(LabelFactory.createLabel("LOGOTIPO:"),"cell 0 0, alignx trailing");
		txtLogo = FieldFactory.createTextField(20);
		txtLogo.setEditable(false);
		panel.add(txtLogo,"cell 1 0 2 1,growx, h 25!");
		
		btnLogo = ButtonFactory.createPrimaryButton("UPLOAD LOGOTIPO");
		panel.add(btnLogo,"cell 3 0, growx, h 25!");
		
		panel.add(LabelFactory.createLabel("NÚM. DE FUNCIONÁRIOS:"),"cell 0 1,alignx trailing");
		spinnerNum = FieldFactory.createSpinnerNumber(1, 0, 120, 1);
		panel.add(spinnerNum,"cell 1 1,growx, h 25!");
		
		panel.add(LabelFactory.createLabel("RAMO DE ATIVIDADE:"),"cell 2 1, alignx trailing");
		txtRamo = FieldFactory.createTextField(20);
		panel.add(txtRamo,"cell 3 1,growx, h 25!");
		
		panel.add(LabelFactory.createLabel("OBSERVAÇÕES:"),"cell 0 2,alignx trailing, aligny center");
		jtaobs = FieldFactory.createTextArea();
		jsobs = FieldFactory.createTextAreaScroll(jtaobs);
		panel.add(jsobs,"cell 1 2 3 1, growx, h 60!");
		
		panel.add(LabelFactory.createLabel("TIPO DOCUMENTO:"),"cell 0 3,alignx trailing");
		cbTipo = ComboBoxFactory.createComboBox("SELECIONE UMA OPÇÃO","CONTRATO SOCIAL","ALVARÁ DE FUNCIONAMENTO","CERTIDÕES NEGATIVAS","REGISTRO NA JUNTA COMERCIAL","LICENÇAS ESPECÍFICAS");
		panel.add(cbTipo,"cell 1 3 2 1,growx, h 25!");
		
		btnAdicionar = ButtonFactory.createPrimaryButton("ADICIONAR");
		panel.add(btnAdicionar, "cell 3 3 4 1, split 2, align right,w 120!, h 25!");
		btnRemover = ButtonFactory.createPrimaryButton("REMOVER");
		panel.add(btnRemover, "w 120!, h 25!");

		tabela = TableFactory.createDefaultTable(
				new String[] {"ID", "TIPO", "NOME DO ARQUIVO", "DATA INCLUSÃO"});
		configurarColunas(tabela);
		panel.add(TableFactory.createTableScrolling(tabela), "cell 0 4 4 1,growx,hmin 80,pushy");
		
	}
	
	private void configurarColunas(JTable tabela) {
		tabela.getColumnModel().getColumn(0).setPreferredWidth(0);
		tabela.getColumnModel().getColumn(0).setMinWidth(0);
		tabela.getColumnModel().getColumn(0).setMaxWidth(0);

		tabela.getColumnModel().getColumn(1).setPreferredWidth(250);
		tabela.getColumnModel().getColumn(1).setMinWidth(250);
		tabela.getColumnModel().getColumn(1).setMaxWidth(250);

		tabela.getColumnModel().getColumn(2).setPreferredWidth(680);
		tabela.getColumnModel().getColumn(2).setMinWidth(680);
		tabela.getColumnModel().getColumn(2).setMaxWidth(680);

		tabela.getColumnModel().getColumn(3).setPreferredWidth(360);
		tabela.getColumnModel().getColumn(3).setMinWidth(360);
		tabela.getColumnModel().getColumn(3).setMaxWidth(360);

	}
	
	
	public JButton getBtnDocumento() {
		return btnDoc;
	}
	public JButton getBtnLogoTipo() {
		return btnLogo;
	}
	
	public JComboBox<String> getCbTipo() {
		return cbTipo;
	}
	
	public String getTipoDocumento() {
		return String.valueOf(cbTipo.getSelectedItem());
	}
	
	public void setTipo(String tipo){
		cbTipo.setSelectedItem(tipo);
	}
	
	public String getLogo() {
		return txtLogo.getText();
	}
	
	public String getFuncionarios() {
		return String.valueOf(spinnerNum.getValue());
	}
	
	public void setFuncionarios(int numero) {
		spinnerNum.setValue(numero);
	}
	
	public JSpinner getSpinnerFuncionario() {
		return spinnerNum;
	}
	
	public String getObservacoes() {
		return jtaobs.getText();
	}
	
	public void setObservacoes(String observacoes) {
		jtaobs.setText(observacoes);
	}
	
	public void setLogo(String logo) {
		txtLogo.setText(logo);
	}
	
	public String getRamo() {
		return txtRamo.getText();
	}
	
	public void setRamo(String ramo) {
		txtRamo.setText(ramo);
	}
	
	public JTextField getTxtRamo() {
		return txtRamo;
	}
	
	public JTextField getTxtLogo() {
		return txtLogo;
	}
	
	public JButton getBtnAdicionar() {
		return btnAdicionar;
	}

	public JButton getBtnRemover() {
		return btnRemover;
	}

	public JTable getTabela() {
		return tabela;
	}
	
	public void limpar() {
		txtLogo.setText("");
		txtRamo.setText("");
		jtaobs.setText("");
		cbTipo.setSelectedIndex(0);
		spinnerNum.setValue(0);
	}
	
	public void desativarAtivar(boolean ativa) {
		txtRamo.setEnabled(ativa);
		jtaobs.setEnabled(ativa);
		cbTipo.setEnabled(ativa);
		spinnerNum.setEnabled(ativa);
	}

}
