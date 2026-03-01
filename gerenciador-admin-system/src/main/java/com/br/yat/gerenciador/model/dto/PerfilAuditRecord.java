package com.br.yat.gerenciador.model.dto;

import com.br.yat.gerenciador.model.Perfil;

public record PerfilAuditRecord(
        Integer idPerfil,
        String nome,
        String descricao
) {
    public static PerfilAuditRecord fromPerfil(Perfil p) {
        if (p == null) return null;

        return new PerfilAuditRecord(
                p.getIdPerfil(),
                p.getNome(),
                p.getDescricao()
        );
    }
}