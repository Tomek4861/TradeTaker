package com.tomek4861.cryptopositionmanager.dto.settings;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class AllSettingsResponse {

    private boolean success;
    private String apiKey;
    private BigDecimal riskPercentage;
}
