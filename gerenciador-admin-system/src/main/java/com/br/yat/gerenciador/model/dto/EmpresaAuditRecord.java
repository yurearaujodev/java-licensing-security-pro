package com.br.yat.gerenciador.model.dto;

import com.br.yat.gerenciador.model.*;
import com.br.yat.gerenciador.model.enums.*;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public record EmpresaAuditRecord(
        Integer idEmpresa,
        TipoCadastro tipoEmpresa,
        String fantasia,
        String razaoSocial,
        TipoDocumento tipoDoc,
        String documento,
        String inscEst,
        String inscMun,
        String contribuinteIcms,
        LocalDate fundacao,
        Cnae cnae,                   // pode ser null para cliente
        PorteEmpresa porte,           // pode ser null para cliente
        NaturezaJuridica naturezaJuridica, // pode ser null para cliente
        Integer crt,                  // pode ser null para cliente
        RegimeTributario regimeTrib,  // pode ser null para cliente
        BigDecimal capital,           // pode ser null para cliente
        SituacaoEmpresa situacao,
        Endereco endereco,
        List<Contato> contatos,
        List<Representante> representantes,
        List<Banco> bancos,
        Complementar complementar,
        List<Documento> documentos
) {

    /**
     * Cria o record de auditoria a partir de uma Empresa e seus relacionamentos.
     * Para clientes, os campos fiscais e relacionamentos extras podem ser nulos.
     */
    public static EmpresaAuditRecord fromEmpresa(
            Empresa e,
            List<Contato> contatos,
            List<Representante> reps,
            List<Banco> bancos,
            Complementar comp,
            List<Documento> docs
    ) {
        if (e == null) return null;

        // Se for cliente, alguns campos fiscais serão nulos
        Cnae cnae = e.getTipoEmpresa() == TipoCadastro.FORNECEDORA ? e.getCnaeEmpresa() : null;
        PorteEmpresa porte = e.getTipoEmpresa() == TipoCadastro.FORNECEDORA ? e.getPorteEmpresa() : null;
        NaturezaJuridica natureza = e.getTipoEmpresa() == TipoCadastro.FORNECEDORA ? e.getNaturezaJuriEmpresa() : null;
        Integer crt = e.getTipoEmpresa() == TipoCadastro.FORNECEDORA ? e.getCrtEmpresa() : null;
        RegimeTributario regime = e.getTipoEmpresa() == TipoCadastro.FORNECEDORA ? e.getRegimeTribEmpresa() : null;
        BigDecimal capital = e.getTipoEmpresa() == TipoCadastro.FORNECEDORA ? e.getCapitalEmpresa() : null;

        return new EmpresaAuditRecord(
                e.getIdEmpresa(),
                e.getTipoEmpresa(),
                e.getFantasiaEmpresa(),
                e.getRazaoSocialEmpresa(),
                e.getTipoDocEmpresa(),
                e.getDocumentoEmpresa(),
                e.getInscEst(),
                e.getInscMun(),
                e.getContribuinteIcmsEmpresa(),
                e.getFundacaoEmpresa(),
                cnae,
                porte,
                natureza,
                crt,
                regime,
                capital,
                e.getSituacaoEmpresa(),
                e.getEndereco(),
                contatos != null ? contatos : List.of(),
                reps != null ? reps : List.of(),
                bancos != null ? bancos : List.of(),
                comp, // complementar pode ser null se cliente
                docs != null ? docs : List.of()
        );
    }
}