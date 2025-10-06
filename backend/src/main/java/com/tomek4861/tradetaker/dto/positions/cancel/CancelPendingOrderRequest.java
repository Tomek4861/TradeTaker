package com.tomek4861.tradetaker.dto.positions.cancel;

import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CancelPendingOrderRequest {
    @NotNull
    @NotEmpty
    private String ticker;
    @NotNull
    @NotEmpty
    private String orderId;
}