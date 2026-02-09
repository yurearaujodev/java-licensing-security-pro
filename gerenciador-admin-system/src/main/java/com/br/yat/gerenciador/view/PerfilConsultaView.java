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

    public PerfilConsultaView() {
        super("Consulta de Perfis de Acesso", true, true, true, true);
        setLayout(new BorderLayout());
        initComponents();
        setSize(900, 500); // Padronizado com a de usuários
    }

    private void initComponents() {
        // Topo: Filtro
        JPanel pnlBusca = new JPanel(new MigLayout("insets 10", "[right][grow,fill][]", "[]"));
        txtBusca = FieldFactory.createTextField(30);
        btnPesquisar = ButtonFactory.createPrimaryButton("BUSCAR", IconFactory.pesquisar());
        
        pnlBusca.add(LabelFactory.createLabel("PESQUISAR:"));
        pnlBusca.add(txtBusca);
        pnlBusca.add(btnPesquisar, "w 120!, h 30!");
        add(pnlBusca, BorderLayout.NORTH);

        // Centro: Tabela
        tableModel = new PerfilTableModel();
        tabela = TableFactory.createAbstractTable(tableModel);
        configurarColunas(tabela);
        add(TableFactory.createTableScrolling(tabela), BorderLayout.CENTER);

        // Base: Ações
        JPanel pnlAcoes = new JPanel(new MigLayout("insets 10", "[left][grow][right]", "[]"));
        btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());
        
        // Ícone de editar e excluir padronizados
        btnEditar = ButtonFactory.createPrimaryButton("EDITAR", IconFactory.salvar()); 
        btnExcluir = ButtonFactory.createPrimaryButton("EXCLUIR", IconFactory.cancelar());
        
        pnlAcoes.add(btnNovo, "w 120!, h 35!");
        // O "cell 2 0" garante que fiquem alinhados à direita como na tela de usuários
        pnlAcoes.add(btnExcluir, "cell 2 0, gapright 10, w 120!, h 35!");
        pnlAcoes.add(btnEditar, "cell 2 0, w 120!, h 35!");
        add(pnlAcoes, BorderLayout.SOUTH);

        // Inicia desabilitado até selecionar algo
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

    public JTextField getTxtBusca() { return txtBusca; }
    public JButton getBtnPesquisar() { return btnPesquisar; }
    public JButton getBtnEditar() { return btnEditar; }
    public JButton getBtnNovo() { return btnNovo; }
    public JButton getBtnExcluir() { return btnExcluir; }
    public JTable getTabela() { return tabela; }
    public PerfilTableModel getTableModel() { return tableModel; }
    
    public Perfil getSelecionado() {
        int row = tabela.getSelectedRow();
        if (row == -1) return null;
        return tableModel.getAt(tabela.convertRowIndexToModel(row));
    }
}