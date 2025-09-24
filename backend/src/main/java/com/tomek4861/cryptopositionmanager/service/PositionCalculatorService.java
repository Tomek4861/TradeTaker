package com.tomek4861.cryptopositionmanager.service;

import com.bybit.api.client.domain.market.response.instrumentInfo.LeverageFilter;
import com.tomek4861.cryptopositionmanager.dto.exchange.InstrumentEntryDTO;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.takeprofit.TakeProfitLevel;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class PositionCalculatorService {
    // TODO: refactor this code

    private final PublicBybitService publicBybitService;

    public PreviewPositionResponse calculatePositionInfo(PreviewPositionRequest request, String apiKey, String secretKey, BigDecimal riskPercentage) {
        UserBybitService userBybitService = new UserBybitService(apiKey, secretKey);
        Optional<BigDecimal> accBalanceOptional = userBybitService.getAccountBalance();

        if (accBalanceOptional.isEmpty()) {
            return new PreviewPositionResponse("Failed to get balance");
        }
        System.out.println(request);

        BigDecimal accBalance = accBalanceOptional.get();
        BigDecimal entry = request.getEntryPrice();
        BigDecimal stopLoss = request.getStopLoss();
        List<TakeProfitLevel> takeProfits = request.getTakeProfitLevels();
        System.out.println(takeProfits + "TPS");

        // risk amount in usd
        BigDecimal riskAmount = accBalance.multiply(riskPercentage.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP));

        // 2. SL to entry distance in percentage
        BigDecimal slDistancePercentage = entry.subtract(stopLoss).abs()
                .divide(entry, 8, RoundingMode.HALF_UP)
                .multiply(BigDecimal.valueOf(100));

        // if SL equals entry
        if (slDistancePercentage.compareTo(BigDecimal.ZERO) == 0) {
            return new PreviewPositionResponse("Stop Loss equals entry price");
        }

        // Total position value
        BigDecimal positionValue = riskAmount.divide(slDistancePercentage.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP), 2, RoundingMode.HALF_UP);

        // Positions size
        BigDecimal positionSize = positionValue.divide(entry, 8, RoundingMode.HALF_UP);

        // Leverage
        BigDecimal maintenanceMarginRate = new BigDecimal("0.003"); // 0.3%
        BigDecimal slDistanceDecimal = slDistancePercentage.divide(BigDecimal.valueOf(100), 8, RoundingMode.HALF_UP);
        if (slDistanceDecimal.compareTo(maintenanceMarginRate) <= 0) {
            return new PreviewPositionResponse("Stop loss is too close to entry price for leverage calculation.");
        }
        BigDecimal numerator = BigDecimal.ONE.subtract(maintenanceMarginRate);
        BigDecimal denominator = slDistanceDecimal;


        BigDecimal leverage = numerator.divide(
                denominator,
                2,
                RoundingMode.UP
        );
        // reduce the leverage by 10% just for safety
        leverage = leverage.multiply(BigDecimal.valueOf(0.9));


        Optional<InstrumentEntryDTO> perpetualTickerBySymbol = publicBybitService.getPerpetualTickerBySymbol(request.getTicker());
        if (perpetualTickerBySymbol.isEmpty()) {
            return new PreviewPositionResponse("Unknown Ticker.");
        }

        InstrumentEntryDTO instrumentEntryDTO = perpetualTickerBySymbol.get();
        LeverageFilter leverageFilter = instrumentEntryDTO.getInstrumentEntry().getLeverageFilter();
        BigDecimal maxLeverage = new BigDecimal(leverageFilter.getMaxLeverage());

        if (leverage.compareTo(maxLeverage) > 0) {
            leverage = maxLeverage;
        }


        // Profit and RR
        BigDecimal potentialProfit = BigDecimal.ZERO;
        BigDecimal riskToRewardRatio = BigDecimal.ZERO;

        // check if take profits sum to 100


        if (takeProfits != null && !takeProfits.isEmpty()) {

            if (!validatePercentageSum(takeProfits)) {
                return new PreviewPositionResponse("Take Profit levels percentage does not sum to 100%");
            }
            BigDecimal avgTPLevel = calculateWeightedAveragePrice(takeProfits);

            potentialProfit = avgTPLevel.subtract(entry).abs().multiply(positionSize);

            if (riskAmount.compareTo(BigDecimal.ZERO) > 0) {
                riskToRewardRatio = potentialProfit.divide(riskAmount, 2, RoundingMode.HALF_UP);
            }
        }

        // Position Size and Value
        BigDecimal requiredMargin = positionValue.divide(leverage, 8, RoundingMode.HALF_UP);


        var response = new PreviewPositionResponse(
                true,
                leverage.setScale(0, RoundingMode.DOWN),
                requiredMargin.setScale(2, RoundingMode.HALF_UP),
                riskAmount.setScale(2, RoundingMode.HALF_UP),
                potentialProfit.setScale(2, RoundingMode.HALF_UP),
                riskToRewardRatio,
                positionValue.setScale(2, RoundingMode.HALF_UP),
                positionSize.setScale(4, RoundingMode.HALF_UP),
                null
        );

        return response;
    }

    private boolean validatePercentageSum(List<TakeProfitLevel> levels) {
        BigDecimal totalPercentage = levels.stream()
                .map(TakeProfitLevel::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPercentage.compareTo(BigDecimal.valueOf(100)) == 0;
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
