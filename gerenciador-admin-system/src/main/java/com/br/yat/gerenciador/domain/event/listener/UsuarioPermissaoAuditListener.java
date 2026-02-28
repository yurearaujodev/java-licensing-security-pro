package com.br.yat.gerenciador.domain.event.listener;

import java.util.Map;

import com.br.yat.gerenciador.domain.event.UsuarioPermissaoEvents;
import com.br.yat.gerenciador.service.AuditLogService;

public class UsuarioPermissaoAuditListener extends GenericDomainEventListener {

    private final AuditLogService auditLogService;

    public UsuarioPermissaoAuditListener(AuditLogService auditLogService) {
        this.auditLogService = auditLogService;
        registerHandlers();
    }

    private void registerHandlers() {

        registerHandler(UsuarioPermissaoEvents.UsuarioAlterado.class, (event, conn) -> {
        	

            System.out.println("ANTES: " + event.getAntes());
            System.out.println("DEPOIS: " + event.getDepois());
            System.out.println("DIFF: " + event.getDiff());

            if (!event.getDiff().isEmpty()) {
            	
                auditLogService.registrarSucesso(
                        conn,
                        "SEGURANCA",
                        "ALTERAR_PERMISSOES_USUARIO",
                        "usuario_permissao",
                        event.getIdUsuario(),
                        event.getAntes(),
                        Map.of("depois",event.getDepois(),"diff",event.getDiff())
                );
            }
        });

        registerHandler(UsuarioPermissaoEvents.PerfilAlterado.class, (event, conn) -> {
        	
        	   System.out.println("ANTES: " + event.getAntes());
               System.out.println("DEPOIS: " + event.getDepois());
               System.out.println("DIFF: " + event.getDiff());

            if (!event.getDiff().isEmpty()) {
      
                auditLogService.registrarSucesso(
                        conn,
                        "SEGURANCA",
                        "ALTERAR_PERMISSOES_PERFIL",
                        "perfil_permissao",
                        event.getIdPerfil(),
                        event.getAntes(),
                        event.getDepois()
     //                   Map.of("depois",event.getDepois(),"diff",event.getDiff())
                );
            }
        });
        
    }
}