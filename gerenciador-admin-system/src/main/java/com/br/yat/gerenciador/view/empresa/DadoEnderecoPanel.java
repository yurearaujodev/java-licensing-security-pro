package com.br.yat.gerenciador.view.empresa;

import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.br.yat.gerenciador.controller.DadoEnderecoController;
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
		new DadoEnderecoController(this);
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

	public JFormattedTextField getFtxCep() {
		return ftxtCep;
	}

	public JTextField getTxtBairro() {
		return txtBairro;
	}

	public void setBairro(String bairro) {
		txtBairro.setText(bairro != null ? bairro.toUpperCase() : "");
	}

	public JTextField getTxtCidade() {
		return txtCidade;
	}

	public void setCidade(String cidade) {
		txtCidade.setText(cidade != null ? cidade.toUpperCase() : "");
	}

	public JTextField getTxtEstado() {
		return txtEstado;
	}

	public void setEstado(String estado) {
		txtEstado.setText(estado != null ? estado.toUpperCase() : "");
	}

	public JTextField getTxtPais() {
		return txtPais;
	}

	public void setPais(String pais) {
		txtPais.setText(pais != null ? pais.toUpperCase() : "");
	}

	public JTextField getTxtLogradouro() {
		return txtLogradouro;
	}

	public void setLogradouro(String logradouro) {
		txtLogradouro.setText(logradouro != null ? logradouro.toUpperCase() : "");
	}

	public JTextField getTxtComplemento() {
		return txtComplemento;
	}

	public void setComplemento(String complemento) {
		txtComplemento.setText(complemento != null ? complemento.toUpperCase() : "");
	}

	public void limparCampos() {
		setLogradouro("");
	}

}
