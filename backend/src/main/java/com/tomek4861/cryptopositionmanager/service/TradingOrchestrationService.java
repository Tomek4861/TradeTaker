package com.tomek4861.cryptopositionmanager.service;


import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.tomek4861.cryptopositionmanager.domain.position.takeprofit.CalculatedTakeProfit;
import com.tomek4861.cryptopositionmanager.dto.exchange.InstrumentEntryDTO;
import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.close.ClosePositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.open.OpenPositionWithTPRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.takeprofit.TakeProfitLevel;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.exception.CalculationException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class TradingOrchestrationService {

    private final UserSettingsService userSettingsService;
    private final UserBybitServiceFactory userBybitServiceFactory;
    private final PublicBybitService publicBybitService;


    public StandardResponse openPositionWithTakeProfits(OpenPositionWithTPRequest request) {
        System.out.println(request);

        System.out.println(request.getStopLoss().toPlainString());

        ApiKey apiKey = getApiKeyForCurrentUser();
        if (apiKey == null) {
            return new StandardResponse(false, "API key not configured.");
        }
        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());

        TradeOrderType tradeType = request.getEntryPrice() != null ? TradeOrderType.LIMIT : TradeOrderType.MARKET;
        boolean areAnyTPs = request.getTakeProfitLevels() != null && !request.getTakeProfitLevels().isEmpty();

        List<CalculatedTakeProfit> calculatedTakeProfitList = new ArrayList<>();

        Optional<BigDecimal> qtyStepOpt = fetchQtyStep(request.getTicker());
        if (qtyStepOpt.isEmpty()) {
            return new StandardResponse(false, "Failed to fetch Qty Step. Probably invalid ticker");
        }
        BigDecimal qtyStep = qtyStepOpt.get();

        BigDecimal finalOrderSize = adjustSizeToQtyStep(new BigDecimal(request.getSize().toPlainString()), qtyStep);



        if (areAnyTPs && tradeType.equals(TradeOrderType.LIMIT)) {
            return new StandardResponse(false, "Cannot set take profit levels for limit order");

        }

        if (areAnyTPs) {
            if (!validatePercentageSum(request.getTakeProfitLevels())) {
                return new StandardResponse(false, "Take Profit levels percentage does not sum to 100%");
            }
            try {
                calculatedTakeProfitList = convertTakeProfitsToQuantities(request.getTakeProfitLevels(), finalOrderSize);
            } catch (CalculationException e) {
                return new StandardResponse(false, e.getMessage());
            }
        }


        TradeOrderRequest openPositionRequest = TradeOrderRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(request.getTicker())
                .side(request.isLong() ? Side.BUY : Side.SELL)
                .orderType(tradeType)
                .qty(finalOrderSize.toPlainString())
                .price(request.getEntryPrice() != null ? request.getEntryPrice().toPlainString() : null)
                .stopLoss(request.getStopLoss().toPlainString())
                .build();

        StandardResponse openPositionResponse = userBybitService.createOrder(openPositionRequest);


        boolean positionOpenedSuccessfully = openPositionResponse.isSuccess();

        if (!positionOpenedSuccessfully) {
            return openPositionResponse;
        }


        // TODO: Add check if the position was filled


        // add take profits
        if (areAnyTPs) {
            for (var tpLevel : calculatedTakeProfitList) {
//                BigDecimal tpOrderSize = adjustSizeToQtyStep(new BigDecimal(tpLevel.getSize().toPlainString()), qtyStep);
                TradeOrderRequest takeProfitRequest = TradeOrderRequest.builder()
                        .category(CategoryType.LINEAR)
                        .symbol(request.getTicker())
                        .side(request.isLong() ? Side.SELL : Side.BUY)
                        .orderType(TradeOrderType.LIMIT)
                        .qty(tpLevel.getSize().toPlainString())
                        .price(tpLevel.getPrice().toPlainString())
                        .reduceOnly(true)
                        .build();

                StandardResponse tpResponse = userBybitService.createOrder(takeProfitRequest);
                if (!tpResponse.isSuccess()) {
                    return new StandardResponse(false, "Position opened, but failed to set Take Profit: " + tpResponse.getError());
                }
            }
        }
        return new StandardResponse(true);

    }


    public StandardResponse closePositionByMarket(ClosePositionRequest request) {
        ApiKey apiKey = getApiKeyForCurrentUser();
        if (apiKey == null) {
            return new StandardResponse(false, "API key not configured.");
        }
        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());

        System.out.println(request);
        System.out.println(request.isLong() ? Side.SELL : Side.BUY);

        TradeOrderRequest closeOrderRequest = TradeOrderRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(request.getTicker())
                .side(request.isLong() ? Side.SELL : Side.BUY)
                .orderType(TradeOrderType.MARKET)
                .qty(request.getSize().toPlainString())
                .reduceOnly(true)
                .build();

        // Bybit automatically closes positions reduce-only

        return userBybitService.createOrder(closeOrderRequest);

    }

    private ApiKey getApiKeyForCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String currentUsername = authentication.getName();
        return userSettingsService.getApiKey(currentUsername);
    }

    private boolean validatePercentageSum(List<TakeProfitLevel> levels) {
        BigDecimal totalPercentage = levels.stream()
                .map(TakeProfitLevel::getPercentage)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        return totalPercentage.compareTo(BigDecimal.valueOf(100)) == 0;
    }

    private List<CalculatedTakeProfit> convertTakeProfitsToQuantities(List<TakeProfitLevel> levels, BigDecimal totalPositionSize) throws CalculationException {
        BigDecimal remainingQtyToAllocate = totalPositionSize;
        List<CalculatedTakeProfit> resultTakeProfits = new ArrayList<>();
        for (int i = 0; i < levels.size() - 1; i++) {

            TakeProfitLevel tpLvl = levels.get(i);
            BigDecimal qty = tpLvl.getPercentage().multiply(totalPositionSize).divide(BigDecimal.valueOf(100));
            qty = qty.setScale(3, RoundingMode.DOWN);

            CalculatedTakeProfit calculatedTakeProfit = new CalculatedTakeProfit(tpLvl.getPrice(), tpLvl.getPercentage(), qty);
            if (qty.compareTo(BigDecimal.ZERO) == 0) {
                throw new CalculationException(
                        "Percentage for TP at price " + tpLvl.getPrice() + " is too small for the total position size, resulting in zero quantity."
                );
            }
            resultTakeProfits.add(calculatedTakeProfit);
            remainingQtyToAllocate = remainingQtyToAllocate.subtract(qty);


        }
        if (remainingQtyToAllocate.compareTo(BigDecimal.ZERO) <= 0) {
            throw new CalculationException("The remaining quantity for the last TP is zero or negative. Check percentages.");
        }

        TakeProfitLevel lastTpLvl = levels.getLast();
        CalculatedTakeProfit lastCalculatedTakeProfit = new CalculatedTakeProfit(lastTpLvl.getPrice(), lastTpLvl.getPercentage(), remainingQtyToAllocate);

        resultTakeProfits.add(lastCalculatedTakeProfit);
        System.out.println(resultTakeProfits);

        return resultTakeProfits;
    }

    private BigDecimal adjustSizeToQtyStep(BigDecimal size, BigDecimal qtyStep) {

        return size.divide(qtyStep, 0, RoundingMode.DOWN).multiply(qtyStep);
    }

    private Optional<BigDecimal> fetchQtyStep(String ticker) {
        Optional<InstrumentEntryDTO> tickerInfoOpt = publicBybitService.getPerpetualTickerBySymbol(ticker);
        if (tickerInfoOpt.isPresent()) {
            return Optional.of(new BigDecimal(tickerInfoOpt.get().getInstrumentEntry().getLotSizeFilter().getQtyStep()));
        } else {
            return Optional.empty();
        }

    }
}


