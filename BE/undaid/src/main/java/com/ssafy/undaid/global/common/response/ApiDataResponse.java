package com.ssafy.undaid.global.common.response;

import lombok.Getter;
import lombok.ToString;

@ToString
@Getter
public class ApiDataResponse<T> extends ApiResponse{

    private final T data;

    public ApiDataResponse(HttpStatusCode httpStatusCode, T data, String message) {
        super(httpStatusCode.getIsSuccess(), httpStatusCode.getStatus(), message);
        this.data = data;
    }

    public static <T> ApiDataResponse<T> of(HttpStatusCode statusCode, T data) {
        return new ApiDataResponse<>(statusCode, data, null);
    }

    public static <T> ApiDataResponse<T> of(HttpStatusCode statusCode, T data, String message) {
        return new ApiDataResponse<>(statusCode, data, message);
    }
}