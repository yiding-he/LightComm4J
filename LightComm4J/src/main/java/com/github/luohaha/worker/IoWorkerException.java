package com.github.luohaha.worker;

/**
 * @author yiding_he
 */
public class IoWorkerException extends RuntimeException {

    public IoWorkerException() {
    }

    public IoWorkerException(String message) {
        super(message);
    }

    public IoWorkerException(String message, Throwable cause) {
        super(message, cause);
    }

    public IoWorkerException(Throwable cause) {
        super(cause);
    }
}
