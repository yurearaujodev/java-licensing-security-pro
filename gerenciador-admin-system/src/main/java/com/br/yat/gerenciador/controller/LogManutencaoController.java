package com.br.yat.gerenciador.controller;

import javax.swing.SwingUtilities;
import com.br.yat.gerenciador.model.LogSistema;
import com.br.yat.gerenciador.model.Sessao;
import com.br.yat.gerenciador.model.enums.ParametroChave;
import com.br.yat.gerenciador.service.LogSistemaService;
import com.br.yat.gerenciador.service.ParametroSistemaService;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.LogManutencaoView;

public class LogManutencaoController extends BaseController {
	private final LogManutencaoView view;
	private final LogSistemaService logService;
	private final ParametroSistemaService paramService;

	public LogManutencaoController(LogManutencaoView view, LogSistemaService logService,
			ParametroSistemaService paramService) {
		this.view = view;
		this.logService = logService;
		this.paramService = paramService;

		init();
	}

	private void init() {
		carregarStatusPolitica();
		carregarHistoricoManutencao();

		view.btnExecutarLimpeza.addActionListener(e -> confirmarExecutarLimpeza());
	}

	private void carregarStatusPolitica() {
		int dias = paramService.getInt(ParametroChave.LOGS_DIAS_RETENCAO, 90);
		view.lblPoliticaAtual.setText("POLÍTICA ATUAL: MANTER LOGS DOS ÚLTIMOS " + dias + " DIAS.");
	}

	private void carregarHistoricoManutencao() {
		runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
			return logService.filtrarLogs("MANUTENCAO", "LIMPEZA_LOGS", null, Sessao.getUsuario());
		}, logs -> {
			view.modelHistorico.setRowCount(0);
			for (LogSistema log : logs) {
				view.modelHistorico.addRow(new Object[] { log.getDataHora(),
						(log.getUsuario() != null && log.getUsuario().getNome() != null ? log.getUsuario().getNome()
								: "SISTEMA"),
						"SUCESSO", log.getDadosNovos() });
			}
		});
	}

	private void confirmarExecutarLimpeza() {
		boolean confirma = DialogFactory.confirmacao(view,
				"DESEJA EXECUTAR A LIMPEZA MANUAL AGORA?\nISSO REMOVERÁ DADOS ANTIGOS CONFORME A POLÍTICA CONFIGURADA.");

		if (confirma) {
			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				logService.executarLimpezaAutomatica(Sessao.getUsuario());
				return null;
			}, r -> {
				DialogFactory.informacao(view, "LIMPEZA CONCLUÍDA COM SUCESSO!");
				carregarHistoricoManutencao();
			});
		}
	}
}