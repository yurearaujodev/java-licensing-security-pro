package com.br.yat.gerenciador.view.empresa;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.br.yat.gerenciador.controller.DadoComplementarController;
import com.br.yat.gerenciador.util.ui.ButtonFactory;
import com.br.yat.gerenciador.util.ui.ComboBoxFactory;
import com.br.yat.gerenciador.util.ui.FieldFactory;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;
import com.br.yat.gerenciador.util.ui.TableFactory;

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
		new DadoComplementarController(this);
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
		spinnerNum = FieldFactory.createSpinnerNumber(18, 0, 120, 1);
		panel.add(spinnerNum,"cell 1 1,growx, h 25!");
		
		panel.add(LabelFactory.createLabel("RAMO DE ATIVIDADE:"),"cell 2 1, alignx trailing");
		txtRamo = FieldFactory.createTextField(20);
		panel.add(txtRamo,"cell 3 1,growx, h 25!");
		
		panel.add(LabelFactory.createLabel("OBSERVAÇÕES:"),"cell 0 2,alignx trailing, aligny center");
		jtaobs = FieldFactory.createTextArea();
		jsobs = FieldFactory.createTextAreaScroll(jtaobs);
		panel.add(jsobs,"cell 1 2 3 1, growx, h 60!");
		
		panel.add(LabelFactory.createLabel("TIPO DOCUMENTO:"),"cell 0 3,alignx trailing");
		cbTipo = ComboBoxFactory.createComboBox("CONTRATO SOCIAL","ALVARÁ DE FUNCIONAMENTO","CERTIDÕES NEGATIVAS","REGISTRO NA JUNTA COMERCIAL","LICENÇAS ESPECÍFICAS");
		panel.add(cbTipo,"cell 1 3 2 1,growx, h 25!");
		
		btnAdicionar = ButtonFactory.createPrimaryButton("ADICIONAR");
		panel.add(btnAdicionar, "cell 3 3 4 1, split 2, align right,w 120!, h 25!");
		btnRemover = ButtonFactory.createPrimaryButton("REMOVER");
		panel.add(btnRemover, "w 120!, h 25!");

		tabela = TableFactory.createDefaultTable(
				new String[] { "TIPO", "NOME DO ARQUIVO", "DATA INCLUSÃO"});
		for (int i = 0; i < tabela.getColumnCount(); i++) {
			if (tabela.getColumnModel().getColumn(i).getPreferredWidth() == 75) {
				tabela.getColumnModel().getColumn(i).setPreferredWidth(211);
			}

		}
		panel.add(TableFactory.createTableScrolling(tabela), "cell 0 4 4 1,growx,hmin 80,pushy");
		
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

}
