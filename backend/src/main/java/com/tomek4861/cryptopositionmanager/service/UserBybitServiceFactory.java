package com.tomek4861.cryptopositionmanager.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class UserBybitServiceFactory {

    private final ObjectMapper objectMapper;


    @Cacheable(value = "bybitServices", key = "#apiKey")
    public UserBybitService create(String apiKey, String secretKey) {

        return new UserBybitService(apiKey, secretKey, this.objectMapper);
    }
}