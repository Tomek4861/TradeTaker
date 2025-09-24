package com.tomek4861.cryptopositionmanager.dto.leverage;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class ChangeLeverageRequest {

    private BigDecimal leverage;
    private String ticker;

}
