package com.br.yat.gerenciador.domain.event.listener;

import com.br.yat.gerenciador.model.dto.EmpresaAuditRecord;
import com.br.yat.gerenciador.service.AuditLogService;
import com.br.yat.gerenciador.domain.event.EmpresaEvents;

public class EmpresaAuditListener extends GenericDomainEventListener {

    private final AuditLogService auditLogService;

    public EmpresaAuditListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
        registerHandlers();
    }

    private void registerHandlers() {

        // Evento de criação
        registerHandler(EmpresaEvents.Criado.class, (event, conn) -> {
            EmpresaAuditRecord e = event.getEmpresa();
            auditLogService.registrarSucesso(
                    conn,
                    "CADASTRO",
                    "INSERIR_EMPRESA",
                    "empresa",
                    e.idEmpresa(),
                    null,
                    e
            );
        });

        // Evento de alteração
        registerHandler(EmpresaEvents.Alterado.class, (event, conn) -> {
            if (!event.getDiff().isEmpty()) {
                EmpresaAuditRecord antes = event.getAntes();
                EmpresaAuditRecord depois = event.getDepois();
                auditLogService.registrarSucesso(
                        conn,
                        "CADASTRO",
                        "ALTERAR_EMPRESA",
                        "empresa",
                        depois.idEmpresa(),
                        antes,
                        depois
                );
            }
        });

        // Evento de exclusão
        registerHandler(EmpresaEvents.Excluido.class, (event, conn) -> {
            EmpresaAuditRecord antes = event.getAntes();
            auditLogService.registrarSucesso(
                    conn,
                    "CADASTRO",
                    "EXCLUIR_EMPRESA",
                    "empresa",
                    antes.idEmpresa(),
                    antes,
                    null
            );
        });

        // Evento de restauração
        registerHandler(EmpresaEvents.Restaurado.class, (event, conn) -> {
            EmpresaAuditRecord depois = event.getDepois();
            auditLogService.registrarSucesso(
                    conn,
                    "CADASTRO",
                    "RESTAURAR_EMPRESA",
                    "empresa",
                    depois.idEmpresa(),
                    null,
                    depois
            );
        });
    }
}