package com.ssafy.undaied.socket.common.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIONamespace;
import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.common.response.AckResponse;
import com.ssafy.undaied.socket.stage.handler.StageHandler;
import com.ssafy.undaied.socket.chat.handler.GameChatHandler;
import com.ssafy.undaied.domain.user.entity.Users;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.socket.common.service.SocketAuthenticationService;
import com.ssafy.undaied.socket.common.service.SocketDisconnectService;
import com.ssafy.undaied.socket.lobby.service.LobbyService;
import com.ssafy.undaied.socket.vote.dto.request.VoteSubmitRequestDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteSubmitResponseDto;
import com.ssafy.undaied.socket.vote.service.VoteService;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
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
    private final SocketIONamespace namespace;
    private final SocketAuthenticationService authenticationService;
    private final SocketDisconnectService disconnectService;
    private final LobbyService lobbyService;
    private final StageHandler stageHandler;
    private final UserRepository userRepository;
    private final GameChatHandler gameChatHandler;
    private final VoteService voteService;

    @PostConstruct
    private void init() {

        namespace.addConnectListener(listenConnected());
        namespace.addDisconnectListener(listenDisconnected());

        addGameStartListeners();
    }

    /**
     * 클라이언트 연결 리스너
     */
    /**
     * 클라이언트 연결 리스너
     */
    public ConnectListener listenConnected() {
        return (client) -> {

            String namespace = client.getNamespace().getName();
            log.info("Client attempting to connect to namespace: {}", namespace);

            // 더 자세한 디버깅을 위한 로그
            SocketIONamespace clientNamespace = client.getNamespace();
            String namespaceName = clientNamespace != null ? clientNamespace.getName() : "null";

            log.info("Client namespace object: {}", clientNamespace);
            log.info("Client attempting to connect to namespace: '{}'", namespaceName);


            try {
                // null 체크를 포함한 네임스페이스 검증
                if (clientNamespace == null || !"/socket.io".equals(namespaceName)) {
                    log.info("Connection failed: wrong or empty namespace: '{}' :: Client SessionId: {}",
                            namespaceName, client.getSessionId());
                    throw new BaseException(SOCKET_CONNECTION_FAILED);
                }

                // 디버깅을 위한 handshake 데이터 출력
                HandshakeData handshakeData = client.getHandshakeData();
                log.debug("Connection attempt - Query params: {}", handshakeData.getUrlParams());
                log.debug("Connection attempt - Request URI: {}", handshakeData.getUrl());
                log.debug("Connection attempt - Address: {}", handshakeData.getAddress());
                log.debug("Connection attempt - HTTP Headers: {}", handshakeData.getHttpHeaders());

                // 클라이언트 인증
                int userId;
                try {
                    userId = authenticationService.authenticateClient(client);
                    log.debug("Authentication successful for connection. UserId: {}", userId);
                } catch (Exception e) {
                    log.error("Authentication failed: ", e);
                    throw new BaseException(SOCKET_CONNECTION_FAILED);
                }

                // 사용자 조회
                Users user;
                try {
                    user = userRepository.findById(userId)
                            .orElseThrow(() -> new BaseException(USER_NOT_FOUND));
                    log.debug("User found: {}", user.getNickname());
                } catch (Exception e) {
                    log.error("User lookup failed: ", e);
                    throw new BaseException(USER_NOT_FOUND);
                }

                // 클라이언트 데이터 설정
                client.set("userId", userId);
                client.set("nickname", user.getNickname());
                client.set("profileImage", user.getProfileImage());

                // 로비 입장
                lobbyService.joinLobby(client);

            } catch (Exception e) {
                log.error("Connection error: ", e);
                client.disconnect();
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
        server.addNamespace("/socket.io").addEventListener(EventType.START_GAME.getValue(), Object.class,
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