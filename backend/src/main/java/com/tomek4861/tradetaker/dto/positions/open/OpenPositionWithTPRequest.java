package com.tomek4861.tradetaker.dto.positions.open;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.tomek4861.tradetaker.dto.positions.takeprofit.TakeProfitLevel;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
@AllArgsConstructor
public class OpenPositionWithTPRequest {
    @NotNull
    private String ticker;

    @JsonProperty("isLong")
    private boolean isLong;

    @NotNull
    @Positive
    private BigDecimal size;

    @Positive // can be null - market order
    private BigDecimal entryPrice;

    @NotNull
    @Positive
    private BigDecimal stopLoss;
    private List<TakeProfitLevel> takeProfitLevels;


}
