package com.ssafy.undaied.socket.common.handler;

import com.corundumstudio.socketio.HandshakeData;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.listener.DataListener;
import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.socket.room.service.RoomService;
import com.ssafy.undaied.domain.user.entity.Users;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.socket.common.service.SocketAuthenticationService;
import com.ssafy.undaied.socket.common.service.SocketDisconnectService;
import com.ssafy.undaied.socket.lobby.service.LobbyService;
import com.ssafy.undaied.socket.stage.service.StageService;
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

    private final SocketIONamespace namespace;
    private final SocketAuthenticationService authenticationService;
    private final SocketDisconnectService disconnectService;
    private final RoomService roomService;
    private final LobbyService lobbyService;
    private final UserRepository userRepository;

    @PostConstruct
    private void init() {

        namespace.addConnectListener(listenConnected());
        namespace.addDisconnectListener(listenDisconnected());
        namespace.addEventListener("pre-disconnect", Object.class, handlePreDisconnect());

    }

    /**
     * 클라이언트 연결 리스너
     */
    public ConnectListener listenConnected() {
        return (client) -> {

            String namespace = client.getNamespace().getName();
            log.debug("클라이언트가 {} 네임스페이스로 소켓 연결 시도 중", namespace);

            // 더 자세한 디버깅을 위한 로그
            SocketIONamespace clientNamespace = client.getNamespace();
            String namespaceName = clientNamespace != null ? clientNamespace.getName() : "null";
//            log.info("Client namespace object: {}", clientNamespace);
//            log.info("Client attempting to connect to namespace: '{}'", namespaceName);


            try {
                // null 체크를 포함한 네임스페이스 검증
                if (clientNamespace == null || !"/socket.io".equals(namespaceName)) {
                    log.error("네임스페이스가 비어있거나 /socket.io가 아니라서 연결에 실패했습니다. - 현재 네임스페이스 {}, 클라이언트 세션아이디: {}", namespaceName, client.getSessionId());
                    throw new BaseException(SOCKET_CONNECTION_FAILED);
                }

                // 디버깅을 위한 handshake 데이터 출력
                HandshakeData handshakeData = client.getHandshakeData();
//                log.debug("Connection attempt - Query params: {}", handshakeData.getUrlParams());
//                log.debug("Connection attempt - Request URI: {}", handshakeData.getUrl());
//                log.debug("Connection attempt - Address: {}", handshakeData.getAddress());
//                log.debug("Connection attempt - HTTP Headers: {}", handshakeData.getHttpHeaders());

                // 클라이언트 인증
                int userId;
                try {
                    log.debug("클라이언트 인증 시도 중");
                    userId = authenticationService.authenticateClient(client);
                    log.debug("세션 연결을 위한 인증에 성공했습니다. - userId: {}", userId);
                } catch (Exception e) {
                    log.error("세션 연결에 실패했습니다.: ", e);
                    throw new BaseException(SOCKET_CONNECTION_FAILED);
                }

                // 사용자 조회
                Users user;
                try {
                    user = userRepository.findById(userId)
                            .orElseThrow(() -> new BaseException(USER_NOT_FOUND));
                    log.debug("소켓 클라이언트 데이터 설정을 위한 유저 데이터를 성공적으로 찾았습니댜. - userNickname: {}", user.getNickname());
                } catch (Exception e) {
                    log.error("소켓 클라이언트 데이터 설정을 위한 유저 데이터를 찾는데 실패했습니다.: ", e);
                    throw new BaseException(USER_NOT_FOUND);
                }

                // 클라이언트 데이터 설정
                client.set("userId", userId);
                client.set("nickname", user.getNickname());
                client.set("profileImage", user.getProfileImage());

                roomService.clientLeaveAllRooms(client);
                log.info("클라이언트가 모든 방에서 성공적으로 나가졌습니다. - userId: {}", userId);

                lobbyService.joinLobby(client);

                log.info("소켓 연결 성공 - userId: {}", userId);

            } catch (Exception e) {
                log.error("소켓 연결 실패: ", e);
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
                log.debug("클라이언트가 소켓 연결 해제 시도 중 - eventType: disconnect");
                Integer userId = client.get("userId");

                // 바로 room 처리를 수행
                roomService.clientLeaveAllRooms(client);

                log.info("성공적으로 소켓 연결 해제 - userId: {}, sessionId: {}",
                        userId, client.getSessionId());
            } catch (Exception e) {
                log.error("클라이언트 연결 해제 실패");
                throw new BaseException(SOCKET_EVENT_ERROR);
            }
        };
    }

    private DataListener<Object> handlePreDisconnect() {
        return (client, data, ackRequest) -> {
            try {
                log.debug("클라이언트 pre-disconnect 이벤트 처리 시작");
                Integer userId = client.get("userId");

                // 방 나가기 처리 및 알림 전송
                roomService.clientLeaveAllRooms(client);

            } catch (Exception e) {
                log.error("pre-disconnect 처리 실패", e);
                throw new BaseException(SOCKET_EVENT_ERROR);
            }
        };
    }
}