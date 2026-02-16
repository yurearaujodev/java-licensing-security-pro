package com.br.yat.gerenciador.policy;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Empresa;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.TipoCadastro;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;

public final class EmpresaPolicy {

    private EmpresaPolicy() {} // impedir instanciação

    // Retorna true se o executor tem privilégios (master)
    public static boolean isPrivilegiado(Usuario u) {
        return u != null && u.isMaster();
    }

    // Setup inicial: permite criar empresa fornecedora mesmo sem executor
    public static boolean isSetupInicial(Empresa empresa, boolean existeEmpresaFornecedora, Usuario executor) {
        return empresa.getTipoEmpresa() == TipoCadastro.FORNECEDORA
                && !existeEmpresaFornecedora
                && executor == null;
    }

    public static void validarAlteracao(Empresa empresa, Usuario executor) {
        if (empresa == null) {
            throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "EMPRESA NÃO INFORMADA.");
        }

        if (empresa.getTipoEmpresa() == TipoCadastro.FORNECEDORA && !isPrivilegiado(executor)) {
            throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
                    "APENAS UM USUÁRIO MASTER PODE ALTERAR OS DADOS DA EMPRESA FORNECEDORA.");
        }
    }

    public static void validarExclusao(Empresa empresa, Usuario executor) {
        if (empresa == null) {
            throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "EMPRESA NÃO ENCONTRADA.");
        }

        if (empresa.getTipoEmpresa() == TipoCadastro.FORNECEDORA) {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD,
                    "A EMPRESA FORNECEDORA NÃO PODE SER INATIVADA.");
        }

        if (!empresa.isAtivo()) {
            throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "EMPRESA JÁ ESTÁ INATIVA.");
        }

        if (!isPrivilegiado(executor)) {
            throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
                    "APENAS USUÁRIO MASTER PODE INATIVAR EMPRESAS.");
        }
    }

    public static void validarRestauracao(Empresa empresa, Usuario executor) {
        if (empresa == null) {
            throw new ValidationException(ValidationErrorType.RESOURCE_NOT_FOUND, "EMPRESA NÃO ENCONTRADA.");
        }

        if (empresa.getTipoEmpresa() == TipoCadastro.FORNECEDORA) {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD,
                    "A EMPRESA FORNECEDORA NÃO PODE SER RESTAURADA.");
        }

        if (empresa.isAtivo()) {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, "EMPRESA JÁ ESTÁ ATIVA.");
        }

        if (!isPrivilegiado(executor)) {
            throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
                    "APENAS USUÁRIO MASTER PODE RESTAURAR EMPRESAS.");
        }
    }

    public static void validarCriacaoFornecedora(Empresa empresa, Usuario executor, boolean existeEmpresaFornecedora) {
        if (empresa.getTipoEmpresa() != TipoCadastro.FORNECEDORA)
            return;

        // Permite criar a primeira fornecedora sem executor (setup inicial)
        if (!isPrivilegiado(executor) && !isSetupInicial(empresa, existeEmpresaFornecedora, executor)) {
            throw new ValidationException(ValidationErrorType.ACCESS_DENIED,
                    "APENAS USUÁRIO MASTER PODE CRIAR EMPRESA FORNECEDORA.");
        }
    }
}
