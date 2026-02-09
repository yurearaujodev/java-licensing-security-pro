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

	public LogManutencaoController(LogManutencaoView view,LogSistemaService logService,ParametroSistemaService paramService) {
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
		// Busca o valor configurado na outra tela de parâmetros
		int dias = paramService.getInt(ParametroChave.LOGS_DIAS_RETENCAO, 90);
		view.lblPoliticaAtual.setText("POLÍTICA ATUAL: Manter logs dos últimos " + dias + " dias.");
	}

	private void carregarHistoricoManutencao() {
	    runAsyncSilent(SwingUtilities.getWindowAncestor(view), () -> {
	        // Use null ou "" para o usuário, para que o DAO não tente filtrar por um nome específico
	        return logService.filtrarLogs("MANUTENCAO", "LIMPEZA_LOGS", null, Sessao.getUsuario());
	    }, logs -> {
	        view.modelHistorico.setRowCount(0);
	        for (LogSistema log : logs) {
	            view.modelHistorico.addRow(new Object[] { 
	                log.getDataHora(),
	                (log.getUsuario() != null && log.getUsuario().getNome() != null ? log.getUsuario().getNome() : "SISTEMA"), 
	                "SUCESSO", // Ou extraia do JSON se preferir
	                log.getDadosNovos() 
	            });
	        }
	    });
	}

	private void confirmarExecutarLimpeza() {
		boolean confirma = DialogFactory.confirmacao(view,
				"Deseja executar a limpeza manual agora?\nIsso removerá dados antigos conforme a política configurada.");

		if (confirma) {
			runAsync(SwingUtilities.getWindowAncestor(view), () -> {
				logService.executarLimpezaAutomatica(Sessao.getUsuario());
				return null;
			}, r -> {
				DialogFactory.informacao(view, "Limpeza concluída com sucesso!");
				carregarHistoricoManutencao(); // Atualiza a tabela
			});
		}
	}
}