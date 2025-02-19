package com.ssafy.undaied.socket.json.dto;

import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
public class JsonRoundInfoDto {
    private String topic;
    private List<ChatDto> topic_debate;
    private List<ChatDto> free_debate;
    private JsonEventDto event;

    @Builder
    public JsonRoundInfoDto(String topic, List<ChatDto> topic_debate, List<ChatDto> free_debate, JsonEventDto event) {
        this.topic = topic;
        this.topic_debate = topic_debate;
        this.free_debate = free_debate;
        this.event = event;
    }
}
