package com.tomek4861.cryptopositionmanager.dto.exchange;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;

import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class WalletBalanceDTO {
    private String totalEquity;
    private String totalWalletBalance;
    private String accountType;


    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Result {
        private List<WalletBalanceDTO> list;
    }


}
