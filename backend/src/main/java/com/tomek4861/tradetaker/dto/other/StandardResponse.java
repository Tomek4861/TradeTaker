package com.tomek4861.tradetaker.dto.other;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
@NoArgsConstructor(access = AccessLevel.PRIVATE)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class StandardResponse<T> {

    private boolean success;
    private T data;
    private String error;

    public static <T> StandardResponse<T> success(T data) {
        return new StandardResponse<>(true, data, null);
    }


    public static <T> StandardResponse<T> success() {
        return new StandardResponse<>(true, null, null);
    }


    public static <T> StandardResponse<T> error(String errorMessage) {
        return new StandardResponse<>(false, null, errorMessage);
    }
}