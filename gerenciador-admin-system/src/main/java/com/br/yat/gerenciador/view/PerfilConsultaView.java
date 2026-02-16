package com.br.yat.gerenciador.view;

import java.awt.BorderLayout;
import java.util.List; // Faltava o import
import javax.swing.*;
import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.*;
import com.br.yat.gerenciador.view.tables.PerfilTableModel;
import net.miginfocom.swing.MigLayout;

public class PerfilConsultaView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JTable tabela;
	private PerfilTableModel tableModel;
	private JTextField txtBusca;
	private JButton btnPesquisar, btnEditar, btnNovo, btnExcluir;
	private JCheckBox chkVerExcluidos;

	public PerfilConsultaView() {
		super("CONSULTA DE PERFIS DE ACESSO", true, true, true, true);
		setLayout(new BorderLayout());
		initComponents();
		setSize(900, 500);
	}

	private void initComponents() {
		JPanel pnlBusca = new JPanel(new MigLayout("insets 10", "[right][grow,fill][]", "[]"));
		txtBusca = FieldFactory.createTextField(30);
		btnPesquisar = ButtonFactory.createPrimaryButton("BUSCAR", IconFactory.pesquisar());

		chkVerExcluidos = new JCheckBox("VER EXCLUIDOS");

		pnlBusca.add(chkVerExcluidos);
		pnlBusca.add(LabelFactory.createLabel("PESQUISAR:"));
		pnlBusca.add(txtBusca);
		pnlBusca.add(btnPesquisar, "w 120!, h 30!");
		add(pnlBusca, BorderLayout.NORTH);

		tableModel = new PerfilTableModel();
		tabela = TableFactory.createAbstractTable(tableModel);
		configurarColunas(tabela);
		add(TableFactory.createTableScrolling(tabela), BorderLayout.CENTER);

		JPanel pnlAcoes = new JPanel(new MigLayout("insets 10", "[left][grow][right]", "[]"));
		btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());

		btnEditar = ButtonFactory.createPrimaryButton("EDITAR", IconFactory.salvar());
		btnExcluir = ButtonFactory.createPrimaryButton("EXCLUIR", IconFactory.cancelar());

		pnlAcoes.add(btnNovo, "w 120!, h 35!");
		pnlAcoes.add(btnExcluir, "cell 2 0, gapright 10, w 120!, h 35!");
		pnlAcoes.add(btnEditar, "cell 2 0, w 120!, h 35!");
		add(pnlAcoes, BorderLayout.SOUTH);

		btnEditar.setEnabled(false);
		btnExcluir.setEnabled(false);
	}

	private void configurarColunas(JTable tabela) {
		// ID (Coluna 0) - Escondido ou pequeno como no Usuário
		tabela.getColumnModel().getColumn(0).setPreferredWidth(0);
		tabela.getColumnModel().getColumn(0).setMinWidth(0);
		tabela.getColumnModel().getColumn(0).setMaxWidth(0);

		// NOME (Coluna 1)
		tabela.getColumnModel().getColumn(1).setPreferredWidth(250);
		tabela.getColumnModel().getColumn(1).setMinWidth(150);

		// DESCRIÇÃO (Coluna 2)
		tabela.getColumnModel().getColumn(2).setPreferredWidth(400);
		tabela.getColumnModel().getColumn(2).setMinWidth(200);
	}

	// --- MÉTODOS PARA O CONTROLLER ---

	public void setDados(List<Perfil> lista) {
		tableModel.setDados(lista);
	}

	public JTextField getTxtBusca() {
		return txtBusca;
	}

	public JButton getBtnPesquisar() {
		return btnPesquisar;
	}

	public JButton getBtnEditar() {
		return btnEditar;
	}

	public JButton getBtnNovo() {
		return btnNovo;
	}

	public JButton getBtnExcluir() {
		return btnExcluir;
	}

	public JTable getTabela() {
		return tabela;
	}

	public PerfilTableModel getTableModel() {
		return tableModel;
	}

	public JCheckBox getChkVerExcluidos() {
		return chkVerExcluidos;
	}

	public Perfil getSelecionado() {
		int row = tabela.getSelectedRow();
		if (row == -1)
			return null;
		return tableModel.getAt(tabela.convertRowIndexToModel(row));
	}
}