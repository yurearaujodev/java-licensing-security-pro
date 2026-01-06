package com.br.yat.gerenciador.view.empresa;

import java.awt.Font;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultComboBoxModel;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.br.yat.gerenciador.controller.DadoEmpresaController;
import com.br.yat.gerenciador.model.enums.Cnae;
import com.br.yat.gerenciador.model.enums.NaturezaJuridica;
import com.br.yat.gerenciador.model.enums.PorteEmpresa;
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
	private JTextField txtEmail;
	private JTextField txtInscEst;
	private JTextField txtInscMun;

	private JTextField txtBairro;
	private JTextField txtLogradouro;
	private JTextField txtNumero;
	private JTextField txtCidade;
	private JTextField txtComplemento;
	private JTextField txtEstado;

	private JFormattedTextField ftxtFundacao;
	private JFormattedTextField ftxtDocumento;
	private JFormattedTextField ftxtTelCel;
	private JFormattedTextField ftxtTelFixo;
	private JFormattedTextField ftxtCapital;
	private JFormattedTextField ftxtCep;

	private JComboBox<String> cbTipoDoc;
	private JComboBox<String> cbSituacao;
	private JComboBox<String> cbCadastro;
	private JComboBox<Cnae> cbCnae;
	private JComboBox<NaturezaJuridica> cbNatJuri;
	private JComboBox<PorteEmpresa> cbPortEmp;
	Map<String, String> mascaras = MaskFactory.createMask();
	
	
	public DadoEmpresaPanel() {
		JPanel panel = PanelFactory.createPanel("gapx 15, gapy 15", "[right][grow,fill][right][grow,fill]",
				"5[25]5[25]5[25]5[25]5[25]5[25]5[25]5[25]5[25]5[25]");
		montarCampos(panel);
		new DadoEmpresaController(this);
		setLayout(new MigLayout("fill","[grow]","[grow]"));
		add(panel,"grow");
	}
	
	private void montarCampos(JPanel panel) {
		panel.add(LabelFactory.createLabel("CÓDIGO: "),"cell 0 0 , alignx trailing");
		
		txtCodigo = FieldFactory.createTextField(20);
		txtCodigo.setEnabled(false);
		panel.add(txtCodigo, "cell 1 0,growx, h 25!,wmax 120");

		panel.add(LabelFactory.createLabel("TP. CADASTRO: "),"cell 0 1, alignx trailing");

		cbCadastro = ComboBoxFactory.createComboBox("SELECIONE","CLIENTE","FORNECEDORA");
		panel.add(cbCadastro, "cell 1 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("DT. FUNDAÇÂO: "), "cell 2 1,alignx trailing");
		ftxtFundacao = FieldFactory.createFormattedField();
		FormatterUtils.applyDateMask(ftxtFundacao, mascaras.get("FUNDACAO"));
		panel.add(ftxtFundacao, "cell 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("TIPO DOC.: "), "cell 0 2,alignx trailing");
		cbTipoDoc = ComboBoxFactory.createComboBox("SELECIONE", "CNPJ", "CPF");
		panel.add(cbTipoDoc, "cell 1 2,growx, h 25!");

		panel.add(LabelFactory.createLabel("DOCUMENTO: "), "cell 2 2,alignx trailing");
		ftxtDocumento = FieldFactory.createFormattedField();
		panel.add(ftxtDocumento, "cell 3 2,growx, h 25!");

		panel.add(LabelFactory.createLabel("RAZÃO SOCIAL: "), "cell 0 3,alignx trailing");
		txtRazao = FieldFactory.createTextField(20);
		panel.add(txtRazao, "cell 1 3 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("NOME FANTASIA: "), "cell 0 4,alignx trailing");
		txtFantasia = FieldFactory.createTextField(20);
		panel.add(txtFantasia, "cell 1 4 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("INSC. ESTADUAL: "), "cell 0 5,alignx trailing");
		txtInscEst = FieldFactory.createTextField(20);
		panel.add(txtInscEst, "cell 1 5,growx, h 25!");

		panel.add(LabelFactory.createLabel("INSC. MUNICIPAL: "), "cell 2 5,alignx trailing");
		txtInscMun = FieldFactory.createTextField(20);
		panel.add(txtInscMun, "cell 3 5,growx, h 25!");

		panel.add(criarLabel("SITUAÇÂO CADASTRAL: "), "cell 0 6,alignx trailing");
		cbSituacao = ComboBoxFactory.createComboBox("SELECIONE", "ATIVA", "INATIVA", "SUSPENSA");
		panel.add(cbSituacao, "cell 1 6,growx, h 25!");

		panel.add(LabelFactory.createLabel("CAPITAL SOCIAL: "), "cell 2 6,alignx trailing");
		ftxtCapital = FieldFactory.createFormattedField();
		FormatterUtils.applyCapitalMask(ftxtCapital, mascaras.get("CAPITAL"));
		panel.add(ftxtCapital, "cell 3 6,growx, h 25!");

		JLabel lblCnae = criarLabel("CNAE :");
		lblCnae.setToolTipText("CLASSIFICAÇÃO NACIONAL DE ATIVADADE ECONÔMICAS");
		panel.add(lblCnae, "cell 0 7,alignx trailing");
		cbCnae = new JComboBox<>(Cnae.values());
		cbCnae.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(cbCnae, "cell 1 7 3 1,growx, h 25!");

		JLabel lblNat = criarLabel("NATUREZA JURÍD. :");
		lblNat.setToolTipText("NATUREZA JURÍDICA");
		panel.add(lblNat, "cell 0 8,alignx trailing");
		cbNatJuri = new JComboBox<>(NaturezaJuridica.values());
		cbNatJuri.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(cbNatJuri, "cell 1 8 3 1,growx, h 25!");

		JLabel lblPorte = criarLabel("PORT. DA EMPRESA :");
		lblPorte.setToolTipText("NATUREZA JURÍDICA");
		panel.add(lblPorte, "cell 0 9,alignx trailing");
		cbPortEmp = new JComboBox<>(PorteEmpresa.values());
		cbPortEmp.setFont(new Font("Tahoma", Font.PLAIN, 12));
		panel.add(cbPortEmp, "cell 1 9 3 1,growx, h 25!");

	}
	
	public JComboBox<String> getCbTipoDoc(){
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
	
	private JLabel criarLabel(String texto) {
		JLabel label = new JLabel(texto);
		label.setFont(new Font("Tahoma", Font.BOLD, 12));
		return label;
	}

	private JTextField criarCampoTexto(boolean editavel) {
		JTextField field = new JTextField();
		field.setFont(new Font("Tahoma", Font.PLAIN, 12));
		field.setEditable(editavel);
		field.setColumns(10);
		return field;
	}

}
