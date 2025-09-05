package com.tomek4861.cryptopositionmanager.service;


import com.tomek4861.cryptopositionmanager.dto.settings.ApiKeyRequest;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class UserSettingsService {

    private final UserRepository userRepository;

    @Transactional
    public void saveApiKey(String username, ApiKeyRequest apiKeyRequest) {

        // todo:  keep the key stored hashed
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        User user = userOptional.get();

        String apiKey = apiKeyRequest.getApiKey();
        String secretKey = apiKeyRequest.getSecretKey();

        ApiKey apiKeyObject = new ApiKey(apiKey, null);

        user.setApiKey(apiKeyObject);

        userRepository.save(user);

    }

    public ApiKey getApiKey(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        User user = userOptional.get();

        return user.getApiKey();

    }

    public BigDecimal getRiskPercentage(String username) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        User user = userOptional.get();
        return user.getRiskPercent();

    }

    public void setRiskPercentage(String username, BigDecimal risk) {
        Optional<User> userOptional = userRepository.findByUsername(username);
        if (userOptional.isEmpty()) {
            throw new UsernameNotFoundException("User not found with username: " + username);
        }
        User user = userOptional.get();
        user.setRiskPercent(risk);

        userRepository.save(user);

    }
}
