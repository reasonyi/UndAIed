package com.ssafy.undaied.socket.chat.handler;

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
                        log.info("들어옴");

                        //임시
                        String gameIdStr = client.getHandshakeData().getSingleUrlParam("gameId");
                        Integer gameId = Integer.parseInt(gameIdStr);

                        log.info("여기가 문제?");
                        //복구해야
//                        Integer gameId = client.get("gameId");

                        Integer userId = client.get("userId");
                        String stageKey = GAME_KEY_PREFIX + gameId + ":stage";

                        //임시 변수
                        String currentStage="free_debate";
//                        String currentStage = redisTemplate.opsForValue().get(stageKey);


                        log.info("currentStage: {}", currentStage);


                        if ("free_debate".equals(currentStage)) {
                            log.info("6. 자유토론 처리 시작");
                            gameChatService.processFreeChat(client, userId, data);
                        } else {
                            gameChatService.storeSubjectChat(client, userId, data);
                        }
                        // 저장 성공 응답 전송
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("errorMessage", null);
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                        }
                    } catch (Exception e) {
                        log.error("Game chat failed: {}", e.getMessage());

                        // 클라이언트에게 오류 이벤트 전송
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", "GAME_CHAT_FAILED");
                        errorData.put("message", "게임 채팅 전송 실패");
                        client.sendEvent("error", errorData);
                    }
                });
    }
}

