package com.tomek4861.cryptopositionmanager.service;


import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.account.AccountType;
import com.bybit.api.client.domain.account.request.AccountDataRequest;
import com.bybit.api.client.domain.position.request.PositionDataRequest;
import com.bybit.api.client.domain.trade.request.TradeOrderRequest;
import com.bybit.api.client.restApi.BybitApiAccountRestClient;
import com.bybit.api.client.restApi.BybitApiPositionRestClient;
import com.bybit.api.client.restApi.BybitApiTradeRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomek4861.cryptopositionmanager.dto.exchange.WalletBalanceDTO;
import com.tomek4861.cryptopositionmanager.dto.leverage.ChangeLeverageRequest;
import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.cancel.CancelPendingOrderRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.close.PositionCloseDTO;
import com.tomek4861.cryptopositionmanager.dto.positions.current.CurrentOpenOrdersResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.current.CurrentOpenPositionsResponse;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.UUID;


public class UserBybitService {

    private final BybitApiClientFactory clientFactory;
    private final ObjectMapper objectMapper;

    public UserBybitService(String apiKey, String secretKey, ObjectMapper objectMapper) {
        this.clientFactory = BybitApiClientFactory.newInstance(apiKey, secretKey);
        this.objectMapper = objectMapper;
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

    public CurrentOpenPositionsResponse getOpenPositions() {
        BybitApiPositionRestClient positionClient = this.clientFactory.newPositionRestClient();
        PositionDataRequest request = PositionDataRequest.builder()
                .category(CategoryType.LINEAR)
                .settleCoin("USDT")
                .build();

        Object positionInfoRawResp = positionClient.getPositionInfo(request);
        TypeReference<GenericResponse<CurrentOpenPositionsResponse>> typeRef = new TypeReference<>() {
        };
        GenericResponse<CurrentOpenPositionsResponse> response = objectMapper.convertValue(positionInfoRawResp, typeRef);
        CurrentOpenPositionsResponse result = response.getResult();
        result.getPositionDTOList().forEach(
                position -> position.setId(generatePositionID(position)
                )

        );
        return result;

    }

    public CurrentOpenOrdersResponse getOpenOrders() {
        BybitApiTradeRestClient tradeClient = this.clientFactory.newTradeRestClient();
        TradeOrderRequest request = TradeOrderRequest.builder()
                .category(CategoryType.LINEAR)
                .settleCoin("USDT")
                .build();
        Object openOrdersRawResp = tradeClient.getOpenOrders(request);
        System.out.println(openOrdersRawResp);
        System.out.println("openOrdersRawResp");

        TypeReference<GenericResponse<CurrentOpenOrdersResponse>> typeRef = new TypeReference<>() {
        };

        GenericResponse<CurrentOpenOrdersResponse> response = objectMapper.convertValue(openOrdersRawResp, typeRef);

        return response.getResult();
    }

    private String generatePositionID(CurrentOpenPositionsResponse.OpenPositionDTO position) {
        if (position.getTicker() == null || position.getPositionIdx() == null) {
            return null;
        }
        String sideStr = position.getIsLong() ? "B" : "S";
        String key = "BYBIT|" + position.getTicker() + "|" + position.getPositionIdx() + "|" + sideStr;
        UUID uuid = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
        return uuid.toString();
    }

    public StandardResponse<Void> createOrder(TradeOrderRequest request) {
        try {


            BybitApiTradeRestClient client = this.clientFactory.newTradeRestClient();
            Object rawApiResponse = client.createOrder(request);
            System.out.println(rawApiResponse);
            TypeReference<GenericResponse<Object>> typeRef = new TypeReference<>() {
            };

            GenericResponse<Object> apiResponse = objectMapper.convertValue(rawApiResponse, typeRef);
            System.out.println(apiResponse);
            if (apiResponse.getRetCode() == 0) {
                return StandardResponse.success();
            } else {
                return StandardResponse.error(apiResponse.getRetMsg());
            }

        } catch (Exception e) {
            return StandardResponse.error("Failed to create order: " + e.getMessage());
        }
    }


    public StandardResponse<Void> changeLeverageForTicker(ChangeLeverageRequest request) {
        BybitApiPositionRestClient positionClient = this.clientFactory.newPositionRestClient();

        PositionDataRequest setLeverageRequest = PositionDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(request.getTicker())
                .buyLeverage(request.getLeverage().toString())
                .sellLeverage(request.getLeverage().toString())
                .build();

        Object rawApiResponse = positionClient.setPositionLeverage(setLeverageRequest);

        TypeReference<GenericResponse<Object>> typeRef = new TypeReference<>() {
        };
        GenericResponse<Object> apiResponse = objectMapper.convertValue(rawApiResponse, typeRef);
        if (apiResponse.getRetCode() == 0) {
            return StandardResponse.success();
        } else {
            return StandardResponse.error(apiResponse.getRetMsg());
        }

    }

    public Optional<PositionCloseDTO.ClosedPnlEntry> getLatestPositionDataForTicker(String ticker) {
        BybitApiPositionRestClient positionClient = this.clientFactory.newPositionRestClient();
        PositionDataRequest request = PositionDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(ticker)
                .limit(1)
                .build();

        Object rawResponse = positionClient.getClosePnlList(request);
        TypeReference<GenericResponse<PositionCloseDTO>> typeRef = new TypeReference<>() {
        };
        GenericResponse<PositionCloseDTO> response = objectMapper.convertValue(rawResponse, typeRef);
        if (response != null && response.getRetCode() == 0 && response.getResult() != null &&
                response.getResult().getPnlEntryList() != null && !response.getResult().getPnlEntryList().isEmpty()) {

            PositionCloseDTO.ClosedPnlEntry finalResponse = response.getResult().getPnlEntryList().getFirst();
            return Optional.of(finalResponse);
        }
        return Optional.empty();

    }

    public StandardResponse<Void> cancelPendingOrder(CancelPendingOrderRequest request) {
        try {
            BybitApiTradeRestClient client = this.clientFactory.newTradeRestClient();

            TradeOrderRequest cancelRequest = TradeOrderRequest.builder()
                    .category(CategoryType.LINEAR)
                    .symbol(request.getTicker())
                    .orderId(request.getOrderId())
                    .build();

            Object rawApiResponse = client.cancelOrder(cancelRequest);

            TypeReference<GenericResponse<Object>> typeRef = new TypeReference<>() {
            };
            GenericResponse<Object> apiResponse = objectMapper.convertValue(rawApiResponse, typeRef);

            if (apiResponse.getRetCode() == 0) {
                return StandardResponse.success();
            } else {
                return StandardResponse.error(apiResponse.getRetMsg());
            }

        } catch (Exception e) {
            return StandardResponse.error("Failed to cancel order: " + e.getMessage());
        }
    }

}
