package com.tomek4861.tradetaker.dto.exchange;

import com.bybit.api.client.domain.market.response.instrumentInfo.InstrumentEntry;
import lombok.Getter;


@Getter
public class InstrumentEntryDTO {

    private final InstrumentEntry instrumentEntry;
    private final String tradingViewFormat;

    public InstrumentEntryDTO(InstrumentEntry instrumentEntry, String exchangeName) {


        this.instrumentEntry = instrumentEntry;
        this.tradingViewFormat = exchangeName.toUpperCase() + ":" + instrumentEntry.getSymbol() + ".P";
    }
}
