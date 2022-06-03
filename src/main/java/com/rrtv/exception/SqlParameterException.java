package com.rrtv.exception;

public class SqlParameterException extends RuntimeException {

    public SqlParameterException() {
        super();
    }

    public SqlParameterException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlParameterException(String message) {
        super(message);
    }

    public SqlParameterException(Throwable cause) {
        super(cause == null ? null : cause.getMessage(), cause);
    }

}
