package com.tomek4861.cryptopositionmanager.dto.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class TickerPriceDTO {


    @JsonProperty("list")
    private List<TickerInfo> list;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class TickerInfo {
        @JsonProperty("symbol")
        private String symbol;

        @JsonProperty("lastPrice")
        private BigDecimal lastPrice;
    }
}

