package com.tomek4861.cryptopositionmanager.dto.positions.close;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ClosePositionRequest {

    @JsonProperty("isLong")
    private boolean isLong;
    private String ticker;
    private BigDecimal size;

}
