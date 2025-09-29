package com.tomek4861.cryptopositionmanager.dto.stats;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.sql.Date;
import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class PnlByDayDTO {
    private LocalDate date;
    private BigDecimal pnl;

    public PnlByDayDTO(Date sqlDate, BigDecimal pnl) {
        this.pnl = pnl;
        this.date = sqlDate.toLocalDate();

    }
}
