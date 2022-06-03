package com.rrtv.exception;

public class TableAssociationException extends RuntimeException {

    public TableAssociationException() {
        super();
    }

    public TableAssociationException(String message, Throwable cause) {
        super(message, cause);
    }

    public TableAssociationException(String message) {
        super(message);
    }

    public TableAssociationException(Throwable cause) {
        super(cause == null ? null : cause.getMessage(), cause);
    }


}
