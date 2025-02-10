package com.ssafy.undaied.socket.result.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.result.dto.response.GameResultResponseDto;
import com.ssafy.undaied.socket.result.dto.response.ParticipantResultDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameResultService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final SocketIOServer socketIOServer;

    public void checkGameResult(SocketIOClient client, int gameId) throws SocketException {
        String gameKey = "game:" + gameId;
        String statusKey = "game:" + gameId + ":player_status";
        String aiKey = "game:" + gameId + ":ai_numbers";
        String mappingKey = "game:" + gameId + ":number_mapping";
        String userNicknameKey = "game:" + gameId + ":user_nicknames";

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
            return;
        }

        // 게임 종료 시 상태 업데이트
        updateGameEndStatus(gameId, winner);

        GameResultResponseDto responseDto = createGameResultResponse(gameId, winner, message,
                playerStatus, mappingKey, userNicknameKey);
        socketIOServer.getRoomOperations("game:" + gameId)
                .sendEvent("game:result", responseDto);

        log.info("Game result broadcast - gameId: {}, winner: {}", gameId, winner);
    }

    private boolean isHumanDefeated(Map<Object, Object> playerStatus, List<String> humanNumbers) {
        return humanNumbers.stream()
                .allMatch(number -> {
                    String status = playerStatus.get(number).toString();
                    return status.contains("isDied=true") || status.contains("isInfected=true");
                });
    }

    private boolean isAIDefeated(Map<Object, Object> playerStatus, Set<String> aiNumbers) {
        return aiNumbers.stream()
                .allMatch(number -> {
                    String status = playerStatus.get(number).toString();
                    return status.contains("isDied=true");
                });
    }

    private void updateGameEndStatus(int gameId, String winner) {
        String gameKey = "game:" + gameId;
        LocalDateTime endedAt = LocalDateTime.now();
        String startedAtStr = jsonRedisTemplate.opsForHash().get(gameKey, "startedAt").toString();
        LocalDateTime startedAt = LocalDateTime.parse(startedAtStr);

        // 플레이타임 계산 (분:초 형식)
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
        List<ParticipantResultDto> participants = new ArrayList<>();

        Map<Object, Object> numberToUserMapping = redisTemplate.opsForHash().entries(mappingKey);
        Map<String, String> reverseMapping = new HashMap<>();
        numberToUserMapping.forEach((userId, number) ->
                reverseMapping.put(number.toString(), userId.toString()));

        playerStatus.forEach((number, status) -> {
            String statusStr = status.toString();
            String userId = reverseMapping.get(number.toString());
            String nickname = redisTemplate.opsForHash().get(userNicknameKey, userId).toString();

            participants.add(ParticipantResultDto.builder()
                    .number(Integer.parseInt(number.toString()))
                    .nickname(nickname)
                    .isDied(statusStr.contains("isDied=true"))
                    .isInfected(statusStr.contains("isInfected=true"))
                    .build());
        });

        participants.sort(Comparator.comparingInt(ParticipantResultDto::getNumber));

        return GameResultResponseDto.builder()
                .gameId(gameId)
                .winner(winner)
                .message(message)
                .participants(participants)
                .build();
    }
}

