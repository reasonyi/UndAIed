package com.ssafy.undaied.socket.chat.service;
import com.corundumstudio.socketio.SocketIONamespace;
import com.ssafy.undaied.socket.chat.dto.response.AINumberDto;
import com.ssafy.undaied.socket.chat.dto.response.AIInputDataDto;
import com.ssafy.undaied.socket.chat.dto.response.GameChatResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.scheduling.TaskScheduler;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.GAME_KEY_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class AIChatService {
    private static final long EXPIRE_TIME = 7200;
    private static final long MIN_DELAY = 5000;  // 스케줄링 최소 지연 시간
    private static final long MAX_DELAY = 2000;  // 스케줄링 최대 지연 시간
    private static final int MIN_CHAT_DELAY = 1000;  // 채팅 응답 최소 지연 시간 (1초)
    private static final int MAX_CHAT_DELAY = 7000;  // 채팅 응답 최대 지연 시간 (7초)
    private static final Random random = new Random();

    private final WebClient webClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final SocketIONamespace namespace;
    private final TaskScheduler taskScheduler;

    private final Map<Integer, ScheduledFuture<?>> gameSchedulers = new ConcurrentHashMap<>();

    public void startGameMessageScheduling(int gameId) {
        log.info("Starting AI message scheduling for game: {}", gameId);
        stopGameMessageScheduling(gameId);

        String currentStage = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":stage");
        if (!isDebateStage(currentStage)) {
            log.info("Current stage is {}, not starting scheduler", currentStage);
            return;
        }

        ScheduledFuture<?> scheduledTask = taskScheduler.scheduleWithFixedDelay(
                () -> processGameMessages(gameId),
                Instant.now().plusMillis(MIN_DELAY),
                Duration.ofMillis(MAX_DELAY)
        );

        gameSchedulers.put(gameId, scheduledTask);
    }

    public void stopGameMessageScheduling(int gameId) {
        log.info("Stopping AI message scheduling for game: {}", gameId);
        ScheduledFuture<?> scheduledTask = gameSchedulers.remove(gameId);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
    }

    private boolean isValidStage(int gameId, String expectedStage) {
        String currentStage = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":stage");
        if (currentStage == null) {
            log.error("Stage not found for game: {}", gameId);
            return false;
        }
        if (!expectedStage.equals(currentStage)) {
            log.info("Stage mismatch - expected: {}, current: {}", expectedStage, currentStage);
            return false;
        }
        return true;
    }

    private boolean isDebateStage(String stage) {
        return "subject_debate".equals(stage) || "free_debate".equals(stage);
    }

    private void processGameMessages(int gameId) {
        String currentStage = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":stage");

        if (currentStage == null) {
            log.error("Stage not found for game: {}. Stopping scheduler.", gameId);
            stopGameMessageScheduling(gameId);
            return;
        }

        if (!isDebateStage(currentStage)) {
            log.debug("Current stage is {}, skipping AI message processing", currentStage);
            return;
        }

        if ("subject_debate".equals(currentStage)) {
            sendAIRequest(gameId, currentStage);
        } else {
            handleFreeDebate(gameId);
        }
    }

    private void handleFreeDebate(int gameId) {
        try {
            if (!isValidStage(gameId, "free_debate")) {
                return;
            }

            String currentRound = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":round");
            String chatKey = String.format("%s%d:round:%s:freechats",
                    GAME_KEY_PREFIX, gameId, currentRound);

            String existingChats = redisTemplate.opsForValue().get(chatKey);
            if (existingChats == null) {
                log.info("No existing chats found for game: {} round: {}", gameId, currentRound);
                return;
            }

            List<AINumberDto> aiList = getGameAIs(gameId);
            if (aiList.isEmpty()) {
                log.error("No alive AI players found for game: {}", gameId);
                return;
            }

            String combinedData = collectAllGameData(gameId);
            AIInputDataDto sendData = AIInputDataDto.builder()
                    .selectedAIs(aiList.toArray(new AINumberDto[0]))
                    .message(combinedData)
                    .build();

            webClient.post()
                    .uri("/api/ai/{gameId}/", gameId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(sendData)
                    .retrieve()
                    .bodyToFlux(GameChatResponseDto.class)
                    .collectList()
                    .flatMapMany(responses -> {
                        return Flux.fromIterable(responses)
                                .concatMap(response -> {
                                    int delay = MIN_CHAT_DELAY + random.nextInt(MAX_CHAT_DELAY - MIN_CHAT_DELAY);
                                    return Mono.just(response)
                                            .delayElement(Duration.ofMillis(delay));
                                });
                    })
                    .subscribe(
                            response -> handleAIResponse(gameId, response, "free_debate"),
                            error -> log.error("AI 서버 통신 실패 - gameId: {}", gameId, error)
                    );

        } catch (Exception e) {
            log.error("Error in free debate processing - gameId: {}", gameId, e);
        }
    }

    private void sendAIRequest(int gameId, String currentStage) {
        try {
            if (!isValidStage(gameId, currentStage)) {
                return;
            }

            List<AINumberDto> aiList = getGameAIs(gameId);
            if (aiList.isEmpty()) {
                log.error("No alive AI players found for game: {}", gameId);
                return;
            }

            String currentRound = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":round");
            String spokenUsersKey = String.format("%s%d:round:%s:subject_speakers",
                    GAME_KEY_PREFIX, gameId, currentRound);

            List<AINumberDto> unspokenAIs = aiList.stream()
                    .filter(ai -> !Boolean.TRUE.equals(redisTemplate.opsForSet()
                            .isMember(spokenUsersKey, String.valueOf(ai.getNumber()))))
                    .toList();

            if (unspokenAIs.isEmpty()) {
                log.info("All AIs have already spoken in this round - gameId: {}, round: {}",
                        gameId, currentRound);
                return;
            }

            String combinedData = collectAllGameData(gameId);
            AIInputDataDto sendData = AIInputDataDto.builder()
                    .selectedAIs(unspokenAIs.toArray(new AINumberDto[0]))
                    .message(combinedData)
                    .build();

            webClient.post()
                    .uri("/api/ai/{gameId}/", gameId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(sendData)
                    .retrieve()
                    .bodyToFlux(GameChatResponseDto.class)
                    .doOnNext(response -> log.info("✅ AI 응답: {}", response))
                    .doOnError(error -> log.error("❌ AI 응답 오류", error))
                    .collectList()
                    .flatMapMany(responses -> {
                        return Flux.fromIterable(responses)
                                .concatMap(response -> {
                                    int delay = MIN_CHAT_DELAY + random.nextInt(MAX_CHAT_DELAY - MIN_CHAT_DELAY);
                                    return Mono.just(response)
                                            .delayElement(Duration.ofMillis(delay));
                                });
                    })
                    .subscribe(
                            response -> handleAIResponse(gameId, response, currentStage),
                            error -> log.error("AI 서버 통신 실패 - gameId: {}", gameId, error)
                    );

        } catch (Exception e) {
            log.error("Error in subject debate processing - gameId: {}", gameId, e);
        }
    }

    private void handleAIResponse(int gameId, GameChatResponseDto response, String originalStage) {
        if (!isValidStage(gameId, originalStage)) {
            return;
        }

        String currentRound = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":round");

        if ("subject_debate".equals(originalStage)) {
            // 주제토론에서 중복 발언 체크
            String spokenUsersKey = String.format("%s%d:round:%s:subject_speakers",
                    GAME_KEY_PREFIX, gameId, currentRound);

            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(spokenUsersKey,
                    String.valueOf(response.getNumber())))) {
                log.info("AI {} has already spoken in subject debate round {}",
                        response.getNumber(), currentRound);
                return;
            }

            // 발언 기록 후 메시지 저장
            redisTemplate.opsForSet().add(spokenUsersKey, String.valueOf(response.getNumber()));
            redisTemplate.expire(spokenUsersKey, EXPIRE_TIME, TimeUnit.SECONDS);
            storeAIMessage(gameId, response, originalStage);
            log.info("Subject debate message stored - gameId: {}, number: {}, content: {}",
                    gameId, response.getNumber(), response.getContent());

        } else if ("free_debate".equals(originalStage)) {
            // 자유토론은 전송 성공한 경우에만 저장
            if (isValidStage(gameId, originalStage)) {
                namespace.getRoomOperations(GAME_KEY_PREFIX + gameId)
                        .sendEvent("game:chat:send", response);
                storeAIMessage(gameId, response, originalStage);
                log.info("Free debate message sent and stored - gameId: {}, number: {}, content: {}",
                        gameId, response.getNumber(), response.getContent());
            }
        }
    }

    private boolean storeAIMessage(int gameId, GameChatResponseDto response, String stage) {
        if (!isValidStage(gameId, stage)) {
            return false;
        }

        String currentRound = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":round");
        String chatKey = String.format("%s%d:round:%s:%s",
                GAME_KEY_PREFIX,
                gameId,
                currentRound,
                stage.equals("subject_debate") ? "subjectchats" : "freechats"
        );

        Integer aiId = findAIId(gameId, response.getNumber());
        if (aiId == null) {
            log.error("AI ID not found for number: {}", response.getNumber());
            return false;
        }

        String message = String.format("{%d} [%s] <%d>(%s) %s|",
                aiId,
                "AI" + aiId,
                response.getNumber(),
                response.getContent(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        redisTemplate.opsForValue().append(chatKey, message);
        redisTemplate.expire(chatKey, EXPIRE_TIME, TimeUnit.SECONDS);
        return true;
    }

    private List<AINumberDto> getGameAIs(int gameId) {
        String aiNumbersKey = GAME_KEY_PREFIX + gameId + ":ai_numbers";
        Set<String> aiNumbers = redisTemplate.opsForSet().members(aiNumbersKey);

        if (aiNumbers == null || aiNumbers.isEmpty()) {
            return new ArrayList<>();
        }

        String statusKey = GAME_KEY_PREFIX + gameId + ":player_status";
        List<AINumberDto> aiList = new ArrayList<>();
        String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";

        for (String number : aiNumbers) {
            String status = (String) redisTemplate.opsForHash().get(statusKey, number);
            if (status != null && status.contains("isDied=true")) {
                log.info("AI number {} is dead, skipping", number);
                continue;
            }

            Integer aiId = findAIId(gameId, Integer.parseInt(number));
            if (aiId != null) {
                aiList.add(AINumberDto.builder()
                        .aiId(aiId)
                        .number(Integer.parseInt(number))
                        .build());
            }
        }

        log.info("Found {} alive AIs for game {}", aiList.size(), gameId);
        return aiList;
    }

    private Integer findAIId(int gameId, int aiNumber) {
        String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
        String aiNumberStr = String.valueOf(aiNumber);

        for (Object key : redisTemplate.opsForHash().keys(mappingKey)) {
            String mappedNumber = (String) redisTemplate.opsForHash().get(mappingKey, key.toString());
            if (mappedNumber != null && mappedNumber.equals(aiNumberStr)) {
                return Integer.parseInt(key.toString());
            }
        }
        return null;
    }

    private String collectAllGameData(int gameId) {
        String roundKey = GAME_KEY_PREFIX + gameId + ":round";
        String currentRound = redisTemplate.opsForValue().get(roundKey);
        int currentRoundNum = Integer.parseInt(currentRound);

        StringBuilder allRoundsData = new StringBuilder();

        for (int round = 1; round <= currentRoundNum; round++) {
            Map<String, String> roundData = collectRoundData(gameId, String.valueOf(round));

            allRoundsData.append("<").append(round).append(">");

            String subjectKey = String.format("%s%d:round:%s:used_subjects",
                    GAME_KEY_PREFIX, gameId, round);
            String subject = redisTemplate.opsForValue().get(subjectKey);

            allRoundsData.append("[topic]");
            allRoundsData.append(subject);
            allRoundsData.append("[topic_debate]");
            allRoundsData.append(roundData.getOrDefault("subject_debate", "")).append("\n");
            allRoundsData.append("[free_debate]");
            allRoundsData.append(roundData.getOrDefault("free_debate", "")).append("\n");
            allRoundsData.append("[events]");
            allRoundsData.append(roundData.getOrDefault("events", "")).append("\n");
        }

        return allRoundsData.toString();
    }

    private Map<String, String> collectRoundData(int gameId, String round) {
        Map<String, String> data = new HashMap<>();

        String subjectDebateKey = String.format("%s%d:round:%s:subjectchats",
                GAME_KEY_PREFIX, gameId, round);
        data.put("subject_debate", redisTemplate.opsForValue().get(subjectDebateKey));

        String freeDebateKey = String.format("%s%d:round:%s:freechats",
                GAME_KEY_PREFIX, gameId, round);
        data.put("free_debate", redisTemplate.opsForValue().get(freeDebateKey));

        String eventsKey = String.format("%s%d:round:%s:events",
                GAME_KEY_PREFIX, gameId, round);
        data.put("events", redisTemplate.opsForValue().get(eventsKey));

        return data;
    }
}
