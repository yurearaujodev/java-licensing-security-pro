package com.br.yat.gerenciador.validation;

import com.br.yat.gerenciador.exception.ValidationException;
import com.br.yat.gerenciador.model.enums.ValidationErrorType;
import com.br.yat.gerenciador.util.ValidationUtils;

public final class DatabaseValidationUtils {
	private DatabaseValidationUtils() {
		throw new AssertionError();
	}

	public static void validarUrl(String url) {
		if (ValidationUtils.isEmpty(url)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "URL É OBRIGATÓRIA.");
		}
		if (!url.startsWith("jdbc:mysql://")) {
			throw new ValidationException(ValidationErrorType.INVALID_FIELD, "URL DEVE INICIAR COM 'jdbc:mysql://'.");
		}
	}

	public static void validarUsuario(String user) {
		if (ValidationUtils.isEmpty(user)) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "USUÁRIO É OBRIGATÓRIO.");
		}
	}

	public static void validarSenha(char[] pass) {
		if (pass == null || pass.length == 0) {
			throw new ValidationException(ValidationErrorType.REQUIRED_FIELD_MISSING, "SENHA É OBRIGATÓRIA.");
		}
	}

	public static void validarConfiguracaoCompleta(String url, String user, char[] password) {
		validarUrl(url);
		validarUsuario(user);
		validarSenha(password);
	}
}
