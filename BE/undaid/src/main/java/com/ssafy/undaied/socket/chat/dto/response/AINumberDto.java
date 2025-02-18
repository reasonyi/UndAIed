package com.ssafy.undaied.socket.chat.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class AINumberDto {
    private Integer aiId;
    private Integer number;

    @Builder
    public AINumberDto(Integer aiId, Integer number) {
        this.aiId = aiId;
        this.number = number;
    }
}
