package com.br.yat.gerenciador.domain.event.listener;

import com.br.yat.gerenciador.domain.event.PerfilEvents;
import com.br.yat.gerenciador.model.dto.PerfilAuditRecord;
import com.br.yat.gerenciador.service.AuditLogService;

public class PerfilAuditListener extends GenericDomainEventListener {

    private final AuditLogService auditLogService;

    public PerfilAuditListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
        registerHandlers();
    }

    private void registerHandlers() {

        registerHandler(PerfilEvents.Criado.class, (event, conn) -> {
            PerfilAuditRecord p = event.getPerfil();
            auditLogService.registrarSucesso(conn,
                    "SEGURANCA",
                    "CRIAR_PERFIL",
                    "perfil",
                    p.idPerfil(),
                    null,
                    p);
        });

        registerHandler(PerfilEvents.Alterado.class, (event, conn) -> {
            if (!event.getDiff().isEmpty()) {
                PerfilAuditRecord depois = event.getDepois();

                auditLogService.registrarSucesso(conn,
                        "SEGURANCA",
                        "ALTERAR_PERFIL",
                        "perfil",
                        depois.idPerfil(),
                        event.getAntes(),
                        event.getDepois());
            }
        });

        registerHandler(PerfilEvents.Excluido.class, (event, conn) -> {
            PerfilAuditRecord antes = event.getAntes();

            auditLogService.registrarSucesso(conn,
                    "SEGURANCA",
                    "EXCLUIR_PERFIL",
                    "perfil",
                    antes.idPerfil(),
                    antes,
                    null);
        });

        registerHandler(PerfilEvents.Restaurado.class, (event, conn) -> {
            PerfilAuditRecord depois = event.getDepois();

            auditLogService.registrarSucesso(conn,
                    "SEGURANCA",
                    "RESTAURAR_PERFIL",
                    "perfil",
                    depois.idPerfil(),
                    null,
                    depois);
        });
    }
}