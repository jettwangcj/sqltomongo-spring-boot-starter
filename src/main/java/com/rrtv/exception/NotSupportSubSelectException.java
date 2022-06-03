package com.rrtv.exception;

public class NotSupportSubSelectException extends RuntimeException{

    public NotSupportSubSelectException() {
        super();
    }

    public NotSupportSubSelectException(String message, Throwable cause) {
        super(message, cause);
    }

    public NotSupportSubSelectException(String message) {
        super(message);
    }

    public NotSupportSubSelectException(Throwable cause) {
        super(cause == null ? null : cause.getMessage(), cause);
    }



}
