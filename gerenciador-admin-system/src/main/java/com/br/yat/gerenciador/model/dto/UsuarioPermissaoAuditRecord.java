package com.br.yat.gerenciador.model.dto;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;

import com.br.yat.gerenciador.model.UsuarioPermissao;

public record UsuarioPermissaoAuditRecord(
        Integer idPermissao,
        String chave,
        String tipo,
        boolean herdada,
        boolean ativa,
        LocalDateTime expiraEm
) {

    public static UsuarioPermissaoAuditRecord from(UsuarioPermissao up) {
        Integer idPermissao = up.getPermissao() != null ? up.getPermissao().getIdPermissoes() : null;
        String chave = up.getPermissao() != null ? up.getPermissao().getChave() : null;
        String tipo = up.getPermissao() != null ? up.getPermissao().getTipo() : null;

        return new UsuarioPermissaoAuditRecord(
                idPermissao,
                chave,
                tipo,
                up.isHerdada(),
                up.isAtiva(),
                up.getExpiraEm() != null ? up.getExpiraEm().truncatedTo(ChronoUnit.SECONDS) : null
        );
    }
    
    public String getIdComChaveTipo() {
        return idPermissao + ":" + chave + ":" + tipo;
    }
    
    public Map<String, Object> toMap() {
        Map<String, Object> map = new HashMap<>();
        map.put("idPermissao", idPermissao);
        map.put("chave", chave);
        map.put("tipo", tipo);
        map.put("herdada", herdada);
        map.put("ativa", ativa);
        map.put("expiraEm", expiraEm);
        return map;
    }
}