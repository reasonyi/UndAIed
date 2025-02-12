package com.ssafy.undaied.socket.vote.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@AllArgsConstructor
@Builder
public class VoteResultResponseDto {
    private Integer number;
    private Integer voteReceived;
}
