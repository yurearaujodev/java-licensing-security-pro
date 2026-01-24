package com.br.yat.gerenciador.util.exception;

public final class ServiceOperationException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public ServiceOperationException(String message, Throwable cause) {
		super(message, cause);
	}

}
