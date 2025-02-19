package com.ssafy.undaied.socket.json.dto;

import lombok.Builder;
import lombok.Getter;

@Getter
public class JsonEventDto {
    private Integer vote_result;
    private Integer died;

    @Builder
    public JsonEventDto(Integer vote_result, Integer died) {
        this.vote_result = vote_result;
        this.died = died;
    }
}
