package com.ssafy.undaied.socket.common.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.socket.lobby.service.LobbyService;
import com.ssafy.undaied.socket.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.LOBBY_ROOM;

@Service
@Slf4j
@RequiredArgsConstructor
public class SocketDisconnectService {
    private static final String GAME_ROOM_PREFIX = "game_";
    private final RoomService roomService;
    private final LobbyService lobbyService;

    /**
     * 클라이언트 연결 종료 시 정리 작업을 수행합니다.
     */
    public void handleDisconnect(SocketIOClient client) {
        Integer userId = client.get("userId");
        log.info("Starting disconnect process for user: {}", userId);

        try {
            // 게임방 및 로비에서 퇴장 처리
            handleRoomDisconnect(client, userId);
            log.info("Room disconnect handled for user: {}", userId);
        } catch (Exception e) {
            log.error("Error during room disconnect for user: {}", userId, e);
        }

        log.info("Client disconnected - userId: {}, sessionId: {}", userId, client.getSessionId());
        client.disconnect();
    }

    /**
     * 클라이언트가 속한 모든 방에서 퇴장 처리합니다.
     */
    private void handleRoomDisconnect(SocketIOClient client, Integer userId) {
        for (String room : client.getAllRooms()) {
            if (room.startsWith(GAME_ROOM_PREFIX)) {
                roomService.leaveGameRoom(client, room);
            } else if (room.equals(LOBBY_ROOM)) {
                lobbyService.leaveLobby(client);
            }
            client.leaveRoom(room);
        }
    }
}