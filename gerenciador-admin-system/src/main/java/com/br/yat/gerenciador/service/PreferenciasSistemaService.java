package com.br.yat.gerenciador.service;

import java.awt.Color;
import java.awt.Font;
import java.awt.Window;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.ParametroSistemaDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.ParametroSistema;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.Tema;
import com.formdev.flatlaf.FlatDarkLaf;
import com.formdev.flatlaf.FlatLightLaf;

public class PreferenciasSistemaService {

    // ==========================================================
    // ==================== MÉTODOS DE CARGA ====================
    // ==========================================================

    public Tema carregarTema() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            ParametroSistemaDao dao = new ParametroSistemaDao(conn);
            return dao.getParametro("tema")
                    .map(p -> Tema.valueOf(p.getValor()))
                    .orElse(Tema.CLARO);
        } catch (SQLException | IllegalArgumentException e) {
            return Tema.CLARO;
        }
    }

    public String carregarFonte() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            ParametroSistemaDao dao = new ParametroSistemaDao(conn);
            return dao.getParametro("fonte")
                    .map(ParametroSistema::getValor)
                    .orElse("Arial");
        } catch (SQLException e) {
            return "Arial";
        }
    }

    public Integer carregarTamanhoFonte() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            ParametroSistemaDao dao = new ParametroSistemaDao(conn);
            return dao.getParametro("tamanho_fonte")
                    .map(p -> Integer.parseInt(p.getValor()))
                    .orElse(12);
        } catch (SQLException | NumberFormatException e) {
            return 12;
        }
    }

    public Color carregarCorTexto() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            ParametroSistemaDao dao = new ParametroSistemaDao(conn);
            return dao.getParametro("cor_texto")
                    .map(p -> Color.decode(p.getValor()))
                    .orElse(Color.BLACK);
        } catch (SQLException | NumberFormatException e) {
            return Color.BLACK;
        }
    }

    public Color carregarCorFundo() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            ParametroSistemaDao dao = new ParametroSistemaDao(conn);
            return dao.getParametro("cor_fundo")
                    .map(p -> Color.decode(p.getValor()))
                    .orElse(Color.WHITE);
        } catch (SQLException | NumberFormatException e) {
            return Color.WHITE;
        }
    }

    public Color carregarCorDestaque() {
        try (Connection conn = ConnectionFactory.getConnection()) {
            ParametroSistemaDao dao = new ParametroSistemaDao(conn);
            return dao.getParametro("cor_destaque")
                    .map(p -> Color.decode(p.getValor()))
                    .orElse(Color.BLUE);
        } catch (SQLException | NumberFormatException e) {
            return Color.BLUE;
        }
    }

    // ==========================================================
    // ==================== MÉTODOS DE SALVAR ===================
    // ==========================================================

    public void salvarTema(Tema tema) {
        Objects.requireNonNull(tema, "O tema não pode ser nulo.");
        executarSalvar("tema", tema.toString(), "Tema do Sistema");
    }

    public void salvarFonte(String fonte) {
        if (fonte == null || fonte.trim().isEmpty()) {
            throw new IllegalArgumentException("Fonte não pode ser vazia.");
        }
        executarSalvar("fonte", fonte, "Fonte do Sistema");
    }

    public void salvarTamanhoFonte(Integer tamanho) {
        if (tamanho == null || tamanho < 8) {
            throw new IllegalArgumentException("Tamanho de fonte inválido.");
        }
        executarSalvar("tamanho_fonte", tamanho.toString(), "Tamanho da Fonte");
    }

    public void salvarCorTexto(Color cor) {
        Objects.requireNonNull(cor, "A cor do texto não pode ser nula.");
        executarSalvar("cor_texto", colorToHex(cor), "Cor do Texto");
    }

    public void salvarCorFundo(Color cor) {
        Objects.requireNonNull(cor, "A cor de fundo não pode ser nula.");
        executarSalvar("cor_fundo", colorToHex(cor), "Cor de Fundo");
    }

    public void salvarCorDestaque(Color cor) {
        Objects.requireNonNull(cor, "A cor de destaque não pode ser nula.");
        executarSalvar("cor_destaque", colorToHex(cor), "Cor de Destaque");
    }

    private void executarSalvar(String chave, String valor, String descricao) {
        try (Connection conn = ConnectionFactory.getConnection()) {
            ParametroSistemaDao dao = new ParametroSistemaDao(conn);
            dao.salvarOuAtualizar(new ParametroSistema(0, chave, valor, descricao));
        } catch (SQLException e) {
            throw new DataAccessException(
                    DataAccessErrorType.QUERY_FAILED,
                    "Erro ao salvar parâmetro: " + chave,
                    e);
        }
    }

    // ==========================================================
    // ================= APLICAÇÃO GLOBAL DO TEMA ===============
    // ==========================================================

    public void aplicarConfiguracoesGlobais() {

        Tema tema = carregarTema();
        String fonteNome = carregarFonte();
        Integer tamanho = carregarTamanhoFonte();
        Color corFundo = carregarCorFundo();
        Color corTexto = carregarCorTexto();
        Color corDestaque = carregarCorDestaque();

        try {

            // Define Look and Feel
            if (tema == Tema.ESCURO) {
                FlatDarkLaf.setup();
            } else {
                FlatLightLaf.setup();
            }

            // Define fonte padrão global
            UIManager.put("defaultFont", new Font(fonteNome, Font.PLAIN, tamanho));

            // Define cores globais
            UIManager.put("Panel.background", corFundo);
            UIManager.put("Label.foreground", corTexto);
            UIManager.put("MenuBar.background", corFundo);
            UIManager.put("MenuBar.foreground", corTexto);
            UIManager.put("Menu.background", corFundo);
            UIManager.put("Menu.foreground", corTexto);
            UIManager.put("MenuItem.background", corFundo);
            UIManager.put("MenuItem.foreground", corTexto);
            UIManager.put("ToolBar.background", corFundo);
            UIManager.put("Desktop.background", corFundo);
            UIManager.put("nimbusSelection", corDestaque);

            // Atualiza todas as janelas abertas
            for (Window window : Window.getWindows()) {
                SwingUtilities.updateComponentTreeUI(window);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    // ==========================================================
    // ================= UTILITÁRIOS INTERNOS ===================
    // ==========================================================

    private String colorToHex(Color cor) {
        return String.format("#%02x%02x%02x",
                cor.getRed(),
                cor.getGreen(),
                cor.getBlue());
    }
}
