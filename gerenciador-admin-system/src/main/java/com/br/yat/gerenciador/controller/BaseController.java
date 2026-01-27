package com.br.yat.gerenciador.controller;

import java.awt.Window;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.br.yat.gerenciador.util.DialogFactory;
import com.br.yat.gerenciador.util.exception.DataAccessException;
import com.br.yat.gerenciador.util.exception.ServiceOperationException;
import com.br.yat.gerenciador.util.exception.ValidationException;
import com.br.yat.gerenciador.util.ui.LoadingDialog;

public abstract class BaseController {
	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	protected final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	private LoadingDialog loadingDialog;

	public void dispose() {
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
					DialogFactory.erro(view, se.getMessage());
				} else {
					DialogFactory.aviso(view, se.getMessage());
				}
			}
			case DataAccessException de -> {
				String msg = "FALHA NO BANCO DE DADOS\nCÓDIGO: %s\nERRO: %s".formatted(de.getErrorCode(),
						de.getMessage());
				DialogFactory.erro(view, msg);
			}
			default -> {
				DialogFactory.erro(view, "ERRO INESPERADO: " + e.getMessage());
				logger.error("ERRO NÃO TRATADO NO SISTEMA", e);
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
			}
		});
	}

	protected void showLoading(Window parent) {
		SwingUtilities.invokeLater(() -> {
			if (loadingDialog == null) {
				loadingDialog = new LoadingDialog(parent);
			}
			loadingDialog.show();
		});
	}

	protected void hideLoading() {
		SwingUtilities.invokeLater(() -> {
			if (loadingDialog != null) {
				loadingDialog.hide();
			}
		});
	}

}
