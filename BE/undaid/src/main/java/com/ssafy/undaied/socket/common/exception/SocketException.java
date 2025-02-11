package com.ssafy.undaied.socket.common.exception;

import lombok.Getter;

@Getter
public class SocketException extends Exception {
    private final SocketErrorCode errorCode;

    public SocketException(SocketErrorCode errorCode) {
        super(errorCode.getMessage());  // Exception의 message로 errorCode의 메시지를 전달
        this.errorCode = errorCode;
    }
}
