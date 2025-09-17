package com.tomek4861.cryptopositionmanager.service;

import com.bybit.api.client.domain.market.response.instrumentInfo.LeverageFilter;
import com.tomek4861.cryptopositionmanager.dto.exchange.InstrumentEntryDTO;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PositionCalculatorService {
    private final PublicBybitService publicBybitService;

    public PreviewPositionResponse calculatePositionInfo(PreviewPositionRequest request, String apiKey, String secretKey, BigDecimal riskPercentage) {
        UserBybitService userBybitService = new UserBybitService(apiKey, secretKey);
        Optional<BigDecimal> accBalanceOptional = userBybitService.getAccountBalance();

        if (accBalanceOptional.isEmpty()) {
            return new PreviewPositionResponse("Failed to get balance");
        }

        BigDecimal accBalance = accBalanceOptional.get();
        BigDecimal entry = request.getEntryPrice();
        BigDecimal stopLoss = request.getStopLossPrice();
        List<BigDecimal> takeProfits = request.getTakeProfitLevels();
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

        if (takeProfits != null && !takeProfits.isEmpty()) {
            BigDecimal avgTPLevel = takeProfits.stream()
                    .reduce(BigDecimal.ZERO, BigDecimal::add)
                    .divide(new BigDecimal(takeProfits.size()), 8, RoundingMode.HALF_UP);

            potentialProfit = avgTPLevel.subtract(entry).abs().multiply(positionSize);

            if (riskAmount.compareTo(BigDecimal.ZERO) > 0) {
                riskToRewardRatio = potentialProfit.divide(riskAmount, 2, RoundingMode.HALF_UP);
            }
        }

        var response = new PreviewPositionResponse(
                true,
                leverage.setScale(0, RoundingMode.DOWN),
                positionValue.setScale(2, RoundingMode.HALF_UP),
                riskAmount.setScale(2, RoundingMode.HALF_UP),
                potentialProfit.setScale(2, RoundingMode.HALF_UP),
                riskToRewardRatio,
                null
        );

        return response;
    }
}
