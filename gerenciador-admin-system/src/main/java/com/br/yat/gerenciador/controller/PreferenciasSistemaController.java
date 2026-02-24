package com.br.yat.gerenciador.controller;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.model.enums.Tema;
import com.br.yat.gerenciador.service.PreferenciasSistemaService;
import com.br.yat.gerenciador.view.PreferenciasSistemaView;

public class PreferenciasSistemaController extends BaseController {

    private final PreferenciasSistemaView view;
    private final PreferenciasSistemaService service;

    public PreferenciasSistemaController(
            PreferenciasSistemaView view,
            PreferenciasSistemaService service) {

        this.view = view;
        this.service = service;

        carregarConfiguracoesIniciais();
        inicializarAcoes();
    }

    private void carregarConfiguracoesIniciais() {

        view.comboTema.setSelectedItem(service.carregarTema());
        view.comboFonte.setSelectedItem(service.carregarFonte());
        view.comboTamanhoFonte.setSelectedItem(service.carregarTamanhoFonte());

        view.btnCorTexto.setBackground(service.carregarCorTexto());
        view.btnCorFundo.setBackground(service.carregarCorFundo());
        view.btnCorDestaque.setBackground(service.carregarCorDestaque());

        atualizarPreview();
    }

    private void inicializarAcoes() {

        view.comboTema.addActionListener(e -> atualizarPreview());
        view.comboFonte.addActionListener(e -> atualizarPreview());
        view.comboTamanhoFonte.addActionListener(e -> atualizarPreview());

        view.btnCorTexto.addActionListener(e ->
                escolherCor(view.btnCorTexto, "Escolha a cor do Texto"));

        view.btnCorFundo.addActionListener(e ->
                escolherCor(view.btnCorFundo, "Escolha a cor de Fundo"));

        view.btnCorDestaque.addActionListener(e ->
                escolherCor(view.btnCorDestaque, "Escolha a cor de Destaque"));

        view.btnSalvar.addActionListener(e -> salvarConfiguracoes());

        view.btnCancelar.addActionListener(e -> {
            dispose();
            view.dispose();
        });
    }

    private void escolherCor(JButton botao, String titulo) {
        Color cor = JColorChooser.showDialog(view, titulo, botao.getBackground());
        if (cor != null) {
            botao.setBackground(cor);
            atualizarPreview();
        }
    }

    private void atualizarPreview() {
        try {
            Tema tema = (Tema) view.comboTema.getSelectedItem();

            // cores base do tema
            Color baseFundo = tema == Tema.ESCURO ? new Color(43, 43, 43) : Color.WHITE;
            Color baseTexto = tema == Tema.ESCURO ? Color.WHITE : Color.BLACK;
            Color baseDestaque = new Color(0, 120, 215);

            // se o usuário já escolheu cores personalizadas, mantém
            Color corFundoPreview = view.btnCorFundo.getBackground() != null
                    ? view.btnCorFundo.getBackground()
                    : baseFundo;

            Color corTextoPreview = view.btnCorTexto.getBackground() != null
                    ? view.btnCorTexto.getBackground()
                    : baseTexto;

            Color corDestaquePreview = view.btnCorDestaque.getBackground() != null
                    ? view.btnCorDestaque.getBackground()
                    : baseDestaque;

            // fonte e tamanho
            String nomeFonte = (String) view.comboFonte.getSelectedItem();
            int tamanho = (Integer) view.comboTamanhoFonte.getSelectedItem();
            Font fonte = new Font(nomeFonte, Font.PLAIN, tamanho);

            // aplica preview com todas as regras
            aplicarPreview(view.previewContainer, fonte, corTextoPreview, corFundoPreview, corDestaquePreview);

            view.previewContainer.revalidate();
            view.previewContainer.repaint();

        } catch (Exception ex) {
            handleException(ex, SwingUtilities.getWindowAncestor(view));
        }
    }

    private void aplicarPreview(
            Component comp,
            Font fonte,
            Color corTexto,
            Color corFundo,
            Color corDestaque) {

        comp.setFont(fonte);

        if (comp instanceof javax.swing.JPanel panel) {
            panel.setBackground(corFundo);
        }

        if (comp instanceof javax.swing.JLabel label) {
            label.setForeground(corTexto);
        }

        if (comp instanceof javax.swing.JButton button) {
            // mantemos a cor do botão de destaque se for botão de ação
            button.setForeground(Color.WHITE);
            button.setBackground(corDestaque);
            button.setFocusPainted(false);
            button.setBorderPainted(false);
            button.setOpaque(true);
        }

        if (comp instanceof javax.swing.JTextArea textArea) {
            textArea.setForeground(corTexto);
            textArea.setBackground(corFundo.brighter());
            textArea.setCaretColor(corTexto);
        }

        if (comp instanceof javax.swing.JScrollPane scroll) {
            scroll.getViewport().setBackground(corFundo);
        }

        if (comp instanceof Container container) {
            for (Component child : container.getComponents()) {
                aplicarPreview(child, fonte, corTexto, corFundo, corDestaque);
            }
        }
    }

    private void salvarConfiguracoes() {

        runAsync(SwingUtilities.getWindowAncestor(view), () -> {

            Tema tema = (Tema) view.comboTema.getSelectedItem();

            service.salvarTema(tema);
            service.salvarFonte((String) view.comboFonte.getSelectedItem());
            service.salvarTamanhoFonte((Integer) view.comboTamanhoFonte.getSelectedItem());
            service.salvarCorTexto(view.btnCorTexto.getBackground());
            service.salvarCorFundo(view.btnCorFundo.getBackground());
            service.salvarCorDestaque(view.btnCorDestaque.getBackground());

            // aplica global após salvar
            service.aplicarConfiguracoesGlobais();

            return null;

        }, result -> {
            dispose();
            view.dispose();
        });
    }
}
