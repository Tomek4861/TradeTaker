package com.tomek4861.tradetaker.service;


import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.tomek4861.tradetaker.domain.position.takeprofit.CalculatedTakeProfit;
import com.tomek4861.tradetaker.dto.exchange.InstrumentEntryDTO;
import com.tomek4861.tradetaker.dto.leverage.ChangeLeverageRequest;
import com.tomek4861.tradetaker.dto.other.StandardResponse;
import com.tomek4861.tradetaker.dto.positions.cancel.CancelPendingOrderRequest;
import com.tomek4861.tradetaker.dto.positions.close.ClosePositionRequest;
import com.tomek4861.tradetaker.dto.positions.close.PositionCloseDTO;
import com.tomek4861.tradetaker.dto.positions.current.CurrentOpenOrdersResponse;
import com.tomek4861.tradetaker.dto.positions.current.CurrentOpenPositionsResponse;
import com.tomek4861.tradetaker.dto.positions.open.OpenPositionWithTPRequest;
import com.tomek4861.tradetaker.dto.positions.takeprofit.TakeProfitLevel;
import com.tomek4861.tradetaker.entity.ApiKey;
import com.tomek4861.tradetaker.entity.ClosedPosition;
import com.tomek4861.tradetaker.entity.User;
import com.tomek4861.tradetaker.exception.CalculationException;
import com.tomek4861.tradetaker.exception.NoApiKeyException;
import com.tomek4861.tradetaker.repository.ClosedPositionRepository;
import lombok.RequiredArgsConstructor;
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
    private final ClosedPositionRepository closedPositionRepository;


    public StandardResponse<Void> openPositionWithTakeProfits(OpenPositionWithTPRequest request, User user) {
        System.out.println(request);

        System.out.println(request.getStopLoss().toPlainString());

        ApiKey apiKey = getApiKeyForUser(user);

        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());

        TradeOrderType tradeType = request.getEntryPrice() != null ? TradeOrderType.LIMIT : TradeOrderType.MARKET;
        boolean areAnyTPs = request.getTakeProfitLevels() != null && !request.getTakeProfitLevels().isEmpty();

        List<CalculatedTakeProfit> calculatedTakeProfitList = new ArrayList<>();

        Optional<BigDecimal> qtyStepOpt = fetchQtyStep(request.getTicker());
        if (qtyStepOpt.isEmpty()) {
            return StandardResponse.error("Failed to fetch Qty Step. Probably invalid ticker");
        }
        BigDecimal qtyStep = qtyStepOpt.get();

        BigDecimal finalOrderSize = adjustSizeToQtyStep(new BigDecimal(request.getSize().toPlainString()), qtyStep);


        if (areAnyTPs && tradeType.equals(TradeOrderType.LIMIT)) {
            return StandardResponse.error("Cannot set take profit levels for limit order");
        }

        if (areAnyTPs) {
            if (!validatePercentageSum(request.getTakeProfitLevels())) {
                return StandardResponse.error("Take Profit levels percentage does not sum to 100%");
            }
            try {
                calculatedTakeProfitList = convertTakeProfitsToQuantities(request.getTakeProfitLevels(), finalOrderSize);
            } catch (CalculationException e) {
                return StandardResponse.error(e.getMessage());
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

        StandardResponse<Void> openPositionResponse = userBybitService.createOrder(openPositionRequest);


        boolean positionOpenedSuccessfully = openPositionResponse.isSuccess();

        if (!positionOpenedSuccessfully) {
            return openPositionResponse;
        }


        // add take profits
        if (areAnyTPs) {
            for (var tpLevel : calculatedTakeProfitList) {
                TradeOrderRequest takeProfitRequest = TradeOrderRequest.builder()
                        .category(CategoryType.LINEAR)
                        .symbol(request.getTicker())
                        .side(request.isLong() ? Side.SELL : Side.BUY)
                        .orderType(TradeOrderType.LIMIT)
                        .qty(tpLevel.getSize().toPlainString())
                        .price(tpLevel.getPrice().toPlainString())
                        .reduceOnly(true)
                        .build();

                StandardResponse<Void> tpResponse = userBybitService.createOrder(takeProfitRequest);
                if (!tpResponse.isSuccess()) {
                    return StandardResponse.error("Position opened, but failed to set Take Profit: " + tpResponse.getError());
                }
            }
        }
        return StandardResponse.success();

    }


    public StandardResponse<Void> closePositionByMarket(ClosePositionRequest request, User user) {
        ApiKey apiKey = getApiKeyForUser(user);

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

        StandardResponse<Void> orderResponse = userBybitService.createOrder(closeOrderRequest);
        if (!orderResponse.isSuccess()) {
            return orderResponse;
        }
        // now we get final position data
        final int fetchAttempts = 5;
        final int fetchDelayMs = 300;
        for (int i = 0; i < fetchAttempts; i++) {
            try {
                // wait for bybit to process positions
                Thread.sleep(fetchDelayMs); // bad practise ik. :(
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
                return StandardResponse.error("Operation interrupted");
            }
            Optional<PositionCloseDTO.ClosedPnlEntry> positionCloseDataOpt = userBybitService.getLatestPositionDataForTicker(request.getTicker());
            if (positionCloseDataOpt.isEmpty()) {
                continue;
            }
            PositionCloseDTO.ClosedPnlEntry positionCloseData = positionCloseDataOpt.get();

            ClosedPosition closedPosition = ClosedPosition.builder()
                    .avgEntryPrice(positionCloseData.getAvgEntryPrice())
                    .avgClosePrice(positionCloseData.getAvgExitPrice())
                    .volume(positionCloseData.getQty())
                    .realizedPnl(positionCloseData.getClosedPnl())
                    .filledAt(positionCloseData.getCreatedTime())
                    .closedAt(positionCloseData.getUpdatedTime())
                    .side(positionCloseData.getSide().equals("Buy") ? ClosedPosition.PositionSide.LONG : ClosedPosition.PositionSide.SHORT)
                    .user(user)
                    .build();

            closedPositionRepository.save(closedPosition);

            return StandardResponse.success();
        }

        return StandardResponse.error("Position was closed, but failed to fetch and save closing data in time");

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

    public CurrentOpenPositionsResponse getOpenPositionForUser(User user) {
        ApiKey apiKey = getApiKeyForUser(user);
        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        return userBybitService.getOpenPositions();

    }

    public CurrentOpenOrdersResponse getOpenOrdersForUser(User user) {
        ApiKey apiKey = getApiKeyForUser(user);

        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        return userBybitService.getOpenOrders();

    }

    public StandardResponse<Void> cancelOrderForUser(User user, CancelPendingOrderRequest request) {
        ApiKey apiKey = getApiKeyForUser(user);

        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        return userBybitService.cancelPendingOrder(request);

    }

    public StandardResponse<Void> changeLeverageForTicker(User user, ChangeLeverageRequest request) {
        ApiKey apiKey = getApiKeyForUser(user);

        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        return userBybitService.changeLeverageForTicker(request);

    }

    public Optional<BigDecimal> getAccountBalance(User user) {
        ApiKey apiKey = getApiKeyForUser(user);

        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        return userBybitService.getAccountBalance();

    }

    private ApiKey getApiKeyForUser(User user) {
        ApiKey apiKey = user.getApiKey();
        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            throw new NoApiKeyException(user);
        }
        return apiKey;

    }
}


