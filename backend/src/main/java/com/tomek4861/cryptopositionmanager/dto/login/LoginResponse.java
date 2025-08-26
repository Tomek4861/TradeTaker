package com.tomek4861.cryptopositionmanager.dto.login;

import lombok.Data;

@Data
public class LoginResponse {

    private final Boolean success;
    private final String token;

}
