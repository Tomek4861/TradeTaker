package com.tomek4861.cryptopositionmanager.service;


import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.market.response.instrumentInfo.InstrumentEntry;
import com.bybit.api.client.domain.market.response.instrumentInfo.InstrumentInfoResult;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class BybitService {

    private final BybitApiClientFactory bybitApiClientFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public Object getAllTickers(CategoryType category) {

        BybitApiMarketRestClient client = bybitApiClientFactory.newMarketDataRestClient();
        MarketDataRequest request = MarketDataRequest.builder().category(category).build();
        return client.getMarketTickers(request);

    }

    public Object getAllPerpetualTickers() {
        BybitApiMarketRestClient client = bybitApiClientFactory.newMarketDataRestClient();
        List<InstrumentEntry> lineanInstruments = getInstrumentsForCategory(client, CategoryType.LINEAR);

        return lineanInstruments;
    }

    public List<InstrumentEntry> getInstrumentsForCategory(BybitApiMarketRestClient client, CategoryType category) {


        MarketDataRequest request = MarketDataRequest.builder()
                .category(category)
                .limit(200)
                .build();
        Object rawResponse = client.getInstrumentsInfo(request);
        TypeReference<GenericResponse<InstrumentInfoResult>> typeRef = new TypeReference<>() {
        };
        GenericResponse<InstrumentInfoResult> response = objectMapper.convertValue(rawResponse, typeRef);

        if (response != null && response.getResult() != null && response.getResult().getInstrumentEntries() != null) {
            return response.getResult().getInstrumentEntries();
        } else {
            return Collections.emptyList();
        }

    }
}
