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
import com.br.yat.gerenciador.model.enums.TipoPermissao;
import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.view.factory.LoadingDialog;

public abstract class BaseController {
	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	protected final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private LoadingDialog loadingDialog;
	private int loadingCounter = 0;

	public void dispose() {
		hideLoading(true);
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

		executor.submit(() -> {
			T result = null;
			Exception error = null;

			try {
				result = task.execute();
			} catch (Exception e) {
				error = e;
			}

			final T res = result;
			final Exception ex = error;

			SwingUtilities.invokeLater(() -> {
				hideLoading(false);

				if (ex != null) {
					handleException(ex, view);
				} else if (onSuccess != null) {
					onSuccess.accept(res);
				}
			});
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
				SwingUtilities.invokeLater(() -> hideLoading(false));
			}
		});
	}

	protected void showLoading(Window parent) {
		synchronized (this) {
			if (loadingDialog == null || loadingDialog.getOwner() != parent) {
				if (loadingDialog != null)
					loadingDialog.dispose();
				loadingDialog = new LoadingDialog(parent);
			}
			loadingCounter++;
			SwingUtilities.invokeLater(() -> {
				if (!loadingDialog.isVisible())
					loadingDialog.show();
			});
		}
	}

	protected void hideLoading(boolean force) {
	    final LoadingDialog dialogRef;

	    synchronized (this) {

	        if (loadingDialog == null)
	            return;

	        loadingCounter = force
	                ? 0
	                : Math.max(0, loadingCounter - 1);

	        if (loadingCounter != 0)
	            return;

	        dialogRef = loadingDialog;
	    }

	    SwingUtilities.invokeLater(dialogRef::hide);
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
		boolean temRead = permissoes.contains(TipoPermissao.READ.name());

		// Sempre mostra a tela se houver permissão de leitura
		boolean podeEscrever = permissoes.contains(TipoPermissao.WRITE.name());
		boolean podeExcluir = permissoes.contains(TipoPermissao.DELETE.name());

		SwingUtilities.invokeLater(() -> {
			if (btnNovo != null)
				btnNovo.setVisible(podeEscrever);
			if (btnEditar != null)
				btnEditar.setVisible(podeEscrever);
			if (btnExcluir != null)
				btnExcluir.setVisible(podeExcluir);
		});

		return temRead;
	}

}
