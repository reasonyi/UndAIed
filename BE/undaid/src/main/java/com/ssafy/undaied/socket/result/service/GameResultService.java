package com.ssafy.undaied.socket.result.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.result.dto.response.GameResultResponseDto;
import com.ssafy.undaied.socket.result.dto.response.PlayerResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.GAME_KEY_PREFIX;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameResultService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final SocketIOServer socketIOServer;
    private static final String ROOM_LIST = "rooms:";

    public GameResultResponseDto checkGameResult(SocketIOClient client, int gameId) throws SocketException {
        String gameKey = GAME_KEY_PREFIX + gameId;
        String statusKey = GAME_KEY_PREFIX + gameId + ":player_status";
        String aiKey = GAME_KEY_PREFIX + gameId + ":ai_numbers";
        String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
        String userNicknameKey = GAME_KEY_PREFIX + gameId + ":user_nicknames";

        Map<Object, Object> playerStatus = redisTemplate.opsForHash().entries(statusKey);
        Set<String> aiNumbers = redisTemplate.opsForSet().members(aiKey);

        List<String> humanNumbers = playerStatus.keySet().stream()
                .map(Object::toString)
                .filter(number -> !aiNumbers.contains(number))
                .collect(Collectors.toList());

        boolean isHumanDefeated = isHumanDefeated(playerStatus, humanNumbers);
        boolean isAIDefeated = isAIDefeated(playerStatus, aiNumbers);

        String winner;
        String message;
        if (isHumanDefeated) {
            winner = "AI";
            message = "AI 승리!";
        } else if (isAIDefeated) {
            winner = "HUMAN";
            message = "HUMAN 승리!";
        } else {
            throw new SocketException(SocketErrorCode.GAME_NOT_ENDED);
        }

        updateGameEndStatus(gameId, winner);

        GameResultResponseDto responseDto = createGameResultResponse(gameId, winner, message,
                playerStatus, mappingKey, userNicknameKey);
        socketIOServer.getRoomOperations(GAME_KEY_PREFIX + gameId)
                .sendEvent("game:result", responseDto);

        log.info("Game result broadcast - gameId: {}, winner: {}", gameId, winner);

        return responseDto;  // ✅ 게임 결과를 반환하도록 변경
    }

    private boolean isHumanDefeated(Map<Object, Object> playerStatus, List<String> humanNumbers) {
        return humanNumbers.stream()
                .allMatch(number -> {
                    String status = playerStatus.get(number).toString();
                    if (!status.contains("isInGame=true")) {
                        return false;
                    }
                    return status.contains("isDied=true") || status.contains("isInfected=true");
                });
    }

    private boolean isAIDefeated(Map<Object, Object> playerStatus, Set<String> aiNumbers) {
        return aiNumbers.stream()
                .allMatch(number -> {
                    String status = playerStatus.get(number).toString();
                    return status.contains("isDied=true") && status.contains("isInGame=true");
                });
    }

    private void updateGameEndStatus(int gameId, String winner) {
        String gameKey = GAME_KEY_PREFIX + gameId;
        LocalDateTime endedAt = LocalDateTime.now();
        String startedAtStr = jsonRedisTemplate.opsForHash().get(gameKey, "startedAt").toString();
        LocalDateTime startedAt = LocalDateTime.parse(startedAtStr);

        long seconds = ChronoUnit.SECONDS.between(startedAt, endedAt);
        String playtime = String.format("%02d:%02d", seconds / 60, seconds % 60);

        Map<String, Object> updates = new HashMap<>();
        updates.put("status", "ENDED");
        updates.put("endedAt", endedAt.toString());
        updates.put("playtime", playtime);
        updates.put("humanWin", winner.equals("HUMAN"));

        jsonRedisTemplate.opsForHash().putAll(gameKey, updates);
    }

    private GameResultResponseDto createGameResultResponse(int gameId, String winner, String message,
                                                           Map<Object, Object> playerStatus,
                                                           String mappingKey, String userNicknameKey) {
        List<PlayerResultDto> players = new ArrayList<>();

        Map<Object, Object> numberToUserMapping = redisTemplate.opsForHash().entries(mappingKey);
        Map<String, String> reverseMapping = new HashMap<>();
        numberToUserMapping.forEach((userId, number) ->
                reverseMapping.put(number.toString(), userId.toString()));

        playerStatus.forEach((number, status) -> {
            String statusStr = status.toString();
            String userId = reverseMapping.get(number.toString());
            String nickname = redisTemplate.opsForHash().get(userNicknameKey, userId).toString();

            players.add(PlayerResultDto.builder()
                    .number(Integer.parseInt(number.toString()))
                    .nickname(nickname)
                    .isDied(statusStr.contains("isDied=true"))
                    .isInfected(statusStr.contains("isInfected=true"))
                    .isInGame(statusStr.contains("isInGame=true"))
                    .build());
        });

        players.sort(Comparator.comparingInt(PlayerResultDto::getNumber));

        return GameResultResponseDto.builder()
                .winner(winner)
                .message(message)
                .players(players)
                .build();
    }

    public boolean movePlayersToRoom(SocketIOClient client, int gameId) {
        // 🔹 Redis에서 roomId 가져오기
        String roomKey = GAME_KEY_PREFIX + gameId + ":roomId";
        String roomIdStr = redisTemplate.opsForValue().get(roomKey);

        if (roomIdStr != null) {
            int roomId = Integer.parseInt(roomIdStr);

            // 🔹 게임 방에서 나가기
            client.leaveRoom(GAME_KEY_PREFIX + gameId);
            log.info("User {} left game room: game:{}", client.get("userId"), gameId);

            // 🔹 원래 방(room:{roomId})으로 복귀
            client.joinRoom(ROOM_LIST + roomId);
            log.info("User {} joined back to room: room:{}", client.get("userId"), roomId);
            return true;
        }
        return false;
    }


}
