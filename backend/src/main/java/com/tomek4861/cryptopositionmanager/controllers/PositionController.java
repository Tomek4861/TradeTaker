package com.tomek4861.cryptopositionmanager.controllers;

import com.tomek4861.cryptopositionmanager.dto.leverage.ChangeLeverageRequest;
import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.cancel.CancelPendingOrderRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.close.ClosePositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.current.CurrentOpenOrdersResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.current.CurrentOpenPositionsResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.open.OpenPositionWithTPRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionResponse;
import com.tomek4861.cryptopositionmanager.dto.stats.ClosedPositionDTO;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.service.*;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionCalculatorService positionCalculatorService;
    private final TradingOrchestrationService tradingOrchestrationService;
    private final UserBybitServiceFactory userBybitServiceFactory;
    private final StatsService statsService;

    @PostMapping("/preview")
    public ResponseEntity<StandardResponse<PreviewPositionResponse>> previewPosition(
            @Valid @RequestBody PreviewPositionRequest request,
            @AuthenticationPrincipal User user) {

        System.out.println("Received preview position request: " + request);

        ApiKey apiKey = user.getApiKey();
        BigDecimal riskPercentage = user.getRiskPercent();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Api key not set"));
        }

        PreviewPositionResponse response = positionCalculatorService.calculatePositionInfo(
                request,
                apiKey.getKey(),
                apiKey.getSecret(),
                riskPercentage
        );

        return ResponseEntity.ok(StandardResponse.success(response));
    }

    @GetMapping("/open")
    public ResponseEntity<StandardResponse<CurrentOpenPositionsResponse>> getOpenPositions(@AuthenticationPrincipal User user) {
        ApiKey apiKey = user.getApiKey();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("Api key not set"));
        }
        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        CurrentOpenPositionsResponse openPositions = userBybitService.getOpenPositions();

        return ResponseEntity.ok(StandardResponse.success(openPositions));

    }

    @GetMapping("/orders")
    public ResponseEntity<StandardResponse<CurrentOpenOrdersResponse>> getOpenOrders(@AuthenticationPrincipal User user) {
        ApiKey apiKey = user.getApiKey();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {

            return ResponseEntity.badRequest().body(StandardResponse.error("Api key not set"));
        }

        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        CurrentOpenOrdersResponse openOrders = userBybitService.getOpenOrders();
        return ResponseEntity.ok(StandardResponse.success(openOrders));
    }

    @PostMapping("/close")
    public ResponseEntity<StandardResponse<Void>> closePosition(@AuthenticationPrincipal User user, @Valid @RequestBody ClosePositionRequest request) {

        var resp = tradingOrchestrationService.closePositionByMarket(request, user);
        if (resp.isSuccess()) {
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.badRequest().body(resp);
        }


    }

    @PostMapping("/cancel-order")
    public ResponseEntity<StandardResponse<Void>> cancelOrder(@AuthenticationPrincipal User user, @Valid @RequestBody CancelPendingOrderRequest request) {
        ApiKey apiKey = user.getApiKey();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            StandardResponse<Void> errResp = StandardResponse.error("API key not set");
            return ResponseEntity.badRequest().body(errResp);
        }

        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        var resp = userBybitService.cancelPendingOrder(request);
        if (resp.isSuccess()) {
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.badRequest().body(resp);
        }


    }

    @PostMapping("/leverage")
    public ResponseEntity<StandardResponse<Void>> changeLeverage(@AuthenticationPrincipal User user, @Valid @RequestBody ChangeLeverageRequest request) {
        ApiKey apiKey = user.getApiKey();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("API key not set"));
        }
        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        var resp = userBybitService.changeLeverageForTicker(request);
        if (resp.isSuccess()) {
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.badRequest().body(resp);
        }


    }

    @PostMapping("/open")
    public ResponseEntity<StandardResponse<Void>> openNewPosition(@AuthenticationPrincipal User user, @Valid @RequestBody OpenPositionWithTPRequest request) {
        ApiKey apiKey = user.getApiKey();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            return ResponseEntity.badRequest().body(StandardResponse.error("API key not set"));
        }

        var resp = tradingOrchestrationService.openPositionWithTakeProfits(request);
        if (resp.isSuccess()) {
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.badRequest().body(resp);
        }

    }

    @GetMapping("/history")
    public ResponseEntity<StandardResponse<List<ClosedPositionDTO>>> getClosedPositionsByDayAndMonth(
            @AuthenticationPrincipal User user,
            @RequestParam @Min(2020) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {

        var closedPositionDTOList = statsService.getClosedPositionsForMonthAndYear(user, year, month);
        return ResponseEntity.ok(StandardResponse.success(closedPositionDTOList));

    }


}