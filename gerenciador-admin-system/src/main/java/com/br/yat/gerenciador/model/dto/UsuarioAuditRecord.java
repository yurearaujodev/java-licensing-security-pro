package com.br.yat.gerenciador.model.dto;

import com.br.yat.gerenciador.model.enums.StatusUsuario;
import com.br.yat.gerenciador.model.Usuario;
import java.time.LocalDateTime;

public record UsuarioAuditRecord(
    Integer idUsuario,
    String nome,
    String email,
    StatusUsuario status,
    Integer idPerfil,
    boolean master,
    boolean forcarResetSenha,
    LocalDateTime senhaExpiraEm,
    LocalDateTime bloqueadoAte
) {
    public static UsuarioAuditRecord fromUsuario(Usuario u) {
        return new UsuarioAuditRecord(
            u.getIdUsuario(),
            u.getNome(),
            u.getEmail(),
            u.getStatus(),
            u.getPerfil() != null ? u.getPerfil().getIdPerfil() : null,
            u.isMaster(),
            u.isForcarResetSenha(),
            u.getSenhaExpiraEm(),
            u.getBloqueadoAte()
        );
    }
}