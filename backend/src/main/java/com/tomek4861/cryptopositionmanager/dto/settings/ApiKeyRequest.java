package com.tomek4861.cryptopositionmanager.dto.settings;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiKeyRequest {

    private String apiKey;
    private String secretKey;

}
