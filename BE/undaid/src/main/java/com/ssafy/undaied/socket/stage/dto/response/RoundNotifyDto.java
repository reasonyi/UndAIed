package com.ssafy.undaied.socket.stage.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class RoundNotifyDto {
    private final Integer number = 0;
    private String message;

    public static RoundNotifyDto notifyRoundStart(int round) {
        return  RoundNotifyDto.builder()
                .message("제 "+ round + " 라운드입니다." )
                .build();
    }
}
