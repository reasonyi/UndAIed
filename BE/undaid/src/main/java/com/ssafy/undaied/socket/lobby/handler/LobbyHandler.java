package com.ssafy.undaied.socket.lobby.handler;

import com.corundumstudio.socketio.SocketIONamespace;
import com.ssafy.undaied.socket.lobby.dto.response.LobbyRoomListResponseDto;
import com.ssafy.undaied.socket.lobby.dto.response.UpdateData;
import com.ssafy.undaied.socket.lobby.service.LobbyService;
import com.ssafy.undaied.socket.room.service.RoomService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.ssafy.undaied.socket.common.constant.EventType.ROOM_LIST_AT_LOBBY;

@Component
@Slf4j
@RequiredArgsConstructor
public class LobbyHandler {

    private final SocketIONamespace namespace;
    private final RoomService roomService;
    private final LobbyService lobbyService;

    @PostConstruct
    private void init() {
        namespace.addEventListener(ROOM_LIST_AT_LOBBY.getValue(), Object.class,
                (client, data, ackRequest) -> {
                    try {

                        // 방에 있었다면 방에서 전부 나가고 로비에 입장
                        lobbyService.joinLobby(client);

                        LobbyRoomListResponseDto responseData = roomService.findWaitingRoomList();
                        log.info("Successfully find waiting room list");

                        // 로비에 입장한 사용자에게 데이터 전송
                        if (ackRequest.isAckRequested()) {
                            Map<String, Object> response = new HashMap<>();
                            response.put("success", true);
                            response.put("errorMessage", null);
                            response.put("data", responseData);
                            ackRequest.sendAckData(response);
                        }

                    } catch (Exception e) {
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
