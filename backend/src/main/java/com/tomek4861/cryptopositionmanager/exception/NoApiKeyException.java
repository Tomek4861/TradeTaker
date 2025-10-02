package com.tomek4861.cryptopositionmanager.exception;

import com.tomek4861.cryptopositionmanager.entity.User;

public class NoApiKeyException extends RuntimeException {

    public NoApiKeyException(User user) {
        super("No API key set for user: " + user.getUsername());
    }

    public NoApiKeyException(String message, Throwable cause) {
        super(message, cause);


    }
}
