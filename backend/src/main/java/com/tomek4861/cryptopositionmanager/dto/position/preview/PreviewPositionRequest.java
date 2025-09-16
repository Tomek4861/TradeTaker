package com.tomek4861.cryptopositionmanager.dto.position.preview;


import lombok.Data;

import java.math.BigDecimal;
import java.util.List;

@Data
public class PreviewPositionRequest {

    private Boolean isLong;
    private String ticker;
    private BigDecimal entryPrice;
    private BigDecimal stopLossPrice;
    private List<BigDecimal> takeProfitLevels;


}
