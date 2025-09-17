package com.tomek4861.cryptopositionmanager.dto.positions.open;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class OpenOrdersResponse {
//TODO: Redo files structure


    @JsonProperty("list")
    private List<ExchangeOrderDTO> ordersList;
    private boolean success;
    private String errorMessage;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @NoArgsConstructor
    public static class ExchangeOrderDTO {
        @JsonProperty("orderId")
        private String id;
        @JsonProperty("ticker")
        private String symbol;
        private String orderType;
        private BigDecimal price;

        private Date createdDate;

        @JsonProperty("isLong")
        private boolean isLong;
        private boolean reduceOnly;
        private String tradingViewFormat;


        @JsonProperty("side")
        public void determineSide(String sideVal) {
            this.isLong = sideVal.equalsIgnoreCase("Buy");
        }

        @JsonProperty("createdTime")
        public void setCreatedDateByTimestamp(String ts) {
            Date date = new Date(Long.parseLong(ts));
            this.createdDate = date;

        }

        @JsonProperty("symbol")
        public void setSymbolAndTradingViewFormat(String symbolVal) {
            this.symbol = symbolVal;
            this.tradingViewFormat = "BYBIT" + ":" + symbolVal + ".P";
        }
    }

}
