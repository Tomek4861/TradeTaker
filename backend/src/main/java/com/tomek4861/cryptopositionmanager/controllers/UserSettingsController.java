package com.tomek4861.cryptopositionmanager.controllers;


import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.settings.ApiKeyRequest;
import com.tomek4861.cryptopositionmanager.dto.settings.ApiKeyResponse;
import com.tomek4861.cryptopositionmanager.dto.settings.RiskPercentRequest;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;


@RequiredArgsConstructor
@RestController()
@RequestMapping("/settings")
public class UserSettingsController {

    private final UserSettingsService userSettingsService;


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
    public ResponseEntity<StandardResponse> saveRisk(Authentication authentication, @RequestBody RiskPercentRequest riskPercentRequest) {

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
