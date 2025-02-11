package com.ssafy.undaied.socket.init.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.init.dto.response.GameInitResponseDto;
import com.ssafy.undaied.socket.init.dto.response.PlayerInfoDto;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameInitService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final SocketIOServer socketIOServer;

    private static final long EXPIRE_TIME = 7200;
    private static final int REQUIRED_PLAYERS = 6;
    private static final int TOTAL_NUMBERS = 8;

    // 테스트 데이터. 나중에 지우려고 함.
    @PostConstruct
    public void initTestData() {
        String roomKey = "room:456:participants";
        redisTemplate.delete(roomKey);
        redisTemplate.opsForSet().add(roomKey,
                "1001", "1002", "1003", "1004", "1005", "1006");

    //     log.info("Test data initialized - roomId: 456, participants: [1001, 1002, 1003, 1004, 1005, 1006]");
    //     log.info("\n" +
    //             "=== Postman 테스트 가이드 ===\n" +
    //             "1. Socket.IO 이벤트명: game:init\n" +
    //             "2. 테스트 데이터:\n" +
    //             "   - roomId: 456\n" +
    //             "   - userId: 1001~1006 중 하나\n" +
    //             "3. 요청 형식:\n" +
    //             "   {\n" +
    //             "     \"roomId\": 456\n" +
    //             "   }\n" +
    //             "=========================");
    }

    public void startGame(SocketIOClient client, int roomId) throws SocketException {
        try { // 예외처리문으로 감쌈
            Integer userId = validateAuthentication(client);
            List<Integer> participants = getRoomParticipants(roomId);
            validateParticipants(participants);

            int gameId = initGame();
            saveGameRoomMapping(gameId, roomId);
            saveParticipantsAndAssignNumbers(gameId, participants);

            handleSocketConnection(client, gameId);
            broadcastGameInit(gameId);

            log.info("Game successfully initialized - gameId: {}, roomId: {}", gameId, roomId);
        } catch (SocketException e) { // 추가된 예외처리 1
            log.error("Socket error occurred: {}", e.getMessage());
            client.sendEvent("error", e.getMessage());
        } catch (Exception e) { // 추가된 예외처리 2
            log.error("Unexpected error occurred: {}", e.getMessage());
            client.sendEvent("error", "An unexpected error occurred");
        }
    }

    private Integer validateAuthentication(SocketIOClient client) throws SocketException {
        Integer userId = client.get("userId");
        if (userId == null) {
            throw new SocketException(SocketErrorCode.SOCKET_AUTHENTICATION_FAILED);
        }
        return userId;
    }

    private List<Integer> getRoomParticipants(int roomId) throws SocketException {
        String roomKey = "room:" + roomId + ":participants";
        Set<String> userIds = redisTemplate.opsForSet().members(roomKey);

        return userIds.stream()
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }

    private void validateParticipants(List<Integer> participants) throws SocketException {
        if (participants.size() != REQUIRED_PLAYERS) {
            throw new SocketException(SocketErrorCode.INVALID_PARTICIPANT_COUNT);
        }
    }

    private int initGame() {
        String gameCounterKey = "game:counter";
        Long gameId = redisTemplate.opsForValue().increment(gameCounterKey);
        String gameKey = "game:" + gameId;

        Map<String, Object> gameData = new HashMap<>();
        gameData.put("status", "PROCESS");
        gameData.put("startedAt", LocalDateTime.now().toString());
        gameData.put("endedAt", "00:00");
        gameData.put("playtime", "00:00");
        gameData.put("humanWin", false);

        jsonRedisTemplate.opsForHash().putAll(gameKey, gameData);
        redisTemplate.expire(gameKey, EXPIRE_TIME, TimeUnit.SECONDS);

        return gameId.intValue();
    }

    private void saveGameRoomMapping(int gameId, int roomId) {
        String mappingKey = "game:" + gameId + ":roomId";
        redisTemplate.opsForValue().set(mappingKey, String.valueOf(roomId));
        redisTemplate.expire(mappingKey, EXPIRE_TIME, TimeUnit.SECONDS);
    }

    private void saveParticipantsAndAssignNumbers(int gameId, List<Integer> participants) {
        List<Integer> availableNumbers = IntStream.rangeClosed(1, TOTAL_NUMBERS)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(availableNumbers);

        String mappingKey = "game:" + gameId + ":number_mapping";
        String participantsKey = "game:" + gameId + ":participants";
        String statusKey = "game:" + gameId + ":player_status";
        String userNicknameKey = "game:" + gameId + ":user_nicknames";
        String numberNicknameKey = "game:" + gameId + ":number_nicknames";

        // 실제 플레이어 할당
        for (int i = 0; i < REQUIRED_PLAYERS; i++) {
            Integer userId = participants.get(i);
            Integer assignedNumber = availableNumbers.remove(0);

            redisTemplate.opsForHash().put(mappingKey, userId.toString(), assignedNumber.toString());
            redisTemplate.opsForSet().add(participantsKey, userId.toString());

            savePlayerStatus(statusKey, assignedNumber.toString(), false, false);
            redisTemplate.opsForHash().put(userNicknameKey, userId.toString(), "User" + userId);
        }

        // AI 플레이어 할당
        String ai1Number = availableNumbers.get(0).toString();
        String ai2Number = availableNumbers.get(1).toString();

        redisTemplate.opsForHash().put(mappingKey, "ai1", ai1Number);
        redisTemplate.opsForHash().put(mappingKey, "ai2", ai2Number);

        String aiKey = "game:" + gameId + ":ai_numbers";
        redisTemplate.opsForSet().add(aiKey, ai1Number, ai2Number);

        savePlayerStatus(statusKey, ai1Number, false, false);
        savePlayerStatus(statusKey, ai2Number, false, false);

        redisTemplate.opsForHash().put(userNicknameKey, "ai1", "AI-1");
        redisTemplate.opsForHash().put(userNicknameKey, "ai2", "AI-2");

        // 기본 닉네임 설정
        createNumberNicknames().forEach(
                (number, nickname) -> redisTemplate.opsForHash().put(numberNicknameKey, number.toString(), nickname));

        // 만료시간 설정
        Arrays.asList(mappingKey, participantsKey, aiKey, userNicknameKey, numberNicknameKey, statusKey)
                .forEach(key -> redisTemplate.expire(key, EXPIRE_TIME, TimeUnit.SECONDS));
    }

    private void savePlayerStatus(String statusKey, String number, boolean isDied, boolean isInfected) {
        Map<String, String> status = new HashMap<>();
        status.put("number", number);
        status.put("isDied", String.valueOf(isDied));
        status.put("isInfected", String.valueOf(isInfected));
        redisTemplate.opsForHash().put(statusKey, number, status.toString());
    }

    private void handleSocketConnection(SocketIOClient client, int gameId) {
        client.set("gameId", gameId);
        client.joinRoom("game:" + gameId);

        // 연결 유지를 위한 ping 이벤트 설정
        // client.sendEvent("ping"); >> 얘는 디버깅용, 나중에 문제생기면 주석 풀어서 테스트트
    }

    private void broadcastGameInit(int gameId) {
        GameInitResponseDto responseDto = getGameInitResponse(gameId);
        socketIOServer.getRoomOperations("game:" + gameId)
                .sendEvent("game:init", responseDto);
    }

    private GameInitResponseDto getGameInitResponse(int gameId) {
        List<PlayerInfoDto> allPlayers = new ArrayList<>();
        String statusKey = "game:" + gameId + ":player_status";

        Map<Object, Object> playerStatus = redisTemplate.opsForHash().entries(statusKey);

        for (Map.Entry<Object, Object> entry : playerStatus.entrySet()) {
            String statusStr = entry.getValue().toString();
            int number = Integer.parseInt(entry.getKey().toString());

            allPlayers.add(PlayerInfoDto.builder()
                    .number(number)
                    .isDied(statusStr.contains("isDied=true"))
                    .isInfected(statusStr.contains("isInfected=true"))
                    .build());
        }

        allPlayers.sort(Comparator.comparingInt(PlayerInfoDto::getNumber));

        return GameInitResponseDto.builder()
                .gameId(gameId)
                .participants(allPlayers)
                .build();
    }

    private Map<Integer, String> createNumberNicknames() {
        return IntStream.rangeClosed(1, TOTAL_NUMBERS)
                .boxed()
                .collect(Collectors.toMap(
                        i -> i,
                        i -> "익명" + i));
    }

    public void updatePlayerStatus(int gameId, int playerNumber, boolean isDied, boolean isInfected) {
        savePlayerStatus("game:" + gameId + ":player_status",
                String.valueOf(playerNumber), isDied, isInfected);
    }
}