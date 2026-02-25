package com.br.yat.gerenciador.model.dto;

import java.time.LocalDateTime;

import com.br.yat.gerenciador.model.Permissao;
import com.br.yat.gerenciador.model.UsuarioPermissao;

public record UsuarioPermissaoDetalheDTO(Permissao permissao, UsuarioPermissao vinculo) {
    // Atalho para facilitar no Controller
    public LocalDateTime getExpiraEm() {
        return vinculo != null ? vinculo.getExpiraEm() : null;
    }
}