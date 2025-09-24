package com.tomek4861.cryptopositionmanager.dto.positions.preview;

import com.tomek4861.cryptopositionmanager.dto.positions.takeprofit.TakeProfitLevel;
import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PreviewPositionRequest {

    private String ticker;
    private Boolean isLong;
    private BigDecimal entryPrice;
    private BigDecimal stopLoss;
    private List<TakeProfitLevel> takeProfitLevels;


}
