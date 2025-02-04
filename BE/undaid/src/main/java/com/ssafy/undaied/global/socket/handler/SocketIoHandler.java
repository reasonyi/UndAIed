package com.ssafy.undaied.global.socket.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.ssafy.undaied.global.auth.token.JwtTokenProvider;
import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.global.common.response.ApiResponse;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

import lombok.extern.slf4j.Slf4j;

import static com.ssafy.undaied.global.common.exception.ErrorCode.*;
import static com.ssafy.undaied.global.socket.dto.SocketRoomConstant.GAME_ROOM_PREFIX;
import static com.ssafy.undaied.global.socket.dto.SocketRoomConstant.LOBBY_ROOM;

/**
 * SocketIOController.
 */
@Component
@Slf4j
public class SocketIoHandler {

    private final SocketIOServer server;
    private final JwtTokenProvider jwtTokenProvider;

    /**
     * 소켓 이벤트 리스너 등록
     */
    public SocketIoHandler(SocketIOServer server, JwtTokenProvider jwtTokenProvider) {
        this.server = server;
        this.jwtTokenProvider = jwtTokenProvider;

        // 소켓 이벤트 리스너 등록
        server.addConnectListener(listenConnected());
        server.addDisconnectListener(listenDisconnected());
    }

    /**
     * 클라이언트 연결 리스너
     */
    public ConnectListener listenConnected() {
        return (client) -> {
            try {

                String bearerToken = client.getHandshakeData().getHttpHeaders().get("Authorization");
                String jwt = jwtTokenProvider.resolveToken(bearerToken);

                if(jwt == null) {
                    throw  new BaseException(NOT_AUTHENTICATED);
                }

                if (!jwtTokenProvider.validateToken(jwt)) {
                    throw new BaseException(TOKEN_VALIDATION_FAILED);
                }

                int userId = jwtTokenProvider.getUserIdFromToken(jwt);
                client.set("userId", userId);

                // 로비 방에 입장
                client.joinRoom(LOBBY_ROOM);
                System.out.println("Client authenticated - userId: " + userId + ", sessionId: " + client.getSessionId() + " joined lobby");

            } catch (Exception e) {
                handleSocketIoException(client, new BaseException(SOCKET_CONNECTION_FAILED));
            }
        };
    }

    /**
     * 클라이언트 연결 해제 리스너
     */
    public DisconnectListener listenDisconnected() {
        return client -> {
            Integer userId = client.get("userId");

            // 모든 방에서 나가기 전에 게임방 정보 정리
            for (String room : client.getAllRooms()) {
                if (room.startsWith(GAME_ROOM_PREFIX)) {
//                    gameRoomService.leaveGameRoom(room, userId);
                }
                client.leaveRoom(room);
            }

            System.out.println("Client disconnected - userId: " + userId + ", sessionId: " + client.getSessionId());
            client.disconnect();
        };
    }

    private void handleSocketIoException(SocketIOClient client, BaseException e) {
        ApiResponse errorResponse = ApiResponse.of(e.getErrorCode());
        // 클라이언트에게 에러 응답 전송
        client.sendEvent("error", errorResponse);
//        log.error("Socket authentication failed: {} - {}",
//                e.getErrorCode().getHttpStatus(), e.getErrorCode().getMessage());
        client.disconnect();
    }
}