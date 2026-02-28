package com.br.yat.gerenciador.domain.event.listener;

import com.br.yat.gerenciador.dao.DaoFactory;
import com.br.yat.gerenciador.domain.event.ErrorEvents;
import com.br.yat.gerenciador.model.LogSistema;
import com.br.yat.gerenciador.util.AuditLogHelper;

public class ErrorEventListener extends GenericDomainEventListener {

    private final DaoFactory daoFactory;

    public ErrorEventListener(DaoFactory daoFactory) {
        this.daoFactory = daoFactory;
        registerHandlers();
    }

    private void registerHandlers() {
        registerHandler(ErrorEvents.ErroSistema.class, (event, conn) -> {
            LogSistema log = AuditLogHelper.gerarLogErro(
                event.getTipo(),
                event.getAcao(),
                event.getEntidade(),
                event.getMensagem()
            );
            daoFactory.createLogSistemaDao(conn).save(log);
        });
    }
}