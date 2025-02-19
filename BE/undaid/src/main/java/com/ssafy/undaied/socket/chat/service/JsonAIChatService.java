package com.ssafy.undaied.socket.chat.service;

import com.corundumstudio.socketio.SocketIONamespace;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.undaied.socket.chat.dto.request.AIRequestDto;
import com.ssafy.undaied.socket.chat.dto.response.AIInputDataDto;
import com.ssafy.undaied.socket.chat.dto.response.AINumberDto;
import com.ssafy.undaied.socket.chat.dto.response.GameChatResponseDto;
import com.ssafy.undaied.socket.json.dto.JsonRoundInfoDto;
import com.ssafy.undaied.socket.json.service.JsonSendService;
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
public class JsonAIChatService {

    private static final long EXPIRE_TIME = 7200;
    private static final long MIN_DELAY = 3000;  // 스케줄링 최소 지연 시간
    private static final long MAX_DELAY = 3000;  // 스케줄링 최대 지연 시간
    private static final int MIN_CHAT_DELAY = 1000;  // 채팅 응답 최소 지연 시간
    private static final int MAX_CHAT_DELAY = 10000;  // 채팅 응답 최대 지연 시간
    private static final Random random = new Random();

    private final WebClient webClient;
    private final RedisTemplate<String, String> redisTemplate;
    private final SocketIONamespace namespace;
    private final TaskScheduler taskScheduler;
    private final JsonSendService jsonSendService;

    private final Map<Integer, Map<String, ScheduledFuture<?>>> aiGameSchedulers = new ConcurrentHashMap<>();
    private final Map<Integer, Boolean> isProcessingGemini = new ConcurrentHashMap<>();
    private final Map<Integer, Boolean> isProcessingChatGPT = new ConcurrentHashMap<>();


    public void startGameMessageScheduling(int gameId) {
        log.info("AI 메시지 스케쥴링이 시작됩니다.: {}", gameId);
        stopGameMessageScheduling(gameId);

        // 해당 게임의 스케줄러 Map 생성
        Map<String, ScheduledFuture<?>> schedulers = new ConcurrentHashMap<>();

        // Gemini 스케줄링
        ScheduledFuture<?> geminiTask = taskScheduler.scheduleWithFixedDelay(
                () -> processGeminiMessages(gameId),
                Instant.now().plusMillis(MIN_DELAY),
                Duration.ofMillis(MAX_DELAY)
        );

        // ChatGPT 스케줄링 (2.5초 딜레이)
        ScheduledFuture<?> chatgptTask = taskScheduler.scheduleWithFixedDelay(
                () -> processChatGPTMessages(gameId),
                Instant.now().plusMillis(MIN_DELAY + 2500),
                Duration.ofMillis(MAX_DELAY)
        );

        schedulers.put("gemini", geminiTask);
        schedulers.put("chatgpt", chatgptTask);
        aiGameSchedulers.put(gameId, schedulers);
    }

    public void stopGameMessageScheduling(int gameId) {
        log.info("AI스케쥴링을 멈춥니다: {}", gameId);
        Map<String, ScheduledFuture<?>> schedulers = aiGameSchedulers.remove(gameId);
        if (schedulers != null) {
            schedulers.values().forEach(task -> {
                if (task != null) {
                    task.cancel(false);
                }
            });
        }
    }

    private boolean isValidStage(int gameId, String expectedStage) {
        String currentStage = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":stage");

        if (!expectedStage.equals(currentStage)) {
            log.info("스테이지가 바뀌었습니다 - expected: {}, current: {}", expectedStage, currentStage);
            return false;
        }
        return true;
    }

    private boolean isDebateStage(String stage) {
        return "subject_debate".equals(stage) || "free_debate".equals(stage);
    }

    private void processGeminiMessages(int gameId) {
        String currentStage = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":stage");

        if (!isDebateStage(currentStage)) {
            log.info("GEMINI.. 현재는 토론 시간이 아닙니다. {}", currentStage);
            return;
        }
        if ("subject_debate".equals(currentStage)) {
            sendGeminiRequest(gameId);
        } else {
            handleGeminiFreeDebate(gameId);
        }
    }

    private void processChatGPTMessages(int gameId) {
        String currentStage = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":stage");

        if (!isDebateStage(currentStage)) {
            log.info("GPT... 현재는 토론 시간이 아닙니다. {}", currentStage);
            return;
        }
        if ("subject_debate".equals(currentStage)) {
            sendChatGPTRequest(gameId);
        } else {
            handleChatGPTFreeDebate(gameId);
        }
    }

    private void sendGeminiRequest(int gameId) {
        if (!isValidStage(gameId, "subject_debate")) {
            return;
        }

        AIRequestDto aiRequestDto = createAIRequest(gameId, true);  // Gemini용
        if (aiRequestDto != null) {
            final int aiNumber = aiRequestDto.getAi_num();
            log.info("ai에 보내는 요청 {}", aiRequestDto);
            webClient.post()
                    .uri("/api/gemini/{gameId}", gameId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(aiRequestDto)
                    .retrieve()
                    .bodyToMono(GameChatResponseDto.class)
                    .map(response -> {
                        response.setNumber(aiNumber);
                        return response;
                    })
                    .delayElement(Duration.ofMillis(MIN_CHAT_DELAY + random.nextInt(MAX_CHAT_DELAY - MIN_CHAT_DELAY)))
                    .subscribe(
                            response -> handleAIResponse(gameId, response, "subject_debate"),
                            error -> log.error("Gemini 요청 실패 - gameId: {}", gameId, error)
                    );
        }
    }

    private void sendChatGPTRequest(int gameId) {
        if (!isValidStage(gameId, "subject_debate")) {
            return;
        }

        AIRequestDto aiRequestDto = createAIRequest(gameId, false);  // ChatGPT용
        if (aiRequestDto != null) {
            final int aiNumber = aiRequestDto.getAi_num();
            log.info("ai에 보내는 요청 {}", aiRequestDto);
            webClient.post()
                    .uri("/api/chatgpt/{gameId}", gameId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(aiRequestDto)
                    .retrieve()
                    .bodyToMono(GameChatResponseDto.class)
                    .map(response -> {
                        response.setNumber(aiNumber);
                        return response;
                    })
                    .delayElement(Duration.ofMillis(MIN_CHAT_DELAY + random.nextInt(MAX_CHAT_DELAY - MIN_CHAT_DELAY)))
                    .subscribe(
                            response -> handleAIResponse(gameId, response, "subject_debate"),
                            error -> log.error("ChatGPT 요청 실패 - gameId: {}", gameId, error)
                    );
        }
    }

    private void handleGeminiFreeDebate(int gameId) {
        if (!isValidStage(gameId, "free_debate")) {
            return;
        }

        // 🚨 이미 처리 중이면 요청하지 않음 (멀티스레드 보호)
        synchronized (isProcessingGemini) {
            if (isProcessingGemini.getOrDefault(gameId, false)) {
                log.info("🚨 Gemini AI가 현재 응답 중입니다 - gameId: {}", gameId);
                return;
            }
            isProcessingGemini.put(gameId, true);
        }

        AIRequestDto aiRequestDto = createAIRequest(gameId, true);  // Gemini용
        if (aiRequestDto != null) {
            final int aiNumber = aiRequestDto.getAi_num();
            webClient.post()
                    .uri("/api/gemini/{gameId}", gameId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(aiRequestDto)
                    .retrieve()
                    .bodyToMono(GameChatResponseDto.class)
                    .map(response -> {
                        response.setNumber(aiNumber);
                        return response;
                    })
                    .delayElement(Duration.ofMillis(MIN_CHAT_DELAY + random.nextInt(MAX_CHAT_DELAY - MIN_CHAT_DELAY)))
                    .subscribe(
                            response -> {
                                handleAIResponse(gameId, response, "free_debate");
                                isProcessingGemini.put(gameId, false); // ✅ 처리 완료 후 다시 false 설정
                            },
                            error -> {
                                log.error("Gemini 요청 실패 - gameId: {}", gameId, error);
                                isProcessingGemini.put(gameId, false); // 🚨 오류 발생해도 다시 false 설정
                            }
                    );
        } else {
            isProcessingGemini.put(gameId, false); // 🚨 요청 실패 시에도 다시 false 설정
        }
    }


    private void handleChatGPTFreeDebate(int gameId) {
        if (!isValidStage(gameId, "free_debate")) {
            return;
        }

        // 🚨 이미 처리 중이면 요청하지 않음 (멀티스레드 보호)
        synchronized (isProcessingChatGPT) {
            if (isProcessingChatGPT.getOrDefault(gameId, false)) {
                log.info("🚨 ChatGPT AI가 현재 응답 중입니다 - gameId: {}", gameId);
                return;
            }
            isProcessingChatGPT.put(gameId, true);
        }

        AIRequestDto aiRequestDto = createAIRequest(gameId, false);  // ChatGPT용
        if (aiRequestDto != null) {
            final int aiNumber = aiRequestDto.getAi_num();
            webClient.post()
                    .uri("/api/chatgpt/{gameId}", gameId)
                    .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(aiRequestDto)
                    .retrieve()
                    .bodyToMono(GameChatResponseDto.class)
                    .map(response -> {
                        response.setNumber(aiNumber);
                        return response;
                    })
                    .delayElement(Duration.ofMillis(MIN_CHAT_DELAY + random.nextInt(MAX_CHAT_DELAY - MIN_CHAT_DELAY)))
                    .subscribe(
                            response -> {
                                handleAIResponse(gameId, response, "free_debate");
                                isProcessingChatGPT.put(gameId, false); // ✅ 처리 완료 후 다시 false 설정
                            },
                            error -> {
                                log.error("ChatGPT 요청 실패 - gameId: {}", gameId, error);
                                isProcessingChatGPT.put(gameId, false); // 🚨 오류 발생해도 다시 false 설정
                            }
                    );
        } else {
            isProcessingChatGPT.put(gameId, false); // 🚨 요청 실패 시에도 다시 false 설정
        }
    }


    private void handleAIResponse(int gameId, GameChatResponseDto response, String originalStage) {
        if(response==null){
            log.info("AI응답이 null입니다-gameId:{}", gameId);
            return;
        }

        // content 유효성 체크 추가
        if(response.getContent() == null || response.getContent().trim().isEmpty()){
            log.info("AI응답의 content가 비어있습니다-gameId:{}", gameId);
            return;
        }

        if (!isValidStage(gameId, originalStage)) {
            return;
        }
        log.info("🚀 AI 응답 전송 준비 - gameId: {}, stage: {}, response: {}", gameId, originalStage, response);
        String currentRound = redisTemplate.opsForValue().get(GAME_KEY_PREFIX + gameId + ":round");
        if ("subject_debate".equals(originalStage)) {
            // 주제토론에서 중복 발언 체크
            String spokenUsersKey = String.format("%s%d:round:%s:subject_speakers",
                    GAME_KEY_PREFIX, gameId, currentRound);

            if (Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(spokenUsersKey,
                    String.valueOf(response.getNumber())))) {
                log.info("⚠️ AI {} 이미 발언 완료 - round {}", response.getNumber(), currentRound);
                return;
            }

            // 발언 기록 후 메시지 저장
            redisTemplate.opsForSet().add(spokenUsersKey, String.valueOf(response.getNumber()));
            redisTemplate.expire(spokenUsersKey, EXPIRE_TIME, TimeUnit.SECONDS);
            storeAIMessage(gameId, response, originalStage);
            log.info("✅ 주제토론 AI 메시지 저장 완료 - gameId: {}, number: {}, content: {}",
                    gameId, response.getNumber(), response.getContent());

        } else if ("free_debate".equals(originalStage)) {
            if (isValidStage(gameId, originalStage)) {
                log.info("🚀 자유토론 AI 메시지 전송 중 - gameId: {}, number: {}, content: {}",
                        gameId, response.getNumber(), response.getContent());
                namespace.getRoomOperations(GAME_KEY_PREFIX + gameId)
                        .sendEvent("game:chat:send", response);
                storeAIMessage(gameId, response, originalStage);
                log.info("✅ 자유토론 AI 메시지 저장 및 전송 완료 - gameId: {}, number: {}, content: {}",
                        gameId, response.getNumber(), response.getContent());
            }
        }
    }

    private AIRequestDto createAIRequest(int gameId, boolean isGemini) {
        try {
            String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
            Map<Object, Object> mappings = redisTemplate.opsForHash().entries(mappingKey);

            Integer numberForMinus2 = null;  // userId가 -2인 플레이어의 번호
            Integer numberForMinus3 = null;  // userId가 -3인 플레이어의 번호

            // mappings를 순회하면서 -2와 -3에 해당하는 number 찾기
            for (Map.Entry<Object, Object> entry : mappings.entrySet()) {
                int userId = Integer.parseInt(entry.getKey().toString());
                int number = Integer.parseInt(entry.getValue().toString());

                if (userId == -2) {
                    numberForMinus2 = number;
                } else if (userId == -3) {
                    numberForMinus3 = number;
                }
            }

            if (numberForMinus2 == null || numberForMinus3 == null) {
                log.error("AI 번호를 찾을 수 없습니다 - gameId: {}", gameId);
                return null;
            }

            // API 종류에 따라 ai_num과 ai_assist 결정
            Integer mainAiNum = isGemini ? numberForMinus2 : numberForMinus3;
            Integer assistAiNum = isGemini ? numberForMinus3 : numberForMinus2;

            // 상태 확인할 AI 번호 선택 (각 AI의 메인 번호로 체크)
            String statusKey = GAME_KEY_PREFIX + gameId + ":player_status";
            String playerStatus = (String) redisTemplate.opsForHash().get(statusKey, String.valueOf(mainAiNum));

            // 플레이어가 죽었거나 게임에서 나갔다면 null 반환
            if (playerStatus.contains("isDied=true")) {
                log.info("{} AI 플레이어가 이미 죽었습니다 - gameId: {}, aiNumber: {}",
                        isGemini ? "Gemini" : "ChatGPT", gameId, mainAiNum);
                return null;
            }

            Map<Integer, JsonRoundInfoDto> AIData=sendGameData(gameId);

            return AIRequestDto.builder()
                    .ai_num(mainAiNum)
                    .ai_assist(assistAiNum)
                    .message(AIData)
                    .build();

        } catch (Exception e) {
            log.error("{} AI Request 생성 중 오류 발생 - gameId: {}",
                    isGemini ? "Gemini" : "ChatGPT", gameId, e);
            return null;
        }
    }

    private boolean storeAIMessage(int gameId, GameChatResponseDto response, String stage) {
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

        String message = String.format("{%d} [%s] <%d> (%s) %s | ",
                aiId,
                "AI" + aiId,
                response.getNumber(),
                response.getContent(),
                LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        redisTemplate.opsForValue().append(chatKey, message);
        redisTemplate.expire(chatKey, EXPIRE_TIME, TimeUnit.SECONDS);
        return true;
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
    private Map<Integer, JsonRoundInfoDto> sendGameData(Integer gameId) {
        String roundKey = String.format("game:%d:round", gameId);
        Integer currentRound = Integer.parseInt(redisTemplate.opsForValue().get(roundKey).toString());

        Map<Integer, JsonRoundInfoDto> roundsMap = new HashMap<>();
        for (Integer i=1; i<= currentRound; i++) {
            JsonRoundInfoDto round = jsonSendService.getSendData(gameId, i);
            roundsMap.put(i, round);
        }

        return roundsMap;
    }
}