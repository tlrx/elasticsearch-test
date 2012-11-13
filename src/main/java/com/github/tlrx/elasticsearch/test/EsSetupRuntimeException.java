package com.github.tlrx.elasticsearch.test;

public class EsSetupRuntimeException extends RuntimeException {

    public EsSetupRuntimeException() {
        super();
    }

    public EsSetupRuntimeException(String message, Throwable cause) {
        super(message, cause);
    }

    public EsSetupRuntimeException(String message) {
        super(message);
    }

    public EsSetupRuntimeException(Throwable cause) {
        super(cause);
    }
}
