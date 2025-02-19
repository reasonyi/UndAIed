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
import com.ssafy.undaied.socket.init.dto.response.NumberResponseDto;
import com.ssafy.undaied.socket.init.dto.response.PlayerInfoDto;
import com.ssafy.undaied.socket.init.service.GameInitService;
import com.ssafy.undaied.socket.lobby.dto.response.LobbyUpdateResponseDto;

import com.ssafy.undaied.socket.stage.service.StageService;
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
    private final StageService stageService;

    @PostConstruct
    public void init() {
        namespace.addEventListener("game:init:emit", Object.class, (client, data, ackRequest) -> {
            try {
                // 클라이언트가 속한 방 찾기
                Set<String> rooms = client.getAllRooms();
                String roomKey = rooms.stream()
                        .filter(room -> room.startsWith(ROOM_KEY_PREFIX))
                        .findFirst()
                        .orElseThrow(() -> new SocketException(SocketErrorCode.ROOM_NOT_FOUND));

                // roomKey에서 roomId 추출 (예: "room:456" -> 456)
                int roomId = Integer.parseInt(roomKey.substring(ROOM_KEY_PREFIX.length()));
                log.info("방 번호 확인 roomId: {}", roomId);
                if (gameInitializationStatus.putIfAbsent(roomId, true) != null) {
                    throw new SocketException(SocketErrorCode.GAME_ALREADY_INITIALIZING);
                }

                try {
                    int gameId = gameInitService.startGame(client, roomId);

                    // ✅ 로비 업데이트 이벤트 전송
                    LobbyUpdateResponseDto updateResponseDto = gameInitService.createLobbyUpdateResponse(roomId);
                    namespace.getRoomOperations(LOBBY_ROOM)
                            .sendEvent(UPDATE_ROOM_AT_LOBBY.getValue(), updateResponseDto);
                    log.info("방 목록 업데이트 및 대기방 삭제");

                    gameInitService.broadcastGameInit(gameId);

                    sendResponse(ackRequest, true, null);

                    log.info("게임 시작");
                    stageService.handleGameStart(gameId);
                } finally {
                    gameInitializationStatus.remove(roomId);
                }

            } catch (SocketException e) {
                log.error("Failed to initialize game: {}", e.getMessage());
                sendResponse(ackRequest, false, e.getMessage());  // null 사용
            } catch (Exception e) {
                log.error("Unexpected error during game initialization: {}", e.getMessage(), e);
                sendResponse(ackRequest, false, e.getMessage());  // null 사용
            }
        });

        // Handle game info requests
        namespace.addEventListener("game:info:emit", Object.class, (client, data, ackRequest) -> {
            try {
                Integer gameId = client.get("gameId");
                if (gameId == null) {
                    throw new SocketException(SocketErrorCode.GAME_NOT_FOUND);
                }

                Integer userId = client.get("userId");
                if (userId == null) {
                    throw new SocketException(SocketErrorCode.SOCKET_AUTHENTICATION_FAILED);
                }

                // 🔹 Redis에서 userId에 해당하는 number 조회
                String numberMappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
                String assignedNumberStr = (String) redisTemplate.opsForHash().get(numberMappingKey, userId.toString());

                Integer assignedNumber = (assignedNumberStr != null) ? Integer.parseInt(assignedNumberStr) : null;

                // ✅ `NumberResponseDto` 객체 생성
                NumberResponseDto numberResponse = NumberResponseDto.builder()
                        .number(assignedNumber)
                        .build();

                // 🔹 응답 전송 (ACK 응답에 number 포함)
                sendResponse(ackRequest, true, numberResponse);
                gameInitService.sendGameInfo(gameId);

            } catch (SocketException e) {
                log.error("Failed to retrieve game info: {}", e.getMessage());
                sendResponse(ackRequest, false, e.getMessage());
            } catch (Exception e) {
                log.error("Unexpected error while retrieving game info: {}", e.getMessage(), e);
                sendResponse(ackRequest, false, "Unexpected error occurred");
            }
        });
    }

    private void sendResponse(AckRequest ackRequest, boolean success, Object data) {
        if (ackRequest.isAckRequested()) {
            Map<String, Object> response = new HashMap<>();
            response.put("success", success);
            response.put("errorMessage", success ? null : data);
            response.put("data", success ? data : null);  // ✅ 성공 시 `data`에 NumberResponseDto 포함

            // success가 true이고 data가 NumberResponseDto인 경우에만 number 추출
            if (success && data instanceof NumberResponseDto) {
                response.put("number", ((NumberResponseDto) data).getNumber());
            } else {
                response.put("number", null);
            }

            ackRequest.sendAckData(response);
        }
    }
}

