package com.br.yat.gerenciador.controller;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BaseController {
	private static final Logger logger = LoggerFactory.getLogger(BaseController.class);
	protected final ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
	protected final ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
	
	public void dispose() {
		if (!executor.isShutdown()) {
			executor.shutdown();
		}
		if (!scheduler.isShutdown()) {
			scheduler.shutdown();
		}
		logger.info(this.getClass().getSimpleName()+"ENCERRANDO E THREADS LIBERADAS.");
	}

}
