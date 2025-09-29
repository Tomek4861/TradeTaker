package com.tomek4861.cryptopositionmanager.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@Builder
public class ClosedPositionDTO {
    private Integer id;
    private String side;
    private BigDecimal volume;
    private BigDecimal avgEntryPrice;
    private BigDecimal avgClosePrice;
    private LocalDateTime filledAt;
    private LocalDateTime closedAt;
    private BigDecimal realizedPnl;
    private BigDecimal paidCommission;

}
