package com.ssafy.undaied.socket.json.service;

import com.ssafy.undaied.socket.json.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;


@Service
@RequiredArgsConstructor
public class JsonSendService {

    private final RedisTemplate redisTemplate;
    private final JsonChatService jsonChatService;
    private final JsonEventService jsonEventService;

    public JsonRoundInfoDto getSendData(Integer gameId, Integer round) {
        String roundKey = String.format("game:%d:round", gameId);
        String stageKey = String.format("game:%d:stage", gameId);
        Integer currentRound = Integer.parseInt(redisTemplate.opsForValue().get(roundKey).toString());
        String currentStage = redisTemplate.opsForValue().get(stageKey).toString();

        String topic = jsonChatService.getSubjectTopic(gameId, round);
        List<ChatDto> topic_debate;

        // 현재 진행 중인 라운드이고 stage가 subject_debate면 빈 배열 반환
        if (round.equals(currentRound) && "subject_debate".equals(currentStage)) {
            topic_debate = new ArrayList<>();
        } else {
            topic_debate = jsonChatService.getSubjectChat(gameId, round);
        }

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
