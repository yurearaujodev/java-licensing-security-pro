package com.br.yat.gerenciador.util.exception;

import com.br.yat.gerenciador.model.enums.ValidationErrorType;

public final class ValidationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final ValidationErrorType errorType;

	public ValidationException(ValidationErrorType errorType) {
		super(resolveMessage(errorType));
		this.errorType = resolveErrorType(errorType);
	}

	public ValidationException(ValidationErrorType errorType, String message) {
		super(message != null ? message : resolveMessage(errorType));
		this.errorType = resolveErrorType(errorType);
	}

	public ValidationErrorType getErrorType() {
		return errorType;
	}

	private static ValidationErrorType resolveErrorType(ValidationErrorType type) {
		return type != null ? type : ValidationErrorType.GENERIC_VALIDATION_ERROR;
	}

	private static String resolveMessage(ValidationErrorType type) {
		return type != null ? type.getMessage() : ValidationErrorType.GENERIC_VALIDATION_ERROR.getMessage();
	}
}
