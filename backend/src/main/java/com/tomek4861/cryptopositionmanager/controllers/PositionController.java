package com.tomek4861.cryptopositionmanager.controllers;

import com.tomek4861.cryptopositionmanager.dto.leverage.ChangeLeverageRequest;
import com.tomek4861.cryptopositionmanager.dto.other.StandardResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.cancel.CancelPendingOrderRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.close.ClosePositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.current.CurrentOpenOrdersResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.current.CurrentOpenPositionsResponse;
import com.tomek4861.cryptopositionmanager.dto.positions.open.OpenPositionWithTPRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionRequest;
import com.tomek4861.cryptopositionmanager.dto.positions.preview.PreviewPositionResponse;
import com.tomek4861.cryptopositionmanager.dto.stats.ClosedPositionDTO;
import com.tomek4861.cryptopositionmanager.entity.User;
import com.tomek4861.cryptopositionmanager.service.PositionCalculatorService;
import com.tomek4861.cryptopositionmanager.service.StatsService;
import com.tomek4861.cryptopositionmanager.service.TradingOrchestrationService;
import com.tomek4861.cryptopositionmanager.service.UserBybitServiceFactory;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/positions")
@RequiredArgsConstructor
public class PositionController {

    private final PositionCalculatorService positionCalculatorService;
    private final TradingOrchestrationService tradingOrchestrationService;
    private final UserBybitServiceFactory userBybitServiceFactory;
    private final StatsService statsService;

    @PostMapping("/preview")
    public ResponseEntity<StandardResponse<PreviewPositionResponse>> previewPosition(
            @Valid @RequestBody PreviewPositionRequest request,
            @AuthenticationPrincipal User user) {

        System.out.println("Received preview position request: " + request);

        PreviewPositionResponse response = positionCalculatorService.calculatePositionInfo(
                request,
                user
        );

        return ResponseEntity.ok(StandardResponse.success(response));
    }

    @GetMapping("/open")
    public ResponseEntity<StandardResponse<CurrentOpenPositionsResponse>> getOpenPositions(@AuthenticationPrincipal User user) {
        CurrentOpenPositionsResponse openPositions = tradingOrchestrationService.getOpenPositionForUser(user);
        return ResponseEntity.ok(StandardResponse.success(openPositions));

    }

    @GetMapping("/orders")
    public ResponseEntity<StandardResponse<CurrentOpenOrdersResponse>> getOpenOrders(@AuthenticationPrincipal User user) {
        CurrentOpenOrdersResponse openOrders = tradingOrchestrationService.getOpenOrdersForUser(user);
        return ResponseEntity.ok(StandardResponse.success(openOrders));
    }

    @PostMapping("/close")
    public ResponseEntity<StandardResponse<Void>> closePosition(@AuthenticationPrincipal User user, @Valid @RequestBody ClosePositionRequest request) {

        var resp = tradingOrchestrationService.closePositionByMarket(request, user);
        if (resp.isSuccess()) {
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.badRequest().body(resp);
        }


    }

    @PostMapping("/cancel-order")
    public ResponseEntity<StandardResponse<Void>> cancelOrder(@AuthenticationPrincipal User user, @Valid @RequestBody CancelPendingOrderRequest request) {
        var resp = tradingOrchestrationService.cancelOrderForUser(user, request);
        if (resp.isSuccess()) {
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.badRequest().body(resp);
        }


    }

    @PostMapping("/leverage")
    public ResponseEntity<StandardResponse<Void>> changeLeverage(@AuthenticationPrincipal User user, @Valid @RequestBody ChangeLeverageRequest request) {
        var resp = tradingOrchestrationService.changeLeverageForTicker(user, request);
        if (resp.isSuccess()) {
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.badRequest().body(resp);
        }


    }

    @PostMapping("/open")
    public ResponseEntity<StandardResponse<Void>> openNewPosition(@AuthenticationPrincipal User user, @Valid @RequestBody OpenPositionWithTPRequest request) {

        var resp = tradingOrchestrationService.openPositionWithTakeProfits(request, user);
        if (resp.isSuccess()) {
            return ResponseEntity.ok(resp);
        } else {
            return ResponseEntity.badRequest().body(resp);
        }

    }

    @GetMapping("/history")
    public ResponseEntity<StandardResponse<List<ClosedPositionDTO>>> getClosedPositionsByDayAndMonth(
            @AuthenticationPrincipal User user,
            @RequestParam @Min(2020) @Max(2100) int year,
            @RequestParam @Min(1) @Max(12) int month
    ) {

        var closedPositionDTOList = statsService.getClosedPositionsForMonthAndYear(user, year, month);
        return ResponseEntity.ok(StandardResponse.success(closedPositionDTOList));

    }


}