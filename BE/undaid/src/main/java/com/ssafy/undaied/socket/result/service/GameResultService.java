package com.ssafy.undaied.socket.result.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.undaied.domain.game.entity.GameRecords;
import com.ssafy.undaied.domain.game.entity.Games;
import com.ssafy.undaied.domain.game.entity.Subjects;
import com.ssafy.undaied.domain.game.entity.respository.GameRecordsRepository;
import com.ssafy.undaied.domain.game.entity.respository.GamesRepository;
import com.ssafy.undaied.domain.game.entity.respository.SubjectsRepository;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.result.dto.response.GameResultResponseDto;
import com.ssafy.undaied.socket.result.dto.response.PlayerResultDto;
import com.ssafy.undaied.socket.room.dto.Room;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.*;
import java.util.stream.Collectors;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.*;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.WAITING_LIST;
import static com.ssafy.undaied.socket.common.exception.SocketErrorCode.ROOM_NOT_FOUND;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameResultService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final SocketIOServer socketIOServer;
    private final ObjectMapper objectMapper;
    private final GamesRepository gamesRepository;
    private final GameRecordsRepository gameRecordsRepository;
    private final SubjectsRepository subjectsRepository;

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
                .sendEvent("game:result:send", responseDto);

        log.info("게임 결과 - gameId: {}, winner: {}", gameId, winner);

        return responseDto;  // ✅ 게임 결과를 반환하도록 변경
    }

    private boolean isHumanDefeated(Map<Object, Object> playerStatus, List<String> humanNumbers) {
        return humanNumbers.stream()
                .allMatch(number -> {
                    String status = playerStatus.get(number).toString();
                    if (!status.contains("isInGame=true")) {
                        return false;
                    }
                    return status.contains("isDied=true");
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
            client.joinRoom(ROOM_KEY_PREFIX + roomId);
            log.info("User {} joined back to room: room:{}", client.get("userId"), roomId);
            return true;
        }
        return false;
    }

    // db에 게임 결과 저장하는 메서드.
    public void saveGameResult(int gameId) {
        try {
            log.debug("DB에 게임 결과 저장을 시도하는 중...");

            // games를 먼저 저장.
            String gameKey = GAME_KEY_PREFIX + gameId;  // game:1

            // games를 위한 데이터 레디스에서 불러오기.
            String roomIdKey = GAME_KEY_PREFIX + gameId + ":roomId";    // game:1:roomId
            String roomId = redisTemplate.opsForValue().get(roomIdKey);

            String key = ROOM_KEY_PREFIX + roomId; // room:1
            String roomKey = ROOM_LIST + key;  // "rooms:room:1"

            // Redis에서 rooms: 네임스페이스의 방 정보 조회
            Object roomObj = jsonRedisTemplate.opsForValue().get(roomKey);
            Room room = objectMapper.convertValue(roomObj, Room.class);
            if (room == null) {
                log.error("redis에서 방을 찾을 수 없어 게임 데이터 저장에 실패 - roomId: {}", roomId);
                return;
            }

            // Redis에서 game 조회
            Map<Object, Object> gameData = jsonRedisTemplate.opsForHash().entries(gameKey);
            if (gameData == null) {
                log.error("redis에서 게임 데이터를 찾을 수 없어 저장에 실패 - roomId: {}", roomId);
                return;
            }

            // Games 객체 생성
            Games game = Games.builder()
                    .roomTitle(room.getRoomTitle())
                    .startedAt(LocalDateTime.parse(gameData.get("startedAt").toString()))
                    .endedAt(LocalDateTime.parse(gameData.get("endedAt").toString()))
                    .playTime((String) gameData.get("playtime"))
                    .humanWin((Boolean) gameData.get("humanWin"))
                    .build();

            gamesRepository.save(game);
            log.debug("Games 객체 성공적으로 저장");

            // 레코드마다 저장....

            // 우선 총 라운드를 가져와야됨.
            String gameRoundKey = GAME_KEY_PREFIX + gameId +":round";
            String gameRoundNumStr = redisTemplate.opsForValue().get(gameRoundKey);
            if (gameRoundNumStr == null) {
                log.error("redis에서 게임 총 라운드를 찾을 수 없어 데이터 저장 실패 - roomId: {}", roomId);
                return;
            }
            int gameRoundNum = Integer.parseInt(gameRoundNumStr);

            for (int i = 1; i <= gameRoundNum; i++) {

                // 여기에 subject 찾는 코드.
                String subjectKey = GAME_KEY_PREFIX + gameId + ":round:" + i +":used_subjects";
                String subjectId = redisTemplate.opsForValue().get(subjectKey);

                Subjects subject = subjectsRepository.findById(Integer.parseInt(subjectId))
                        .orElse(null);

                if (subject == null) {
                    log.error("주제를 찾을 수 없어 데이터 저장 실패");
                    return;
                }

                String subjectTalkKey = GAME_KEY_PREFIX + gameId + ":round:" + i +":subjectchats";
                String subjectTalks = redisTemplate.opsForValue().get(subjectTalkKey);

                String freeTalkKey = GAME_KEY_PREFIX + gameId + ":round:" + i +":freechats";
                String freeTalks = redisTemplate.opsForValue().get(freeTalkKey);

                String eventKey = GAME_KEY_PREFIX + gameId + ":round:" + i +":freechats";
                String events = redisTemplate.opsForValue().get(eventKey);

                GameRecords gameRecord = GameRecords.builder()
                        .game(game)
                        .subject(subject)
                        .roundNumber(gameRoundNum)
                        .subjectTalk(subjectTalks)
                        .freeTalk(freeTalks)
                        .events(events)
                        .build();

                gameRecordsRepository.save(gameRecord);

            }

            log.info("게임 데이터 성공적으로 저장 - gameId: {}, roundNum: {}", gameId, gameRoundNum);

        } catch (Exception e) {
            log.error("게임 데이터 저장 중 예상하지 못한 에러 발생: {}", e.getMessage());
        }

    }

}
