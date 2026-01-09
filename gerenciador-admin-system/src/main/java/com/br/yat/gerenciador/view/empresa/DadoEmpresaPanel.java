package com.br.yat.gerenciador.view.empresa;

import java.util.Map;

import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.br.yat.gerenciador.controller.DadoEmpresaController;
import com.br.yat.gerenciador.util.ui.ComboBoxFactory;
import com.br.yat.gerenciador.util.ui.FieldFactory;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;

import net.miginfocom.swing.MigLayout;

public class DadoEmpresaPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private JTextField txtFantasia;
	private JTextField txtRazao;
	private JTextField txtCodigo;
	private JTextField txtInscEst;
	private JTextField txtInscMun;

	private JFormattedTextField ftxtFundacao;
	private JFormattedTextField ftxtDocumento;
	private JFormattedTextField ftxtCapital;

	private JComboBox<String> cbTipoDoc;
	private JComboBox<String> cbSituacao;
	private JComboBox<String> cbCadastro;

	Map<String, String> mascaras = MaskFactory.createMask();

	public DadoEmpresaPanel() {
		setLayout(new MigLayout("fill", "[grow]", "[grow]"));

		montarTela();
		new DadoEmpresaController(this);
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
		cbTipoDoc = ComboBoxFactory.createComboBox("SELECIONE", "CNPJ", "CPF");
		panel.add(cbTipoDoc, "cell 1 1,growx, h 25!");

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
		txtInscEst = FieldFactory.createTextField(20);
		panel.add(txtInscEst, "cell 1 5,growx, h 25!");

		panel.add(LabelFactory.createLabel("INSC. MUNICIPAL:"), "cell 2 5,alignx trailing");
		txtInscMun = FieldFactory.createTextField(20);
		panel.add(txtInscMun, "cell 3 5,growx, h 25!");

		panel.add(LabelFactory.createLabel("SITUAÇÂO GERAL:"), "cell 0 6,alignx trailing");
		cbSituacao = ComboBoxFactory.createComboBox("SELECIONE", "ATIVA", "INAPTA", "SUSPENSA", "BAIXADA", "NULA");
		panel.add(cbSituacao, "cell 1 6,growx, h 25!");

		panel.add(LabelFactory.createLabel("CAPITAL SOCIAL:"), "cell 2 6,alignx trailing");
		ftxtCapital = FieldFactory.createFormattedField();
		FormatterUtils.applyCapitalMask(ftxtCapital, mascaras.get("CAPITAL"));
		panel.add(ftxtCapital, "cell 3 6,growx, h 25!");

	}

	public JComboBox<String> getCbTipoDoc() {
		return cbTipoDoc;
	}

	public JFormattedTextField getFtxDocumento() {
		return ftxtDocumento;
	}

	public JTextField getTxtInscEst() {
		return txtInscEst;
	}

	public JTextField getTxtInscMun() {
		return txtInscMun;
	}

	public JFormattedTextField getFtxCapitalSocial() {
		return ftxtCapital;
	}

	public JFormattedTextField getFtxFundacao() {
		return ftxtFundacao;
	}
}
