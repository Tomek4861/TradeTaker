package com.tomek4861.tradetaker.exception;

import com.tomek4861.tradetaker.entity.User;

public class NoApiKeyException extends RuntimeException {

    public NoApiKeyException(User user) {
        super("No API key set for user: " + user.getUsername());
    }

    public NoApiKeyException(String message, Throwable cause) {
        super(message, cause);


    }
}
