package com.br.yat.gerenciador.view;

import java.util.List;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;

import com.br.yat.gerenciador.model.LogSistema;
import com.br.yat.gerenciador.view.factory.*;

public class LogSistemaView extends JInternalFrame {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JTable tabela;
	private DefaultTableModel model;
	private JTextField txtFiltroAcao, txtFiltroUsuario;
	private JComboBox<String> cbTipo, cbEntidade;
	private JTextArea txtDetalhesJson;
	private JButton btnPesquisar;

	public LogSistemaView() {
		super("Auditoria de Logs do Sistema", true, true, true, true);
		setSize(1100, 650);
		setLayout(new MigLayout("fill, insets 20", "[grow]", "[][grow][200!]"));

		// Painel de Filtros
		JPanel pnlFiltros = new JPanel(new MigLayout("", "[][fill, grow][][fill, grow][][fill, grow][]"));
		pnlFiltros.add(LabelFactory.createLabel("TIPO:"));
		cbTipo = new JComboBox<>(new String[] { "", "SEGURANCA", "CADASTRO", "SISTEMA", "ERRO" });
		pnlFiltros.add(cbTipo);

		pnlFiltros.add(LabelFactory.createLabel("AÇÃO:"));
		txtFiltroAcao = FieldFactory.createTextField(20);
		pnlFiltros.add(txtFiltroAcao);
		pnlFiltros.add(LabelFactory.createLabel("USUÁRIO:"));
		txtFiltroUsuario = FieldFactory.createTextField(20); // Adicione esta linha
		pnlFiltros.add(txtFiltroUsuario);
		pnlFiltros.add(LabelFactory.createLabel("ENTIDADE:"));
        cbEntidade = new JComboBox<>(new String[] { "", "usuario", "empresa", "usuario_permissao" });
        pnlFiltros.add(cbEntidade);
		btnPesquisar = ButtonFactory.createPrimaryButton("PESQUISAR", null);
		pnlFiltros.add(btnPesquisar, "w 120!");
		
		add(pnlFiltros, "wrap");

		// Tabela de Logs
		model = new DefaultTableModel(new Object[] { "DATA/HORA", "USUÁRIO", "TIPO", "ENTIDADE", "AÇÃO", "SUCESSO" }, 0);tabela = new JTable(model);
		add(new JScrollPane(tabela), "grow, wrap");

		// Área de detalhes do JSON
		txtDetalhesJson = new JTextArea();
		txtDetalhesJson.setEditable(false);
		txtDetalhesJson.setBorder(BorderFactory.createTitledBorder("DADOS ANTERIORES / NOVOS (JSON)"));
		add(new JScrollPane(txtDetalhesJson), "growx");
	}

	// Adicione esses campos na classe LogSistemaView
	private List<LogSistema> listaLogsAtual;

	// Adicione esses métodos (Getters)
	public JTable getTabela() {
		return tabela;
	}
	public JComboBox<String> getCbEntidade() {
        return cbEntidade;
    }

	public DefaultTableModel getTableModel() {
		return model;
	}

	public JComboBox<String> getCbTipo() {
		return cbTipo;
	}

	public JTextField getTxtFiltroAcao() {
		return txtFiltroAcao;
	}

	public JTextField getTxtFiltroUsuario() {
		return txtFiltroUsuario;
	}

	public JTextArea getTxtDetalhesJson() {
		return txtDetalhesJson;
	}

	public JButton getBtnPesquisar() {
		return btnPesquisar;
	}

	public List<LogSistema> getListaLogsAtual() {
		return listaLogsAtual;
	}

	public void setListaLogsAtual(java.util.List<LogSistema> lista) {
		this.listaLogsAtual = lista;
	}
}
