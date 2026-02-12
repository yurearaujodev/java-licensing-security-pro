package com.br.yat.gerenciador.validation;

import java.util.regex.Pattern;
import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.Usuario;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;

public final class UsuarioValidationUtils {

    // Regex para e-mail padrão RFC 5322
    private static final String EMAIL_REGEX = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,6}$";
    private static final Pattern EMAIL_PATTERN = Pattern.compile(EMAIL_REGEX);

    private UsuarioValidationUtils() {
        throw new AssertionError("Classe Utilitária.");
    }

    public static void validarUsuario(Usuario usuario) {
        if (usuario == null) {
            throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "OBJETO USUÁRIO NÃO PODE SER NULO.");
        }

        validarNome(usuario.getNome());
        validarEmail(usuario.getEmail());
        
        // Só valida senha se for um novo usuário ou se o campo de senha foi preenchido na edição
        if (usuario.getIdUsuario() == null || (usuario.getSenhaHash() != null && usuario.getSenhaHash().length > 0)) {
            validarSenha(usuario.getSenhaHash());
        }
        
        if (!usuario.isMaster() && (usuario.getPerfil() == null || usuario.getPerfil().getIdPerfil() == null)) {
            throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "TODO USUÁRIO COMUM DEVE TER UM PERFIL VINCULADO.");
        }
        
        if (usuario.getEmpresa() == null || usuario.getEmpresa().getIdEmpresa() == null || usuario.getEmpresa().getIdEmpresa() <= 0) {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, "USUÁRIO DEVE ESTAR VINCULADO A UMA EMPRESA.");
        }
    }

    private static void validarNome(String nome) {
        if (nome == null || nome.trim().isEmpty()) {
            throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "NOME DO USUÁRIO É OBRIGATÓRIO.");
        }
        if (nome.trim().length() < 3) {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, "NOME DEVE TER NO MÍNIMO 3 CARACTERES.");
        }
    }

    private static void validarEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "E-MAIL É OBRIGATÓRIO.");
        }
        if (!EMAIL_PATTERN.matcher(email).matches()) {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, "FORMATO DE E-MAIL INVÁLIDO.");
        }
    }

    private static void validarSenha(char[] senha) {
        if (senha == null || senha.length == 0) {
            throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "A SENHA NÃO PODE SER VAZIA.");
        }
        if (senha.length < 6) {
            throw new ValidationException(ValidationErrorType.INVALID_FIELD, "A SENHA DEVE TER NO MÍNIMO 6 CARACTERES.");
        }
        
        // Aqui você pode adicionar lógica de complexidade (Ex: exigir número ou caractere especial)
    }
}
