package com.github.luohaha.handler;

/**
 * @author yiding_he
 */
public class IoHandlerException extends RuntimeException {

    public IoHandlerException() {
    }

    public IoHandlerException(String message) {
        super(message);
    }

    public IoHandlerException(String message, Throwable cause) {
        super(message, cause);
    }

    public IoHandlerException(Throwable cause) {
        super(cause);
    }
}
