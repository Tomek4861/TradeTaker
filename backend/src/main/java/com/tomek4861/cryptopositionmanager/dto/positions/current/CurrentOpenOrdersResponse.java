package com.tomek4861.cryptopositionmanager.dto.positions.current;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonInclude(JsonInclude.Include.NON_NULL)

public class CurrentOpenOrdersResponse {


    @JsonProperty("list")
    private List<ExchangeOrderDTO> ordersList;
    private boolean success;
    private String error;

    @JsonIgnoreProperties(ignoreUnknown = true)
    @Data
    @NoArgsConstructor
    public static class ExchangeOrderDTO {
        @JsonProperty("orderId")
        private String id;
        @JsonProperty("symbol")
        private String ticker;
        private String orderType;
        private BigDecimal price;

        private LocalDateTime createdTime;

        @JsonProperty("qty")
        private BigDecimal quantity;

        private String side;
        private boolean reduceOnly;

        public BigDecimal getValue() {
            if (price != null && quantity != null) {
                return price.multiply(quantity);
            }
            return null;
        }

        public boolean getIsLong() {
            return (side != null && side.equalsIgnoreCase("Buy"));
        }


        @JsonProperty("createdTime")
        public void setCreatedTime(String timestamp) {
            long epochMilli = Long.parseLong(timestamp);
            this.createdTime = LocalDateTime.ofInstant(Instant.ofEpochMilli(epochMilli), ZoneOffset.UTC);
        }


        public String getTradingViewFormat() {
            if (ticker != null) {
                return "BYBIT:" + ticker + ".P";
            }
            return null;
        }

    }

}
