package com.br.yat.gerenciador.view.empresa;

import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

import com.br.yat.gerenciador.controller.DadoFiscalController;
import com.br.yat.gerenciador.model.enums.Cnae;
import com.br.yat.gerenciador.model.enums.NaturezaJuridica;
import com.br.yat.gerenciador.model.enums.PorteEmpresa;
import com.br.yat.gerenciador.model.enums.RegimeTributario;
import com.br.yat.gerenciador.util.ui.ComboBoxFactory;
import com.br.yat.gerenciador.util.ui.FieldFactory;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;

import net.miginfocom.swing.MigLayout;

public class DadoFiscalPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	
	private JComboBox<Cnae> cbCnae;
	private JComboBox<NaturezaJuridica> cbNatJuri;
	private JComboBox<PorteEmpresa> cbPortEmp;
	private JComboBox<RegimeTributario> cbRegTri;
	private JComboBox<String> cbContIcms;
	
	private JTextField txtCrt;

	public DadoFiscalPanel() {
		setLayout(new MigLayout("fill", "[grow]", "[grow]"));
		montarTela();
		new DadoFiscalController(this);
	}
	
	private void montarTela() {
		JPanel panel = PanelFactory.createPanel("gapx 10, gapy 10", "[right][grow,fill][right][grow,fill]",
				"[][][][][]");
		montarCampos(panel);
		add(panel, "grow");
	}
	
	private void montarCampos(JPanel panel) {
		JLabel lblCnae = LabelFactory.createLabel("CNAE: ");
		lblCnae.setToolTipText("CLASSIFICAÇÃO NACIONAL DE ATIVADADE ECONÔMICAS");
		panel.add(lblCnae, "cell 0 0,alignx trailing");
		cbCnae = ComboBoxFactory.createEnumComboBox(Cnae.class);
		panel.add(cbCnae, "cell 1 0 3 1,growx, h 25!");

		JLabel lblNat = LabelFactory.createLabel("NATUREZA JURÍD.: ");
		lblNat.setToolTipText("NATUREZA JURÍDICA");
		panel.add(lblNat, "cell 0 1,alignx trailing");
		cbNatJuri = ComboBoxFactory.createEnumComboBox(NaturezaJuridica.class);
		panel.add(cbNatJuri, "cell 1 1 3 1,growx, h 25!");

		JLabel lblPorte = LabelFactory.createLabel("PORT. DA EMPRESA: ");
		lblPorte.setToolTipText("PORTE DA EMPRESA");
		panel.add(lblPorte, "cell 0 2,alignx trailing");
		cbPortEmp = ComboBoxFactory.createEnumComboBox(PorteEmpresa.class);
		panel.add(cbPortEmp, "cell 1 2 3 1,growx, h 25!");
		
		JLabel lblRegime = LabelFactory.createLabel("REG. TRIBUTÁRIO: ");
		lblRegime.setToolTipText("REGIME TRIBUTÁRIO");
		panel.add(lblRegime, "cell 0 3,alignx trailing");
		cbRegTri = ComboBoxFactory.createEnumComboBox(RegimeTributario.class);
		panel.add(cbRegTri, "cell 1 3 3 1,growx, h 25!");
		
		JLabel lblCrt = LabelFactory.createLabel("CRT:");
		lblCrt.setToolTipText("CÓDIGO DE REGIME TRIBUTÁRIO");
		panel.add(lblCrt,"cell 0 4, alignx trailing");
		txtCrt = FieldFactory.createTextField(20);
		txtCrt.setEditable(false);
		panel.add(txtCrt, "cell 1 4,growx, h 25!, wmin 100,wmax 600");
		
		JLabel lblCont = LabelFactory.createLabel("CONT. ICMS"); 
		lblCont.setToolTipText("CONTRIBUINTE ICMS");
		panel.add(lblCont,"cell 2 4, alignx trailing");
		cbContIcms = ComboBoxFactory.createComboBox("SELECIONE","SIM","NÃO","ISENTO");
		panel.add(cbContIcms,"cell 3 4,growx, h 25!,wmin 300,wmax 600");
	}
	
	public JComboBox<RegimeTributario> getCbRegTri() {
		return cbRegTri;
	}
	
	public JTextField getTxtCrt() {
		return txtCrt;
	}
	
	public void setCrt(Integer crt) {
		txtCrt.setText(crt != null ? String.valueOf(crt):"");
	}

}
