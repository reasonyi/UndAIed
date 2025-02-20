package com.ssafy.undaied.socket.json.service;

import com.ssafy.undaied.socket.json.dto.JsonEventDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Arrays;

@Service
@RequiredArgsConstructor
@Slf4j
public class JsonEventService {

    private final RedisTemplate redisTemplate;

    public JsonEventDto getEventData(Integer gameId, Integer round) {
        log.debug("Starting sendEventData for game {}", gameId);

        // 이벤트 조회 키
        String eventKey = "game:" + gameId + ":round:" + round + ":events";

        // DTO
        Integer vote_result = null;
        Integer died = null;

        if ( redisTemplate.hasKey(eventKey) && !redisTemplate.opsForValue().get(eventKey).toString().trim().isEmpty()) {
            String allEvents = redisTemplate.opsForValue().get(eventKey).toString();
            String[] events = allEvents.split("\\s*\\|\\s*");
            log.debug("events: {}", Arrays.stream(events).toList());

            for (String event : events) {
                log.debug(event.trim());
                if (event.trim().startsWith("{vote_result}")) {
                    // 투표 결과
                    log.debug("🍳Convert to Json Vote Result data...");
                    String[] parts = event.split("\\s+");   // 공백 기준으로 나누기
                    String targetPart = parts[4];
                    String targetStr = targetPart.substring(1, targetPart.length() -1);
                    Integer targetNumber;
                    if (targetStr.equals("null")) {
                        targetNumber = -1;
                    } else {
                        targetNumber = Integer.parseInt(targetPart.substring(1, targetPart.length() - 1));
                    }
                    vote_result = targetNumber;
                }
                else if (event.trim().startsWith("{infection}")) {
                    // 구 감염
                    log.debug("🍳Convert to Json Infection data...");
                    String[] parts = event.split("\\s+");   // 공백 기준으로 나누기
                    String targetPart = parts[4];
                    Integer targetNumber = Integer.parseInt(targetPart.substring(1, targetPart.length() - 1));

                    died = targetNumber;
                }
            }
            log.debug("🍳GameId : {}, Round : {}, Driver Data to JSON completed", gameId, round);
            log.debug("🍳Vote Result : {}, Dead : {}", vote_result, died);
        }
        return JsonEventDto.builder()
                .vote_result(vote_result)
                .died(died)
                .build();
    }
}
