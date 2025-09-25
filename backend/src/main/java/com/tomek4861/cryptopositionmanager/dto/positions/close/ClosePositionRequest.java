package com.tomek4861.cryptopositionmanager.dto.positions.close;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
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
    @NotNull
    @NotEmpty
    private String ticker;

    @NotNull
    @Positive
    private BigDecimal size;

}
