package com.tomek4861.tradetaker.controllers;


import com.bybit.api.client.domain.CategoryType;
import com.tomek4861.tradetaker.dto.other.StandardResponse;
import com.tomek4861.tradetaker.entity.User;
import com.tomek4861.tradetaker.service.PublicBybitService;
import com.tomek4861.tradetaker.service.TradingOrchestrationService;
import com.tomek4861.tradetaker.service.UserBybitServiceFactory;
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
    private final UserBybitServiceFactory userBybitServiceFactory;
    private final TradingOrchestrationService tradingOrchestrationService;


    @GetMapping("/tickers")
    public ResponseEntity<Object> getAllTickers(@RequestParam String category) {
        CategoryType categoryType = CategoryType.valueOf(category.toUpperCase());
        Object tickers = publicBybitService.getAllTickers(categoryType);
        return ResponseEntity.ok(tickers);


    }

    @GetMapping("/perpetual-tickers")
    public ResponseEntity<StandardResponse<Object>> getPerpTickers() {
        Object tickers = publicBybitService.getAllPerpetualTickers();
        return ResponseEntity.ok(StandardResponse.success(tickers));

    }

    @GetMapping("/balance")
    public ResponseEntity<StandardResponse<BigDecimal>> getAccountBalance(@AuthenticationPrincipal User user) {
        Optional<BigDecimal> balance = tradingOrchestrationService.getAccountBalance(user);
        if (balance.isPresent()) {
            return ResponseEntity.ok(StandardResponse.success(balance.get()));
        } else {
            return ResponseEntity.internalServerError().build();
        }
    }

    @GetMapping("/ticker-price")
    public ResponseEntity<StandardResponse<BigDecimal>> getPriceForTicker(@RequestParam String ticker) {
        Optional<BigDecimal> priceOptional = publicBybitService.getTickerPrice(ticker);

        if (priceOptional.isPresent()) {
            return ResponseEntity.ok(StandardResponse.success(priceOptional.get()));
        } else {
            return ResponseEntity.notFound().build();
        }
    }

}
