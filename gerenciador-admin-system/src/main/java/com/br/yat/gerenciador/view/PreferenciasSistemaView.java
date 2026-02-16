package com.br.yat.gerenciador.view;

import java.awt.GraphicsEnvironment;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JInternalFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.border.TitledBorder;

import com.br.yat.gerenciador.model.enums.Tema;

import net.miginfocom.swing.MigLayout;

public class PreferenciasSistemaView extends JInternalFrame {

    private static final long serialVersionUID = 1L;

    public JComboBox<Tema> comboTema;
    public JComboBox<String> comboFonte;
    public JComboBox<Integer> comboTamanhoFonte;
    public JButton btnCorTexto;
    public JButton btnCorFundo;
    public JButton btnCorDestaque;
    public JButton btnSalvar;
    public JButton btnCancelar;

    // Container inteiro do preview (usado pelo controller)
    public JPanel previewContainer;

    public PreferenciasSistemaView() {
        setTitle("Configurações do Sistema");
        setClosable(true);
        setResizable(true);
        setSize(850, 520);

        setLayout(new MigLayout("fill, insets 10", "[300!][grow]", "[grow][pref]"));

        // =============================
        // PAINEL CONFIGURAÇÕES
        // =============================
        JPanel painelConfig = new JPanel(
                new MigLayout("fill, insets 10", "[right][grow]", "[][][][][][]"));
        painelConfig.setBorder(new TitledBorder("Configurações"));

        comboTema = new JComboBox<>(Tema.values());
        comboFonte = new JComboBox<>(
                GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames());
        comboTamanhoFonte = new JComboBox<>(new Integer[]{10, 11, 12, 14, 16, 18, 20, 22, 24});

        btnCorTexto = new JButton("Selecionar");
        btnCorFundo = new JButton("Selecionar");
        btnCorDestaque = new JButton("Selecionar");

        painelConfig.add(new JLabel("Tema:"), "alignx right");
        painelConfig.add(comboTema, "growx, wrap");

        painelConfig.add(new JLabel("Fonte:"), "alignx right");
        painelConfig.add(comboFonte, "growx, wrap");

        painelConfig.add(new JLabel("Tamanho da Fonte:"), "alignx right");
        painelConfig.add(comboTamanhoFonte, "growx, wrap");

        painelConfig.add(new JLabel("Cor do Texto:"), "alignx right");
        painelConfig.add(btnCorTexto, "growx, wrap");

        painelConfig.add(new JLabel("Cor de Fundo:"), "alignx right");
        painelConfig.add(btnCorFundo, "growx, wrap");

        painelConfig.add(new JLabel("Cor de Destaque:"), "alignx right");
        painelConfig.add(btnCorDestaque, "growx, wrap");

        // =============================
        // PAINEL PREVIEW
        // =============================
        previewContainer = new JPanel(
                new MigLayout("fill, insets 10", "[grow]", "[pref][pref][grow]"));
        previewContainer.setBorder(new TitledBorder("Pré-visualização"));

        // Fake Menu
        JLabel fakeMenu = new JLabel("MENU   |   CADASTROS   |   RELATÓRIOS   |   AJUDA");
        fakeMenu.setHorizontalAlignment(JLabel.CENTER);
        fakeMenu.setOpaque(true);
        previewContainer.add(fakeMenu, "growx, wrap");

        // Fake Toolbar
        JPanel fakeToolbar = new JPanel(new MigLayout("insets 5", "[][grow][]", "[]"));
        fakeToolbar.add(new JButton("Novo"));
        fakeToolbar.add(new JLabel("Barra de Ferramentas"), "center");
        fakeToolbar.add(new JButton("Salvar"));
        previewContainer.add(fakeToolbar, "growx, wrap");

        // Fake Content
        JPanel fakeContent = new JPanel(new MigLayout("fill, insets 10", "[grow]", "[][grow][]"));
        fakeContent.setBorder(new TitledBorder("Conteúdo da Tela"));

        fakeContent.add(new JLabel("Nome:"));
        fakeContent.add(new JScrollPane(
                new JTextArea("Exemplo de conteúdo...\nPré-visualização do tema aplicado.")),
                "grow, wrap");
        fakeContent.add(new JButton("Botão de Ação"), "right");

        previewContainer.add(fakeContent, "grow, push");

        // =============================
        // BOTÕES
        // =============================
        JPanel painelBotoes = new JPanel(new MigLayout("right", "[grow][pref][pref]", "[]"));

        btnSalvar = new JButton("Salvar");
        btnCancelar = new JButton("Cancelar");

        painelBotoes.add(btnSalvar, "cell 1 0");
        painelBotoes.add(btnCancelar, "cell 2 0");

        // =============================
        // ADD AO FRAME
        // =============================
        add(painelConfig, "cell 0 0, growy");
        add(previewContainer, "cell 1 0, grow, push");
        add(painelBotoes, "cell 0 1 2 1, growx, align right");

        setVisible(true);
    }
}
