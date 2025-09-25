package com.tomek4861.cryptopositionmanager.dto.positions.preview;

import com.tomek4861.cryptopositionmanager.dto.positions.takeprofit.TakeProfitLevel;

import java.math.BigDecimal;
import java.util.List;

public record CalculationParameters(
        BigDecimal accountBalance,
        BigDecimal riskPercentage,
        BigDecimal entryPrice,
        BigDecimal stopLoss,
        List<TakeProfitLevel> takeProfits,
        BigDecimal maxLeverage,
        BigDecimal qtyStep
) {
}
