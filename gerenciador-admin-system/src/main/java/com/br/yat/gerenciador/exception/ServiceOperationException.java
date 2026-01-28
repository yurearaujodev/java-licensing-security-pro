package com.br.yat.gerenciador.exception;

import com.br.yat.gerenciador.model.enums.ServiceErrorType;

public final class ServiceOperationException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final ServiceErrorType errorType;

	public ServiceOperationException(ServiceErrorType errorType) {
		super(resolveMessage(errorType));
		this.errorType = resolveErrorType(errorType);
	}

	public ServiceOperationException(ServiceErrorType errorType, String message, Throwable cause) {
		super(message != null ? message : resolveMessage(errorType), cause);
		this.errorType = resolveErrorType(errorType);
	}

	public ServiceErrorType getErrorType() {
		return errorType;
	}

	private static ServiceErrorType resolveErrorType(ServiceErrorType type) {
		return type != null ? type : ServiceErrorType.INTERNAL_ERROR;
	}

	private static String resolveMessage(ServiceErrorType type) {
		return type != null ? type.getMessage() : ServiceErrorType.INTERNAL_ERROR.getMessage();
	}

}
