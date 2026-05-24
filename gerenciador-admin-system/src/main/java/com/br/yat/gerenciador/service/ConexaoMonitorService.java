package com.br.yat.gerenciador.service;

import java.sql.Connection;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

import javax.swing.SwingUtilities;

import com.br.yat.gerenciador.configurations.ConnectionFactory;
import com.br.yat.gerenciador.domain.event.DomainEventPublisher;
import com.br.yat.gerenciador.security.SecurityService;

public class ConexaoMonitorService extends BaseService {

    private final ScheduledExecutorService scheduler;

    public ConexaoMonitorService(
            ScheduledExecutorService scheduler,
            DomainEventPublisher eventPublisher,
            SecurityService securityService) {

        super(eventPublisher, securityService);

        this.scheduler = scheduler;
    }

    public void iniciarMonitor(
            Consumer<Boolean> callbackStatus) {

        scheduler.scheduleAtFixedRate(() -> {

            boolean estaValida = validarConexao();

            SwingUtilities.invokeLater(() ->
                    callbackStatus.accept(estaValida));

            if (!estaValida) {
                dispararAlertaConexao();
            }

        }, 10, 30, TimeUnit.SECONDS);
    }

    private boolean validarConexao() {

        try (Connection conn =
                     ConnectionFactory.getConnection()) {

            return conn != null && conn.isValid(5);

        } catch (Exception e) {

            logger.error(
                    "ERRO AO VALIDAR CONEXÃO COM BANCO",
                    e);

            return false;
        }
    }

    private void dispararAlertaConexao() {

        logger.error(
                "CONEXÃO COM O BANCO DE DADOS PERDIDA!");
    }
}