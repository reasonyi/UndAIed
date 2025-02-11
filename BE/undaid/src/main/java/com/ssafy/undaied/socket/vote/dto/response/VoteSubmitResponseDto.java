package com.ssafy.undaied.socket.vote.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VoteSubmitResponseDto {
    private Integer number;
    private String message;

    @Builder
    public VoteSubmitResponseDto(Integer number) {
        this.number = number;
        this.message = "투표가 제출되었습니다.";
    }
}
