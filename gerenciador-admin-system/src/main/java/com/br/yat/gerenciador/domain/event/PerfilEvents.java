package com.br.yat.gerenciador.domain.event;

import com.br.yat.gerenciador.model.Perfil;
import com.br.yat.gerenciador.model.dto.PerfilAuditRecord;
import com.br.yat.gerenciador.util.AuditDiffUtil;

import java.util.Collections;
import java.util.Map;

public class PerfilEvents {

    public static class Criado extends DomainEvent {
        private final PerfilAuditRecord perfil;

        public Criado(Perfil p) {
            this.perfil = PerfilAuditRecord.fromPerfil(p);
        }

        public PerfilAuditRecord getPerfil() {
            return perfil;
        }
    }

    public static class Alterado extends DomainEvent {
        private final PerfilAuditRecord antes;
        private final PerfilAuditRecord depois;
        private final Map<String, Object[]> diff;

        public Alterado(Perfil pAntes, Perfil pDepois) {
            this.antes = PerfilAuditRecord.fromPerfil(pAntes);
            this.depois = PerfilAuditRecord.fromPerfil(pDepois);
            this.diff = AuditDiffUtil.calcularDiff(this.antes, this.depois);
        }

        public PerfilAuditRecord getAntes() {
            return antes;
        }

        public PerfilAuditRecord getDepois() {
            return depois;
        }

        public Map<String, Object[]> getDiff() {
            return Collections.unmodifiableMap(diff);
        }
    }

    public static class Excluido extends DomainEvent {
        private final PerfilAuditRecord antes;

        public Excluido(Perfil p) {
            this.antes = PerfilAuditRecord.fromPerfil(p);
        }

        public PerfilAuditRecord getAntes() {
            return antes;
        }
    }

    public static class Restaurado extends DomainEvent {
        private final PerfilAuditRecord depois;

        public Restaurado(Perfil p) {
            this.depois = PerfilAuditRecord.fromPerfil(p);
        }

        public PerfilAuditRecord getDepois() {
            return depois;
        }
    }
}