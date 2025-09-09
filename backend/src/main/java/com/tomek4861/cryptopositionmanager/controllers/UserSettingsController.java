package com.tomek4861.cryptopositionmanager.controllers;


import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.settings.*;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RequiredArgsConstructor
@RestController
@RequestMapping("/settings")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;


    @PutMapping
    public ResponseEntity<StandardResponse> saveSettings(@RequestBody @Valid AllSettingsRequest settingsRequest, Authentication authentication) {
        String username = authentication.getName();
        userSettingsService.saveAllSettings(username, settingsRequest);
        var resp = new StandardResponse(true);
        return ResponseEntity.ok(resp);

    }

    @GetMapping
    public ResponseEntity<AllSettingsResponse> getSettings(Authentication authentication) {
        String username = authentication.getName();
        var resp = userSettingsService.getAllSettings(username);
        return ResponseEntity.ok(resp);

    }


    @PostMapping("/apikey")
    public ResponseEntity<StandardResponse> saveApiKey(@RequestBody ApiKeyRequest apiKeyRequest, Authentication authentication) {
        String username = authentication.getName();
        userSettingsService.saveApiKey(username, apiKeyRequest);

        var resp = new StandardResponse(true);
        return ResponseEntity.ok(resp);

    }

    @GetMapping("/apikey")
    public ResponseEntity<ApiKeyResponse> getApiKey(Authentication authentication) {
        String username = authentication.getName();
        ApiKey apiKey = userSettingsService.getApiKey(username);

        var resp = new ApiKeyResponse(apiKey.getKey());
        return ResponseEntity.ok(resp);

    }

    @PostMapping("/risk-percentage")
    public ResponseEntity<StandardResponse> saveRisk(Authentication authentication, @Valid @RequestBody RiskPercentRequest riskPercentRequest) {

        String username = authentication.getName();
        userSettingsService.setRiskPercentage(username, riskPercentRequest.getRiskPercent());
        return ResponseEntity.ok(new StandardResponse(true));
    }

    @GetMapping("/risk-percentage")
    public ResponseEntity<BigDecimal> getRisk(Authentication authentication) {

        String username = authentication.getName();
        return ResponseEntity.ok(userSettingsService.getRiskPercentage(username));
    }


}
