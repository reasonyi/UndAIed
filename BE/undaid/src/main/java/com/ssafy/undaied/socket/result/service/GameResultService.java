package com.ssafy.undaied.socket.result.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.ssafy.undaied.domain.ai.entity.AIBenchmarks;
import com.ssafy.undaied.domain.ai.entity.AIs;
import com.ssafy.undaied.domain.ai.entity.repository.AIBenchmarksRepository;
import com.ssafy.undaied.domain.ai.entity.repository.AIRepository;
import com.ssafy.undaied.domain.game.entity.GameParticipants;
import com.ssafy.undaied.domain.game.entity.GameRecords;
import com.ssafy.undaied.domain.game.entity.Games;
import com.ssafy.undaied.domain.game.entity.Subjects;
import com.ssafy.undaied.domain.game.entity.respository.GameParticipantsRepository;
import com.ssafy.undaied.domain.game.entity.respository.GameRecordsRepository;
import com.ssafy.undaied.domain.game.entity.respository.GamesRepository;
import com.ssafy.undaied.domain.game.entity.respository.SubjectsRepository;
import com.ssafy.undaied.domain.user.entity.Users;
import com.ssafy.undaied.domain.user.entity.repository.UserRepository;
import com.ssafy.undaied.socket.chat.service.JsonAIChatService;
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

import com.fasterxml.jackson.core.type.TypeReference;
import static com.ssafy.undaied.socket.common.constant.SocketRoom.WAITING_LIST;
import static com.ssafy.undaied.socket.common.exception.SocketErrorCode.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class GameResultService {
    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final ObjectMapper objectMapper;
    private final GamesRepository gamesRepository;
    private final GameRecordsRepository gameRecordsRepository;
    private final SubjectsRepository subjectsRepository;
    private final AIBenchmarksRepository aiBenchmarksRepository;
    private final AIRepository aiRepository;
    private final SocketIONamespace namespace;
    private final JsonAIChatService jsonAIChatService;
    private final UserRepository userRepository;
    private final GameParticipantsRepository gameParticipantsRepository;

    public String checkGameResult(int gameId) throws SocketException {
        try {
            String statusKey = GAME_KEY_PREFIX + gameId + ":player_status";
            String aiKey = GAME_KEY_PREFIX + gameId + ":ai_numbers";

            Map<Object, Object> playerStatus = redisTemplate.opsForHash().entries(statusKey);
            if (playerStatus.isEmpty()) {
                log.error("Player status not found for game: {}", gameId);
                throw new SocketException(GAME_STATUS_NOT_FOUND);
            }

            Set<String> aiNumbers = redisTemplate.opsForSet().members(aiKey);
            if (aiNumbers == null || aiNumbers.isEmpty()) {
                log.error("AI numbers not found for game: {}", gameId);
                throw new SocketException(GAME_DATA_NOT_FOUND);
            }

            List<String> humanNumbers = playerStatus.keySet().stream()
                    .map(Object::toString)
                    .filter(number -> !aiNumbers.contains(number))
                    .collect(Collectors.toList());

            if (humanNumbers.isEmpty()) {
                log.error("No human players found for game: {}", gameId);
                throw new SocketException(NO_PLAYERS_FOUND);
            }

            int aliveHumans = countAliveHuman(playerStatus, humanNumbers);
            int aliveAIs = countAliveAI(playerStatus, aiNumbers);

            return aliveHumans <= aliveAIs ? "AI" : (aliveAIs == 0 ? "HUMAN" : null);

        } catch (Exception e) {
            log.error("Error checking game result for game {}: {}", gameId, e.getMessage());
            throw new SocketException(CHECKING_GAME_ERROR);
        }
    }

    private int countAliveHuman(Map<Object, Object> playerStatus, List<String> humanNumbers) {
        try {
            long count = humanNumbers.stream()
                    .filter(number ->  {
                        String status = playerStatus.get(number).toString();
                        boolean isAlive = !status.contains("isDied=true") && status.contains("isInGame=true");
                        log.debug("Player {} status: {}", number, status);
                        return  isAlive;
                    }).count();

            return (int) count;

        } catch (Exception e) {
            log.error("인간 생존 수 확인 중 에러: {}", e.getMessage());
            return -1;
        }
    }

    private int countAliveAI(Map<Object, Object> playerStatus, Set<String> aiNumbers) {
        try {
            long count = aiNumbers.stream()
                    .filter(number -> {
                        String status = playerStatus.get(number).toString();
                        boolean isAlive = !status.contains("isDied=true") && status.contains("isInGame=true");
                        log.debug("AI {} status: {}", number, status);
                        return isAlive;
                    }).count();

            return (int) count;
        } catch (Exception e) {
            log.error("AI 생존 수 확인 중 에러: {}", e.getMessage());
            return -1;
        }
    }

    public void gameEnd(int gameId, String winner) throws SocketException {
        try {
            log.debug("게임 종료 과정이 시행됩니다.: {}", gameId);

            updateGameEndStatus(gameId, winner);
            // 게임 결과 발표
            GameResultResponseDto responseDto = createGameResultResponse(gameId, winner);
            namespace.getRoomOperations(GAME_KEY_PREFIX + gameId)
                    .sendEvent("game:result:send", responseDto);

            // AI 메시지 스케줄링 중지
            jsonAIChatService.stopGameMessageScheduling(gameId);
            log.info("AI 메시지 스케줄링 중지: {}", gameId);

            // 플레이어 로비로 이동
            movePlayersToLobby(gameId);
            // Redis 게임 결과 저장

            saveGameResult(gameId);

            log.debug("게임 종료가 성공적으로 진행됐습니다.: {}", gameId);
        } catch (Exception e) {
            log.error("게임 종료 과정 중 에러가 발생했습니다. {}: {}", gameId, e.getMessage());
            throw new SocketException(GAME_END_PROCESS_FAILED);
        }
    }

    private void updateGameEndStatus(int gameId, String winner) throws SocketException {
        try {
            String gameKey = GAME_KEY_PREFIX + gameId;
            Object startedAtObj = jsonRedisTemplate.opsForHash().get(gameKey, "startedAt");

            if (startedAtObj == null) {
                log.error("게임 시작 시간을 찾을 수 없습니다.: {}", gameId);
                throw new SocketException(GAME_DATA_NOT_FOUND);
            }

            LocalDateTime endedAt = LocalDateTime.now();
            LocalDateTime startedAt = LocalDateTime.parse(startedAtObj.toString());

            long seconds = ChronoUnit.SECONDS.between(startedAt, endedAt);
            String playtime = String.format("%02d:%02d", seconds / 60, seconds % 60);

            Map<String, Object> updates = new HashMap<>();
            updates.put("status", "ENDED");
            updates.put("endedAt", endedAt.toString());
            updates.put("playtime", playtime);
            updates.put("humanWin", winner.equals("HUMAN"));

            jsonRedisTemplate.opsForHash().putAll(gameKey, updates);
            log.debug("게임 상태가 성공적으로 변경되었습니다.: {}", gameId);
        } catch (Exception e) {
            log.error("게임 상태 변경에 실패했습니다. {}: {}", gameId, e.getMessage());
            throw new SocketException(GAME_UPDATE_FAILED);
        }
    }

    private GameResultResponseDto createGameResultResponse(int gameId, String winner) throws SocketException {
        try {
            String statusKey = GAME_KEY_PREFIX + gameId + ":player_status";
            String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
            String userNicknameKey = GAME_KEY_PREFIX + gameId + ":user_nicknames";

            // 🔹 Redis에서 데이터 가져오기
            Map<Object, Object> playerStatus = redisTemplate.opsForHash().entries(statusKey);
            Map<Object, Object> numberToUserMapping = redisTemplate.opsForHash().entries(mappingKey);
            Map<Object, Object> userNicknames = redisTemplate.opsForHash().entries(userNicknameKey);

            // 🔹 number -> userId 매핑 변환
            Map<String, String> reverseMapping = new HashMap<>();
            numberToUserMapping.forEach((userId, number) ->
                    reverseMapping.put(number.toString(), userId.toString()));

            List<PlayerResultDto> players = playerStatus.entrySet().stream()
                    .map(entry -> {
                        String number = entry.getKey().toString();
                        String statusStr = entry.getValue().toString();
                        String userId = reverseMapping.get(number);
                        String nickname = (userId != null) ? userNicknames.getOrDefault(userId, "Unknown").toString() : "Unknown";

                        return PlayerResultDto.builder()
                                .number(Integer.parseInt(number))
                                .nickname(nickname)
                                .isDied(statusStr.contains("isDied=true"))
                                .isInGame(statusStr.contains("isInGame=true"))
                                .build();
                    })
                    .sorted(Comparator.comparingInt(PlayerResultDto::getNumber))
                    .collect(Collectors.toList());

            // 🔹 승리 메시지 설정
            String message = (winner.equals("HUMAN")) ? "인간 승리" : "AI 승리";

            return GameResultResponseDto.builder()
                    .winner(winner)
                    .message(message)
                    .players(players)
                    .build();
        } catch (Exception e) {
            log.error("게임 결과 응답 생성 중 에러가 발생했습니다. {}: {}", gameId, e.getMessage());
            throw new SocketException(RESULT_CREATION_FAILED);
        }
    }

    public void movePlayersToLobby(int gameId) throws SocketException {
        Collection<SocketIOClient> clients = namespace.getRoomOperations(GAME_KEY_PREFIX+gameId).getClients();
        try {
            if (clients == null) {
                log.error("Client가 null 입니다 : {}", gameId);
                throw new SocketException(CLIENT_NOT_FOUND);
            }
            for (SocketIOClient client : clients) {
                client.leaveRoom(GAME_KEY_PREFIX + gameId);
                client.joinRoom(LOBBY_ROOM);
            }
            log.debug("백엔드상 게임방 나가기 처리됩니다");
        } catch (Exception e) {
            log.error("백엔드상 게임방 나가기 처리 중 에러가 발생했습니다. {}: {}", gameId, e.getMessage());
            throw new SocketException(ROOM_OPERATION_FAILED);
        }
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

            // 게임 참여자 정보 저장 및
            // 유저 승패 업데이트
            log.debug("게임에 참여한 유저 승패 저장중...");
            String ingameUserKey = GAME_KEY_PREFIX + gameId + ":user_nicknames";
            Map<Object, Object> ingameUserData = redisTemplate.opsForHash().entries(ingameUserKey);

            log.debug("Redis key: {}", ingameUserKey);
            log.debug("Retrieved data: {}", ingameUserData);  // 데이터 확인용 로그

            if (ingameUserData.isEmpty()) {
                log.error("redis에서 게임에 참여한 유저를 찾을 수 없어 승패 저장 실패 - roomId: {}", roomId);
            } else {
                for (Map.Entry<Object, Object> entry : ingameUserData.entrySet()) {
                    String keyStr = entry.getKey().toString();
                    String mapValue = entry.getValue().toString();

                    Integer mapKey = Integer.parseInt(keyStr);

                    if(mapKey > 0) {
//                        log.debug("음수는 패스하고 양수인 유저 아이디값이어서 여기 잘 들어옴");
                        Users ingameUser = userRepository.findById(mapKey)
                                .orElse(null);

                        if(ingameUser == null) {
                            log.error("{} 아이디값으로 유저를 찾을 수 없어 승패 저장에 실패", mapKey);
                        } else {
                            if((Boolean) gameData.get("humanWin")) {
                                ingameUser.win();
                            } else {
                                ingameUser.lose();
                            }
                            userRepository.save(ingameUser);
                            log.debug("{}유저의 승패 저장 성공", mapValue);

                            GameParticipants gp = new GameParticipants();
                            gp.setGame(game);
                            gp.setUser(ingameUser);

                            gameParticipantsRepository.save(gp);
                            log.debug("{}유저의 게임 참여 정보 저장 성공", mapValue);
                        }
                    } else {
                        Integer aiId = mapKey * -1;
                        AIs ais = aiRepository.findById(aiId)
                                .orElse(null);

                        if(ais != null) {
                            GameParticipants gp = new GameParticipants();
                            gp.setGame(game);
                            gp.setAi(ais);

                            gameParticipantsRepository.save(gp);
                            log.debug("{}의 게임 참여 정보 저장 성공", ais.getAiName());
                        }
                    }
                }
            }

            // AI 죽은 결과 저장.
            log.debug("AI 죽은 결과 저장 시도중...");
            String aiDeadKey = GAME_KEY_PREFIX + gameId + ":ai_died";   // game:1:ai_died
            Map<Object, Object> aiDeadResult = redisTemplate.opsForHash().entries(aiDeadKey);

            // AI 탈락 결과를 순회하면서 처리
            aiDeadResult.forEach((aiId, eliminatedRound) -> {
                log.debug("AI 탈락 결과 순회중");

                AIs ai = aiRepository.findById(Integer.parseInt(aiId.toString()))
                        .orElse(null);

                if (ai == null) {
                    log.error("AI를 찾을 수 없어 데이터 저장 실패");
                } else {
                    AIBenchmarks aiBenchmarks = AIBenchmarks.builder()
                            .game(game)
                            .ai(ai)
                            .deadRound(Integer.parseInt(eliminatedRound.toString()))
                            .build();

                    aiBenchmarksRepository.save(aiBenchmarks);
                    log.debug("AI 탈락 결과 성공적으로 저장");
                }
            });

            // 레코드마다 저장....

            // 우선 총 라운드를 가져와야됨.
            String gameRoundKey = GAME_KEY_PREFIX + gameId +":round";
            String gameRoundNumStr = redisTemplate.opsForValue().get(gameRoundKey);
            if (gameRoundNumStr == null) {
                log.error("redis에서 게임 총 라운드를 찾을 수 없어 데이터 저장 실패 - roomId: {}", roomId);
                return;
            }
            int gameRoundNum = Integer.parseInt(gameRoundNumStr);

            String subjectKey = GAME_KEY_PREFIX + gameId + ":subjects"; // game:{gameId}:subjects
            Map<Object, Object> subject = redisTemplate.opsForHash().entries(subjectKey);
//            System.out.println("Redis 해시 전체 데이터: " + subject);
//
//            // 각 엔트리 순회하면서 키와 값 출력
//            subject.forEach((keykey, value) -> {
//                System.out.println("키: " + keykey + ", 값: " + value + ", 타입: " + value.getClass());
//            });

            for (int i = 1; i <= subject.size(); i++) {
                log.debug("게임 레코드 저장 시도중...");

                // 여기에 subject 찾는 코드.
                Integer subjectId = Integer.parseInt((String) subject.get(String.valueOf(i)));

                Subjects subject1 = subjectsRepository.findById(subjectId)
                        .orElse(null);

                if (subject1 == null) {
                    log.error("주제를 찾을 수 없어 데이터 저장 실패");
                    break;
                }
                else {
                    System.out.println(gameId+ "번 게임의 "+i+"라운드 주제: "+subject1.getItem());
                }

                String subjectTalkKey = GAME_KEY_PREFIX + gameId + ":round:" + i +":subjectchats";  // 키  game:{gameId}:round:{roundNum}:subjectchats
                String subjectTalks = redisTemplate.opsForValue().get(subjectTalkKey);
                System.out.println("조회하려는 주제토론 키: " + subjectTalkKey);
                System.out.println("주제토론 내용: " + subjectTalks);

                String freeTalkKey = GAME_KEY_PREFIX + gameId + ":round:" + i +":freechats";    // 키  game:{gameId}:round:{roundNum}:freechats
                String freeTalks = redisTemplate.opsForValue().get(freeTalkKey);
                System.out.println("조회하려는 자유토론 키: " + freeTalkKey);
                System.out.println("자유토론 내용: " + freeTalks);

                String eventKey = GAME_KEY_PREFIX + gameId + ":round:" + i +":events";  // 키  game:{gameId}:round:{roundNumber}:events
                String events = redisTemplate.opsForValue().get(eventKey);
                System.out.println("조회하려는 이벤트 키: " + eventKey);
                System.out.println("이벤트 내용: " + events);

                GameRecords gameRecord = GameRecords.builder()
                        .game(game)
                        .subject(subject1)
                        .roundNumber(i)
                        .subjectTalk(subjectTalks != null ? subjectTalks : " ")  // 빈 배열 문자열로 기본값 설정
                        .freeTalk(freeTalks != null ? freeTalks : " ")
                        .events(events != null ? events : " ")
                        .build();

                gameRecordsRepository.save(gameRecord);

            }

            log.info("게임 데이터 성공적으로 저장 - gameId: {}, roundNum: {}", gameId, gameRoundNum);

        } catch (Exception e) {
            log.error("게임 데이터 저장 중 예상하지 못한 에러 발생: {}", e.getMessage());
        }

    }

}
