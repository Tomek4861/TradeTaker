package com.tomek4861.cryptopositionmanager.dto.settings;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@AllArgsConstructor
@NoArgsConstructor
@Data
public class ApiKeyRequest {

    @NotNull
    @NotEmpty
    private String apiKey;
    @NotNull
    @NotEmpty
    private String secretKey;

}
