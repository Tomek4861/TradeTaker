package com.tomek4861.tradetaker.config;


import com.bybit.api.client.service.BybitApiClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class BybitApiConfig {


    @Bean
    public BybitApiClientFactory bybitApiClientFactory() {

        return BybitApiClientFactory.newInstance();
    }


}
