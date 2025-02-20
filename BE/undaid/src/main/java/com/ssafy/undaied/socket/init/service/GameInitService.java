package com.ssafy.undaied.socket.init.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.common.util.GameTimer;
import com.ssafy.undaied.socket.init.dto.response.*;
import com.ssafy.undaied.socket.lobby.dto.response.LobbyUpdateResponseDto;
import com.ssafy.undaied.socket.lobby.dto.response.UpdateData;
import com.ssafy.undaied.socket.room.dto.Room;
import com.ssafy.undaied.socket.room.dto.RoomUser;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameInitService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final ObjectMapper objectMapper;
    private final SocketIONamespace namespace;
    private final GameTimer gameTimer;
    private static final long EXPIRE_TIME = 7200;
    private static final int REQUIRED_PLAYERS = 6;
    private static final int TOTAL_NUMBERS = 8;

    // AI 후보 리스트
    private static final Map<String, String> AI_POOL = new HashMap<>() {{
        put("2", "gemini");
        put("3", "chatgpt");
    }};

    public int startGame(SocketIOClient client, int roomId) throws SocketException {
        Integer userId = validateAuthentication(client);
        Room room = getRoomInfo(roomId);

        if (!isHost(userId, room)) {
            throw new SocketException(SocketErrorCode.NOT_HOST);
        }

        List<Integer> players = room.getCurrentPlayers().stream()
                .map(RoomUser::getUserId)
                .collect(Collectors.toList());

        validatePlayers(players);

        int gameId = initGame();
        saveGameRoomMapping(gameId, roomId);

        // ✅ AI 2개 선택 후 저장
        List<String> selectedAIs = selectTwoAIs();
        savePlayersAndAssignNumbers(gameId, players, room, selectedAIs);

        handleSocketConnections(gameId, roomId);

        // AI 요청 스케줄링 시작
        log.info("AI 요청 시작: {}", gameId);

        return gameId;
    }

    // AI 3개 중 2개를 랜덤 선택하는 메서드
    private List<String> selectTwoAIs() {
        List<String> aiIds = new ArrayList<>(AI_POOL.keySet()); // AI key(ai1, ai2, ai3) 리스트 가져오기
        Collections.shuffle(aiIds); // 랜덤 섞기
        return aiIds.subList(0, 2); // 앞에서 2개 선택
    }

    private Integer validateAuthentication(SocketIOClient client) throws SocketException {
        Integer userId = client.get("userId");
        if (userId == null) {
            throw new SocketException(SocketErrorCode.SOCKET_AUTHENTICATION_FAILED);
        }
        return userId;
    }

    private boolean isHost(Integer userId, Room room) {
        return room.getCurrentPlayers().stream()
                .anyMatch(player -> player.getUserId().equals(userId) && Boolean.TRUE.equals(player.getIsHost()));
    }

    private Room getRoomInfo(int roomId) throws SocketException {
        String roomKey = ROOM_LIST+ ROOM_KEY_PREFIX + roomId;
        Object roomObj = jsonRedisTemplate.opsForValue().get(roomKey);

        if (roomObj == null) {
            throw new SocketException(SocketErrorCode.ROOM_NOT_FOUND);
        }

        Room room = objectMapper.convertValue(roomObj, Room.class);
        if (room.getCurrentPlayers() == null || room.getCurrentPlayers().isEmpty()) {
            throw new SocketException(SocketErrorCode.INVALID_PLAYER_COUNT);
        }

        return room;
    }

    private void validatePlayers(List<Integer> players) throws SocketException {

        if (players.size() != REQUIRED_PLAYERS) {
            throw new SocketException(SocketErrorCode.INVALID_PLAYER_COUNT);
        }
    }

    private int initGame() {
        String gameCounterKey = GAME_KEY_PREFIX + "counter";
        Long gameId = redisTemplate.opsForValue().increment(gameCounterKey);
        String gameKey = GAME_KEY_PREFIX + gameId;

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
        String mappingKey = GAME_KEY_PREFIX + gameId + ":roomId";
        redisTemplate.opsForValue().set(mappingKey, String.valueOf(roomId));
        redisTemplate.expire(mappingKey, EXPIRE_TIME, TimeUnit.SECONDS);
    }

    private void handleSocketConnections(int gameId, int roomId) {
        String roomKey = ROOM_LIST+ ROOM_KEY_PREFIX + roomId;
        Object roomObj = jsonRedisTemplate.opsForValue().get(roomKey);

        if (roomObj == null) {
            log.error("Room not found in Redis - roomId: {}", roomId);
            return;
        }

        Room room = objectMapper.convertValue(roomObj, Room.class);

        if (room.getCurrentPlayers() != null && !room.getCurrentPlayers().isEmpty()) {
            List<Integer> roomUserIds = room.getCurrentPlayers().stream()
                    .map(RoomUser::getUserId)
                    .collect(Collectors.toList());

            namespace.getAllClients().forEach(client -> {
                Integer clientUserId = client.get("userId");
                if (clientUserId != null && roomUserIds.contains(clientUserId)) {

                    client.getAllRooms().forEach(client::leaveRoom);
                    client.set("gameId", gameId);
                    client.joinRoom(GAME_KEY_PREFIX + gameId);
                    log.info("Player joined game room - userId: {}, gameId: {}, nickname: {}",
                            clientUserId,
                            gameId,
                            room.getCurrentPlayers().stream()
                                    .filter(user -> user.getUserId().equals(clientUserId))
                                    .map(RoomUser::getNickname)
                                    .findFirst()
                                    .orElse("Unknown")
                    );
                }
            });
        } else {
            log.warn("No players found in room - roomId: {}", roomId);
        }
    }

    private void savePlayersAndAssignNumbers(int gameId, List<Integer> players, Room room, List<String> selectedAIs) {
        List<Integer> availableNumbers = IntStream.rangeClosed(1, TOTAL_NUMBERS)
                .boxed()
                .collect(Collectors.toList());
        Collections.shuffle(availableNumbers);

        // Redis 키 준비
        String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
        String playersKey = GAME_KEY_PREFIX + gameId + ":players";
        String statusKey = GAME_KEY_PREFIX + gameId + ":player_status";
        String userNicknameKey = GAME_KEY_PREFIX + gameId + ":user_nicknames";
        String numberNicknameKey = GAME_KEY_PREFIX + gameId + ":number_nicknames";
        String aiKey = GAME_KEY_PREFIX + gameId + ":ai_numbers";

        // 실제 플레이어 닉네임 매핑
        Map<Integer, String> userNicknames = room.getCurrentPlayers().stream()
                .collect(Collectors.toMap(
                        RoomUser::getUserId,
                        RoomUser::getNickname
                ));

        // 실제 플레이어 할당
        for (int i = 0; i < REQUIRED_PLAYERS; i++) {
            Integer userId = players.get(i);
            Integer assignedNumber = availableNumbers.remove(0);

            redisTemplate.opsForHash().put(mappingKey, userId.toString(), assignedNumber.toString());
            redisTemplate.opsForSet().add(playersKey, userId.toString());

            savePlayerStatus(statusKey, assignedNumber.toString(), false, true);
            String nickname = userNicknames.get(userId);
            redisTemplate.opsForHash().put(userNicknameKey, userId.toString(), nickname);
            redisTemplate.opsForHash().put(numberNicknameKey, assignedNumber.toString(), nickname);
        }
        for (String aiIdStr : selectedAIs) {
            int originalAiId = Integer.parseInt(aiIdStr);
            int negativeAiId = -originalAiId;  // AI ID를 음수로 변환
            int aiNumber = availableNumbers.remove(0);
            String AIName = AI_POOL.get(String.valueOf(originalAiId));


            // Redis에 음수 AI ID로 정보 저장
            redisTemplate.opsForHash().put(mappingKey, String.valueOf(negativeAiId), String.valueOf(aiNumber));
            redisTemplate.opsForHash().put(userNicknameKey, String.valueOf(negativeAiId), AIName);
            redisTemplate.opsForHash().put(numberNicknameKey, String.valueOf(aiNumber), AIName);
            savePlayerStatus(statusKey, String.valueOf(aiNumber), false, true);

            // AI 번호를 Redis Set에 추가
            redisTemplate.opsForSet().add(aiKey, String.valueOf(aiNumber));
        }

        // Redis 키 만료시간 설정
        Arrays.asList(mappingKey, playersKey, statusKey, userNicknameKey, numberNicknameKey)
                .forEach(key -> redisTemplate.expire(key, EXPIRE_TIME, TimeUnit.SECONDS));
    }

    private void savePlayerStatus(String statusKey, String number, boolean isDied, boolean isInGame) {
        Map<String, String> status = new HashMap<>();
        status.put("number", number);
        status.put("isDied", String.valueOf(isDied));
        status.put("isInGame", String.valueOf(isInGame));
        redisTemplate.opsForHash().put(statusKey, number, status.toString());
    }


    // 게임 정보를 특정 요청에 대한 응답으로 전송
    public void sendGameInfo(Integer gameId) {

        GameInfoResponseDto gameInfo=createGameInfoResponse(gameId);
        // 2. 다른 모든 클라이언트에게도 최신 정보 브로드캐스트
        namespace.getRoomOperations(GAME_KEY_PREFIX + gameId)
                .sendEvent("game:info:send", gameInfo);
    }

    public void broadcastGameInit(Integer gameId) {

        // BroadcastResponseDto 객체 생성
        BroadcastResponseDto responseDto = BroadcastResponseDto.builder()
            .gameId(gameId)
            .build();

        // 연결된 클라이언트 확인
        Collection<SocketIOClient> clients = namespace.getRoomOperations(GAME_KEY_PREFIX + gameId).getClients();
        log.debug("Clients in game room {}: {}", GAME_KEY_PREFIX + gameId, clients.size());
        
        namespace.getRoomOperations(GAME_KEY_PREFIX + gameId)
                .sendEvent(EventType.GAME_INIT_SEND.getValue(), responseDto);
    }

    public void updatePlayerStatus(int gameId, int number, boolean isDied, boolean isInGame) {
        savePlayerStatus(GAME_KEY_PREFIX + gameId + ":player_status",
                String.valueOf(number), isDied, isInGame);
    }

    public GameInfoResponseDto createGameInfoResponse(int gameId) {
        String statusKey = GAME_KEY_PREFIX + gameId + ":player_status";
        Map<Object, Object> allStatus = redisTemplate.opsForHash().entries(statusKey);

        // round 값 처리
        String roundKey = "game:" + gameId + ":round";
        String roundValue = redisTemplate.opsForValue().get(roundKey);
        int round =Integer.parseInt(roundValue);

        // stage 값 처리
        String stageKey = "game:" + gameId + ":stage";
        String stageValue = redisTemplate.opsForValue().get(stageKey);
        String stage = (stageValue == null) ? "start" : stageValue;

        Integer remainingTime = gameTimer.getRemainingTime(gameId);
        log.debug("타이머 동작 확인: gameId={}, remainingTime={}", gameId, remainingTime);

        List<PlayerInfoDto> players = allStatus.entrySet().stream()
                .map(entry -> {
                    String status = entry.getValue().toString();
                    return PlayerInfoDto.builder()
                            .number(Integer.parseInt(entry.getKey().toString()))
                            .isDied(status.contains("isDied=true"))
                            .isInGame(status.contains("isInGame=true"))
                            .build();
                })
                .sorted(Comparator.comparingInt(PlayerInfoDto::getNumber))
                .collect(Collectors.toList());

        return GameInfoResponseDto.builder()
                .gameId(gameId)
                .round(round)
                .stage(stage)
                .timer(remainingTime)
                .players(players)
                .build();
    }

    public LobbyUpdateResponseDto createLobbyUpdateResponse(int roomId) throws SocketException {
        String roomKey = ROOM_LIST + ROOM_KEY_PREFIX + roomId;
        Object roomObj = jsonRedisTemplate.opsForValue().get(roomKey);
        Room room = objectMapper.convertValue(roomObj, Room.class);

        if (room == null) {
            throw new SocketException(SocketErrorCode.ROOM_NOT_FOUND);
        }

            // waiting 리스트에서 해당 방 제거
        String waitingKey = WAITING_LIST + ROOM_KEY_PREFIX + roomId;  // "waiting:room:roomId" 형식
        Boolean isDeleted = jsonRedisTemplate.delete(waitingKey);
        log.debug("레디스 대기방 목록에서 방을 제거합니다. - waitingKey: {}, 제거 성공 여부: {}", waitingKey, isDeleted);

        return LobbyUpdateResponseDto.builder()
                .type("update")
                .data(UpdateData.builder()
                        .roomId(room.getRoomId())
                        .roomTitle(room.getRoomTitle())
                        .isPrivate(room.getIsPrivate())
                        .currentPlayerNum(room.getCurrentPlayers().size())
                        .playing(true)  // 게임 시작했으므로 true로 설정
                        .build())
                .build();
    }

}
