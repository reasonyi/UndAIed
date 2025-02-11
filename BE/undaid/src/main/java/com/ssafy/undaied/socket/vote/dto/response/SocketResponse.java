package com.ssafy.undaied.socket.vote.dto.response;

import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class SocketResponse<T> {
    private final boolean success;
    private final T data;
    private final SocketErrorCode errorCode;

    public static <T> SocketResponse<T> success(T data) {
        return SocketResponse.<T>builder()
                .success(true)
                .data(data)
                .build();
    }

    public static <T> SocketResponse error(SocketErrorCode errorCode) {
        return SocketResponse.<T>builder()
                .success(false)
                .errorCode(errorCode)
                .build();
    }

}
