package com.ssafy.undaied.socket.result.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameResultResponseDto {
    private String winner;
    private String message;
    private List<PlayerResultDto> players;
}
