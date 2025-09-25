package com.tomek4861.cryptopositionmanager.dto.leverage;


import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ChangeLeverageRequest {

    @NotNull
    @Positive
    private BigDecimal leverage;
    @NotNull
    @NotEmpty
    private String ticker;

}
