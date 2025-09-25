package com.tomek4861.cryptopositionmanager.service;

import com.tomek4861.cryptopositionmanager.dto.positions.preview.CalculationParameters;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.CalculationResult;
import com.tomek4861.cryptopositionmanager.dto.positions.takeprofit.TakeProfitLevel;
import com.tomek4861.cryptopositionmanager.exception.CalculationException;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;

@Component
public class PositionCalculator {
    private static final BigDecimal MAINTENANCE_MARGIN_RATE = new BigDecimal("0.003");
    private static final BigDecimal LEVERAGE_SAFETY_BUFFER = new BigDecimal("0.9");

    public CalculationResult calculate(CalculationParameters params) throws CalculationException {

        BigDecimal initialRiskAmount = calculateRiskAmount(params.accountBalance(), params.riskPercentage());
        BigDecimal slDistancePercentage = calculateSlDistancePercentage(params.entryPrice(), params.stopLoss());
        BigDecimal rawPositionSize = calculateRawPositionSize(initialRiskAmount, params.entryPrice(), slDistancePercentage);


        BigDecimal finalPositionSize = adjustSizeToQtyStep(rawPositionSize, params.qtyStep());

        BigDecimal stopLossDistanceInDollars = params.entryPrice().subtract(params.stopLoss()).abs();
        BigDecimal finalPotentialLoss = finalPositionSize.multiply(stopLossDistanceInDollars);


        BigDecimal positionValue = finalPositionSize.multiply(params.entryPrice());

        BigDecimal leverage = calculateLeverage(params.maxLeverage(), slDistancePercentage);

        BigDecimal requiredMargin = positionValue.divide(leverage, 8, RoundingMode.HALF_UP);

        BigDecimal potentialProfit = calculatePotentialProfit(params.takeProfits(), finalPositionSize, params.entryPrice());

        BigDecimal riskToRewardRatio = calculateRiskToRewardRatio(potentialProfit, finalPotentialLoss);

        return new CalculationResult(finalPotentialLoss, positionValue, finalPositionSize, leverage, requiredMargin, potentialProfit, riskToRewardRatio);

    }

    private BigDecimal calculateRiskAmount(BigDecimal accBalance, BigDecimal riskPercentage) {
        return accBalance.multiply(riskPercentage.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));

    }

    private BigDecimal calculateSlDistancePercentage(BigDecimal entry, BigDecimal stopLoss) {
        // SL to entry distance in percentage
        BigDecimal slDistancePercentage = entry.subtract(stopLoss).abs()
                .divide(entry, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // if SL equals entry
        if (slDistancePercentage.compareTo(BigDecimal.ZERO) == 0) {
            throw new CalculationException("Stop Loss equals entry price");
        }
        return slDistancePercentage;
    }

    private BigDecimal calculateRawPositionSize(BigDecimal riskAmount, BigDecimal entry, BigDecimal slDistancePercentage) {


        // Total position value
        BigDecimal positionValue = riskAmount.divide(slDistancePercentage.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP);

        // Positions size
        BigDecimal positionSize = positionValue.divide(entry, 8, RoundingMode.HALF_UP);
        return positionSize;

    }

    private BigDecimal adjustSizeToQtyStep(BigDecimal size, BigDecimal qtyStep) {

        return size.divide(qtyStep, 0, RoundingMode.DOWN).multiply(qtyStep);
    }

    private BigDecimal calculateLeverage(BigDecimal maxLeverage, BigDecimal slDistancePercentage) {

        BigDecimal slDistanceDecimal = slDistancePercentage.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        if (slDistanceDecimal.compareTo(MAINTENANCE_MARGIN_RATE) <= 0) {
            throw new CalculationException("Stop loss is too close to entry price for leverage calculation.");
        }
        BigDecimal numerator = BigDecimal.ONE.subtract(MAINTENANCE_MARGIN_RATE);
        BigDecimal denominator = slDistanceDecimal;


        BigDecimal leverage = numerator.divide(
                denominator,
                2,
                RoundingMode.UP
        );
        // reduce the leverage by 10% just for safety
        leverage = leverage.multiply(LEVERAGE_SAFETY_BUFFER);
        if (leverage.compareTo(maxLeverage) > 0) {
            leverage = maxLeverage;
        }
        if (leverage.compareTo(BigDecimal.ONE) < 0) {
            leverage = BigDecimal.ONE;
        }

        return leverage;

    }

    private BigDecimal calculatePotentialProfit(List<TakeProfitLevel> takeProfits, BigDecimal size, BigDecimal entry) {
        if (takeProfits == null || takeProfits.isEmpty()) {
            return BigDecimal.ZERO;
        }

        BigDecimal avgTPLevel = calculateWeightedAveragePrice(takeProfits);

        BigDecimal potentialProfit = avgTPLevel.subtract(entry).abs().multiply(size);

        return potentialProfit;

    }

    private BigDecimal calculateRiskToRewardRatio(BigDecimal profit, BigDecimal risk) {
        BigDecimal riskToRewardRatio = BigDecimal.ZERO;
        if (risk.compareTo(BigDecimal.ZERO) > 0) {
            riskToRewardRatio = profit.divide(risk, 2, RoundingMode.HALF_UP);
        }
        return riskToRewardRatio;

    }


    private BigDecimal calculateWeightedAveragePrice(List<TakeProfitLevel> levels) {
        BigDecimal weightedSum = levels.stream()
                .map(level -> level.getPrice().multiply(level.getPercentage()))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal totalWeight = levels.stream()
                .map(TakeProfitLevel::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return weightedSum.divide(totalWeight, 8, RoundingMode.HALF_UP);
    }


}
