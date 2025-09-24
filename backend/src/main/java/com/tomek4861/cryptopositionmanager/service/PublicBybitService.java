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
import com.tomek4861.cryptopositionmanager.dto.exchange.InstrumentEntryDTO;
import com.tomek4861.cryptopositionmanager.dto.exchange.TickerPriceDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;


@Service
@RequiredArgsConstructor
public class PublicBybitService {

    private final BybitApiClientFactory bybitApiClientFactory;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public Object getAllTickers(CategoryType category) {

        BybitApiMarketRestClient client = bybitApiClientFactory.newMarketDataRestClient();
        MarketDataRequest request = MarketDataRequest.builder().category(category).build();
        return client.getMarketTickers(request);

    }

    @Cacheable("perpetualTickers")
    public List<InstrumentEntryDTO> getAllPerpetualTickers() {
        BybitApiMarketRestClient client = bybitApiClientFactory.newMarketDataRestClient();

        return getInstrumentsForCategory(client, CategoryType.LINEAR);
    }


    @Cacheable(value = "perpetualTickerBySymbol", key = "#symbol")
    public Optional<InstrumentEntryDTO> getPerpetualTickerBySymbol(String symbol) {
        List<InstrumentEntryDTO> allTickers = getAllPerpetualTickers();

        return allTickers.stream()
                .filter(tickerDto -> tickerDto.getInstrumentEntry().getSymbol().equalsIgnoreCase(symbol)).findFirst();
    }


    public List<InstrumentEntryDTO> getInstrumentsForCategory(BybitApiMarketRestClient client, CategoryType category) {


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

            List<InstrumentEntryDTO> filteredEntries = instrumentEntries.stream()
                    .filter(
                            elem -> "LinearPerpetual".equals(elem.getContractType()) && elem.getSymbol().endsWith("USDT")
                    )
                    .sorted(Comparator.comparing(InstrumentEntry::getLaunchTime))
                    .map(
                            elem -> new InstrumentEntryDTO(elem, "BYBIT")
                    )
                    .toList();

            System.out.println("size arr " + filteredEntries.size());

            return filteredEntries;
        } else {
            return Collections.emptyList();
        }

    }


    public Optional<BigDecimal> getTickerPrice(String ticker) {
        BybitApiMarketRestClient client = bybitApiClientFactory.newMarketDataRestClient();
        MarketDataRequest request = MarketDataRequest.builder()
                .category(CategoryType.LINEAR)
                .symbol(ticker)
                .build();
        Object rawResponse = client.getMarketTickers(request);

        TypeReference<GenericResponse<TickerPriceDTO>> typeRef = new TypeReference<>() {
        };
        GenericResponse<TickerPriceDTO> response = objectMapper.convertValue(rawResponse, typeRef);
        if (response != null && response.getRetCode() == 0 && response.getResult() != null &&
                response.getResult().getList() != null && !response.getResult().getList().isEmpty()) {

            BigDecimal lastPrice = response.getResult().getList().getFirst().getLastPrice();
            return Optional.of(lastPrice);

        }
        return Optional.empty();


    }

}
