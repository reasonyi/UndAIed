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
    private static final long MAX_DELAY = 7000;  // 스케줄링 최대 지연 시간
    private static final int MIN_CHAT_DELAY = 1000;  // 채팅 응답 최소 지연 시간 (1초)
    private static final int MAX_CHAT_DELAY = 3000;  // 채팅 응답 최대 지연 시간 (3초)
    private static final Random random = new Random();

    private final WebClient webClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final SocketIONamespace namespace;
    private final TaskScheduler taskScheduler;

    private final Map<Integer, ScheduledFuture<?>> gameSchedulers = new ConcurrentHashMap<>();
    /**
     * 게임 시작 시 AI 메시지 스케줄링을 시작
     * MIN_DELAY ~ MAX_DELAY 사이의 간격으로 주기적으로 메시지 처리
     * @param gameId 게임 ID
     */
    public void startGameMessageScheduling(int gameId) {
        log.info("Starting AI message scheduling for game: {}", gameId);
        stopGameMessageScheduling(gameId);

        ScheduledFuture<?> scheduledTask = taskScheduler.scheduleWithFixedDelay(
                () -> processGameMessages(gameId),
                Instant.now().plusMillis(MIN_DELAY),
                Duration.ofMillis(MAX_DELAY)
        );

        gameSchedulers.put(gameId, scheduledTask);
    }

    /**
     * 게임 종료 시 AI 메시지 스케줄링을 중지
     * @param gameId 게임 ID
     */
    public void stopGameMessageScheduling(int gameId) {
        log.info("Stopping AI message scheduling for game: {}", gameId);
        ScheduledFuture<?> scheduledTask = gameSchedulers.remove(gameId);
        if (scheduledTask != null) {
            scheduledTask.cancel(false);
        }
    }

    /**
     * 현재 게임 스테이지에 따라 메시지 처리를 수행
     * 토론 단계일 때만 AI 요청을 보냄
     * @param gameId 게임 ID
     */
    private void processGameMessages(int gameId) {
        String stageKey = GAME_KEY_PREFIX + gameId + ":stage";
        String currentStage = redisTemplate.opsForValue().get(stageKey);

        if (currentStage == null) {
            log.error("Stage not found for game: {}. Stopping scheduler.", gameId);
            stopGameMessageScheduling(gameId);
            return;
        }

        switch (currentStage) {
            case "subject_debate":
                sendAIRequest(gameId, currentStage);
                break;
            case "free_debate":
                handleFreeDebate(gameId);
                break;
            default:
                break;
        }
    }

    private void handleFreeDebate(int gameId) {
        try {
            String currentRound = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":round");
            String chatKey = String.format("%s%d:round:%s:freechats",
                    GAME_KEY_PREFIX, gameId, currentRound);

            // 현재 저장된 채팅 내용 확인
            String existingChats = redisTemplate.opsForValue().get(chatKey);
            if (existingChats == null) {
                log.info("No existing chats found for game: {} round: {}", gameId, currentRound);
                return;
            }

            String combinedData = collectAllGameData(gameId);
            List<AINumberDto> aiList = getGameAIs(gameId);

            if (aiList.isEmpty()) {
                log.error("No AI players found for game: {}", gameId);
                return;
            }

            AIInputDataDto sendData = AIInputDataDto.builder()
                    .selectedAIs(aiList.toArray(new AINumberDto[0]))
                    .message(combinedData)
                    .build();

            webClient.post()
                    .uri("/api/ai/{gameId}/chat", gameId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .bodyValue(sendData)
                    .retrieve()
                    .bodyToFlux(GameChatResponseDto.class)
                    .collectList()
                    .flatMapMany(responses -> {
                        return Flux.fromIterable(responses)
                                .concatMap(response -> {
                                    // 각 응답에 대해 저장 및 전송 지연
                                    int delay = MIN_CHAT_DELAY + random.nextInt(MAX_CHAT_DELAY - MIN_CHAT_DELAY);
                                    return Mono.just(response)
                                            .delayElement(Duration.ofMillis(delay));
                                });
                    })
                    .subscribe(
                            response -> {
                                // 자유토론 메시지 저장 및 전송
                                String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
                                String aiNumberStr = String.valueOf(response.getNumber());
                                Integer aiId = null;

                                for (Object key : redisTemplate.opsForHash().keys(mappingKey)) {
                                    String mappedNumber = (String) redisTemplate.opsForHash().get(mappingKey, key.toString());
                                    if (mappedNumber != null && mappedNumber.equals(aiNumberStr)) {
                                        aiId = Integer.parseInt(key.toString());
                                        break;
                                    }
                                }

                                if (aiId != null) {
                                    String message = String.format("{%d} [%s] <%d>(%s) %s|",
                                            aiId,
                                            "AI" + response.getNumber(),
                                            response.getNumber(),
                                            response.getContent(),
                                            LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

                                    redisTemplate.opsForValue().append(chatKey, message);
                                    redisTemplate.expire(chatKey, EXPIRE_TIME, TimeUnit.SECONDS);

                                    // 실시간 메시지 전송
                                    namespace.getRoomOperations(GAME_KEY_PREFIX + gameId)
                                            .sendEvent("game:chat:send", response);

                                    log.info("Free debate message stored and sent - gameId: {}, number: {}, content: {}",
                                            gameId, response.getNumber(), response.getContent());
                                }
                            },
                            error -> log.error("AI 서버 통신 실패 - gameId: {}", gameId, error)
                    );

        } catch (Exception e) {
            log.error("Error in free debate processing - gameId: {}", gameId, e);
        }
    }

    /**
     * AI 서버로 채팅 데이터를 전송
     * 이전 라운드를 포함한 모든 게임 데이터를 전송
     * @param gameId 게임 ID
     * @param currentStage 현재 스테이지
     */
    private void sendAIRequest(int gameId, String currentStage) {
        try {
            String combinedData = collectAllGameData(gameId);
            List<AINumberDto> aiList = getGameAIs(gameId);

            if (aiList.isEmpty()) {
                log.error("No AI players found for game: {}", gameId);
                return;
            }

            String currentRound = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":round");
            String spokenUsersKey = String.format("%s%d:round:%s:subject_speakers",
                    GAME_KEY_PREFIX, gameId, currentRound);

            // 아직 발언하지 않은 AI만 필터링
            List<AINumberDto> unspokenAIs = aiList.stream()
                    .filter(ai -> !Boolean.TRUE.equals(redisTemplate.opsForSet()
                            .isMember(spokenUsersKey, String.valueOf(ai.getNumber()))))
                    .toList();

            if (unspokenAIs.isEmpty()) {
                log.info("All AIs have already spoken in this round - gameId: {}, round: {}",
                        gameId, currentRound);
                return;
            }

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

    /**
     * 모든 라운드의 게임 데이터를 수집하여 하나의 문자열로 조합
     * 각 라운드별로 주제, 토론, 이벤트 데이터를 포함
     * @param gameId 게임 ID
     * @return 포맷팅된 전체 게임 데이터
     */
    private String collectAllGameData(int gameId) {
        String roundKey = GAME_KEY_PREFIX + gameId + ":round";
        String currentRound = redisTemplate.opsForValue().get(roundKey);
        int currentRoundNum = Integer.parseInt(currentRound);

        StringBuilder allRoundsData = new StringBuilder();

        // 1라운드부터 현재 라운드까지 순차적으로 데이터 수집
        for (int round = 1; round <= currentRoundNum; round++) {
            Map<String, String> roundData = collectRoundData(gameId, String.valueOf(round));

            // 라운드 시작 표시
            allRoundsData.append("<").append(round).append(">");

            // 주제 정보 추가
            String subjectKey = String.format("%s%d:round:%s:used_subjects",
                    GAME_KEY_PREFIX, gameId, round);
            String subject = redisTemplate.opsForValue().get(subjectKey);

            // 데이터를 형식에 맞게 포맷팅
            allRoundsData.append("[subject]\n");
            allRoundsData.append("(").append(subject).append(")\n");
            allRoundsData.append("[subject_debate]\n");
            allRoundsData.append(roundData.getOrDefault("subject_debate", "")).append("\n");
            allRoundsData.append("[free_debate]\n");
            allRoundsData.append(roundData.getOrDefault("free_debate", "")).append("\n");
            allRoundsData.append("[events]\n");
            allRoundsData.append(roundData.getOrDefault("events", "")).append("\n");

            // 마지막 라운드가 아닐 경우 구분선 추가
            if (round < currentRoundNum) {
                allRoundsData.append("\n----- 라운드 구분선 -----\n\n");
            }
        }

        return allRoundsData.toString();
    }

    /**
     * 특정 라운드의 데이터를 수집
     * @param gameId 게임 ID
     * @param round 라운드 번호
     * @return 해당 라운드의 채팅 및 이벤트 데이터
     */
    private Map<String, String> collectRoundData(int gameId, String round) {
        Map<String, String> data = new HashMap<>();

        // 각 유형별 데이터 수집
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

    /**
     * 게임의 AI 정보 조회
     * 게임 시작 시 선택된 AI 정보를 Redis에서 조회
     * @param gameId 게임 ID
     * @return AI 정보 목록
     */
    private List<AINumberDto> getGameAIs(int gameId) {
        // AI에게 할당된 번호 목록 조회
        String aiNumbersKey = GAME_KEY_PREFIX + gameId + ":ai_numbers";
        Set<String> aiNumbers = redisTemplate.opsForSet().members(aiNumbersKey);

        if (aiNumbers == null || aiNumbers.isEmpty()) {
            return new ArrayList<>();
        }

        // 플레이어 상태 키
        String statusKey = GAME_KEY_PREFIX + gameId + ":player_status";

        // AI 번호에 해당하는 AI ID 찾기
        List<AINumberDto> aiList = new ArrayList<>();
        String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";

        for (String number : aiNumbers) {
            // 플레이어 상태 확인
            String status = (String) redisTemplate.opsForHash().get(statusKey, number);
            // 죽은 AI는 제외
            if (status != null && status.contains("isDied=true")) {
                log.info("AI number {} is dead, skipping", number);
                continue;
            }

            // number_mapping에서 해당 번호에 매핑된 AI ID 찾기
            for (Object key : redisTemplate.opsForHash().keys(mappingKey)) {
                String mappedNumber = (String) redisTemplate.opsForHash().get(mappingKey, key);
                if (mappedNumber != null && mappedNumber.equals(number)) {
                    // 이미 선택된 AI의 ID (음수값 그대로 사용)
                    int aiId = Integer.parseInt(key.toString());
                    aiList.add(AINumberDto.builder()
                            .aiId(aiId)
                            .number(Integer.parseInt(number))
                            .build());
                    break;
                }
            }
        }

        log.info("Found {} alive AIs for game {}", aiList.size(), gameId);
        return aiList;
    }

    /**
     * AI 응답 처리
     * 스테이지가 변경되지 않았을 경우에만 응답을 처리
     * @param gameId 게임 ID
     * @param response AI 응답
     * @param originalStage 원래 스테이지
     */
    private void handleAIResponse(int gameId, GameChatResponseDto response, String originalStage) {
        String currentStage = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":stage");
        String currentRound = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":round");

        if (!originalStage.equals(currentStage)) {
            log.info("Stage changed during AI processing, ignoring response");
            return;
        }

        // 주제토론에서 AI가 이미 발언했는지 확인
        if ("subject_debate".equals(currentStage)) {
            String spokenUsersKey = String.format("%s%d:round:%s:subject_speakers",
                    GAME_KEY_PREFIX, gameId, currentRound);

            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(spokenUsersKey,
                    String.valueOf(response.getNumber())))) {
                log.info("AI {} has already spoken in subject debate round {}",
                        response.getNumber(), currentRound);
                return;
            }

            // AI 발언 기록
            redisTemplate.opsForSet().add(spokenUsersKey, String.valueOf(response.getNumber()));
            redisTemplate.expire(spokenUsersKey, EXPIRE_TIME, TimeUnit.SECONDS);
        }

        storeAIMessage(gameId, response);

        // 자유토론에서만 실시간 전송
        if ("free_debate".equals(currentStage)) {
            namespace.getRoomOperations(GAME_KEY_PREFIX + gameId)
                    .sendEvent("game:chat:send", response);
            log.info("Sent AI message in free debate - gameId: {}, number: {}, content: {}",
                    gameId, response.getNumber(), response.getContent());
        }
    }

    /**
     * AI 메시지를 Redis에 저장
     * 채팅 메시지 포맷: {aiId} [AI-number] <number>(content) timestamp|
     * @param gameId 게임 ID
     * @param response AI 응답
     */
    private void storeAIMessage(int gameId, GameChatResponseDto response) {
        String currentRound = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":round");
        String stage = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":stage");

        String chatKey = String.format("%s%d:round:%s:%s",
                GAME_KEY_PREFIX,
                gameId,
                currentRound,
                stage.equals("subject_debate") ? "subjectchats" : "freechats"
        );

        String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
        String aiNumberStr = String.valueOf(response.getNumber());
        Integer aiId = null;

        for (Object key : redisTemplate.opsForHash().keys(mappingKey)) {
            String mappedNumber = (String) redisTemplate.opsForHash().get(mappingKey, key.toString());
            if (mappedNumber != null && mappedNumber.equals(aiNumberStr)) {
                aiId = Integer.parseInt(key.toString());
                break;
            }
        }

        if (aiId == null) {
            log.error("AI ID not found for number: {}", response.getNumber());
            return;
        }

        String message = String.format("{%d} [%s] <%d>(%s) %s|",
                aiId,
                "AI-" + response.getNumber(),
                response.getNumber(),
                response.getContent(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        redisTemplate.opsForValue().append(chatKey, message);
        redisTemplate.expire(chatKey, EXPIRE_TIME, TimeUnit.SECONDS);

        log.info("AI message stored - gameId: {}, number: {}, stage: {}, content: {}",
                gameId, response.getNumber(), stage, response.getContent());
    }
}
