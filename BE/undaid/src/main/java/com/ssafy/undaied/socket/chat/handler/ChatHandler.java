package com.ssafy.undaied.socket.chat.handler;

import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.chat.dto.request.RoomChatRequestDto;
import com.ssafy.undaied.socket.chat.service.ChatService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Map;

import static com.ssafy.undaied.socket.common.constant.EventType.*;
import static com.ssafy.undaied.socket.common.exception.SocketErrorCode.ROOM_CHAT_FAILED;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatHandler {

    private final SocketIOServer server;
    private final ChatService chatService;

    @PostConstruct
    private void init() {
        server.addEventListener(ROOM_CHAT.getValue(), RoomChatRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        // 방 채팅.
                        chatService.roomChat(data, client);
                        log.info("Room chat - RoomId: {}, userId: {}, nickname: {}, message: {}", data.getRoomId(), client.get("userId"), client.get("nickname"), data.getMessage());
                    } catch (Exception e) {
                        log.error("Room chat failed: {}", e.getMessage());
                        // connection 유지하면서 error 이벤트 emit
                        Map<String, Object> errorData = new HashMap<>();
                        errorData.put("code", ROOM_CHAT_FAILED.getStatus());
                        errorData.put("message", ROOM_CHAT_FAILED.getMessage());
                        client.sendEvent("error", errorData);
                    }
                }
        );
    }

}
