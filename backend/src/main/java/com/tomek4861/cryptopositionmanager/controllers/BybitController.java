package com.tomek4861.cryptopositionmanager.controllers;


import com.bybit.api.client.domain.CategoryType;
import com.tomek4861.cryptopositionmanager.service.BybitService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/bybit")
@RequiredArgsConstructor
public class BybitController {

    private final BybitService bybitService;


    @GetMapping("/tickers")
    public ResponseEntity<Object> getAllTickers(@RequestParam String category) {
        CategoryType categoryType = CategoryType.valueOf(category.toUpperCase());
        Object tickers = bybitService.getAllTickers(categoryType);
        return ResponseEntity.ok(tickers);


    }

    @GetMapping("/perpetual-tickers")
    public ResponseEntity<Object> getPerpTickers() {
        Object tickers = bybitService.getAllPerpetualTickers();
        return ResponseEntity.ok(tickers);


    }


}
