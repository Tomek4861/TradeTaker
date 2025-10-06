package com.tomek4861.tradetaker.dto.positions.preview;

import java.math.BigDecimal;

public record CalculationResult(
        BigDecimal riskAmount,
        BigDecimal positionValue,
        BigDecimal positionSize,
        BigDecimal leverage,
        BigDecimal requiredMargin,
        BigDecimal potentialProfit,
        BigDecimal riskToRewardRatio
) {
}
