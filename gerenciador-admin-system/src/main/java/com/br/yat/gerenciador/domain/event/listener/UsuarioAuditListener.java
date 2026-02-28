package com.br.yat.gerenciador.domain.event.listener;

import com.br.yat.gerenciador.model.dto.UsuarioAuditRecord;
import com.br.yat.gerenciador.service.AuditLogService;
import com.br.yat.gerenciador.domain.event.UsuarioEvents;

public class UsuarioAuditListener extends GenericDomainEventListener {

    private final AuditLogService auditLogService;

    public UsuarioAuditListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
        registerHandlers();
    }

    private void registerHandlers() {
        registerHandler(UsuarioEvents.Criado.class, (event, conn) -> {
            UsuarioAuditRecord u = event.getUsuario();
            auditLogService.registrarSucesso(conn, "CADASTRO", "INSERIR_USUARIO", "usuario", u.idUsuario(), null, u);
        });

        registerHandler(UsuarioEvents.Alterado.class, (event, conn) -> {
            if (!event.getDiff().isEmpty()) {
                UsuarioAuditRecord depois = event.getDepois();
                auditLogService.registrarSucesso(conn, "CADASTRO", "ALTERAR_USUARIO", "usuario", depois.idUsuario(), event.getAntes(), event.getDepois());
            }
        });

        registerHandler(UsuarioEvents.Excluido.class, (event, conn) -> {
            UsuarioAuditRecord antes = event.getAntes();
            auditLogService.registrarSucesso(conn, "CADASTRO", "EXCLUIR_USUARIO", "usuario", antes.idUsuario(), antes, null);
        });

        registerHandler(UsuarioEvents.Restaurado.class, (event, conn) -> {
            UsuarioAuditRecord depois = event.getDepois();
            auditLogService.registrarSucesso(conn, "CADASTRO", "RESTAURAR_USUARIO", "usuario", depois.idUsuario(), null, depois);
        });

        registerHandler(UsuarioEvents.SenhaAlterada.class, (event, conn) -> {
            UsuarioAuditRecord u = event.getUsuario();
            auditLogService.registrarSucesso(conn, "SEGURANCA", "SENHA_ALTERADA", "usuario", u.idUsuario(), "Senha alterada com sucesso.", null);
        });

        registerHandler(UsuarioEvents.StatusAlterado.class, (event, conn) -> {
            UsuarioAuditRecord antes = event.getAntes();
            UsuarioAuditRecord depois = event.getDepois();
            auditLogService.registrarSucesso(conn, "CADASTRO", "ALTERAR_STATUS_USUARIO", "usuario", depois.idUsuario(), antes.status(), depois.status());
        });
    }
}