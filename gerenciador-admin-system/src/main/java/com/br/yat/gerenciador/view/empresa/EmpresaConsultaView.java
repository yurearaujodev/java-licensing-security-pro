package com.br.yat.gerenciador.view.empresa;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.ButtonFactory;
import com.br.yat.gerenciador.view.factory.FieldFactory;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import com.br.yat.gerenciador.view.factory.PanelFactory;
import com.br.yat.gerenciador.view.factory.TableFactory;
import com.br.yat.gerenciador.view.tables.EmpresaTableModel;

import net.miginfocom.swing.MigLayout;

public class EmpresaConsultaView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JTextField txtBusca;
	private JTable tabela;
	private JButton btnEditar;
	private JButton btnNovo;
	private EmpresaTableModel tablemodel;

	public EmpresaConsultaView() {
		super("", true, true, true, true);
		setLayout(new MigLayout("gapx 15, gapy 15", "[grow,fill]", "[grow][]"));
		montarTela();
		add(criarBotoes(), "cell 0 1,grow");
		setSize(690, 530);
	}

	private void montarTela() {
		JPanel panel = PanelFactory.createPanel("gapx 10,gapy 10", "[right][grow,fill]", "[][grow]");
		montarCampos(panel);
		add(panel, "grow");
	}

	private void montarCampos(JPanel panel) {
		panel.add(LabelFactory.createLabel("BUSCAR (NOME/DOC): "), "cell 0 0,alignx trailing");
		txtBusca = FieldFactory.createTextField(20);
		panel.add(txtBusca, "cell 1 0 2 1,growx,h 25!");

		tablemodel = new EmpresaTableModel();
		tabela = TableFactory.createAbstractTable(tablemodel);

		panel.add(TableFactory.createTableScrolling(tabela), "cell 0 1 2 1,growx, hmin 100,pushy");
		configurarColunas(tabela);
	}

	private JPanel criarBotoes() {
		JPanel panel = PanelFactory.createPanel("insets 5", "[left][grow][right]", "[]");

		btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());
		panel.add(btnNovo, "cell 0 0,h 35!,w 140!");

		btnEditar = ButtonFactory.createPrimaryButton("ALTERAR", IconFactory.salvar());
		panel.add(btnEditar, "cell 2 0,h 35!,w 140!");
		return panel;
	}

	private void configurarColunas(JTable tabela) {
		tabela.getColumnModel().getColumn(0).setPreferredWidth(100);
		tabela.getColumnModel().getColumn(0).setMinWidth(100);
		tabela.getColumnModel().getColumn(0).setMaxWidth(100);

		tabela.getColumnModel().getColumn(1).setPreferredWidth(462);
		tabela.getColumnModel().getColumn(1).setMinWidth(462);
		tabela.getColumnModel().getColumn(1).setMaxWidth(462);

		tabela.getColumnModel().getColumn(2).setPreferredWidth(180);
		tabela.getColumnModel().getColumn(2).setMinWidth(180);
		tabela.getColumnModel().getColumn(2).setMaxWidth(180);

		tabela.getColumnModel().getColumn(3).setPreferredWidth(180);
		tabela.getColumnModel().getColumn(3).setMinWidth(180);
		tabela.getColumnModel().getColumn(3).setMaxWidth(180);
	}

	public JTextField getTxtBusca() {
		return txtBusca;
	}

	public JTable getTabela() {
		return tabela;
	}

	public JButton getBtnEditar() {
		return btnEditar;
	}

	public JButton getBtnNovo() {
		return btnNovo;
	}

	public EmpresaTableModel getTableModel() {
		return tablemodel;
	}
}
