package com.tomek4861.cryptopositionmanager.service;


import com.tomek4861.cryptopositionmanager.dto.settings.AllSettingsRequest;
import com.tomek4861.cryptopositionmanager.dto.settings.AllSettingsResponse;
import com.tomek4861.cryptopositionmanager.dto.settings.ApiKeyRequest;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserRepository userRepository;

    @Transactional
    public void saveApiKey(String username, ApiKeyRequest apiKeyRequest) {

        // todo:  keep the key stored hashed

        User user = findUserByUsername(username);

        String apiKey = apiKeyRequest.getApiKey();
        String secretKey = apiKeyRequest.getSecretKey();

        ApiKey apiKeyObject = new ApiKey(apiKey, secretKey);

        user.setApiKey(apiKeyObject);

        userRepository.save(user);

    }

    @Transactional
    public void saveAllSettings(String username, AllSettingsRequest settingsRequest) {
        User user = findUserByUsername(username);

        String requestApiKey = settingsRequest.getApiKey();
        String requestSecretKey = settingsRequest.getSecretKey();

        ApiKey apiKeyEntity = user.getApiKey();
        if (apiKeyEntity == null) {
            apiKeyEntity = new ApiKey();
            user.setApiKey(apiKeyEntity);
        }

        if (requestApiKey != null) {
            if (!requestApiKey.isBlank()) {
                apiKeyEntity.setKey(requestApiKey);
            }
        }

        if (requestSecretKey != null) {
            if (!requestSecretKey.isBlank()) {
                apiKeyEntity.setSecret(requestSecretKey);
            }
        }

        user.setApiKey(apiKeyEntity);

        BigDecimal riskPercentage = settingsRequest.getRiskPercentage();

        user.setRiskPercent(riskPercentage);
        userRepository.save(user);

    }

    public AllSettingsResponse getAllSettings(String username) {
        User user = findUserByUsername(username);

        ApiKey apiKeyEntity = user.getApiKey();
        BigDecimal riskPercent = user.getRiskPercent();

        String key = (apiKeyEntity != null) ? apiKeyEntity.getKey() : null;

        return new AllSettingsResponse(true, key, riskPercent);
    }


    public ApiKey getApiKey(String username) {
        User user = findUserByUsername(username);
        return user.getApiKey();

    }

    public BigDecimal getRiskPercentage(String username) {
        User user = findUserByUsername(username);

        return user.getRiskPercent();

    }

    public void setRiskPercentage(String username, BigDecimal risk) {

        User user = findUserByUsername(username);
        user.setRiskPercent(risk);

        userRepository.save(user);

    }

    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("User not found with username: " + username));
    }

}
