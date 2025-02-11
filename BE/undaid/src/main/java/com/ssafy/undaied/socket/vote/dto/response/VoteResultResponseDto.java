package com.ssafy.undaied.socket.vote.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Builder
@Getter
@RequiredArgsConstructor
public class VoteResultResponseDto {
    private Integer number;
    private Integer voteReceived;
}
