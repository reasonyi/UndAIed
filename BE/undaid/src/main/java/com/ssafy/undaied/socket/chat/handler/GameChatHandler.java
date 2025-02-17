package com.ssafy.undaied.socket.chat.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.ssafy.undaied.socket.chat.dto.request.GameChatRequestDto;
import com.ssafy.undaied.socket.chat.service.GameChatService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.GAME_KEY_PREFIX;

@Component
@Slf4j
@RequiredArgsConstructor
public class GameChatHandler {

    private final SocketIONamespace namespace;
    private final GameChatService gameChatService;
    private final RedisTemplate<String, String> redisTemplate;

    @PostConstruct
    private void init() {
        namespace.addEventListener("game:chat:emit", GameChatRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        log.info("게임 채팅 요청 확인");
                        log.info("Received data object: {}", data);
                        log.info("Content from data: {}", data.getContent());

                        Integer gameId = client.get("gameId");
                        Integer userId = client.get("userId");

                        if (gameId == null || userId == null) {
                            throw new IllegalStateException("게임 또는 사용자 정보를 찾을 수 없습니다.");
                        }

                        String stageKey = GAME_KEY_PREFIX + gameId + ":stage";
                        String currentStage = redisTemplate.opsForValue().get(stageKey);
                        log.info("현재 stage: {}", currentStage);

                        if ("subject_debate".equals(currentStage)) {
                            String errorMessage = gameChatService.storeSubjectChat(gameId, client, userId, data);
                            sendResponse(ackRequest, errorMessage == null, errorMessage);
                        } else {
                            String errorMessage = gameChatService.processFreeChat(gameId, client, userId, data);
                            sendResponse(ackRequest, errorMessage == null, errorMessage);
                        }

                    } catch (Exception e) {
                        log.error("채팅 처리 중 오류 발생: {}", e.getMessage(), e);
                        sendResponse(ackRequest, false, "채팅 처리 중 오류가 발생했습니다. 다시 시도해 주세요.");
                    }
                });
    }

    private void sendResponse(AckRequest ackRequest, boolean success, String errorMessage) {
        if (ackRequest.isAckRequested()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("errorMessage", errorMessage);
            response.put("data", null);
            ackRequest.sendAckData(response);
        }
    }
}

