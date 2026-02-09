package com.br.yat.gerenciador.view;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import net.miginfocom.swing.MigLayout;
import com.br.yat.gerenciador.view.factory.*;

public class LogManutencaoView extends JInternalFrame {
    private static final long serialVersionUID = 1L;
    
    public JLabel lblPoliticaAtual;
    public JButton btnExecutarLimpeza;
    public JTable tabelaHistorico;
    public DefaultTableModel modelHistorico;

    public LogManutencaoView() {
        super("Configuração de Limpeza de Logs", true, true, true, true);
        setSize(700, 450);
        setLayout(new MigLayout("fill, insets 20", "[grow]", "[][][grow]"));

        // Painel Superior: Status da Política
        JPanel pnlStatus = new JPanel(new MigLayout("insets 10", "[grow][]"));
        pnlStatus.setBorder(BorderFactory.createTitledBorder("Política de Retenção"));
        
        lblPoliticaAtual = new JLabel("Carregando política...");
        btnExecutarLimpeza = ButtonFactory.createPrimaryButton("EXECUTAR LIMPEZA AGORA", null);
        
        pnlStatus.add(lblPoliticaAtual, "pushx");
        pnlStatus.add(btnExecutarLimpeza, "w 220!");
        
        add(pnlStatus, "growx, wrap");

        // Informativo
        add(new JLabel("<html><i>* A limpeza remove logs mais antigos que o definido nos parâmetros do sistema.</i></html>"), "wrap");

        // Tabela de Histórico de Limpezas
        modelHistorico = new DefaultTableModel(new Object[] { "DATA EXECUÇÃO", "EXECUTADO POR", "REGISTROS REMOVIDOS", "DETALHES" }, 0);
        tabelaHistorico = new JTable(modelHistorico);
        
        JScrollPane scroll = new JScrollPane(tabelaHistorico);
        scroll.setBorder(BorderFactory.createTitledBorder("Histórico de Manutenções"));
        add(scroll, "grow");
    }
}