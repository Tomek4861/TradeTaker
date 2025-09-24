package com.tomek4861.cryptopositionmanager.controllers;


import com.bybit.api.client.domain.CategoryType;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.service.PublicBybitService;
import com.tomek4861.cryptopositionmanager.service.UserBybitService;
import com.tomek4861.cryptopositionmanager.service.UserSettingsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;
import java.util.Optional;

@RestController
@RequestMapping("/bybit")
@RequiredArgsConstructor
public class BybitController {

    private final PublicBybitService publicBybitService;
    private final UserSettingsService userSettingsService;


    @GetMapping("/tickers")
    public ResponseEntity<Object> getAllTickers(@RequestParam String category) {
        CategoryType categoryType = CategoryType.valueOf(category.toUpperCase());
        Object tickers = publicBybitService.getAllTickers(categoryType);
        return ResponseEntity.ok(tickers);


    }

    @GetMapping("/perpetual-tickers")
    public ResponseEntity<Object> getPerpTickers() {
        Object tickers = publicBybitService.getAllPerpetualTickers();
        return ResponseEntity.ok(tickers);

    }

    @GetMapping("/balance")
    public ResponseEntity<BigDecimal> getAccountBalance(Authentication authentication) {
        String username = authentication.getName();
        ApiKey apikeyObj = userSettingsService.getApiKey(username);
        UserBybitService userBybitService = new UserBybitService(apikeyObj.getKey(), apikeyObj.getSecret());
        Optional<BigDecimal> balance = userBybitService.getAccountBalance();
        if (balance.isPresent()) {
            return ResponseEntity.ok(balance.get());
        } else {
            return ResponseEntity.internalServerError().body(new BigDecimal("-1"));
        }
    }

    @GetMapping("/ticker-price")
    public ResponseEntity<BigDecimal> getPriceForTicker(@RequestParam String ticker) {
        Optional<BigDecimal> priceOptional = publicBybitService.getTickerPrice(ticker);

        if (priceOptional.isPresent()) {
            return ResponseEntity.ok(priceOptional.get());
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
