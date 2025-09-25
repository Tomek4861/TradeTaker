package com.tomek4861.cryptopositionmanager.controllers;

import com.tomek4861.cryptopositionmanager.dto.leverage.ChangeLeverageRequest;
import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.close.ClosePositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.current.CurrentOpenPositionsResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.current.CurrentPendingOrdersResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.open.OpenPositionWithTPRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionResponse;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.service.PositionCalculatorService;
import com.tomek4861.cryptopositionmanager.service.TradingOrchestrationService;
import com.tomek4861.cryptopositionmanager.service.UserBybitService;
import com.tomek4861.cryptopositionmanager.service.UserBybitServiceFactory;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionCalculatorService positionCalculatorService;
    private final TradingOrchestrationService tradingOrchestrationService;
    private final UserBybitServiceFactory userBybitServiceFactory;

    @PostMapping("/preview")
    public ResponseEntity<PreviewPositionResponse> previewPosition(
            @Valid @RequestBody PreviewPositionRequest request,
            @AuthenticationPrincipal User user) {

        System.out.println("Received preview position request: " + request);

        ApiKey apiKey = user.getApiKey();
        BigDecimal riskPercentage = user.getRiskPercent();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            return ResponseEntity.badRequest().body(new PreviewPositionResponse("API key not set"));
        }

        PreviewPositionResponse response = positionCalculatorService.calculatePositionInfo(
                request,
                apiKey.getKey(),
                apiKey.getSecret(),
                riskPercentage
        );

        return ResponseEntity.ok(response);
    }

    @GetMapping("/open")
    public ResponseEntity<CurrentOpenPositionsResponse> getOpenPositions(@AuthenticationPrincipal User user) {
        ApiKey apiKey = user.getApiKey();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            CurrentOpenPositionsResponse errResp = new CurrentOpenPositionsResponse();
            errResp.setSuccess(false);
            errResp.setError("API key not set");
            return ResponseEntity.badRequest().body(errResp);
        }
        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        CurrentOpenPositionsResponse openPositions = userBybitService.getOpenPositions();

        return ResponseEntity.ok(openPositions);

    }

    @GetMapping("/orders")
    public ResponseEntity<CurrentPendingOrdersResponse> getOpenOrders(@AuthenticationPrincipal User user) {
        ApiKey apiKey = user.getApiKey();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            CurrentPendingOrdersResponse errResp = new CurrentPendingOrdersResponse();
            errResp.setError("API key not set");
            errResp.setSuccess(false);
            return ResponseEntity.badRequest().body(errResp);
        }

        UserBybitService userBybitService = userBybitServiceFactory.create(apiKey.getKey(), apiKey.getSecret());
        CurrentPendingOrdersResponse openOrders = userBybitService.getOpenOrders();
        return ResponseEntity.ok(openOrders);
    }

    @PostMapping("/close")
    public ResponseEntity<StandardResponse> closePosition(@AuthenticationPrincipal User user, @Valid @RequestBody ClosePositionRequest request) {
        ApiKey apiKey = user.getApiKey();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            StandardResponse errResp = new StandardResponse(false, "API key not set");
            return ResponseEntity.badRequest().body(errResp);
        }

        var resp = tradingOrchestrationService.closePositionByMarket(request);
        if (resp.isSuccess()) {
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.badRequest().body(resp);
        }


    }

    @PostMapping("/leverage")
    public ResponseEntity<StandardResponse> changeLeverage(@AuthenticationPrincipal User user, @Valid @RequestBody ChangeLeverageRequest request) {
        ApiKey apiKey = user.getApiKey();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            StandardResponse errResp = new StandardResponse(false, "API key not set");
            return ResponseEntity.badRequest().body(errResp);
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
    public ResponseEntity<StandardResponse> openNewPosition(@AuthenticationPrincipal User user, @Valid @RequestBody OpenPositionWithTPRequest request) {
        ApiKey apiKey = user.getApiKey();

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            StandardResponse errResp = new StandardResponse(false, "API key not set");
            return ResponseEntity.badRequest().body(errResp);
        }

        var resp = tradingOrchestrationService.openPositionWithTakeProfits(request);
        if (resp.isSuccess()) {
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.badRequest().body(resp);
        }

    }


}