package com.tomek4861.cryptopositionmanager.dto.register;


import lombok.Data;

@Data
public class RegisterRequest {

    private final String username;
    private final String email;
    private final String password;
}
