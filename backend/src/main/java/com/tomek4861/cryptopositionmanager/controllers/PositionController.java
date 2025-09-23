package com.tomek4861.cryptopositionmanager.controllers;

import com.tomek4861.cryptopositionmanager.dto.leverage.ChangeLeverageRequest;
import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.close.ClosePositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.current.CurrentOpenPositionsResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.current.CurrentPendingOrdersResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionResponse;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.service.PositionCalculatorService;
import com.tomek4861.cryptopositionmanager.service.UserBybitService;
import com.tomek4861.cryptopositionmanager.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionCalculatorService positionCalculatorService;
    private final UserSettingsService userSettingsService;

    @PostMapping("/preview")
    public ResponseEntity<PreviewPositionResponse> previewPosition(
            @Valid @RequestBody PreviewPositionRequest request,
            Authentication authentication) {

        System.out.println("Received preview position request: " + request);

        String username = authentication.getName();
        ApiKey apiKey = userSettingsService.getApiKey(username);
        BigDecimal riskPercentage = userSettingsService.getRiskPercentage(username);

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
    public ResponseEntity<CurrentOpenPositionsResponse> getOpenPositions(Authentication auth) {
        String username = auth.getName();
        ApiKey apiKey = userSettingsService.getApiKey(username);

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            CurrentOpenPositionsResponse errResp = new CurrentOpenPositionsResponse();
            errResp.setSuccess(false);
            errResp.setErrorMessage("API key not set");
            return ResponseEntity.badRequest().body(errResp);
        }
        UserBybitService userBybitService = new UserBybitService(apiKey.getKey(), apiKey.getSecret());
        CurrentOpenPositionsResponse openPositions = userBybitService.getOpenPositions();

        return ResponseEntity.ok(openPositions);

    }

    @GetMapping("/orders")
    public ResponseEntity<CurrentPendingOrdersResponse> getOpenOrders(Authentication auth) {
        String username = auth.getName();
        ApiKey apiKey = userSettingsService.getApiKey(username);

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            CurrentPendingOrdersResponse errResp = new CurrentPendingOrdersResponse();
            errResp.setErrorMessage("API key not set");
            errResp.setSuccess(false);
            return ResponseEntity.badRequest().body(errResp);
        }

        UserBybitService userBybitService = new UserBybitService(apiKey.getKey(), apiKey.getSecret());
        CurrentPendingOrdersResponse openOrders = userBybitService.getOpenOrders();
        return ResponseEntity.ok(openOrders);
    }

    @PostMapping("/close")
    public ResponseEntity<StandardResponse> closePosition(Authentication auth, @Valid @RequestBody ClosePositionRequest request) {
        String username = auth.getName();
        ApiKey apiKey = userSettingsService.getApiKey(username);

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            StandardResponse errResp = new StandardResponse(false, "API key not set");
            return ResponseEntity.badRequest().body(errResp);
        }

        UserBybitService userBybitService = new UserBybitService(apiKey.getKey(), apiKey.getSecret());
        var resp = userBybitService.closePositionByMarket(request);
        return ResponseEntity.ok(resp);


    }

    @PostMapping("/leverage")
    public ResponseEntity<StandardResponse> changeLeverage(Authentication auth, @Valid @RequestBody ChangeLeverageRequest request) {
        String username = auth.getName();
        ApiKey apiKey = userSettingsService.getApiKey(username);

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            StandardResponse errResp = new StandardResponse(false, "API key not set");
            return ResponseEntity.badRequest().body(errResp);
        }
        UserBybitService userBybitService = new UserBybitService(apiKey.getKey(), apiKey.getSecret());
        var resp = userBybitService.changeLeverageForTicker(request);
        return ResponseEntity.ok(resp);


    }


}