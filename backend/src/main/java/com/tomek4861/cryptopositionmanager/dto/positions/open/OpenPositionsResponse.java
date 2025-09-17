package com.tomek4861.cryptopositionmanager.dto.positions.open;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.UUID;


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

        private String id;
        @JsonProperty("ticker")
        private String symbol;
        private Integer leverage;
        private BigDecimal avgPrice;
        private BigDecimal liqPrice;
        @JsonProperty("unrealisedPnl")
        private BigDecimal unrealizedPnl;
        @JsonProperty("markPrice")
        private BigDecimal marketPrice;
        @JsonProperty("isLong")
        private boolean isLong;
        private String tradingViewFormat;
        @JsonProperty("curRealisedPnl")
        private BigDecimal realizedPnl;
        private BigDecimal positionValue;
        private Date createdDate;

        @JsonProperty("positionIdx")
        private Integer positionIdx;


        @JsonProperty("id")
        public String getId() {
            //TODO: Reconsider this logic
            if (symbol == null || positionIdx == null) {
                return null;
            }
            String sideStr = isLong ? "B" : "S";
            String key = "BYBIT|" + symbol + "|" + positionIdx + "|" + sideStr;
            UUID uuid = UUID.nameUUIDFromBytes(key.getBytes(StandardCharsets.UTF_8));
            return uuid.toString();
        }


        @JsonProperty("side")
        public void determineSide(String sideVal) {
            this.isLong = sideVal.equalsIgnoreCase("Buy");
        }

        @JsonProperty("symbol")
        public void setSymbolAndTradingViewFormat(String symbolVal) {
            this.symbol = symbolVal;
            this.tradingViewFormat = "BYBIT" + ":" + symbolVal + ".P";

        }

        @JsonProperty("createdTime")
        public void setCreatedDateByTimestamp(String ts) {
            Date date = new Date(Long.parseLong(ts));
            this.createdDate = date;

        }


    }
}

