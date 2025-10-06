package com.tomek4861.tradetaker.dto.positions.preview;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class PreviewPositionResponse {
    private BigDecimal leverage;
    private BigDecimal requiredMargin;
    private BigDecimal potentialLoss;
    private BigDecimal potentialProfit;
    private BigDecimal riskToRewardRatio;
    private BigDecimal value;
    private BigDecimal size;
    private String error;

    public PreviewPositionResponse(String error) {
        this.error = error;
    }

}
