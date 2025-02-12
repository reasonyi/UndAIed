package com.ssafy.undaied.socket.chat.service;

import com.corundumstudio.socketio.SocketIOClient;
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

import static com.ssafy.undaied.socket.common.constant.SocketRoom.GAME_KEY_PREFIX;

@Slf4j
@Service
@RequiredArgsConstructor
public class GameChatService {

    //만료시간 2시간
    private static final long EXPIRE_TIME = 7200;

    private final RedisTemplate<String, String> redisTemplate;
    private final RedisTemplate<String, Object> jsonRedisTemplate;
    private final Random random = new Random();
    private final SocketIOServer server;

    //임시 변수, 나중에 수정해야
    int gameId=1;

    private static final Map<Integer, String> SUBJECTS = new HashMap<>() {{
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

    public void sendSubject(int gameId) {
        String gameKey = "game:" + gameId;

        String usedSubjectsKey = gameKey + ":used_subjects";
        Set<String> usedSubjects = redisTemplate.opsForSet().members(usedSubjectsKey);

        List<Integer> availableSubjects = new ArrayList<>();
        for (int i = 1; i <= SUBJECTS.size(); i++) {
            if (usedSubjects == null || !usedSubjects.contains(String.valueOf(i))) {
                availableSubjects.add(i);
            }

        }

        // 선택된 주제 ID 저장
        int subjectId = availableSubjects.get(new Random().nextInt(availableSubjects.size()));
        redisTemplate.opsForSet().add(usedSubjectsKey, String.valueOf(subjectId));
        redisTemplate.expire(usedSubjectsKey, EXPIRE_TIME, TimeUnit.SECONDS);

        // 응답 전송
        SendSubjectResponseDto sendSubjectresponseDto = SendSubjectResponseDto.builder()
                .item(SUBJECTS.get(subjectId))
                .build();

        server.getRoomOperations( String.valueOf(gameId)).sendEvent("send:subject", sendSubjectresponseDto);

    }

    public void processGameChat(SocketIOClient client, Integer userId, GameChatRequestDto gameChatRequestDto) {
        // 임시 지정
//        Integer gameId=client.get("gameId");
//        Integer gameId=1;

        // URL 파라미터에서 gameId 가져오기 // 나중에 수정해야.
        String gameIdStr = client.getHandshakeData().getSingleUrlParam("gameId");
        Integer gameId = Integer.parseInt(gameIdStr);

        // 클라이언트를 해당 게임 방에 조인시키기 // 나중에 수정해야.
        String gameRoom = GAME_KEY_PREFIX + gameId;
        if (!client.getAllRooms().contains(gameRoom)) {
            client.joinRoom(gameRoom);
            log.info("Client joined game room - userId: {}, gameRoom: {}", userId, gameRoom);
        }

        log.info(String.valueOf(gameId));
        if (gameId == null) {
            log.warn("Game ID not found for userId: {}", userId);
            return; // 게임 ID가 없으면 처리 중단
        }

        String nickname = client.get("nickname");
        LocalDateTime timestamp = LocalDateTime.now();

        // 🔹 Redis에서 userId에 해당하는 익명 번호 가져오기
        String mappingKey = GAME_KEY_PREFIX + gameId + ":number_mapping";
        Object numberObj = redisTemplate.opsForHash().get(mappingKey, userId.toString());
        if (numberObj == null) {
            log.warn("No number found for userId: {}", userId);
            return; // 해당 유저가 번호를 부여받지 않았다면 그냥 리턴
        }

        int number = Integer.parseInt(numberObj.toString());

        // 채팅 메시지 포맷
        String chatKey = "game:" + gameId + ":chats";
        String message = String.format("[%s] (%d) %s - %s",
                nickname, number, gameChatRequestDto.getContent(),
                timestamp.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME));

        // Redis에 채팅 저장
        redisTemplate.opsForList().rightPush(chatKey, message);
        redisTemplate.expire(chatKey, EXPIRE_TIME, TimeUnit.SECONDS);

        // 익명 번호(number)를 포함한 응답 전송
        GameChatResponseDto gameChatResponseDto = GameChatResponseDto.builder()
                .number(number)
                .content(gameChatRequestDto.getContent())
                .build();

        server.getRoomOperations("game:" + gameId).sendEvent("chat:game", gameChatResponseDto);

        log.info("Game chat sent - gameId: {}, userId: {}, number: {}, message: {}",
                gameId, userId, number, gameChatRequestDto.getContent());
    }
}

