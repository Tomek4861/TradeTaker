package com.tomek4861.cryptopositionmanager.dto.positions.close;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@NoArgsConstructor
@JsonIgnoreProperties(ignoreUnknown = true)
public class PositionCloseDTO {
    @JsonProperty("list")
    private List<ClosedPnlEntry> pnlEntryList;

    @Data
    @NoArgsConstructor
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class ClosedPnlEntry {

        private String symbol;
        private String orderId;
        private String side;
        private BigDecimal qty;
        private BigDecimal closedPnl;
        private BigDecimal avgEntryPrice;
        private BigDecimal avgExitPrice;
        private LocalDateTime createdTime;
        private LocalDateTime updatedTime;

        @JsonProperty("createdTime")
        public void setAndConvertCreatedTime(String createdTime) {
            if (createdTime == null) {
                this.createdTime = null;
                return;
            }
            long millis = Long.parseLong(createdTime);
            this.createdTime = java.time.Instant.ofEpochMilli(millis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        }

        @JsonProperty("updatedTime")
        public void setAndConvertUpdatedTime(String updatedTime) {
            if (updatedTime == null) {
                this.updatedTime = null;
                return;
            }
            long millis = Long.parseLong(updatedTime);
            this.updatedTime = java.time.Instant.ofEpochMilli(millis)
                    .atZone(java.time.ZoneId.systemDefault())
                    .toLocalDateTime();
        }

    }

}
