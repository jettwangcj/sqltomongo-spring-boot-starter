package com.rrtv.exception;

/**
 * @Classname PluginException
 * @Description
 * @Date 2022/8/10 16:58
 * @Created by wangchangjiu
 */
public class PluginException extends RuntimeException{

    private static final long serialVersionUID = 4300802238789381562L;

    public PluginException() {
        super();
    }

    public PluginException(String message) {
        super(message);
    }

    public PluginException(String message, Throwable cause) {
        super(message, cause);
    }

    public PluginException(Throwable cause) {
        super(cause);
    }

}
