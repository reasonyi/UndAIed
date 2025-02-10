package com.ssafy.undaied.socket.common.handler;

import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.gameChat.handler.GameChatHandler;
import com.ssafy.undaied.socket.stage.handler.StageHandler;
import com.ssafy.undaied.domain.user.entity.Users;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.socket.common.service.SocketAuthenticationService;
import com.ssafy.undaied.socket.common.service.SocketDisconnectService;
import com.ssafy.undaied.socket.lobby.service.LobbyService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.listener.ConnectListener;
import com.corundumstudio.socketio.listener.DisconnectListener;

import lombok.extern.slf4j.Slf4j;

import static com.ssafy.undaied.global.common.exception.ErrorCode.*;

/**
 * SocketIOController.
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class SocketIoHandler {

    private final SocketIOServer server;
    private final SocketAuthenticationService authenticationService;
    private final SocketDisconnectService disconnectService;
    private final LobbyService lobbyService;
    private final StageHandler stageHandler;
    private final UserRepository userRepository;
    private final GameChatHandler gameChatHandler;

    @PostConstruct
    private void init() {
        server.addConnectListener(listenConnected());
        server.addDisconnectListener(listenDisconnected());
        server.addListeners(gameChatHandler);

        addGameStartListeners();
    }

    /**
     * 클라이언트 연결 리스너
     */
    public ConnectListener listenConnected() {
        return (client) -> {
            try {
                // 클라이언트 인증
                int userId = authenticationService.authenticateClient(client);
                Users user = userRepository.findById(userId)
                                .orElseThrow(() -> new BaseException(USER_NOT_FOUND));

                client.set("userId", userId);
                client.set("nickname", user.getNickname());

                // 로비 입장
                lobbyService.joinLobby(client, userId);
            } catch (Exception e) {
                throw new BaseException(SOCKET_CONNECTION_FAILED);
            }
        };
    }

    /**
     * 클라이언트 연결 해제 리스너
     */
    private DisconnectListener listenDisconnected() {
        return client -> {
            try {
                disconnectService.handleDisconnect(client);
            } catch (Exception e) {
                throw new BaseException(SOCKET_EVENT_ERROR);
            }
        };
    }

    public void addGameStartListeners() {
        server.addEventListener(EventType.START_GAME.getValue(), Object.class,
                (client, data, ack) -> {
                    Integer userId = client.get("userId");
                    if (userId == null) {
                        client.sendEvent("error", "UserId is required");
                        return;
                    }
                    Integer gameId = 1; // 테스트를 위해 임시로 1로 지정
//                            client.get("gameId");
                    client.joinRoom(String.valueOf(gameId));
                    stageHandler.handleGameStart(gameId);
                });
    }

}