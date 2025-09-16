package com.tomek4861.cryptopositionmanager.dto.position.preview;


import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class PreviewPositionResponse {
    private Boolean success;
    private BigDecimal leverage;
    private BigDecimal requiredMargin;
    private BigDecimal potentialLoss;
    private BigDecimal potentialProfit;
    private BigDecimal riskToRewardRatio;
    private String errorMessage;

    public PreviewPositionResponse(String errorMsg) {
        this.success = false;
        this.errorMessage = errorMsg;
    }

}
