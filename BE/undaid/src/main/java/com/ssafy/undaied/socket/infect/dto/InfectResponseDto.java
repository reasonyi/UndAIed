package com.ssafy.undaied.socket.infect.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class InfectResponseDto {
    private Integer number = 0;
    private String content;

    @Builder
    public InfectResponseDto(String infectedNumber) {
        this.content = String.format("밤 사이에 인간 익명%s 플레이어가 사라졌습니다.", infectedNumber);
    }
}
