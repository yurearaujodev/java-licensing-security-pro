package com.br.yat.gerenciador.service;

import java.awt.Color;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Objects;

import javax.swing.UIManager;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.ParametroSistemaDao;
import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.model.ParametroSistema;
import com.br.yat.gerenciador.model.enums.DataAccessErrorType;
import com.br.yat.gerenciador.model.enums.Tema;

public class ParametroSistemaService {

	// --- MÉTODOS DE CARREGAMENTO ---

	public Tema carregarTema() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			ParametroSistemaDao dao = new ParametroSistemaDao(conn);
			return dao.getParametro("tema").map(p -> Tema.valueOf(p.getValor())).orElse(Tema.CLARO);
		} catch (SQLException | IllegalArgumentException e) {
			return Tema.CLARO;
		}
	}

	public String carregarFonte() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			ParametroSistemaDao dao = new ParametroSistemaDao(conn);
			return dao.getParametro("fonte").map(ParametroSistema::getValor).orElse("Arial");
		} catch (SQLException e) {
			return "Arial";
		}
	}

	public Color carregarCorTexto() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			ParametroSistemaDao dao = new ParametroSistemaDao(conn);
			return dao.getParametro("cor_texto").map(p -> Color.decode(p.getValor())).orElse(Color.BLACK);
		} catch (SQLException | NumberFormatException e) {
			return Color.BLACK;
		}
	}

	public Color carregarCorFundo() {
		try (Connection conn = ConnectionFactory.getConnection()) {
			ParametroSistemaDao dao = new ParametroSistemaDao(conn);
			return dao.getParametro("cor_fundo").map(p -> Color.decode(p.getValor())).orElse(null);
		} catch (SQLException | NumberFormatException e) {
			return Color.WHITE;
		}
	}

	// --- MÉTODOS DE SALVAMENTO (COM DUPLA VALIDAÇÃO) ---

	public void salvarTema(Tema tema) {
		// Validação no Service
		Objects.requireNonNull(tema, "O tema não pode ser nulo.");

		executarSalvar("tema", tema.toString(), "Tema do Sistema");
	}

	public void salvarFonte(String fonte) {
		// Validação no Service
		if (fonte == null || fonte.trim().isEmpty()) {
			throw new IllegalArgumentException("A fonte não pode ser vazia.");
		}

		executarSalvar("fonte", fonte, "Fonte do Sistema");
	}

	public void salvarCorTexto(Color cor) {
		Objects.requireNonNull(cor, "A cor do texto não pode ser nula.");
		String hex = colorToHex(cor);
		executarSalvar("cor_texto", hex, "Cor do texto do sistema");
	}

	public void salvarCorFundo(Color cor) {
		Objects.requireNonNull(cor, "A cor de fundo não pode ser nula.");
		String hex = colorToHex(cor);
		executarSalvar("cor_fundo", hex, "Cor de fundo do sistema");
	}

	// Método auxiliar para evitar repetição de código
	private void executarSalvar(String chave, String valor, String descricao) {
		try (Connection conn = ConnectionFactory.getConnection()) {
			ParametroSistemaDao dao = new ParametroSistemaDao(conn);
			dao.salvarOuAtualizar(new ParametroSistema(0, chave, valor, descricao));
		} catch (SQLException e) {
			throw new DataAccessException(DataAccessErrorType.QUERY_FAILED, "Erro ao salvar parâmetro: " + chave, e);
		}
	}

	private String colorToHex(Color cor) {
		return String.format("#%02x%02x%02x", cor.getRed(), cor.getGreen(), cor.getBlue());
	}

	public void aplicarConfiguracoesGlobais() {
		try {
			Tema tema = carregarTema();

			// 1. Aplica o LookAndFeel primeiro
			if (tema == Tema.ESCURO) {
				com.formdev.flatlaf.FlatDarkLaf.setup();
			} else {
				com.formdev.flatlaf.FlatLightLaf.setup();
			}

			// 2. Busca as cores (que agora podem vir nulas)
			Color corFundo = carregarCorFundo();
			Color corTexto = carregarCorTexto();

			// 3. SE O BANCO ESTIVER VAZIO (corFundo == null),
			// pegamos a cor nativa do FlatLaf que acabou de ser carregado
			if (corFundo == null) {
				corFundo = UIManager.getColor("Panel.background");
				corTexto = UIManager.getColor("Label.foreground");
			}

			// 4. AGORA SIM, injetamos no UIManager para as Factories usarem
			UIManager.put("MenuBar.background", corFundo);
			UIManager.put("Menu.background", corFundo);
			UIManager.put("Desktop.background", corFundo);
			UIManager.put("Panel.background", corFundo);
			UIManager.put("MenuBar.foreground", corTexto);
			UIManager.put("Menu.foreground", corTexto);
			UIManager.put("Label.foreground", corTexto);

			// Garante que o JMenuBar não seja transparente
			UIManager.put("MenuBar.opaque", true);

		} catch (Exception e) {
			com.formdev.flatlaf.FlatIntelliJLaf.setup();
		}
	}

}