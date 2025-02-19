package com.ssafy.undaied.socket.json.service;

import com.ssafy.undaied.socket.json.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;


@Service
@RequiredArgsConstructor
public class JsonSendService {

    private final JsonChatService jsonChatService;
    private final JsonEventService jsonEventService;

    public JsonRoundInfoDto getSendData(Integer gameId, Integer round) {
        String topic = jsonChatService.getSubjectTopic(gameId, round);
        List<ChatDto> topic_debate = jsonChatService.getSubjectChat(gameId, round);
        List<ChatDto> free_debate = jsonChatService.getFreeChat(gameId, round);
        JsonEventDto event = jsonEventService.getEventData(gameId, round);
        JsonRoundInfoDto roundDto = JsonRoundInfoDto.builder()
                .topic(topic)
                .topic_debate(topic_debate)
                .free_debate(free_debate)
                .event(event)
                .build();

    return roundDto;
    }
}
