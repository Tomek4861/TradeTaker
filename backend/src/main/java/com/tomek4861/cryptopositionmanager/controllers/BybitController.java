package com.tomek4861.cryptopositionmanager.controllers;


import com.bybit.api.client.domain.CategoryType;
import com.tomek4861.cryptopositionmanager.entity.ApiKey;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.service.PublicBybitService;
import com.tomek4861.cryptopositionmanager.service.UserBybitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
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
    public ResponseEntity<BigDecimal> getAccountBalance(@AuthenticationPrincipal User user) {
        ApiKey apikeyObj = user.getApiKey();
        if (apikeyObj == null) {
            return ResponseEntity.badRequest().build();
        }
        UserBybitService userBybitService = new UserBybitService(apikeyObj.getKey(), apikeyObj.getSecret());
        Optional<BigDecimal> balance = userBybitService.getAccountBalance();
        if (balance.isPresent()) {
            return ResponseEntity.ok(balance.get());
        } else {
            return ResponseEntity.internalServerError().build();
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
