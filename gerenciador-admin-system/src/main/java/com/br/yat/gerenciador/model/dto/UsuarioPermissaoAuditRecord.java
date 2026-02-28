package com.br.yat.gerenciador.model.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;

import com.br.yat.gerenciador.model.UsuarioPermissao;

public record UsuarioPermissaoAuditRecord(
        Integer idPermissao,
        boolean herdada,
        boolean ativa,
        LocalDateTime expiraEm
) {

    public static UsuarioPermissaoAuditRecord from(UsuarioPermissao up) {
        return new UsuarioPermissaoAuditRecord(
                up.getIdPermissoes(),
                up.isHerdada(),
                up.isAtiva(),
                up.getExpiraEm() != null
                        ? up.getExpiraEm().truncatedTo(ChronoUnit.SECONDS)
                        : null
        );
    }
}