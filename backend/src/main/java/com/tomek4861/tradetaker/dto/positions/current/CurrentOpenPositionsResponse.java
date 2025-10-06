package com.tomek4861.tradetaker.dto.positions.current;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;


@JsonIgnoreProperties(ignoreUnknown = true)
@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CurrentOpenPositionsResponse {

    @JsonProperty("list")
    List<OpenPositionDTO> positionDTOList;


    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @NoArgsConstructor
    public static class OpenPositionDTO {

        private String id;
        @JsonProperty("symbol")
        private String ticker;
        private Integer leverage;
        private BigDecimal avgPrice;
        private BigDecimal liqPrice;
        private BigDecimal unrealisedPnl;
        @JsonProperty("markPrice")
        private BigDecimal marketPrice;
        private String tradingViewFormat;

        @JsonProperty("curRealisedPnl")
        private BigDecimal realizedPnl;
        @JsonProperty("positionValue")
        private BigDecimal positionValue;
        private LocalDateTime createdTime;
        private BigDecimal size;

        private String side;

        @JsonProperty("positionIdx")
        private Integer positionIdx;


        public boolean getIsLong() {
            return (side != null && side.equalsIgnoreCase("Buy"));
        }

        public String getTradingViewFormat() {
            if (ticker != null) {
                return "BYBIT:" + ticker + ".P";
            }
            return null;
        }


        @JsonProperty("createdTime")
        public void setCreatedTime(String timestamp) {
            long epochMilli = Long.parseLong(timestamp);
            this.createdTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC);
        }

        @JsonProperty("currentPnlPercent")
        public BigDecimal getCurrentPnlPercent() {
            if (unrealisedPnl == null || positionValue == null || positionValue.compareTo(BigDecimal.ZERO) == 0) {
                return BigDecimal.ZERO;
            }
            return unrealisedPnl
                    .divide(positionValue, 8, RoundingMode.HALF_UP)
                    .multiply(BigDecimal.valueOf(100))
                    .setScale(2, RoundingMode.HALF_UP);
        }

        @JsonProperty("margin")
        public BigDecimal getMargin() {
            if (positionValue == null || leverage == null || leverage == 0) {
                return BigDecimal.ZERO;
            }
            return positionValue
                    .divide(BigDecimal.valueOf(leverage), 8, RoundingMode.HALF_UP)
                    .setScale(2, RoundingMode.HALF_UP);
        }



    }
}

