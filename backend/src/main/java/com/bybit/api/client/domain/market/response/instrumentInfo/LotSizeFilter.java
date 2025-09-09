package com.bybit.api.client.domain.market.response.instrumentInfo;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;

@JsonIgnoreProperties(ignoreUnknown = true)
@Getter
public class LotSizeFilter {
    private String maxOrderQty;
    private String minOrderQty;
    private String qtyStep;
    private String postOnlyMaxOrderQty;
    private String basePrecision;
    private String quotePrecision;
    private String minOrderAmt;
    private String maxOrderAmt;
}