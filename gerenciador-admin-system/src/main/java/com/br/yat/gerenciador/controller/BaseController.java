package com.br.yat.gerenciador.controller;

import java.awt.Window;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import javax.swing.AbstractButton;
import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.exception.DataAccessException;
import com.br.yat.gerenciador.exception.ServiceOperationException;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.factory.LoadingDialog;

public abstract class BaseController {
	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	protected final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private LoadingDialog loadingDialog;

	public void dispose() {
		hideLoading(); // Garante que o loading não fique travado se a tela fechar
		if (loadingDialog != null) {
			loadingDialog.dispose();
			loadingDialog = null;
		}
		if (!executor.isShutdown()) {
			executor.shutdown();
		}
		if (!scheduler.isShutdown()) {
			scheduler.shutdown();
		}
		logger.info("{} ENCERRANDO E THREADS LIBERADAS.", getClass().getSimpleName());
	}

	protected void handleException(Exception e, Window view) {
		SwingUtilities.invokeLater(() -> {
			switch (e) {
			case ValidationException ve -> DialogFactory.aviso(view, ve.getMessage());

			case ServiceOperationException se -> {
				if (se.getErrorType().isCritical()) {
					logger.error("ERRO CRÍTICO NA SERVICE: {}", se.getMessage(), se);
					DialogFactory.erro(view, se.getMessage());
				} else {
					DialogFactory.aviso(view, se.getMessage());
				}
			}

			case DataAccessException de -> {
				logger.error("FALHA DE INFRAESTRUTURA [CÓDIGO: {}]: {}", de.getErrorCode(), de.getMessage(), de);

				String msgUsuario = "NÃO FOI POSSÍVEL SE COMUNICAR COM O BANCO DE DADOS NO MOMENTO.\n"
						+ "POR FAVOR, TENTE NOVAMENTE MAIS TARDE OU CONTATE O SUPORTE.";
				DialogFactory.erro(view, msgUsuario);
			}

			default -> {
				logger.error("ERRO NÃO TRATADO NO SISTEMA", e);
				DialogFactory.erro(view, "OCORREU UM ERRO INESPERADO. DETALHES FORAM REGISTRADOS NO LOG.");
			}
			}
		});
	}

	protected <T> void runAsync(Window view, TaskWithResult<T> task, Consumer<T> onSuccess) {
		showLoading(view);
		runAsyncSilent(view, task, result -> {
			hideLoading();
			if (onSuccess != null) {
				onSuccess.accept(result);
			}
		});

	}

	protected <T> void runAsyncSilent(Window view, TaskWithResult<T> task, Consumer<T> onSuccess) {
		executor.submit(() -> {
			try {
				T result = task.execute();
				SwingUtilities.invokeLater(() -> onSuccess.accept(result));

			} catch (Exception e) {
				SwingUtilities.invokeLater(() -> handleException(e, view));
			} finally {
				SwingUtilities.invokeLater(this::hideLoading);
			}
		});
	}

	protected void showLoading(Window parent) {
		SwingUtilities.invokeLater(() -> {
			// Se o dialog já existe, mas o parent mudou ou o dialog antigo foi descartado
			if (loadingDialog != null && loadingDialog.getOwner() != parent) {
				loadingDialog.dispose(); // Limpa o antigo da memória
				loadingDialog = null;
			}

			// Cria apenas se necessário para o parent atual
			if (loadingDialog == null) {
				loadingDialog = new LoadingDialog(parent);
			}

			if (!loadingDialog.isVisible()) {
				loadingDialog.show();
			}
		});
	}

	protected void hideLoading() {
		SwingUtilities.invokeLater(() -> {
			if (loadingDialog != null && loadingDialog.isVisible()) {
				loadingDialog.hide();
			}
		});
	}

	/**
	 * Aplica o "cadeado visual" nos botões da view baseado nas permissões do
	 * usuário. * @param permissoes Lista de strings ("READ", "WRITE", "DELETE")
	 * 
	 * @param btnNovo    Botão de criação (opcional)
	 * @param btnEditar  Botão de edição (opcional)
	 * @param btnExcluir Botão de exclusão (opcional)
	 * @return true se tiver permissão de leitura, false caso contrário
	 */
	protected boolean aplicarRestricoesVisuais(List<String> permissoes, AbstractButton btnNovo,
			AbstractButton btnEditar, AbstractButton btnExcluir) {

		// Se não tem leitura, avisa a Controller filha para fechar a tela
		if (!permissoes.contains("READ")) {
			return false;
		}

		boolean podeEscrever = permissoes.contains("WRITE");
		boolean podeExcluir = permissoes.contains("DELETE");

		if (btnNovo != null)
			btnNovo.setVisible(podeEscrever);
		if (btnEditar != null)
			btnEditar.setVisible(podeEscrever);
		if (btnExcluir != null)
			btnExcluir.setVisible(podeExcluir);

		return true;
	}

}
