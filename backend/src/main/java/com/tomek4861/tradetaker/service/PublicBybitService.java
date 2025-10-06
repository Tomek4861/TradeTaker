package com.tomek4861.tradetaker.service;


import com.bybit.api.client.domain.CategoryType;
import com.bybit.api.client.domain.GenericResponse;
import com.bybit.api.client.domain.market.request.MarketDataRequest;
import com.bybit.api.client.domain.market.response.instrumentInfo.InstrumentEntry;
import com.bybit.api.client.domain.market.response.instrumentInfo.InstrumentInfoResult;
import com.bybit.api.client.restApi.BybitApiMarketRestClient;
import com.bybit.api.client.service.BybitApiClientFactory;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.tomek4861.tradetaker.dto.exchange.InstrumentEntryDTO;
import com.tomek4861.tradetaker.dto.exchange.TickerPriceDTO;
import lombok.AllArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.*;


@Service
@AllArgsConstructor
public class PublicBybitService {

    private final BybitApiClientFactory bybitApiClientFactory;
    private final ObjectMapper objectMapper;

    private static final List<String> CUSTOM_ORDER_LIST = List.of("BTCUSDT", "ETHUSDT", "SOLUSDT", "BNBUSDT");
    private static final Map<String, Integer> CUSTOM_ORDER_MAP = new HashMap<>();

    static {
        for (int i = 0; i < CUSTOM_ORDER_LIST.size(); i++) {
            CUSTOM_ORDER_MAP.put(CUSTOM_ORDER_LIST.get(i), i);
        }
    }


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
                    .sorted(
                            Comparator.comparing(
                                    (InstrumentEntry entry) -> CUSTOM_ORDER_MAP.getOrDefault(entry.getSymbol(), Integer.MAX_VALUE)
                            ).thenComparing(
                                    InstrumentEntry::getLaunchTime
                            )

                    )
                    .map(
                            elem -> new InstrumentEntryDTO(elem, "BYBIT")
                    )
                    .toList();

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
