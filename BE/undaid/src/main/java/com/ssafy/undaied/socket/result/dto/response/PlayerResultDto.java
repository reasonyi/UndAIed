package com.ssafy.undaied.socket.result.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class PlayerResultDto {
    private int number;
    private String nickname;
    private boolean isDied;
    private boolean isInGame;
}