package com.br.yat.gerenciador.controller;

import java.awt.Color;
import java.awt.Font;
import java.awt.Window;

import javax.swing.JColorChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.br.yat.gerenciador.model.enums.Tema;
import com.br.yat.gerenciador.service.ParametroSistemaService;
import com.br.yat.gerenciador.view.ParametroSistemaView;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class ParametroSistemaController {
    private final ParametroSistemaView view;
    private final ParametroSistemaService service;

    public ParametroSistemaController(ParametroSistemaView view, ParametroSistemaService service) {
        this.view = view;
        this.service = service;

        carregarConfiguracoesIniciais();
        inicializarAcoes();
    }

    private void carregarConfiguracoesIniciais() {
        view.comboTema.setSelectedItem(service.carregarTema());
        view.comboFonte.setSelectedItem(service.carregarFonte());
        
        Color corTexto = service.carregarCorTexto();
        Color corFundo = service.carregarCorFundo();
        
        view.btnCorTexto.setBackground(corTexto);
        view.btnCorFundo.setBackground(corFundo);

        atualizarPreview();
    }

    private void inicializarAcoes() {
        view.comboTema.addActionListener(e -> atualizarPreview());
        view.comboFonte.addActionListener(e -> atualizarPreview());

        view.btnCorTexto.addActionListener(e -> {
            Color cor = JColorChooser.showDialog(view, "Escolha a Cor do Texto", view.btnCorTexto.getBackground());
            if (cor != null) {
                view.btnCorTexto.setBackground(cor);
                atualizarPreview();
            }
        });

        view.btnCorFundo.addActionListener(e -> {
            Color cor = JColorChooser.showDialog(view, "Escolha a Cor de Fundo", view.btnCorFundo.getBackground());
            if (cor != null) {
                view.btnCorFundo.setBackground(cor);
                atualizarPreview();
            }
        });

        view.btnSalvar.addActionListener(e -> salvarConfiguracoes());
        view.btnCancelar.addActionListener(e -> view.dispose());
    }

    private void atualizarPreview() {
        try {
            Tema tema = (Tema) view.comboTema.getSelectedItem();
            if (tema == Tema.ESCURO) {
                UIManager.setLookAndFeel(new FlatDarkLaf());
            } else {
                UIManager.setLookAndFeel(new FlatLightLaf());
            }

            String nomeFonte = (String) view.comboFonte.getSelectedItem();
            Font fonte = new Font(nomeFonte, Font.PLAIN, 12);
            UIManager.put("defaultFont", fonte);
            Color texto = view.btnCorTexto.getBackground();
            Color fundo = view.btnCorFundo.getBackground();
            
            // --- AS CHAVES QUE FALTAVAM ---
            UIManager.put("Panel.background", fundo);
            UIManager.put("Label.foreground", texto);
            
            // MenuBar e Menus
            UIManager.put("MenuBar.background", fundo);
            UIManager.put("MenuBar.foreground", texto);
            UIManager.put("Menu.background", fundo);
            UIManager.put("Menu.foreground", texto);
            UIManager.put("MenuItem.background", fundo);
            UIManager.put("MenuItem.foreground", texto);
            
            // DesktopPane (Fundo atrás das janelas internas)
            UIManager.put("Desktop.background", fundo);
            
            // ToolBar (Barra de ferramentas)
            UIManager.put("ToolBar.background", fundo);
            UIManager.put("Control", fundo);
            
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    private void salvarConfiguracoes() {
        try {
            service.salvarTema((Tema) view.comboTema.getSelectedItem());
            service.salvarFonte(view.comboFonte.getSelectedItem().toString());
            service.salvarCorTexto(view.btnCorTexto.getBackground());
            service.salvarCorFundo(view.btnCorFundo.getBackground());

            JOptionPane.showMessageDialog(view, "Configurações salvas com sucesso!");
            SwingUtilities.updateComponentTreeUI(view);
            view.dispose();
        } catch (Exception e) {
            JOptionPane.showMessageDialog(view, "Erro ao salvar: " + e.getMessage(), "Erro", JOptionPane.ERROR_MESSAGE);
        }
    }
}