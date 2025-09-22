package com.tomek4861.cryptopositionmanager.service;


import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.TradeOrderType;
import com.bybit.api.client.domain.account.AccountType;
import com.bybit.api.client.domain.account.request.AccountDataRequest;
import com.bybit.api.client.domain.position.request.PositionDataRequest;
import com.bybit.api.client.domain.trade.Side;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.restApi.BybitApiAccountRestClient;
import com.bybit.api.client.restApi.BybitApiPositionRestClient;
import com.bybit.api.client.restApi.BybitApiTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomek4861.cryptopositionmanager.dto.exchange.WalletBalanceDTO;
import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.close.ClosePositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.open.OpenOrdersResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.open.OpenPositionsResponse;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;


public class UserBybitService {

    private final BybitApiClientFactory clientFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public UserBybitService(String apiKey, String secretKey) {
        this.clientFactory = BybitApiClientFactory.newInstance(apiKey, secretKey);

    }

    public Optional<BigDecimal> getAccountBalance() {
        BybitApiAccountRestClient accountClient = this.clientFactory.newAccountRestClient();

        AccountDataRequest request = AccountDataRequest.builder()
                .accountType(AccountType.UNIFIED)
                .build();

        Object rawResp = accountClient.getWalletBalance(request);
        TypeReference<GenericResponse<WalletBalanceDTO.Result>> typeRef = new TypeReference<>() {
        };
        GenericResponse<WalletBalanceDTO.Result> response = objectMapper.convertValue(rawResp, typeRef);
        if (response != null && response.getResult() != null && response.getResult().getList() != null && !response.getResult().getList().isEmpty()) {
            String totalEquityStr = response.getResult().getList().getFirst().getTotalEquity();
            if (totalEquityStr != null && !totalEquityStr.isBlank()) {
                return Optional.of(new BigDecimal(totalEquityStr));
            }
        }

        return Optional.empty();
    }

    public OpenPositionsResponse getOpenPositions() {
        BybitApiPositionRestClient positionClient = this.clientFactory.newPositionRestClient();
        PositionDataRequest request = PositionDataRequest.builder()
                .category(CategoryType.LINEAR)
                .settleCoin("USDT")
                .build();

        Object positionInfoRawResp = positionClient.getPositionInfo(request);
        System.out.println(positionInfoRawResp);
        System.out.println("positionInfoRawResp");
        TypeReference<GenericResponse<OpenPositionsResponse>> typeRef = new TypeReference<>() {
        };
        GenericResponse<OpenPositionsResponse> response = objectMapper.convertValue(positionInfoRawResp, typeRef);
        OpenPositionsResponse result = response.getResult();
        result.setSuccess(true);
        result.getPositionDTOList().forEach(
                position -> position.setId(generatePositionID(position)
                )

        );
        return result;

    }

    public OpenOrdersResponse getOpenOrders() {
        BybitApiTradeRestClient tradeClient = this.clientFactory.newTradeRestClient();
        TradeOrderRequest request = TradeOrderRequest.builder()
                .category(CategoryType.LINEAR)
                .settleCoin("USDT")
                .build();
        Object openOrdersRawResp = tradeClient.getOpenOrders(request);
        System.out.println(openOrdersRawResp);
        System.out.println("openOrdersRawResp");

        TypeReference<GenericResponse<OpenOrdersResponse>> typeRef = new TypeReference<>() {
        };

        GenericResponse<OpenOrdersResponse> response = objectMapper.convertValue(openOrdersRawResp, typeRef);

        OpenOrdersResponse result = response.getResult();
        result.setSuccess(true);


        return result;
    }

    private String generatePositionID(OpenPositionsResponse.OpenPositionDTO position) {
        if (position.getTicker() == null || position.getPositionIdx() == null) {
            return null;
        }
        String sideStr = position.getIsLong() ? "B" : "S";
        String key = "BYBIT|" + position.getTicker() + "|" + position.getPositionIdx() + "|" + sideStr;
        UUID uuid = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
        return uuid.toString();
    }

    public StandardResponse closePositionByMarket(ClosePositionRequest request) {
        BybitApiTradeRestClient client = this.clientFactory.newTradeRestClient();
        System.out.println(request);
        System.out.println(request.isLong() ? Side.SELL : Side.BUY);

        TradeOrderRequest closeOrderRequest = TradeOrderRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(request.getTicker())
                .side(request.isLong() ? Side.SELL : Side.BUY)
                .orderType(TradeOrderType.MARKET)
                .qty(request.getSize().toString())
                .reduceOnly(true)
                .build();
        Object rawApiResponse = client.createOrder(closeOrderRequest);
        System.out.println(rawApiResponse);
        System.out.println("RAWAPIRESP");
        TypeReference<GenericResponse<Object>> typeRef = new TypeReference<>() {
        };

        GenericResponse<Object> apiResponse = objectMapper.convertValue(rawApiResponse, typeRef);
        System.out.println(apiResponse);
        StandardResponse standardResponse;
        if (apiResponse.getRetMsg().equals("OK")) {
            standardResponse = new StandardResponse(true);
        } else {
            standardResponse = new StandardResponse(false, apiResponse.getRetMsg());
        }

        return standardResponse;

    }

}
