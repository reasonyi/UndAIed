package com.ssafy.undaied.socket.chat.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.chat.dto.request.GameChatRequestDto;
import com.ssafy.undaied.socket.chat.dto.response.GameChatResponseDto;
import com.ssafy.undaied.socket.chat.dto.response.SendSubjectResponseDto;
import com.ssafy.undaied.socket.chat.util.SubjectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static com.ssafy.undaied.socket.common.constant.SocketRoom.GAME_KEY_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameChatService {

    private static final long EXPIRE_TIME = 7200;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final Random random = new Random();
    private final SocketIONamespace namespace;
    private final JsonAIChatService jsonAIChatService;

    public void sendSubject(int gameId) {
        log.debug("🍳여기는 들어오겠지");
        String gameKey = "game:" + gameId;
        String roundKey = gameKey + ":round";
        String subjectKey = String.format("game:%d:subjects", gameId);
        String currentRound = redisTemplate.opsForValue().get(roundKey);

        log.debug("🍳여기도 들어올 거 같은데....");
        Set<Integer> usedSubjects = new HashSet<>();
        if (redisTemplate.hasKey(subjectKey)) {
            log.debug("🍳여기가 문제인가요....?");
            Map<Object, Object> subjectMap = redisTemplate.opsForHash().entries(subjectKey);
            if (!subjectMap.isEmpty()) {
                log.debug("🍳GameId : {}, Used subjects size : {}", gameId, subjectMap.size());
                for (Map.Entry<Object, Object> entry : subjectMap.entrySet()) {
                        usedSubjects.add(Integer.parseInt(entry.getValue().toString()));
                        log.debug("🍳{} round used Subject : {}", entry.getKey(), entry.getValue());
                }
            }
        } else {
            log.debug("🍳No subjects used yet for game: {}", gameId);
        }
        log.debug("🍳Ended searching used subjects");

        // 사용 가능한 주제 리스트 생성
        Map<Integer, String> subjects = SubjectUtil.SUBJECTS;
        List<Integer> availableSubjects = new ArrayList<>();
        for (int i = 1; i <= subjects.size(); i++) {
            if (!usedSubjects.contains(i)) {
                availableSubjects.add(i);
            }
        }

        if (availableSubjects.isEmpty()) {
            log.error("No available subjects left for game: {}", gameId);
            // 예외 처리 또는 적절한 대응 필요
            return;
        }

        // 주제 선택 (랜덤)
        int subjectId = availableSubjects.get(new Random().nextInt(availableSubjects.size()));

        // 선택된 주제 ID 저장
        redisTemplate.opsForHash().put(subjectKey, currentRound, String.valueOf(subjectId));
        redisTemplate.expire(subjectKey, EXPIRE_TIME, TimeUnit.SECONDS);

        SendSubjectResponseDto sendSubjectResponseDto = SendSubjectResponseDto.builder()
                .number(0)
                .content(subjects.get(subjectId))
                .build();

        log.info("Selected subject {} for game {} round {}", subjectId, gameId, currentRound);
        namespace.getRoomOperations("game:" + gameId).sendEvent("game:chat:send", sendSubjectResponseDto);
        jsonAIChatService.startGameMessageScheduling(gameId);
    }

    private boolean hasUserSpokenInSubjectDebate(Integer gameId, String round, int number) {
        String spokenUsersKey = String.format("%s%d:round:%s:subject_speakers", GAME_KEY_PREFIX, gameId, round);
        return Boolean.TRUE.equals(redisTemplate.opsForSet().isMember(spokenUsersKey, String.valueOf(number)));
    }

    private void markUserAsSpoken(Integer gameId, String round, int number) {
        String spokenUsersKey = String.format("%s%d:round:%s:subject_speakers", GAME_KEY_PREFIX, gameId, round);
        redisTemplate.opsForSet().add(spokenUsersKey, String.valueOf(number));
        redisTemplate.expire(spokenUsersKey, EXPIRE_TIME, TimeUnit.SECONDS);
    }

    public String processFreeChat(Integer gameId, SocketIOClient client, Integer userId, GameChatRequestDto gameChatRequestDto) {
        log.info("Chat content received: {}", gameChatRequestDto.getContent());

        String nickname = client.get("nickname");
        if (nickname == null) {
            log.warn("닉네임을 찾을 수 없습니다.", nickname);
            return "사용자 정보를 찾을 수 없습니다.";
        }

        // Redis에서 익명 번호 조회
        String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
        Object numberObj = redisTemplate.opsForHash().get(mappingKey, userId.toString());
        if (numberObj == null) {
            log.warn("No number found for userId: {}", userId);
            return "참가자 번호를 찾을 수 없습니다.";
        }

        int number = Integer.parseInt(numberObj.toString());
        String roundKey = GAME_KEY_PREFIX + gameId + ":round";
        String currentRound = redisTemplate.opsForValue().get(roundKey);

        try {
            // 자유토론 채팅 저장
            String chatKey = String.format("%s%d:round:%s:freechats", GAME_KEY_PREFIX, gameId, currentRound);
            String message = String.format("{%d} [%s] <%d> (%s) %s | ",
                    userId, nickname, number, gameChatRequestDto.getContent(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            redisTemplate.opsForValue().append(chatKey, message);
            redisTemplate.expire(chatKey, EXPIRE_TIME, TimeUnit.SECONDS);

            // 실시간 채팅 전송
            GameChatResponseDto response = GameChatResponseDto.builder()
                    .number(number)
                    .content(gameChatRequestDto.getContent())
                    .build();

            namespace.getRoomOperations(GAME_KEY_PREFIX + gameId).sendEvent("game:chat:send", response);
            log.info("Free chat sent - gameId: {}, userId: {}, number: {}, message: {}",
                    gameId, userId, number, gameChatRequestDto.getContent());

            return null; // 성공 시 null 반환
        } catch (Exception e) {
            log.error("자유토론 채팅 처리 중 오류 발생", e);
            return "채팅 처리 중 오류가 발생했습니다.";
        }
    }

    public String storeSubjectChat(Integer gameId, SocketIOClient client, Integer userId, GameChatRequestDto gameChatRequestDto) {
        String nickname = client.get("nickname");
        if (nickname == null) {
            log.warn("닉네임을 찾을 수 없습니다.", nickname);
            return "사용자 정보를 찾을 수 없습니다.";
        }

        // Redis에서 익명 번호 조회
        String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
        Object numberObj = redisTemplate.opsForHash().get(mappingKey, userId.toString());
        if (numberObj == null) {
            log.warn("No number found for userId: {}", userId);
            return "참가자 번호를 찾을 수 없습니다.";
        }

        int number = Integer.parseInt(numberObj.toString());
        String roundKey = GAME_KEY_PREFIX + gameId + ":round";
        String currentRound = redisTemplate.opsForValue().get(roundKey);

        // 이미 발언했는지 확인
        if (hasUserSpokenInSubjectDebate(gameId, currentRound, number)) {
            log.warn("User {} (number {}) has already spoken in subject debate round {}",
                    userId, number, currentRound);
            return "주제 토론에서는 한 번만 발언할 수 있습니다.";
        }

        try {
            // 주제토론 채팅 저장
            String chatKey = String.format("%s%d:round:%s:subjectchats", GAME_KEY_PREFIX, gameId, currentRound);
            String message = String.format("{%d} [%s] <%d> (%s) %s | ",
                    userId, nickname, number, gameChatRequestDto.getContent(),
                    LocalDateTime.now().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

            redisTemplate.opsForValue().append(chatKey, message);
            redisTemplate.expire(chatKey, EXPIRE_TIME, TimeUnit.SECONDS);

            // 발언자 기록
            markUserAsSpoken(gameId, currentRound, number);

            log.info("Subject chat stored - gameId: {}, userId: {}, number: {}, message: {}",
                    gameId, userId, number, gameChatRequestDto.getContent());

            return null; // 성공 시 null 반환
        } catch (Exception e) {
            log.error("주제토론 채팅 처리 중 오류 발생", e);
            return "채팅 처리 중 오류가 발생했습니다.";
        }
    }

    public List<GameChatResponseDto> getSubjectDebateChats(Integer gameId, String round) {
        String chatKey = String.format("%s%d:round:%s:subjectchats", GAME_KEY_PREFIX, gameId, round);
        String chatLog = redisTemplate.opsForValue().get(chatKey);

        if (chatLog == null || chatLog.isEmpty()) {
            return new ArrayList<>();
        }

        List<GameChatResponseDto> result = new ArrayList<>();
        String[] messages = chatLog.split("\\|");

        for (String message : messages) {
            if (message.isEmpty()) continue;

            Pattern pattern = Pattern.compile("\\{(-?\\d+)\\} \\[(.*?)\\] <(\\d+)> \\((.*?)\\) (.*)");
            Matcher matcher = pattern.matcher(message);

            if (matcher.find()) {
                int number = Integer.parseInt(matcher.group(3));
                String content = matcher.group(4);

                result.add(GameChatResponseDto.builder()
                        .number(number)
                        .content(content)
                        .build());
            }
        }
        return result;
    }
}