package com.tomek4861.cryptopositionmanager.config;

import com.bybit.api.client.domain.market.response.instrumentInfo.LotSizeFilter;
import com.tomek4861.cryptopositionmanager.config.jackson.LotSizeFilterMixIn;
import org.springframework.boot.autoconfigure.jackson.Jackson2ObjectMapperBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {

    @Bean
    public Jackson2ObjectMapperBuilderCustomizer jacksonCustomizer() {
        return builder -> builder.mixIn(LotSizeFilter.class, LotSizeFilterMixIn.class);
    }
}