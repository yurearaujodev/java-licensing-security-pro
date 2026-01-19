package com.br.yat.gerenciador.view.empresa;

import javax.swing.JButton;
import javax.swing.JFormattedTextField;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.br.yat.gerenciador.util.ui.ButtonFactory;
import com.br.yat.gerenciador.util.ui.FieldFactory;
import com.br.yat.gerenciador.util.ui.FormatterUtils;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.MaskFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;
import com.br.yat.gerenciador.util.ui.TableFactory;

import net.miginfocom.swing.MigLayout;

public class DadoBancarioPanel extends JPanel {

	private static final long serialVersionUID = 1L;
	private JTextField txtBanco;
	private JTextField txtAgencia;
	private JTextField txtConta;
	private JTextField txtTipo;

	private JFormattedTextField ftxtCodigo;

	private JTable tabela;
	private JButton btnAdicionar;
	private JButton btnRemover;

	public DadoBancarioPanel() {
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
		panel.add(LabelFactory.createLabel("CÓD. DO BANCO:"), "cell 0 0,alignx trailing");
		ftxtCodigo = FieldFactory.createFormattedField();
		FormatterUtils.applyBank(ftxtCodigo, MaskFactory.createMask().get("BANCO"));
		panel.add(ftxtCodigo, "cell 1 0,growx, h 25!");

		panel.add(LabelFactory.createLabel("BANCO:"), "cell 2 0,alignx trailing");
		txtBanco = FieldFactory.createTextField(20);
		panel.add(txtBanco, "cell 3 0, growx, h 25!");

		panel.add(LabelFactory.createLabel("AGÊNCIA:"), "cell 0 1,alignx trailing");
		txtAgencia = FieldFactory.createTextField(20);
		panel.add(txtAgencia, "cell 1 1, growx, h 25!");

		panel.add(LabelFactory.createLabel("NÚM. DA CONTA:"), "cell 2 1,alignx trailing");
		txtConta = FieldFactory.createTextField(20);
		panel.add(txtConta, "cell 3 1, growx, h 25!");

		panel.add(LabelFactory.createLabel("TIPO DE CONTA:"), "cell 0 2,alignx trailing");
		txtTipo = FieldFactory.createTextField(20);
		panel.add(txtTipo, "cell 1 2 3 1, growx, h 25!");

		btnAdicionar = ButtonFactory.createPrimaryButton("ADICIONAR");
		panel.add(btnAdicionar, "cell 0 3 4 1, split 2, align right,w 120!, h 25!");
		btnRemover = ButtonFactory.createPrimaryButton("REMOVER");
		panel.add(btnRemover, "w 120!, h 25!");

		tabela = TableFactory.createDefaultTable(
				new String[] { "CÓD. DO BANCO", "BANCO", "AGÊNCIA", "NÚM. DA CONTA", "TIPO DE CONTA" });
		for (int i = 0; i < tabela.getColumnCount(); i++) {
			if (tabela.getColumnModel().getColumn(i).getPreferredWidth() == 75) {
				tabela.getColumnModel().getColumn(i).setPreferredWidth(150);
			}

		}
		panel.add(TableFactory.createTableScrolling(tabela), "cell 0 4 4 1,growx,hmin 100,pushy");
	}

	public String getCodigoBanco() {
		return ftxtCodigo.getText();
	}

	public String getBanco() {
		return txtBanco.getText();
	}

	public String getAgencia() {
		return txtAgencia.getText();
	}

	public String getConta() {
		return txtConta.getText();
	}

	public String getTipoConta() {
		return txtTipo.getText();
	}

	public JFormattedTextField getFtxtCodBanco() {
		return ftxtCodigo;
	}

	public void setCodigoBanco(String codigo) {
		ftxtCodigo.setText(codigo);
	}

	public void setBanco(String banco) {
		txtBanco.setText(banco);
	}

	public void setAgencia(String agencia) {
		txtAgencia.setText(agencia);
	}

	public void setConta(String conta) {
		txtConta.setText(conta);
	}

	public void setTipoConta(String tipo) {
		txtTipo.setText(tipo);
	}

	public JTextField getTxtBanco() {
		return txtBanco;
	}

	public JTextField getTxtAgencia() {
		return txtAgencia;
	}

	public JTextField getTxtConta() {
		return txtConta;
	}

	public JTextField getTxtTipo() {
		return txtTipo;
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
