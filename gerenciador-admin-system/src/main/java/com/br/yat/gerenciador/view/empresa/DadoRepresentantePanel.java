package com.br.yat.gerenciador.view.empresa;

import java.util.Map;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

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

	private JComboBox<String> cbNacionalidade;
	private JComboBox<String> cbEstadoCivil;
	Map<String, String> mascaras = MaskFactory.createMask();

	private JFormattedTextField ftxtCpf;
	private JFormattedTextField ftxtTelefone;

	private JButton btnAdicionar;
	private JButton btnRemover;
	private JTable tabela;

	public DadoRepresentantePanel() {
		setLayout(new MigLayout("fill", "[grow]", "[grow]"));
		montarTela();
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
		ftxtCpf = FieldFactory.createFormattedField();
		panel.add(ftxtCpf, "cell 1 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("CARGO:"), "cell 2 1,alignx trailing");
		txtCargo = FieldFactory.createTextField(20);
		FormatterUtils.applyDocumentMask(ftxtCpf, mascaras.get("CPF"));
		panel.add(txtCargo, "cell 3 1,growx, h 25!");

		panel.add(LabelFactory.createLabel("RG:"), "cell 0 2,alignx trailing");
		txtRg = FieldFactory.createTextField(20);
		panel.add(txtRg, "cell 1 2,growx, h 25!");

		panel.add(LabelFactory.createLabel("NACIONALIDADE:"), "cell 2 2,alignx trailing");
		cbNacionalidade = ComboBoxFactory.createComboBox("SELECIONE", "BRASILEIRA", "ESTRANGEIRA");
		panel.add(cbNacionalidade, "cell 3 2,growx, h 25!");

		panel.add(LabelFactory.createLabel("ESTADO CIVIL:"), "cell 0 3,alignx trailing");
		cbEstadoCivil = ComboBoxFactory.createComboBox("SELECIONE", "SOLTEIRO(A)", "CASADO(A)", "DIVORCIADO(A)",
				"VIÚVO(A)");
		panel.add(cbEstadoCivil, "cell 1 3,growx, h 25!");

		panel.add(LabelFactory.createLabel("TELEFONE:"), "cell 2 3,alignx trailing");
		ftxtTelefone = FieldFactory.createFormattedField();
		FormatterUtils.applyPhoneMask(ftxtTelefone, mascaras.get("CELULAR"));
		panel.add(ftxtTelefone, "cell 3 3,growx, h 25!");

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

	public String getNome() {
		return txtNome.getText();
	}

	public String getCpf() {
		return ftxtCpf.getText();
	}

	public String getRg() {
		return txtRg.getText();
	}

	public String getCargo() {
		return txtCargo.getText();
	}

	public String getNacionalidade() {
		return String.valueOf(cbNacionalidade.getSelectedItem());
	}

	public String getEstadoCivil() {
		return String.valueOf(cbEstadoCivil.getSelectedItem());
	}

	public String getTelefone() {
		return ftxtTelefone.getText();
	}

	public String getEmail() {
		return txtEmail.getText();
	}

	public void setNome(String nome) {
		txtNome.setText(nome);
	}

	public void setCpf(String cpf) {
		ftxtCpf.setText(cpf);
	}

	public void setRg(String rg) {
		txtRg.setText(rg);
	}

	public void setCargo(String cargo) {
		txtCargo.setText(cargo);
	}

	public void setNacionalidade(String nacionalidade) {
		cbNacionalidade.setSelectedItem(nacionalidade);
	}

	public void setEstadoCivil(String estado) {
		cbEstadoCivil.setSelectedItem(estado);
	}

	public void setTelefone(String telefone) {
		ftxtTelefone.setText(telefone);
	}

	public void setEmail(String email) {
		txtEmail.setText(email);
	}

	public JComboBox<String> getCbEstado() {
		return cbEstadoCivil;
	}

	public JComboBox<String> getCbNacionalidade() {
		return cbNacionalidade;
	}

	public JFormattedTextField getFtxtCpf() {
		return ftxtCpf;
	}

	public JFormattedTextField getFtxtTelefone() {
		return ftxtTelefone;
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

	public void limpar() {
		txtNome.setText("");
		txtCargo.setText("");
		txtEmail.setText("");
		txtRg.setText("");
		ftxtCpf.setValue(null);
		cbEstadoCivil.setSelectedIndex(0);
		cbNacionalidade.setSelectedIndex(0);
		ftxtTelefone.setValue(null);
	}

	public void desativarAtivar(boolean ativa) {
		txtNome.setEnabled(ativa);
		txtCargo.setEnabled(ativa);
		txtEmail.setEnabled(ativa);
		txtRg.setEnabled(ativa);
		ftxtCpf.setEnabled(ativa);
		cbEstadoCivil.setEnabled(ativa);
		cbNacionalidade.setEnabled(ativa);
		ftxtTelefone.setEnabled(ativa);
	}

}
