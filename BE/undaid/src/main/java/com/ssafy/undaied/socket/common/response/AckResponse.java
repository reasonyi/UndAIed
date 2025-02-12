package com.ssafy.undaied.socket.common.response;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
public class AckResponse {
    private boolean success;
    private String errorMessage;
    private Object data;

    public AckResponse(boolean success, String errorMessage, Object data) {
        this.success = success;
        this.errorMessage = errorMessage;
        this.data = data;
    }


}
