package com.ssafy.undaied.socket.init.handler;

import com.corundumstudio.socketio.AckRequest;
import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.corundumstudio.socketio.annotation.OnEvent;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.init.dto.request.GameInitRequestDto;
import com.ssafy.undaied.socket.init.dto.response.GameInfoResponseDto;
import com.ssafy.undaied.socket.init.dto.response.PlayerInfoDto;
import com.ssafy.undaied.socket.init.service.GameInitService;
import com.ssafy.undaied.socket.lobby.dto.response.LobbyUpdateResponseDto;
import com.ssafy.undaied.socket.stage.handler.StageHandler;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static com.ssafy.undaied.socket.common.constant.EventType.UPDATE_ROOM_AT_LOBBY;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.*;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameInitHandler {


    private final SocketIONamespace namespace;
    private final GameInitService gameInitService;
    private final RedisTemplate<String, String> redisTemplate;
    private final Map<Integer, Boolean> gameInitializationStatus = new ConcurrentHashMap<>();
    private final StageHandler stageHandler;

    @PostConstruct
    public void init() {
        namespace.addEventListener("game:init:emit", Object.class, (client, data, ackRequest) -> {
            try {
                log.info("Game init request received.");

//                // 클라이언트가 속한 방 찾기
//                Set<String> rooms = client.getAllRooms();
//                String roomKey = rooms.stream()
//                        .filter(room -> room.startsWith(ROOM_KEY_PREFIX))
//                        .findFirst()
//                        .orElseThrow(() -> new SocketException(SocketErrorCode.ROOM_NOT_FOUND));
//
//                // roomKey에서 roomId 추출 (예: "room:456" -> 456)
//                int roomId = Integer.parseInt(roomKey.substring(ROOM_KEY_PREFIX.length()));

                //임시
                int roomId=456;

                log.info("Game initialization requested - roomId: {}", roomId);

                if (gameInitializationStatus.putIfAbsent(roomId, true) != null) {
                    throw new SocketException(SocketErrorCode.GAME_ALREADY_INITIALIZING);
                }

                try {
                    int gameId = gameInitService.startGame(client, roomId);

//                    Thread.sleep(100);


                    log.info("Checking ackRequest: {}", ackRequest);
                    stageHandler.handleGameStart(gameId);


                    sendResponse(ackRequest, true, null, gameId);
                    log.info("After sending ACK response");

                    // ✅ 로비 업데이트 이벤트 전송
//                    LobbyUpdateResponseDto updateResponseDto = gameInitService.createLobbyUpdateResponse(roomId);
//                    namespace.getRoomOperations(LOBBY_ROOM)
//                            .sendEvent(UPDATE_ROOM_AT_LOBBY.getValue(), updateResponseDto);

                    gameInitService.broadcastGameInit(gameId);
                    log.info("Game initialization completed - gameId: {}, roomId: {}", gameId, roomId);


                } finally {
                    gameInitializationStatus.remove(roomId);
                }

            } catch (SocketException e) {
                log.error("Failed to initialize game: {}", e.getMessage());
                sendResponse(ackRequest, false, e.getMessage(), null);  // null 사용
            } catch (Exception e) {
                log.error("Unexpected error during game initialization: {}", e.getMessage(), e);
                sendResponse(ackRequest, false, e.getMessage(), null);  // null 사용
            }
        });

        // Handle game info requests
        namespace.addEventListener("game:info", Integer.class, (client, gameId, ackRequest) -> {
            try {
                if (gameId == null) {
                    throw new SocketException(SocketErrorCode.GAME_NOT_FOUND);
                }

                log.info("Game info requested - gameId: {}", gameId);
                GameInfoResponseDto gameInfo = gameInitService.createGameInfoResponse(gameId);
                sendGameInfo(gameId, gameInfo, ackRequest);

            } catch (SocketException e) {
                log.error("Failed to retrieve game info: {}", e.getMessage());
                sendResponse(ackRequest, false, e.getMessage(), null);
            } catch (Exception e) {
                log.error("Unexpected error while retrieving game info: {}", e.getMessage(), e);
                sendResponse(ackRequest, false, "Unexpected error occurred", null);
            }
        });
    }

    // 게임 정보를 특정 요청에 대한 응답으로 전송 (ackRequest가 있는 경우)
    private void sendGameInfo(int gameId, GameInfoResponseDto gameInfo, AckRequest ackRequest) {
        // 1. 요청한 클라이언트에게 응답 전송
        sendResponse(ackRequest, true, null, gameId);

        // 2. 다른 모든 클라이언트에게도 최신 정보 브로드캐스트
        namespace.getRoomOperations(GAME_KEY_PREFIX + gameId)
                .sendEvent("game:info", gameInfo);
    }

    private void sendResponse(AckRequest ackRequest, boolean success, String errorMessage, Integer gameId) {  // Integer로 변경
//        if (ackRequest.isAckRequested())
        {
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("errorMessage", errorMessage);
            response.put("data", gameId);  // gameId가 null이면 그대로 null이 전달됨
            ackRequest.sendAckData(response);
            log.info("📢 Sending ACK Response: {}", response);
        }
    }
}
