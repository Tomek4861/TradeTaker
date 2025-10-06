package com.tomek4861.tradetaker.config.jackson;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class LotSizeFilterMixIn {
}