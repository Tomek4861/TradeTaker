package com.tomek4861.cryptopositionmanager.dto.positions.preview;

import com.tomek4861.cryptopositionmanager.dto.positions.takeprofit.TakeProfitLevel;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PreviewPositionRequest {
    @NotNull
    @NotEmpty
    private String ticker;
    @NotNull
    private Boolean isLong;
    @NotNull
    @Positive
    private BigDecimal entryPrice;
    @NotNull
    @Positive
    private BigDecimal stopLoss;
    private List<TakeProfitLevel> takeProfitLevels;


}
