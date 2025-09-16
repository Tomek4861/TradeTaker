package com.tomek4861.cryptopositionmanager.dto.position.open;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OpenPositionsResponse {

    private boolean success;
    private String errorMessage;


    @JsonProperty("list")
    List<OpenPositionDTO> positionDTOList;


    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @NoArgsConstructor
    public static class OpenPositionDTO {

        private String symbol;
        private Integer leverage;
        private BigDecimal avgPrice;
        private BigDecimal liqPrice;
        @JsonProperty("unrealisedPnl")
        private BigDecimal unrealizedPnl;
        @JsonProperty("markPrice")
        private BigDecimal marketPrice;
        private Boolean isLong;
        private String tradingViewFormat;
        @JsonProperty("curRealisedPnl")
        private BigDecimal realizedPnl;
        private BigDecimal positionValue;

        @JsonProperty("side")
        public void determineSide(String sideVal) {
            this.isLong = sideVal.equalsIgnoreCase("Buy");
        }

        @JsonProperty("symbol")
        public void setSymbolAndTradingViewFormat(String symbolVal) {
            this.symbol = symbolVal;
            this.tradingViewFormat = "BYBIT" + ":" + symbolVal + ".P";

        }


    }
}

