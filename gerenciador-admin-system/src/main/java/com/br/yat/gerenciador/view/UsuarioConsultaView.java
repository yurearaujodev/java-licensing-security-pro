package com.br.yat.gerenciador.view;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.ButtonFactory;
import com.br.yat.gerenciador.view.factory.FieldFactory;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import com.br.yat.gerenciador.view.factory.TableFactory;
import com.br.yat.gerenciador.view.tables.UsuarioTableModel;

import net.miginfocom.swing.MigLayout;

public class UsuarioConsultaView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JTable tabela;
	private UsuarioTableModel tableModel;
	private JCheckBox chkVerExcluidos;
	private JTextField txtBusca;
	private JButton btnPesquisar, btnEditar, btnNovo, btnExcluir;
	private JButton btnResetarSenha;

	public UsuarioConsultaView() {
		super("Consulta de Usuários", true, true, true, true);
		setLayout(new BorderLayout());
		initComponents();
		setSize(900, 500);
	}

	private void initComponents() {
		// Topo: Filtro
		JPanel pnlBusca = new JPanel(new MigLayout("insets 10", "[right][grow,fill][]", "[]"));
		txtBusca = FieldFactory.createTextField(30);
		btnPesquisar = ButtonFactory.createPrimaryButton("BUSCAR", IconFactory.pesquisar());
		chkVerExcluidos = new javax.swing.JCheckBox("VER EXCLUÍDOS");
		pnlBusca.add(chkVerExcluidos);
		pnlBusca.add(LabelFactory.createLabel("PESQUISAR:"));
		pnlBusca.add(txtBusca);
		pnlBusca.add(btnPesquisar, "w 120!, h 30!");
		add(pnlBusca, BorderLayout.NORTH);

		// Centro: Tabela
		tableModel = new UsuarioTableModel();
		tabela = TableFactory.createAbstractTable(tableModel);
		configurarColunas(tabela);
		add(TableFactory.createTableScrolling(tabela), BorderLayout.CENTER);

		// Base: Ações
		JPanel pnlAcoes = new JPanel(new MigLayout("insets 10", "[left][grow][right]", "[]"));
		btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());

		btnResetarSenha = ButtonFactory.createPrimaryButton("RESETAR SENHA", null);

		btnEditar = ButtonFactory.createPrimaryButton("EDITAR", IconFactory.salvar());
		btnExcluir = ButtonFactory.createPrimaryButton("EXCLUIR", IconFactory.cancelar());

		pnlAcoes.add(btnNovo, "w 120!, h 35!");

		pnlAcoes.add(btnResetarSenha, "cell 2 0, gapright 10, w 130!, h 35!");
		pnlAcoes.add(btnExcluir, "cell 2 0, gapright 10, w 120!, h 35!");
		pnlAcoes.add(btnEditar, "cell 2 0, w 120!, h 35!");

		add(pnlAcoes, BorderLayout.SOUTH);

		btnEditar.setEnabled(false);
		btnExcluir.setEnabled(false);
		btnResetarSenha.setEnabled(false);
	}

	private void configurarColunas(JTable tabela) {
		// ID (Coluna 0) - Pequena e fixa
		tabela.getColumnModel().getColumn(0).setPreferredWidth(0);
		tabela.getColumnModel().getColumn(0).setMinWidth(0);
		tabela.getColumnModel().getColumn(0).setMaxWidth(0);

		// NOME (Coluna 1) - Espaço maior
		tabela.getColumnModel().getColumn(1).setPreferredWidth(250);
		tabela.getColumnModel().getColumn(1).setMinWidth(150);

		// E-MAIL (Coluna 2) - Espaço maior
		tabela.getColumnModel().getColumn(2).setPreferredWidth(250);
		tabela.getColumnModel().getColumn(2).setMinWidth(150);

		// STATUS/PERMISSÃO (Coluna 3) - Médio
		tabela.getColumnModel().getColumn(3).setPreferredWidth(120);
		tabela.getColumnModel().getColumn(3).setMinWidth(100);
	}

	// --- GETTERS PARA O CONTROLLER ---
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

	public JButton getBtnResetarSenha() {
		return btnResetarSenha;
	}

	public JTable getTabela() {
		return tabela;
	}

	public UsuarioTableModel getTableModel() {
		return tableModel;
	}

	public JButton getBtnExcluir() {
		return btnExcluir;
	}

	public void setDados(List<Usuario> lista) {
		tableModel.setDados(lista);
	}

	public JCheckBox getChkVerExcluidos() {
		return chkVerExcluidos;
	}

	public Usuario getSelecionado() {
		int row = tabela.getSelectedRow();
		if (row == -1)
			return null;
		return tableModel.getAt(tabela.convertRowIndexToModel(row));
	}

}
