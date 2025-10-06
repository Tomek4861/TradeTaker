package com.tomek4861.tradetaker.controllers;


import com.tomek4861.tradetaker.dto.other.StandardResponse;
import com.tomek4861.tradetaker.dto.settings.*;
import com.tomek4861.tradetaker.entity.ApiKey;
import com.tomek4861.tradetaker.entity.User;
import com.tomek4861.tradetaker.service.UserSettingsService;
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
    public ResponseEntity<StandardResponse<Void>> saveSettings(@RequestBody @Valid AllSettingsRequest settingsRequest, Authentication authentication) {
        String username = authentication.getName();
        userSettingsService.saveAllSettings(username, settingsRequest);
        return ResponseEntity.ok(StandardResponse.success());

    }

    @GetMapping
    public ResponseEntity<StandardResponse<AllSettingsResponse>> getSettings(Principal principal) {
        String username = principal.getName();
        var resp = userSettingsService.getAllSettings(username);
        return ResponseEntity.ok(StandardResponse.success(resp));

    }


    @PostMapping("/apikey")
    public ResponseEntity<StandardResponse<Void>> saveApiKey(@RequestBody ApiKeyRequest apiKeyRequest, Principal principal) {
        String username = principal.getName();
        userSettingsService.saveApiKey(username, apiKeyRequest);

        return ResponseEntity.ok(StandardResponse.success());

    }

    @GetMapping("/apikey")
    public ResponseEntity<StandardResponse<ApiKeyResponse>> getApiKey(@AuthenticationPrincipal User user) {
        ApiKey apiKey = user.getApiKey();

        String key = apiKey != null ? apiKey.getKey() : null;
        var resp = new ApiKeyResponse(key);
        return ResponseEntity.ok(StandardResponse.success(resp));
    }

    @PostMapping("/risk-percentage")
    public ResponseEntity<StandardResponse<Void>> saveRisk(Principal principal, @Valid @RequestBody RiskPercentRequest riskPercentRequest) {

        String username = principal.getName();
        userSettingsService.setRiskPercentage(username, riskPercentRequest.getRiskPercent());
        return ResponseEntity.ok(StandardResponse.success());
    }

    @GetMapping("/risk-percentage")
    public ResponseEntity<StandardResponse<BigDecimal>> getRisk(@AuthenticationPrincipal User user) {

        BigDecimal riskPercent = user.getRiskPercent();
        if (riskPercent == null) {
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.ok(StandardResponse.success(riskPercent));
    }


}
