package com.br.yat.gerenciador.controller;

import javax.swing.SwingUtilities;
import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.dao.LogSistemaDao;
import com.br.yat.gerenciador.model.LogSistema;
import com.br.yat.gerenciador.view.LogSistemaView;
import java.sql.Connection;

public class LogSistemaController extends BaseController {
	private final LogSistemaView view;

	public LogSistemaController(LogSistemaView view) {
		this.view = view;
		registrarAcoes();
		pesquisar();
	}

	private void registrarAcoes() {
		view.getBtnPesquisar().addActionListener(e -> pesquisar());

		view.getTabela().getSelectionModel().addListSelectionListener(e -> {
			if (!e.getValueIsAdjusting()) {
				mostrarDetalhesLog();
			}
		});
	}

	private void pesquisar() {
		String tipo = (String) view.getCbTipo().getSelectedItem();
		String acao = view.getTxtFiltroAcao().getText();
		String usuario = view.getTxtFiltroUsuario().getText();

		runAsync(SwingUtilities.getWindowAncestor(view), () -> {
			try (Connection conn = ConnectionFactory.getConnection()) {
				LogSistemaDao dao = new LogSistemaDao(conn);
				return dao.listarComFiltros(tipo, acao, usuario);
			}
		}, logs -> {
			view.getTableModel().setRowCount(0);
			for (LogSistema log : logs) {
				view.getTableModel()
						.addRow(new Object[] { log.getDataHora(),
								(log.getUsuario() != null ? log.getUsuario().getNome() : "SISTEMA"), log.getTipo(),
								log.getEntidade(), log.getAcao(), log.isSucesso() ? "SIM" : "NÃO" });
			}
			view.setListaLogsAtual(logs);
		});
	}

	private void mostrarDetalhesLog() {
		int row = view.getTabela().getSelectedRow();
		if (row >= 0) {
			LogSistema log = view.getListaLogsAtual().get(row);

			StringBuilder sb = new StringBuilder();
			sb.append("--- DADOS ANTERIORES ---\n");
			sb.append(log.getDadosAnteriores() != null ? log.getDadosAnteriores() : "N/A");
			sb.append("\n\n--- DADOS NOVOS ---\n");
			sb.append(log.getDadosNovos() != null ? log.getDadosNovos() : "N/A");

			if (!log.isSucesso()) {
				sb.append("\n\n--- MENSAGEM DE ERRO ---\n");
				sb.append(log.getMensagemErro());
			}

			view.getTxtDetalhesJson().setText(sb.toString());
			view.getTxtDetalhesJson().setCaretPosition(0);
		}
	}
}
