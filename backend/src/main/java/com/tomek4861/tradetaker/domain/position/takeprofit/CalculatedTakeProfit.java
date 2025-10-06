package com.tomek4861.tradetaker.domain.position.takeprofit;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
public class CalculatedTakeProfit {

    private BigDecimal price;
    private BigDecimal percentage;
    private BigDecimal size;
}
