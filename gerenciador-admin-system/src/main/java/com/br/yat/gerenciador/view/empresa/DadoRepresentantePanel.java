package com.br.yat.gerenciador.view.empresa;

import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.br.yat.gerenciador.controller.DadoRepresentanteController;
import com.br.yat.gerenciador.util.ui.ButtonFactory;
import com.br.yat.gerenciador.util.ui.ComboBoxFactory;
import com.br.yat.gerenciador.util.ui.FieldFactory;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;
import com.br.yat.gerenciador.util.ui.TableFactory;

import net.miginfocom.swing.MigLayout;

public class DadoRepresentantePanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField txtNome;
	private JTextField txtRg;
	private JTextField txtCargo;
	private JTextField txtEmail;
	private JComboBox<String> cbNac;
	private JComboBox<String> cbEst;
	Map<String, String> mascaras = MaskFactory.createMask();

	private JFormattedTextField txtCpf;
	private JFormattedTextField txtTel;

	private JButton btnAdicionar;
	private JButton btnRemover;
	private JTable tabela;

	public DadoRepresentantePanel() {
		setLayout(new MigLayout("fill", "[grow]", "[grow]"));
		montarTela();
		new DadoRepresentanteController(this);
	}

	private void montarTela() {
		JPanel panel = PanelFactory.createPanel("gapx 10, gapy 10", "[right][grow,fill][right][grow,fill]",
				"[][][][][][][grow]");
		montarCampos(panel);
		add(panel, "grow");
	}

	private void montarCampos(JPanel panel) {
		panel.add(LabelFactory.createLabel("NOME:"), "cell 0 0, alignx trailing");
		txtNome = FieldFactory.createTextField(20);
		panel.add(txtNome, "cell 1 0 3 1, growx, h 25!");

		panel.add(LabelFactory.createLabel("CPF:"), "cell 0 1,alignx trailing");
		txtCpf = FieldFactory.createFormattedField();
		panel.add(txtCpf, "cell 1 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("CARGO:"), "cell 2 1,alignx trailing");
		txtCargo = FieldFactory.createTextField(20);
		FormatterUtils.applyDocumentMask(txtCpf, mascaras.get("CPF"));
		panel.add(txtCargo, "cell 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("RG:"), "cell 0 2,alignx trailing");
		txtRg = FieldFactory.createTextField(20);
		panel.add(txtRg, "cell 1 2,growx, h 25!");

		panel.add(LabelFactory.createLabel("NACIONALIDADE:"), "cell 2 2,alignx trailing");
		cbNac = ComboBoxFactory.createComboBox("SELECIONE", "BRASILEIRA", "ESTRANGEIRA");
		panel.add(cbNac, "cell 3 2,growx, h 25!");

		panel.add(LabelFactory.createLabel("ESTADO CIVIL:"), "cell 0 3,alignx trailing");
		cbEst = ComboBoxFactory.createComboBox("SELECIONE", "SOLTEIRO(A)", "CASADO(A)", "DIVORCIADO(A)", "VIÚVO(A)");
		panel.add(cbEst, "cell 1 3,growx, h 25!");

		panel.add(LabelFactory.createLabel("TELEFONE:"), "cell 2 3,alignx trailing");
		txtTel = FieldFactory.createFormattedField();
		FormatterUtils.applyPhoneMask(txtTel, mascaras.get("CELULAR"));
		panel.add(txtTel, "cell 3 3,growx, h 25!");

		panel.add(LabelFactory.createLabel("E-MAIL:"), "cell 0 4, alignx trailing");
		txtEmail = FieldFactory.createTextField(20);
		panel.add(txtEmail, "cell 1 4 3 1, growx, h 25!");

		btnAdicionar = ButtonFactory.createPrimaryButton("ADICIONAR");
		panel.add(btnAdicionar, "cell 0 5 4 1, split 2, align right,w 120!, h 25!");
		btnRemover = ButtonFactory.createPrimaryButton("REMOVER");
		panel.add(btnRemover, "w 120!, h 25!");

		tabela = TableFactory.createDefaultTable(
				new String[] { "NOME", "CPF", "RG", "CARGO", "NACIONALIDADE", "ESTADO CIVIL", "TELEFONE", "E-MAIL" });
		for (int i = 0; i < tabela.getColumnCount(); i++) {
			if (tabela.getColumnModel().getColumn(i).getPreferredWidth() == 75) {
				tabela.getColumnModel().getColumn(i).setPreferredWidth(150);
			}

		}
		panel.add(TableFactory.createTableScrolling(tabela), "cell 0 6 4 1,growx,hmin 100,pushy");
	}

	public JComboBox<String> getCbEstado() {
		return cbEst;
	}

	public JComboBox<String> getCbNacionalidade() {
		return cbNac;
	}

	public JFormattedTextField getFtxtCpf() {
		return txtCpf;
	}

	public void setCpf(String cpf) {
		txtCpf.setValue((cpf == null || cpf.isBlank()) ? null : cpf);
	}

	public void setTelefone(String telefone) {
		txtTel.setValue((telefone == null || telefone.isBlank()) ? null : telefone);
	}

	public JFormattedTextField getFtxtTelefone() {
		return txtTel;
	}

	public JTextField getTxtNome() {
		return txtNome;
	}

	public JTextField getTxtCargo() {
		return txtCargo;
	}

	public JTextField getTxtEmail() {
		return txtEmail;
	}

	public JTextField getTxtRg() {
		return txtRg;
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
