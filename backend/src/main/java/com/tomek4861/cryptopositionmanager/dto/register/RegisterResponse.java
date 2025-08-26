package com.tomek4861.cryptopositionmanager.dto.register;


import lombok.Data;

@Data
public class RegisterResponse {

    private final boolean success;
    private final String token;

}
