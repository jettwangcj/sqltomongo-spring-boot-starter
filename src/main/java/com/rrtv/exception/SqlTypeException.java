package com.rrtv.exception;

public class SqlTypeException extends RuntimeException {

    public SqlTypeException() {
        super();
    }

    public SqlTypeException(String message, Throwable cause) {
        super(message, cause);
    }

    public SqlTypeException(String message) {
        super(message);
    }

    public SqlTypeException(Throwable cause) {
        super(cause == null ? null : cause.getMessage(), cause);
    }


}
