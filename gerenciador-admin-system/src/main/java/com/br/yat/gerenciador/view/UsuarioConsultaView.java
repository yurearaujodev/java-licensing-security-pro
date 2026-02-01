package com.br.yat.gerenciador.view;

import java.awt.BorderLayout;
import java.util.List;
import javax.swing.JButton;
import javax.swing.JInternalFrame;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTextField;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.util.IconFactory;
import com.br.yat.gerenciador.view.factory.ButtonFactory;
import com.br.yat.gerenciador.view.factory.DesktopFactory;
import com.br.yat.gerenciador.view.factory.FieldFactory;
import com.br.yat.gerenciador.view.factory.LabelFactory;
import com.br.yat.gerenciador.view.tables.UsuarioTableModel;

import net.miginfocom.swing.MigLayout;

public class UsuarioConsultaView extends JInternalFrame {

    private static final long serialVersionUID = 1L;
    private JTable tabela;
    private UsuarioTableModel tableModel;
    private JTextField txtPesquisa;
    private JButton btnPesquisar, btnEditar, btnNovo;

    public UsuarioConsultaView() {
        super("Consulta de Usuários", true, true, true, true);
        setLayout(new BorderLayout());
        initComponents();
        setSize(900, 500);
    }

    private void initComponents() {
        // Topo: Filtro
        JPanel pnlBusca = new JPanel(new MigLayout("insets 10", "[right][grow,fill][]", "[]"));
        txtPesquisa = FieldFactory.createTextField(30);
        btnPesquisar = ButtonFactory.createPrimaryButton("BUSCAR", IconFactory.pesquisar());
        
        pnlBusca.add(LabelFactory.createLabel("PESQUISAR:"));
        pnlBusca.add(txtPesquisa);
        pnlBusca.add(btnPesquisar, "w 120!, h 30!");
        add(pnlBusca, BorderLayout.NORTH);

        // Centro: Tabela
        tableModel = new UsuarioTableModel();
        tabela = new JTable(tableModel);
        tabela.setRowHeight(25);
        add(DesktopFactory.createScroll(tabela), BorderLayout.CENTER);

        // Base: Ações
        JPanel pnlAcoes = new JPanel(new MigLayout("insets 10", "[left][grow][right]", "[]"));
        btnNovo = ButtonFactory.createPrimaryButton("NOVO", IconFactory.novo());
        btnEditar = ButtonFactory.createPrimaryButton("EDITAR", IconFactory.salvar());
        
        pnlAcoes.add(btnNovo, "w 120!, h 35!");
        pnlAcoes.add(btnEditar, "cell 2 0, w 120!, h 35!");
        add(pnlAcoes, BorderLayout.SOUTH);
    }

    public String getTextoPesquisa() { return txtPesquisa.getText().trim(); }
    public JButton getBtnPesquisar() { return btnPesquisar; }
    public JButton getBtnEditar() { return btnEditar; }
    public JButton getBtnNovo() { return btnNovo; }
    
    public void setDados(List<Usuario> lista) { tableModel.setDados(lista); }

    public Usuario getSelecionado() {
        int row = tabela.getSelectedRow();
        return (row != -1) ? tableModel.getAt(tabela.convertRowIndexToModel(row)) : null;
    }
}
