package com.tomek4861.cryptopositionmanager.controllers;

import com.tomek4861.cryptopositionmanager.dto.position.open.OpenPositionsResponse;
import com.tomek4861.cryptopositionmanager.dto.position.preview.PreviewPositionRequest;
import com.tomek4861.cryptopositionmanager.dto.position.preview.PreviewPositionResponse;
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
    public ResponseEntity<OpenPositionsResponse> getOpenPositions(Authentication auth) {
        String username = auth.getName();
        ApiKey apiKey = userSettingsService.getApiKey(username);

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            OpenPositionsResponse errResp = new OpenPositionsResponse();
            errResp.setSuccess(false);
            errResp.setErrorMessage("API key not set");
            return ResponseEntity.badRequest().body(errResp);
        }
        UserBybitService userBybitService = new UserBybitService(apiKey.getKey(), apiKey.getSecret());
        OpenPositionsResponse openPositions = userBybitService.getOpenPositions();

        return ResponseEntity.ok(openPositions);

    }

    @GetMapping("/orders")
    public ResponseEntity<Object> getOpenOrders(Authentication auth) {
        String username = auth.getName();
        ApiKey apiKey = userSettingsService.getApiKey(username);

        if (apiKey == null || apiKey.getKey() == null || apiKey.getSecret() == null) {
            return ResponseEntity.badRequest().body("API key not set");
        }

        UserBybitService userBybitService = new UserBybitService(apiKey.getKey(), apiKey.getSecret());
        Object openOrders = userBybitService.getOpenOrders();
        return ResponseEntity.ok(openOrders);
    }


}