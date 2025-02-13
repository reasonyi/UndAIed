package com.ssafy.undaied.socket.chat.handler;

import com.corundumstudio.socketio.SocketIONamespace;
import com.ssafy.undaied.socket.chat.dto.request.LobbyChatRequestDto;
import com.ssafy.undaied.socket.chat.dto.request.RoomChatRequestDto;
import com.ssafy.undaied.socket.chat.dto.response.RoomChatResponseDto;
import com.ssafy.undaied.socket.chat.service.ChatService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


import java.util.HashMap;
import java.util.Map;

import static com.ssafy.undaied.socket.common.constant.EventType.*;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.LOBBY_ROOM;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.ROOM_KEY_PREFIX;

@Component
@Slf4j
@RequiredArgsConstructor
public class ChatHandler {

//    private final SocketIOServer server;
    private final SocketIONamespace namespace;  // 추가
    private final ChatService chatService;

    @PostConstruct
    private void init() {
        namespace.addEventListener(ROOM_CHAT_EMIT.getValue(), RoomChatRequestDto.class,
                (client, data, ackRequest) -> {
                    try {
                        // 방 채팅.
                        RoomChatResponseDto roomChat =  chatService.roomChat(data, client);
                        log.info("Room chat - RoomId: {}, userId: {}, nickname: {}, message: {}", data.getRoomId(), client.get("userId"), client.get("nickname"), data.getMessage());

                        // 채팅을 보낸 클라이언트에게 전송
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("errorMessage", null);
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                        }

                        String key = ROOM_KEY_PREFIX + data.getRoomId();
                        namespace.getRoomOperations(key).sendEvent(ROOM_CHAT_SEND.getValue(), roomChat);

                    } catch (Exception e) {
                        log.error("Room chat failed: {}", e.getMessage());
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", false);
                            response.put("errorMessage", e.getMessage());
                            response.put("data", null);
                            ackRequest.sendAckData(response);
                        }
                    }
                }
        );

        namespace.addEventListener(LOBBY_CHAT.getValue(), LobbyChatRequestDto.class,
                (client, data, ackRequest) -> {
                try {
                    RoomChatResponseDto loobyChat = chatService.lobbyChat(data, client);
                    log.info("lobby chat - userId: {}, nickname: {}, message: {}", client.get("userId"), client.get("nickname"), data.getMessage());

                    // 채팅을 보낸 클라이언트에게 전송
                    if (ackRequest.isAckRequested()) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("errorMessage", null);
                        response.put("data", null);
                        ackRequest.sendAckData(response);
                    }

                    namespace.getRoomOperations(LOBBY_ROOM).sendEvent(LOBBY_CHAT.getValue(), loobyChat);

                } catch (Exception e) {
                    log.error("Lobby chat failed: {}", e.getMessage());
                    if (ackRequest.isAckRequested()) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", false);
                        response.put("errorMessage", e.getMessage());
                        response.put("data", null);
                        ackRequest.sendAckData(response);
                    }
                }

            }
        );
    }

}
