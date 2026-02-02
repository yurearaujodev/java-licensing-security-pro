package com.br.yat.gerenciador.view;

import java.awt.BorderLayout;
import javax.swing.*;
import com.br.yat.gerenciador.model.enums.MenuChave;
import com.br.yat.gerenciador.view.factory.*;
import com.br.yat.gerenciador.view.tables.UsuarioTableModel;
import net.miginfocom.swing.MigLayout;

public class PermissaoConsultaView extends JInternalFrame {

	private static final long serialVersionUID = 1L;
	private JComboBox<MenuChave> cbPermissoes;
    private JTable tabela;
    private UsuarioTableModel tableModel;
    private JButton btnFechar;

    public PermissaoConsultaView() {
        super("Consulta por Permissões", true, true, true, true);
        setLayout(new BorderLayout());
        initComponents();
        setSize(800, 500);
    }

    private void initComponents() {
        // Topo: Seletor de Permissão
        JPanel pnlTopo = new JPanel(new MigLayout("insets 10", "[right][grow,fill]", "[]"));
        cbPermissoes = new JComboBox<>(MenuChave.values());
        
        pnlTopo.add(LabelFactory.createLabel("FILTRAR POR PERMISSÃO:"));
        pnlTopo.add(cbPermissoes, "h 30!");
        add(pnlTopo, BorderLayout.NORTH);

        // Centro: Tabela de usuários que possuem essa permissão
        tableModel = new UsuarioTableModel();
        tabela = new JTable(tableModel);
        add(DesktopFactory.createScroll(tabela), BorderLayout.CENTER);

        // Base
        JPanel pnlBase = new JPanel(new MigLayout("insets 10", "[grow][right]"));
        btnFechar = ButtonFactory.createPrimaryButton("FECHAR", null);
        pnlBase.add(btnFechar, "w 120!, h 35!");
        add(pnlBase, BorderLayout.SOUTH);
    }

    public JComboBox<MenuChave> getCbPermissoes() { return cbPermissoes; }
    public UsuarioTableModel getTableModel() { return tableModel; }
    public JButton getBtnFechar() { return btnFechar; }
}
