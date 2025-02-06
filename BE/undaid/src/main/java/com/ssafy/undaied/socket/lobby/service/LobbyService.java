package com.ssafy.undaied.socket.lobby.service;

import com.corundumstudio.socketio.SocketIOClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.LOBBY_ROOM;

@Service
@Slf4j
@RequiredArgsConstructor
public class LobbyService {
    /**
     * 클라이언트를 로비에 입장시킵니다.
     */
    public void joinLobby(SocketIOClient client, int userId) {
        client.joinRoom(LOBBY_ROOM);
        log.info("User {} (sessionId: {}) joined lobby", userId, client.getSessionId());
    }

    /**
     * 클라이언트를 로비에서 퇴장시킵니다.
     */
    public void leaveLobby(SocketIOClient client) {
        client.leaveRoom(LOBBY_ROOM);
        log.info("User {} (sessionId: {}) left lobby", client.get("userId"), client.getSessionId());
    }

    public boolean isUserInLobby(SocketIOClient client) {
        Set<String> rooms = new HashSet<>(client.getAllRooms()); // 새로운 Set으로 복사
        rooms.remove(""); // 빈 room 제거

        if (rooms.size() != 1 || !rooms.contains(LOBBY_ROOM)) {
            return false;
        }
        return true;
    }

}