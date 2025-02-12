package com.ssafy.undaied.socket.chat.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.ssafy.undaied.socket.stage.handler.StageHandler;
import com.ssafy.undaied.socket.chat.dto.request.GameChatRequestDto;
import com.ssafy.undaied.socket.chat.service.GameChatService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
@Slf4j
@RequiredArgsConstructor
public class GameChatHandler {

    private final SocketIONamespace namespace;
    private final GameChatService gameChatService;

    @PostConstruct
    private void init() {
        namespace.addEventListener("chat:game", GameChatRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        log.info("Received data: {}", data);
                        Integer userId = client.get("userId");
                        gameChatService.processGameChat(client, userId, data);

                        log.info("Game chat - gameId: {}, userId: {}, nickname: {}, message: {}",
                                1, userId, client.get("nickname"), data.getContent());
                    } catch (Exception e) {
                        log.error("Game chat failed: {}", e.getMessage());

                        // 클라이언트에게 오류 이벤트 전송
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", "GAME_CHAT_FAILED");
                        errorData.put("message", "게임 채팅 전송 실패");
                        client.sendEvent("error", errorData);
                    }
                }
        );
    }
}

