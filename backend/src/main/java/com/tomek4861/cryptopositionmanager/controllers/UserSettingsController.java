package com.tomek4861.cryptopositionmanager.controllers;


import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.settings.*;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.service.UserSettingsService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.security.Principal;


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
    public ResponseEntity<AllSettingsResponse> getSettings(Principal principal) {
        String username = principal.getName();
        var resp = userSettingsService.getAllSettings(username);
        return ResponseEntity.ok(resp);

    }


    @PostMapping("/apikey")
    public ResponseEntity<StandardResponse> saveApiKey(@RequestBody ApiKeyRequest apiKeyRequest, Principal principal) {
        String username = principal.getName();
        userSettingsService.saveApiKey(username, apiKeyRequest);

        var resp = new StandardResponse(true);
        return ResponseEntity.ok(resp);

    }

    @GetMapping("/apikey")
    public ResponseEntity<ApiKeyResponse> getApiKey(@AuthenticationPrincipal User user) {
        ApiKey apiKey = user.getApiKey();

        String key = apiKey != null ? apiKey.getKey() : null;
        var resp = new ApiKeyResponse(key);
        return ResponseEntity.ok(resp);
    }

    @PostMapping("/risk-percentage")
    public ResponseEntity<StandardResponse> saveRisk(Principal principal, @Valid @RequestBody RiskPercentRequest riskPercentRequest) {

        String username = principal.getName();
        userSettingsService.setRiskPercentage(username, riskPercentRequest.getRiskPercent());
        return ResponseEntity.ok(new StandardResponse(true));
    }

    @GetMapping("/risk-percentage")
    public ResponseEntity<BigDecimal> getRisk(@AuthenticationPrincipal User user) {

        BigDecimal riskPercent = user.getRiskPercent();
        if (riskPercent == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(riskPercent);
    }


}
