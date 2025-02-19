package com.ssafy.undaied.socket.quit.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.init.dto.response.GameInfoResponseDto;
import com.ssafy.undaied.socket.init.service.GameInitService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.GAME_KEY_PREFIX;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.LOBBY_ROOM;

@Service
@RequiredArgsConstructor
@Slf4j
public class QuitService {
    private final RedisTemplate<String, String> redisTemplate;
    private final SocketIOServer socketIOServer;
    private final GameInitService gameInitService;

    //연결 끊겼을 경우
    public void handleGameDisconnect(SocketIOClient client, String gameRoom) throws SocketException {
        try {
            Integer userId = client.get("userId");
            if (userId == null || !gameRoom.startsWith(GAME_KEY_PREFIX)) {
                return;
            }

            // gameRoom에서 gameId 추출 ("game:123" -> 123)
            int gameId = Integer.parseInt(gameRoom.substring(GAME_KEY_PREFIX.length()));
            String playersKey = GAME_KEY_PREFIX + gameId + ":players";
            Boolean isPlayer = redisTemplate.opsForSet().isMember(playersKey, userId.toString());

            if (Boolean.FALSE.equals(isPlayer)) {
                return;
            }

            String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
            String number = redisTemplate.opsForHash().get(mappingKey, userId.toString()).toString();

            // 현재 상태 가져오기
            String currentStatus = redisTemplate.opsForHash()
                    .get(GAME_KEY_PREFIX + gameId + ":player_status", number).toString();
            boolean isDied = currentStatus.contains("isDied=true");
            boolean isInfected = currentStatus.contains("isInfected=true");

            // 플레이어 상태 업데이트 (게임에서 나감)
            gameInitService.updatePlayerStatus(gameId, Integer.parseInt(number), isDied, false);

            // 게임방에서 나가기
            client.leaveRoom("game:" + gameId);
            log.info("User {} left game room: game:{}", userId, gameId);

            // 로비 방으로 이동
            client.joinRoom(LOBBY_ROOM);
            log.info("User {} joined lobby", userId);

            // 게임 정보 업데이트 브로드캐스트
            GameInfoResponseDto gameInfo = gameInitService.createGameInfoResponse(gameId);
            socketIOServer.getRoomOperations(gameRoom).sendEvent("game:info", gameInfo);

            // 퇴장 메시지 전송
            String numberNickname = "익명" + number;
            Map<String, String> chatMessage = Map.of("message", numberNickname + "님의 통신이 끊어졌습니다.");
            socketIOServer.getRoomOperations(gameRoom).sendEvent("game:chat", chatMessage);

            log.info("Player disconnected from game - userId: {}, gameId: {}, playerNumber: {}",
                    userId, gameId, number);

        } catch (Exception e) {
            log.error("Error handling game disconnect for room {}", gameRoom, e);
            throw new SocketException(SocketErrorCode.SOCKET_DISCONNECTION_ERROR);
        }
    }


}