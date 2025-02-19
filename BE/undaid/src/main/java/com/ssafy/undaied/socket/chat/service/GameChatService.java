package com.ssafy.undaied.socket.chat.service;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIONamespace;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.chat.dto.request.GameChatRequestDto;
import com.ssafy.undaied.socket.chat.dto.response.GameChatResponseDto;
import com.ssafy.undaied.socket.chat.dto.response.SendSubjectResponseDto;
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

    public static final Map<Integer, String> SUBJECTS = new HashMap<>() {{
        put(1, "인공지능이 인간을 이길 수 있을까?");
        put(2, "인공지능은 인류를 멸망시킬까?");
        put(3, "사람의 존재를 규정하는 건 뭘까?");
        put(4, "기후 변화는 인류의 가장 큰 위협인가?");
        put(5, "우주 탐사에 더 많은 자원을 투자해야 하는가?");
        put(6, "완전한 프라이버시는 디지털 시대에 가능한가?");
        put(7, "유전자 조작은 윤리적으로 허용되어야 하는가?");
        put(8, "보편적 기본소득이 필요한가?");
        put(9, "온라인 교육이 전통적 교육을 대체할 수 있는가?");
        put(10, "소셜 미디어는 사회를 더 좋게 만들었는가?");
        put(11, "인간의 수명 연장 연구는 계속되어야 하는가?");
        put(12, "디지털 화폐가 실물 화폐를 대체해야 하는가?");
        put(13, "자율주행 자동차는 도로를 더 안전하게 만들 것인가?");
        put(14, "메타버스는 현실 세계를 대체할 수 있는가?");
        put(15, "인공지능 예술은 진정한 예술인가?");
        put(16, "개인정보 보호와 국가 안보 중 무엇이 더 중요한가?");
        put(17, "로봇에게 시민권을 부여해야 하는가?");
        put(18, "인터넷 접속은 기본권이 되어야 하는가?");
        put(19, "인공지능 판사가 가능한가?");
        put(20, "우주 자원 채굴은 허용되어야 하는가?");
    }};

    public String getSubject(int subjectId) {
        return SUBJECTS.get(subjectId);
    }

    public void sendSubject(int gameId) {
        String gameKey = "game:" + gameId;
        String roundKey = gameKey + ":round";
        String currentRound = redisTemplate.opsForValue().get(roundKey);

        // 해당 게임에서 지금까지 사용된 모든 주제 확인
        Set<String> usedSubjects = new HashSet<>();
        for (int round = 1; round <= Integer.parseInt(currentRound); round++) {
            String roundSubjectKey = String.format("%s:round:%d:used_subjects", gameKey, round);
            String usedSubject = redisTemplate.opsForValue().get(roundSubjectKey);
            if (usedSubject != null) {
                usedSubjects.add(usedSubject);
            }
        }

        // 사용 가능한 주제 리스트 생성
        List<Integer> availableSubjects = new ArrayList<>();
        for (int i = 1; i <= SUBJECTS.size(); i++) {
            if (!usedSubjects.contains(String.valueOf(i))) {
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
        String usedSubjectsKey = String.format("%s:round:%s:used_subjects", gameKey, currentRound);

        // 선택된 주제 ID 저장
        redisTemplate.opsForValue().set(usedSubjectsKey, String.valueOf(subjectId));
        redisTemplate.expire(usedSubjectsKey, EXPIRE_TIME, TimeUnit.SECONDS);

        SendSubjectResponseDto sendSubjectResponseDto = SendSubjectResponseDto.builder()
                .number(0)
                .content(SUBJECTS.get(subjectId))
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

            Pattern pattern = Pattern.compile("\\{(\\d+)\\} \\[(.*?)\\] <(\\d+)> \\((.*?)\\) (.*)");
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