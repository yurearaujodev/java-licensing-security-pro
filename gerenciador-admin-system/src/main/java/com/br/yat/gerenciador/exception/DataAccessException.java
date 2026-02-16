package com.br.yat.gerenciador.exception;

import com.br.yat.gerenciador.model.enums.DataAccessErrorType;

public final class DataAccessException extends RuntimeException {

	private static final long serialVersionUID = 1L;
	private final DataAccessErrorType errorType;

	public DataAccessException(DataAccessErrorType errorType) {
		super(resolveMessage(errorType));
		this.errorType = resolveErrorType(errorType);
	}

	public DataAccessException(DataAccessErrorType errorType, String message, Throwable cause) {
		super(message != null ? message : resolveMessage(errorType), cause);
		this.errorType = resolveErrorType(errorType);
	}
	
	public DataAccessException(DataAccessErrorType errorType, Throwable cause) {
	    super(resolveMessage(errorType), cause);
	    this.errorType = resolveErrorType(errorType);
	}

	public DataAccessErrorType getErrorType() {
		return errorType;
	}

	public String getErrorCode() {
		return errorType.getCode();
	}

	private static DataAccessErrorType resolveErrorType(DataAccessErrorType type) {
		return type != null ? type : DataAccessErrorType.INTERNAL_ERROR;
	}

	private static String resolveMessage(DataAccessErrorType type) {
		return type != null ? type.getMessage() : DataAccessErrorType.INTERNAL_ERROR.getMessage();
	}

}
