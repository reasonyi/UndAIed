package com.ssafy.undaied.socket.vote.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class VoteSubmitRequestDto {
    Integer target;
}
