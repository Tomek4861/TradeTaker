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
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

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

    @Cacheable("perpetualTickers")
    public Object getAllPerpetualTickers() {
        BybitApiMarketRestClient client = bybitApiClientFactory.newMarketDataRestClient();

        return getInstrumentsForCategory(client, CategoryType.LINEAR);
    }

    public List<InstrumentEntry> getInstrumentsForCategory(BybitApiMarketRestClient client, CategoryType category) {


        MarketDataRequest request = MarketDataRequest.builder()
                .category(category)
                .limit(1000)
                .build();
        Object rawResponse = client.getInstrumentsInfo(request);
        TypeReference<GenericResponse<InstrumentInfoResult>> typeRef = new TypeReference<>() {
        };
        GenericResponse<InstrumentInfoResult> response = objectMapper.convertValue(rawResponse, typeRef);

        if (response != null && response.getResult() != null && response.getResult().getInstrumentEntries() != null) {
            List<InstrumentEntry> instrumentEntries = response.getResult().getInstrumentEntries();

            List<InstrumentEntry> filteredEntries = instrumentEntries.stream()
                    .filter(
                            elem -> "LinearPerpetual".equals(elem.getContractType()) && elem.getSymbol().endsWith("USDT")
                    )
                    .toList();

            System.out.println("size arr " + filteredEntries.size());

            return filteredEntries;
        } else {
            return Collections.emptyList();
        }

    }
}
