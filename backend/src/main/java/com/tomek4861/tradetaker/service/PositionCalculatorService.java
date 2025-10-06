package com.tomek4861.tradetaker.service;

import com.tomek4861.tradetaker.dto.exchange.InstrumentEntryDTO;
import com.tomek4861.tradetaker.dto.positions.preview.CalculationParameters;
import com.tomek4861.tradetaker.dto.positions.preview.CalculationResult;
import com.tomek4861.tradetaker.dto.positions.preview.PreviewPositionRequest;
import com.tomek4861.tradetaker.dto.positions.preview.PreviewPositionResponse;
import com.tomek4861.tradetaker.dto.positions.takeprofit.TakeProfitLevel;
import com.tomek4861.tradetaker.entity.ApiKey;
import com.tomek4861.tradetaker.entity.User;
import com.tomek4861.tradetaker.exception.CalculationException;
import com.tomek4861.tradetaker.exception.NoApiKeyException;
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
    private final UserBybitServiceFactory userBybitServiceFactory;
    private final PositionCalculator positionCalculator;

    public PreviewPositionResponse calculatePositionInfo(PreviewPositionRequest request, User user) {
        ApiKey apiKey = user.getApiKey();
        BigDecimal riskPercentage = user.getRiskPercent();
        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            throw new NoApiKeyException(user);
        }

        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        Optional<BigDecimal> accBalanceOpt = userBybitService.getAccountBalance();

        if (accBalanceOpt.isEmpty()) {
            return new PreviewPositionResponse("Failed to get balance");
        }

        Optional<InstrumentEntryDTO> tickerInfoOpt = publicBybitService.getPerpetualTickerBySymbol(request.getTicker());

        if (tickerInfoOpt.isEmpty()) {
            return new PreviewPositionResponse("Failed to fetch instrument info for ticker: " + request.getTicker());
        }
        System.out.println(request);
        boolean isLimitOrder = request.getEntryPrice() == null;
        boolean areAnyTPs = request.getTakeProfitLevels() != null && !request.getTakeProfitLevels().isEmpty();


        if (isLimitOrder && areAnyTPs) {
            return new PreviewPositionResponse("Setting Take Profit is only available for Market orders.");
        }
        if (areAnyTPs && !validatePercentageSum(request.getTakeProfitLevels())) {
            return new PreviewPositionResponse("Take Profit levels percentage does not sum to 100%");
        }

        InstrumentEntryDTO tickerInfo = tickerInfoOpt.get();
        CalculationParameters params = new CalculationParameters(
                accBalanceOpt.get(),
                riskPercentage,
                request.getEntryPrice(),
                request.getStopLoss(),
                request.getTakeProfitLevels(),
                new BigDecimal(tickerInfo.getInstrumentEntry().getLeverageFilter().getMaxLeverage()),
                new BigDecimal(tickerInfo.getInstrumentEntry().getLotSizeFilter().getQtyStep())
        );

        try {
            CalculationResult result = positionCalculator.calculate(params);


            return new PreviewPositionResponse(
                    result.leverage().setScale(0, RoundingMode.DOWN),
                    result.requiredMargin().setScale(2, RoundingMode.HALF_UP),
                    result.riskAmount().setScale(2, RoundingMode.HALF_UP),
                    result.potentialProfit().setScale(2, RoundingMode.HALF_UP),
                    result.riskToRewardRatio().setScale(2, RoundingMode.HALF_UP),
                    result.positionValue().setScale(2, RoundingMode.HALF_UP),
                    result.positionSize().setScale(4, RoundingMode.HALF_UP),
                    null
            );
        } catch (CalculationException e) {
            return new PreviewPositionResponse(e.getMessage());
        }

    }

    private boolean validatePercentageSum(List<TakeProfitLevel> levels) {
        if (levels == null || levels.isEmpty()) {
            return false;
        }

        BigDecimal total = BigDecimal.ZERO;

        for (TakeProfitLevel level : levels) {
            if (level == null || level.getPercentage() == null) {
                return false;
            }
            total = total.add(level.getPercentage());
        }

        return total.compareTo(BigDecimal.valueOf(100)) == 0;
    }
}
