package com.tomek4861.tradetaker.dto.bybit;


import com.bybit.api.client.domain.market.response.instrumentInfo.LotSizeFilter;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class SafeLotSizeFilter extends LotSizeFilter {


}
