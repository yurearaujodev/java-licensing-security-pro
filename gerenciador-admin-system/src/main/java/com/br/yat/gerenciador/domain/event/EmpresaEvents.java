package com.br.yat.gerenciador.domain.event;

import com.br.yat.gerenciador.model.Banco;
import com.br.yat.gerenciador.model.Complementar;
import com.br.yat.gerenciador.model.Contato;
import com.br.yat.gerenciador.model.Documento;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Representante;
import com.br.yat.gerenciador.model.dto.EmpresaAuditRecord;
import com.br.yat.gerenciador.util.AuditDiffUtil;

import java.util.Collections;
import java.util.List;
import java.util.Map;

public class EmpresaEvents {

    public static class Criado extends DomainEvent {
        private final EmpresaAuditRecord empresa;

        public Criado(Empresa empresa,
                      List<Contato> contatos,
                      List<Representante> representantes,
                      List<Banco> bancos,
                      Complementar complementar,
                      List<Documento> documentos) {
            this.empresa = EmpresaAuditRecord.fromEmpresa(
                    empresa, contatos, representantes, bancos, complementar, documentos
            );
        }

        public EmpresaAuditRecord getEmpresa() {
            return empresa;
        }
    }

    public static class Alterado extends DomainEvent {
        private final EmpresaAuditRecord antes;
        private final EmpresaAuditRecord depois;
        private final Map<String, Object[]> diff;

        public Alterado(Empresa antesEmpresa, Empresa depoisEmpresa,
                        List<Contato> contatosAntes, List<Representante> repsAntes,
                        List<Banco> bancosAntes, Complementar compAntes,
                        List<Documento> docsAntes,
                        List<Contato> contatosDepois, List<Representante> repsDepois,
                        List<Banco> bancosDepois, Complementar compDepois,
                        List<Documento> docsDepois) {

            this.antes = EmpresaAuditRecord.fromEmpresa(
                    antesEmpresa, contatosAntes, repsAntes, bancosAntes, compAntes, docsAntes
            );

            this.depois = EmpresaAuditRecord.fromEmpresa(
                    depoisEmpresa, contatosDepois, repsDepois, bancosDepois, compDepois, docsDepois
            );

            this.diff = AuditDiffUtil.calcularDiff(this.antes, this.depois);
        }

        public EmpresaAuditRecord getAntes() { return antes; }
        public EmpresaAuditRecord getDepois() { return depois; }
        public Map<String, Object[]> getDiff() { return Collections.unmodifiableMap(diff); }
    }

    public static class Excluido extends DomainEvent {
        private final EmpresaAuditRecord antes;

        public Excluido(Empresa empresa,
                        List<Contato> contatos,
                        List<Representante> representantes,
                        List<Banco> bancos,
                        Complementar complementar,
                        List<Documento> documentos) {
            this.antes = EmpresaAuditRecord.fromEmpresa(
                    empresa, contatos, representantes, bancos, complementar, documentos
            );
        }

        public EmpresaAuditRecord getAntes() { return antes; }
    }

    public static class Restaurado extends DomainEvent {
        private final EmpresaAuditRecord depois;

        public Restaurado(Empresa empresa,
                          List<Contato> contatos,
                          List<Representante> representantes,
                          List<Banco> bancos,
                          Complementar complementar,
                          List<Documento> documentos) {
            this.depois = EmpresaAuditRecord.fromEmpresa(
                    empresa, contatos, representantes, bancos, complementar, documentos
            );
        }

        public EmpresaAuditRecord getDepois() { return depois; }
    }
}