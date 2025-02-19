package com.ssafy.undaied.socket.json.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
public class JsonDataDto {
    private Map<Integer, JsonRoundInfoDto> rounds;

    @Builder
    public JsonDataDto(Map<Integer, JsonRoundInfoDto> rounds) {
        this.rounds = rounds;
    }
}
