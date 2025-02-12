package com.ssafy.undaied.socket.init.handler;

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

import static com.ssafy.undaied.socket.common.constant.SocketRoom.ROOM_KEY_PREFIX;

@Component
@RequiredArgsConstructor
@Slf4j
public class GameInitHandler {

    private final SocketIONamespace namespace;
    private final GameInitService gameInitService;
    private final RedisTemplate<String, String> redisTemplate;
    private final Map<Integer, Boolean> gameInitializationStatus = new ConcurrentHashMap<>();

    @PostConstruct
    public void init() {
        namespace.addEventListener("game:init", Object.class, (client, data, ackRequest) -> {
            try {
                log.info("Game init request received.");

                int roomId=456;

                log.info("Game initialization requested - roomId: {}", roomId);
                log.info("Game initialization requested - roomId: {}", roomId);

                // ✅ 중복 체크
                if (gameInitializationStatus.putIfAbsent(roomId, true) != null) {
                    throw new SocketException(SocketErrorCode.GAME_ALREADY_INITIALIZING);
                }

                try {
                    // ✅ 게임 시작 처리
                    GameInfoResponseDto gameInfo = gameInitService.startGame(client, roomId);

                    // ✅ 성공 응답 전송
                    if (ackRequest.isAckRequested()) {
                        Map<String, Object> response = new HashMap<>();
                        response.put("success", true);
                        response.put("errorMessage", null);
                        response.put("data", gameInfo);
                        ackRequest.sendAckData(response);
                    }

                } finally {
                    // ✅ 초기화 상태 해제
                    gameInitializationStatus.remove(roomId);
                }

            } catch (SocketException e) {
                log.error("Failed to initialize game: {}", e.getMessage());
                if (ackRequest.isAckRequested()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("errorMessage", e.getMessage());
                    response.put("data", null);
                    ackRequest.sendAckData(response);
                }

            } catch (Exception e) {
                log.error("Unexpected error during game initialization: {}", e.getMessage(), e);
                if (ackRequest.isAckRequested()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("errorMessage", e.getMessage());
                    response.put("data", null);
                    ackRequest.sendAckData(response);
                }
            }
        });
    }

    @PostConstruct
    public void gameInfo() {
        namespace.addEventListener("game:info", Integer.class, (client, gameId, ackRequest) -> {
            try {
                // 1. 게임 ID 검증
                if (gameId == null) {
                    throw new SocketException(SocketErrorCode.GAME_NOT_FOUND);
                }

                log.info("Game info requested - gameId: {}", gameId);

                // 2. 서비스에서 플레이어 정보 조회
                GameInfoResponseDto players = gameInitService.createGameInfoResponse(gameId);

                // 3. 성공 응답 전송
                if (ackRequest.isAckRequested()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", true);
                    response.put("errorMessage", null);
                    response.put("data", players);
                    ackRequest.sendAckData(response);
                }

            } catch (SocketException e) {
                log.error("Failed to retrieve game info: {}", e.getMessage());
                if (ackRequest.isAckRequested()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("errorMessage", e.getMessage());
                    response.put("data", null);
                    ackRequest.sendAckData(response);
                }

            } catch (Exception e) {
                log.error("Unexpected error while retrieving game info: {}", e.getMessage(), e);
                if (ackRequest.isAckRequested()) {
                    Map<String, Object> response = new HashMap<>();
                    response.put("success", false);
                    response.put("errorMessage", "Unexpected error occurred");
                    response.put("data", null);
                    ackRequest.sendAckData(response);
                }
            }
        });
    }
}

