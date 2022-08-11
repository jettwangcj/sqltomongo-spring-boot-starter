package com.rrtv.exception;

/**
 * @Classname BindingException
 * @Description
 * @Date 2022/8/10 16:58
 * @Created by wangchangjiu
 */
public class BindingException extends RuntimeException{

    private static final long serialVersionUID = 4300802238789381562L;

    public BindingException() {
        super();
    }

    public BindingException(String message) {
        super(message);
    }

    public BindingException(String message, Throwable cause) {
        super(message, cause);
    }

    public BindingException(Throwable cause) {
        super(cause);
    }

}
