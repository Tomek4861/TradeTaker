package com.tomek4861.tradetaker.exception;

public class BybitIPBindingException extends RuntimeException {
    public BybitIPBindingException(String message) {
        super(message);
    }

    public BybitIPBindingException(String message, Throwable throwable) {
        super(message, throwable);
    }
}
