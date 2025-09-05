package com.tomek4861.cryptopositionmanager.dto.other;


import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;


@Data
@AllArgsConstructor
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class StandardResponse {

    public StandardResponse(boolean success) {
        this.success = success;
        this.error = null;
    }

    private boolean success;
    private String error;

}
