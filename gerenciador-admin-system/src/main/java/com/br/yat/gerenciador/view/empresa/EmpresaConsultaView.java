package com.br.yat.gerenciador.view.empresa;

import java.awt.BorderLayout;

import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;

import com.br.yat.gerenciador.util.EmpresaTableModel;
import com.br.yat.gerenciador.util.ui.ButtonFactory;
import com.br.yat.gerenciador.util.ui.FieldFactory;
import com.br.yat.gerenciador.util.ui.LabelFactory;
import com.br.yat.gerenciador.util.ui.PanelFactory;
import com.br.yat.gerenciador.util.ui.TableFactory;

public class EmpresaConsultaView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JTextField txtBusca;
	private JTable tabela;
	private JButton btnEditar;
	private EmpresaTableModel tablemodel;

	public EmpresaConsultaView() {
		super("CONSULTA DE CLIENTES", true, true, true, true);
		setLayout(new BorderLayout());
		montarTela();
		setSize(690, 530);
	}

	private void montarTela() {
		JPanel panel = PanelFactory.createPanel("insets 10", "[right][grow,fill]", "[]");
		txtBusca = FieldFactory.createTextField(30);
		btnEditar = ButtonFactory.createPrimaryButton("EDITAR SELECIONADO");

		panel.add(LabelFactory.createLabel("BUSCAR (NOME/DOC): "));
		panel.add(txtBusca);
		panel.add(btnEditar);

		tablemodel = new EmpresaTableModel();
		tabela = TableFactory.createAbstractTable(tablemodel);

		add(panel, BorderLayout.NORTH);
		add(TableFactory.createTableScrolling(tabela), BorderLayout.CENTER);
		configurarColunas(tabela);
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

	public EmpresaTableModel getTableModel() {
		return tablemodel;
	}
}
