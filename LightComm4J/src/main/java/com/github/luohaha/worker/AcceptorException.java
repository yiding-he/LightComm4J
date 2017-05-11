package com.github.luohaha.worker;

/**
 * @author yiding_he
 */
public class AcceptorException extends RuntimeException {

    public AcceptorException() {
    }

    public AcceptorException(String message) {
        super(message);
    }

    public AcceptorException(String message, Throwable cause) {
        super(message, cause);
    }

    public AcceptorException(Throwable cause) {
        super(cause);
    }
}
