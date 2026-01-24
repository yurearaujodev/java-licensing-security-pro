package com.br.yat.gerenciador.view.empresa;

import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.br.yat.gerenciador.util.ui.ComboBoxFactory;
import com.br.yat.gerenciador.util.ui.FieldFactory;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;

import net.miginfocom.swing.MigLayout;

public class DadoPrincipalPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTextField txtFantasia;
	private JTextField txtRazao;
	private JTextField txtCodigo;
	private JTextField txtInscricaoEstadual;
	private JTextField txtInscricaoMunicipal;

	private JFormattedTextField ftxtFundacao;
	private JFormattedTextField ftxtDocumento;
	private JFormattedTextField ftxtCapital;

	private JComboBox<String> cbTipoDocumento;
	private JComboBox<String> cbSituacao;
	private JComboBox<String> cbCadastro;

	Map<String, String> mascaras = MaskFactory.createMask();

	public DadoPrincipalPanel() {
		setLayout(new MigLayout("fill", "[grow]", "[grow]"));

		montarTela();
	}

	private void montarTela() {
		JPanel panel = PanelFactory.createPanel("gapx 10, gapy 10", "[right][grow,fill][right][grow,fill]",
				"[][][][][][][]");
		montarCampos(panel);
		add(panel, "grow");
	}

	private void montarCampos(JPanel panel) {
		panel.add(LabelFactory.createLabel("CÓDIGO:"), "cell 0 0 , alignx trailing");
		txtCodigo = FieldFactory.createTextField(20);
		txtCodigo.setEnabled(false);
		panel.add(txtCodigo, "cell 1 0,growx, h 25!,wmax 120");

		panel.add(LabelFactory.createLabel("TIPO DOC.:"), "cell 0 1,alignx trailing");
		cbTipoDocumento = ComboBoxFactory.createComboBox("SELECIONE", "CNPJ", "CPF");
		panel.add(cbTipoDocumento, "cell 1 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("TP. CADASTRO:"), "cell 2 1, alignx trailing");
		cbCadastro = ComboBoxFactory.createComboBox("SELECIONE", "CLIENTE", "FORNECEDORA");
		panel.add(cbCadastro, "cell 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("DOCUMENTO:"), "cell 0 2,alignx trailing");
		ftxtDocumento = FieldFactory.createFormattedField();
		panel.add(ftxtDocumento, "cell 1 2,growx, h 25!");

		panel.add(LabelFactory.createLabel("DT. FUNDAÇÂO:"), "cell 2 2,alignx trailing");
		ftxtFundacao = FieldFactory.createFormattedField();
		FormatterUtils.applyDateMask(ftxtFundacao, mascaras.get("FUNDACAO"));
		panel.add(ftxtFundacao, "cell 3 2,growx, h 25!");

		panel.add(LabelFactory.createLabel("RAZÃO SOCIAL:"), "cell 0 3,alignx trailing");
		txtRazao = FieldFactory.createTextField(20);
		panel.add(txtRazao, "cell 1 3 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("NOME FANTASIA:"), "cell 0 4,alignx trailing");
		txtFantasia = FieldFactory.createTextField(20);
		panel.add(txtFantasia, "cell 1 4 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("INSC. ESTADUAL:"), "cell 0 5,alignx trailing");
		txtInscricaoEstadual = FieldFactory.createTextField(20);
		panel.add(txtInscricaoEstadual, "cell 1 5,growx, h 25!");

		panel.add(LabelFactory.createLabel("INSC. MUNICIPAL:"), "cell 2 5,alignx trailing");
		txtInscricaoMunicipal = FieldFactory.createTextField(20);
		panel.add(txtInscricaoMunicipal, "cell 3 5,growx, h 25!");

		panel.add(LabelFactory.createLabel("SITUAÇÂO GERAL:"), "cell 0 6,alignx trailing");
		cbSituacao = ComboBoxFactory.createComboBox("SELECIONE", "ATIVA", "INAPTA", "SUSPENSA", "BAIXADA", "NULA");
		panel.add(cbSituacao, "cell 1 6,growx, h 25!");

		panel.add(LabelFactory.createLabel("CAPITAL SOCIAL:"), "cell 2 6,alignx trailing");
		ftxtCapital = FieldFactory.createFormattedField();
		FormatterUtils.applyCapitalMask(ftxtCapital, mascaras.get("CAPITAL"));
		panel.add(ftxtCapital, "cell 3 6,growx, h 25!");

	}

	public String getCodigo() {
		return txtCodigo.getText();
	}

	public String getRazaoSocial() {
		return txtRazao.getText();
	}

	public String getNomeFantasia() {
		return txtFantasia.getText();
	}

	public String getDocumento() {
		return ftxtDocumento.getText();
	}

	public String getTipoDocumento() {
		return String.valueOf(cbTipoDocumento.getSelectedItem());
	}

	public String getDataFundacao() {
		return ftxtFundacao.getText();
	}

	public String getInscricaoEstadual() {
		return txtInscricaoEstadual.getText();
	}

	public String getInscricaoMunicipal() {
		return txtInscricaoMunicipal.getText();
	}

	public String getSituacao() {
		return String.valueOf(cbSituacao.getSelectedItem());
	}

	public String getCapitalSocial() {
		return ftxtCapital.getText();
	}

	public String getTipoCadastro() {
		return String.valueOf(cbCadastro.getSelectedItem());
	}

	public void setCodigo(String codigo) {
		txtCodigo.setText(codigo);
	}

	public void setRazaoSocial(String razao) {
		txtRazao.setText(razao);
	}

	public void setNomeFantasia(String fantasia) {
		txtFantasia.setText(fantasia);
	}

	public void setDocumento(String documento) {
		ftxtDocumento.setText(documento);
	}

	public void setTipoDocumento(String tipo) {
		cbTipoDocumento.setSelectedItem(tipo);
	}

	public void setDataFundacao(String data) {
		ftxtFundacao.setText(data);
	}

	public void setInscricaoEstadual(String estadual) {
		txtInscricaoEstadual.setText(estadual);
	}

	public void setInscricaoMunicipal(String municipal) {
		txtInscricaoMunicipal.setText(municipal);
	}

	public void setSituacao(String situacao) {
		cbSituacao.setSelectedItem(situacao);
	}

	public void setCapitalSocial(String capital) {
		ftxtCapital.setText(capital);
	}

	public void setTipoCadastro(String tipoCadastro) {
		cbCadastro.setSelectedItem(tipoCadastro);
	}

	public JComboBox<String> getCbTipoDocumento() {
		return cbTipoDocumento;
	}
	
	public JComboBox<String> getCbTipoCadatro() {
		return cbCadastro;
	}

	public JFormattedTextField getFtxtDocumento() {
		return ftxtDocumento;
	}

	public JTextField getTxtInscricaoEstadual() {
		return txtInscricaoEstadual;
	}

	public JTextField getTxtInscricaoMunicipal() {
		return txtInscricaoMunicipal;
	}

	public JFormattedTextField getFtxtCapital() {
		return ftxtCapital;
	}

	public JFormattedTextField getFtxtFundacao() {
		return ftxtFundacao;
	}
	
	public JTextField getTxtRazaoSocial() {
		return txtRazao;
	}
	public JTextField getTxtFantasia() {
		return txtFantasia;
	}
	
	public void limpar() {
		txtCodigo.setText("");
		cbTipoDocumento.setSelectedIndex(0);
		cbCadastro.setSelectedIndex(1);
		ftxtDocumento.setValue(null);
		ftxtFundacao.setValue(null);
		txtRazao.setText("");
		txtFantasia.setText("");
		txtInscricaoEstadual.setText("");
		txtInscricaoMunicipal.setText("");
		cbSituacao.setSelectedIndex(0);
		ftxtCapital.setValue(null);
	}
	
	public void desativarAtivar(boolean ativa) {
		cbTipoDocumento.setEnabled(ativa);
		ftxtFundacao.setEnabled(ativa);
		txtRazao.setEnabled(ativa);
		txtFantasia.setEnabled(ativa);
		txtInscricaoEstadual.setEnabled(ativa);
		txtInscricaoMunicipal.setEnabled(ativa);
		cbSituacao.setEnabled(ativa);
		ftxtCapital.setEnabled(ativa);
	}
}
