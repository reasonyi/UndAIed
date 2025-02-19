package com.ssafy.undaied.socket.common.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.init.service.GameInitService;
import com.ssafy.undaied.socket.lobby.service.LobbyService;
import com.ssafy.undaied.socket.quit.service.QuitService;
import com.ssafy.undaied.socket.room.service.RoomService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import static com.ssafy.undaied.global.common.exception.ErrorCode.SOCKET_DISCONNECTION_ERROR;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.*;

@Service
@Slf4j
@RequiredArgsConstructor
public class SocketDisconnectService {

    private final RoomService roomService;
    private final QuitService quitService;

    /**
     * 클라이언트 연결 종료 시 정리 작업을 수행합니다.
     */
    public void handleDisconnect(SocketIOClient client) {
        Integer userId = client.get("userId");

        handleGameDisconnect(client);

        // 게임방 및 로비에서 퇴장 처리
        handleRoomDisconnect(client);

        log.info("Client disconnected - userId: {}, sessionId: {}", userId, client.getSessionId());
        client.disconnect();
    }

    /**
     * 클라이언트가 속한 모든 방에서 퇴장 처리합니다.
     */
    private void handleRoomDisconnect(SocketIOClient client) {
        try {
            roomService.clientLeaveAllRooms(client);
        } catch (Exception e) {
            throw new BaseException(SOCKET_DISCONNECTION_ERROR);
        }

    }

    private void handleGameDisconnect(SocketIOClient client){
        try {
            for (String game : client.getAllRooms()) {
                if (game.startsWith(GAME_KEY_PREFIX)) {
                    quitService.handleGameDisconnect(client, game);
                    client.leaveRoom(game);
                }
            }
        } catch (Exception e) {
            throw new BaseException(SOCKET_DISCONNECTION_ERROR);
        }
    }


}