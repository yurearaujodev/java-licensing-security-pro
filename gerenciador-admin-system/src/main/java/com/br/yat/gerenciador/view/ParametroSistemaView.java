package com.br.yat.gerenciador.view;

import javax.swing.*;
import net.miginfocom.swing.MigLayout;

public class ParametroSistemaView extends JInternalFrame {

    private static final long serialVersionUID = 1L;

    // --- LOGIN ---
    public JSpinner spnLoginMaxTentativas;
    public JSpinner spnLoginTempoBloqueio;
    public JSpinner spnSenhaMinTamanho;
    public JSpinner spnForcarTrocaSenha;
    public JSpinner spnTempoSessaoMin;

    // --- LICENÇA ---
    public JSpinner spnLicencaExpiracaoAlertaDias;
    public JSpinner spnLicencaMaxDispositivos;
    public JCheckBox chkLicencaAtivaPadrao;

    // --- SISTEMA / LOGS ---
    public JCheckBox chkLogSistemaAtivo;
    public JComboBox<String> cmbNivelLogPadrao;
    public JSpinner spnTempoRefreshDashboard;
    public JSpinner spnLogsDiasRetencao;

    // --- NOTIFICAÇÕES / EMAIL ---
    public JCheckBox chkEmailNotificacaoAtivo;
    public JTextField txtEmailAlertaLicenca;

    // --- BOTÕES ---
    public JButton btnSalvar;
    public JButton btnCancelar;

    public ParametroSistemaView() {
        setTitle("Parâmetros do Sistema");
        setClosable(true);
        setResizable(false);
        setSize(600, 500);

        // --- Painel de abas ---
        JTabbedPane tabbedPane = new JTabbedPane();

        tabbedPane.add("Login", criarPainelLogin());
        tabbedPane.add("Licença", criarPainelLicenca());
        tabbedPane.add("Sistema / Logs", criarPainelSistema());
        tabbedPane.add("Notificações", criarPainelNotificacoes());

        // --- Painel principal com botões ---
        JPanel painelPrincipal = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[grow][]"));
        painelPrincipal.add(tabbedPane, "cell 0 0, grow");
        
        JPanel painelBotoes = new JPanel(new MigLayout("", "[grow][][]", "[]"));
        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");
        painelBotoes.add(new JLabel(), "grow"); // Espaço vazio
        painelBotoes.add(btnSalvar);
        painelBotoes.add(btnCancelar);

        painelPrincipal.add(painelBotoes, "cell 0 1, growx, align right");

        setContentPane(painelPrincipal);
    }

    // --- ABA LOGIN ---
    private JPanel criarPainelLogin() {
        JPanel panel = new JPanel(new MigLayout("insets 15", "[right][grow]", "[]10[]10[]10[]10[]"));

        spnLoginMaxTentativas = new JSpinner(new SpinnerNumberModel(5, 1, 20, 1));
        spnLoginTempoBloqueio = new JSpinner(new SpinnerNumberModel(5, 1, 120, 1));
        spnSenhaMinTamanho = new JSpinner(new SpinnerNumberModel(6, 4, 32, 1));
        spnForcarTrocaSenha = new JSpinner(new SpinnerNumberModel(90, 0, 365, 1));
        spnTempoSessaoMin = new JSpinner(new SpinnerNumberModel(30, 1, 180, 1));

        panel.add(new JLabel("Máx. tentativas de login:"), "cell 0 0");
        panel.add(spnLoginMaxTentativas, "cell 1 0, growx");

        panel.add(new JLabel("Tempo bloqueio (min):"), "cell 0 1");
        panel.add(spnLoginTempoBloqueio, "cell 1 1, growx");

        panel.add(new JLabel("Tamanho mínimo da senha:"), "cell 0 2");
        panel.add(spnSenhaMinTamanho, "cell 1 2, growx");

        panel.add(new JLabel("Forçar troca de senha (dias):"), "cell 0 3");
        panel.add(spnForcarTrocaSenha, "cell 1 3, growx");

        panel.add(new JLabel("Tempo sessão (min):"), "cell 0 4");
        panel.add(spnTempoSessaoMin, "cell 1 4, growx");

        return panel;
    }

    // --- ABA LICENÇA ---
    private JPanel criarPainelLicenca() {
        JPanel panel = new JPanel(new MigLayout("insets 15", "[right][grow]", "[]10[]10[]"));

        spnLicencaExpiracaoAlertaDias = new JSpinner(new SpinnerNumberModel(7, 1, 30, 1));
        spnLicencaMaxDispositivos = new JSpinner(new SpinnerNumberModel(3, 1, 50, 1));
        chkLicencaAtivaPadrao = new JCheckBox("Licença ativa por padrão");

        panel.add(new JLabel("Dias para alerta de expiração:"), "cell 0 0");
        panel.add(spnLicencaExpiracaoAlertaDias, "cell 1 0, growx");

        panel.add(new JLabel("Máx. dispositivos por licença:"), "cell 0 1");
        panel.add(spnLicencaMaxDispositivos, "cell 1 1, growx");

        panel.add(new JLabel("Ativar licença por padrão:"), "cell 0 2");
        panel.add(chkLicencaAtivaPadrao, "cell 1 2, growx");

        return panel;
    }

    // --- ABA SISTEMA / LOGS ---
    private JPanel criarPainelSistema() {
        JPanel panel = new JPanel(new MigLayout("insets 15", "[right][grow]", "[]10[]10[]10[]"));

        chkLogSistemaAtivo = new JCheckBox("Log do sistema ativo");
        cmbNivelLogPadrao = new JComboBox<>(new String[]{"INFO", "DEBUG", "ERROR"});
        spnTempoRefreshDashboard = new JSpinner(new SpinnerNumberModel(60, 10, 3600, 10));
        
        // Novo Spinner: mínimo 1 dia, máximo 365, padrão 90
        spnLogsDiasRetencao = new JSpinner(new SpinnerNumberModel(90, 1, 365, 1));

        panel.add(new JLabel("Ativar log do sistema:"), "cell 0 0");
        panel.add(chkLogSistemaAtivo, "cell 1 0, growx");

        panel.add(new JLabel("Nível de log padrão:"), "cell 0 1");
        panel.add(cmbNivelLogPadrao, "cell 1 1, growx");

        panel.add(new JLabel("Tempo refresh dashboard (s):"), "cell 0 2");
        panel.add(spnTempoRefreshDashboard, "cell 1 2, growx");

        // Adicionando o novo campo na interface
        panel.add(new JLabel("Manter logs por (dias):"), "cell 0 3");
        panel.add(spnLogsDiasRetencao, "cell 1 3, growx");

        return panel;
    }

    // --- ABA NOTIFICAÇÕES ---
    private JPanel criarPainelNotificacoes() {
        JPanel panel = new JPanel(new MigLayout("insets 15", "[right][grow]", "[]10[]"));

        chkEmailNotificacaoAtivo = new JCheckBox("Notificações por email ativas");
        txtEmailAlertaLicenca = new JTextField();

        panel.add(new JLabel("Ativar email de notificações:"), "cell 0 0");
        panel.add(chkEmailNotificacaoAtivo, "cell 1 0, growx");

        panel.add(new JLabel("Email alerta licença:"), "cell 0 1");
        panel.add(txtEmailAlertaLicenca, "cell 1 1, growx");

        return panel;
    }
}