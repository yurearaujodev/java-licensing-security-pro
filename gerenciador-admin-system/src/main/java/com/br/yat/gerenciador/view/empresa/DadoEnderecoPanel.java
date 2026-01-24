package com.br.yat.gerenciador.view.empresa;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.br.yat.gerenciador.util.ui.FieldFactory;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;

import net.miginfocom.swing.MigLayout;

public class DadoEnderecoPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTextField txtBairro;
	private JTextField txtLogradouro;
	private JTextField txtNumero;
	private JTextField txtCidade;
	private JTextField txtComplemento;
	private JTextField txtEstado;
	private JTextField txtPais;

	private JFormattedTextField ftxtCep;

	public DadoEnderecoPanel() {
		setLayout(new MigLayout("fill", "[grow]", "[grow]"));

		montarTela();
	}

	private void montarTela() {
		JPanel panel = PanelFactory.createPanel("gapx 10, gapy 10", "[right][grow,fill][right][grow,fill]",
				"[][][][][][]");
		montarCampos(panel);
		add(panel, "grow");
	}

	private void montarCampos(JPanel panel) {
		panel.add(LabelFactory.createLabel("CEP:"), "cell 0 0,alignx trailing");

		ftxtCep = FieldFactory.createFormattedField();
		FormatterUtils.applyPostalCodeMask(ftxtCep, MaskFactory.createMask().get("CEP"));
		panel.add(ftxtCep, "cell 1 0,growx, h 25!,wmin 150,wmax 600");

		panel.add(LabelFactory.createLabel("LOGRADOURO:"), "cell 0 1,alignx trailing");
		txtLogradouro = FieldFactory.createTextField(20);
		panel.add(txtLogradouro, "cell 1 1 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("COMPLEMENTO:"), "cell 0 2,alignx trailing");
		txtComplemento = FieldFactory.createTextField(20);
		panel.add(txtComplemento, "cell 1 2 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("BAIRRO:"), "cell 0 3,alignx trailing");
		txtBairro = FieldFactory.createTextField(20);
		panel.add(txtBairro, "cell 1 3,growx, h 25!");

		panel.add(LabelFactory.createLabel("NÚMERO:"), "cell 2 3,alignx trailing");
		txtNumero = FieldFactory.createTextField(20);
		panel.add(txtNumero, "cell 3 3,growx, h 25!");

		panel.add(LabelFactory.createLabel("CIDADE:"), "cell 0 4,alignx trailing");
		txtCidade = FieldFactory.createTextField(20);
		panel.add(txtCidade, "cell 1 4,growx, h 25!");

		panel.add(LabelFactory.createLabel("ESTADO:"), "cell 2 4,alignx trailing");
		txtEstado = FieldFactory.createTextField(20);
		panel.add(txtEstado, "cell 3 4,growx, h 25!");

		panel.add(LabelFactory.createLabel("PAÍS:"), "cell 0 5,alignx trailing");
		txtPais = FieldFactory.createTextField(20);
		panel.add(txtPais, "cell 1 5,growx, h 25!");
	}

	public String getCep() {
		return ftxtCep.getText();
	}

	public String getLogradouro() {
		return txtLogradouro.getText();
	}

	public String getComplemento() {
		return txtComplemento.getText();
	}

	public String getBairro() {
		return txtBairro.getText();
	}

	public String getNumero() {
		return txtNumero.getText();
	}

	public String getCidade() {
		return txtCidade.getText();
	}

	public String getEstado() {
		return txtEstado.getText();
	}

	public String getPais() {
		return txtPais.getText();
	}

	public void setCep(String cep) {
		ftxtCep.setText(cep);
	}

	public void setLogradouro(String logradouro) {
		txtLogradouro.setText(logradouro);
	}

	public void setComplemento(String complemento) {
		txtComplemento.setText(complemento);
	}

	public void setBairro(String bairro) {
		txtBairro.setText(bairro);
	}

	public void setNumero(String numero) {
		txtNumero.setText(numero);
	}

	public void setCidade(String cidade) {
		txtCidade.setText(cidade);
	}

	public void setEstado(String estado) {
		txtEstado.setText(estado);
	}

	public void setPais(String pais) {
		txtPais.setText(pais);
	}

	public JFormattedTextField getFtxtCep() {
		return ftxtCep;
	}

	public JTextField getTxtLogradouro() {
		return txtLogradouro;
	}
	public JTextField getTxtComplemento() {
		return txtComplemento;
	}
	public JTextField getTxtBairro() {
		return txtBairro;
	}

	public JTextField getTxtCidade() {
		return txtCidade;
	}

	public JTextField getTxtEstado() {
		return txtEstado;
	}

	public JTextField getTxtPais() {
		return txtPais;
	}
	
	public void limpar() {
		ftxtCep.setValue(null);
		txtLogradouro.setText("");
		txtComplemento.setText("");
		txtBairro.setText("");
		txtNumero.setText("");
		txtCidade.setText("");
		txtEstado.setText("");
		txtPais.setText("");
	}
	
	public void desativarAtivar(boolean ativa) {
		ftxtCep.setEnabled(ativa);
		txtLogradouro.setEnabled(ativa);
		txtComplemento.setEnabled(ativa);
		txtBairro.setEnabled(ativa);
		txtNumero.setEnabled(ativa);
		txtCidade.setEnabled(ativa);
		txtEstado.setEnabled(ativa);
		txtPais.setEnabled(ativa);
	}
	
}
