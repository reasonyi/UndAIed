package com.ssafy.undaied.socket.result.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class GameResultResponseDto {
    private int gameId;
    private String winner;
    private String message;
    private List<ParticipantResultDto> participants;
}
