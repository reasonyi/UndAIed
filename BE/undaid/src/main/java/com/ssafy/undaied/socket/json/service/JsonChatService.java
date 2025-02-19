package com.ssafy.undaied.socket.json.service;

import com.ssafy.undaied.socket.chat.util.SubjectUtil;
import com.ssafy.undaied.socket.json.dto.ChatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class JsonChatService {

    private final RedisTemplate redisTemplate;

    public String getSubjectTopic(Integer gameId, Integer round) {
        log.debug("🍳Start getting subject topic Id...");
        String subjectKey = String.format("game:%d:subjects", gameId);
        Integer subjectId = Integer.parseInt(redisTemplate.opsForHash().get(subjectKey, String.valueOf(round)).toString());
        log.debug("🍳Subject debate topic ID : {}", subjectId);

        String topic = SubjectUtil.SUBJECTS.get(subjectId);
        log.debug("🍳Subject debate topic : {}", topic);

        return topic;
    }

    public List<ChatDto> getSubjectChat(Integer gameId, Integer round) {
        log.debug("🍳Start getting subject chat data...");
        String subjectChatKey = String.format("game:%d:round:%d:subjectchats", gameId, round);
        List<ChatDto> subjectList = new ArrayList<>();

        String subjectChatStr = redisTemplate.opsForValue().get(subjectChatKey).toString();

        if (subjectChatStr == null || subjectChatStr.trim().isEmpty()) {
            log.debug("No subject data found for game:{} and round:{}", gameId, round);
            return subjectList;
        }

        String[] subjectChats = subjectChatStr.split("\\s*\\|\\s*");
        log.debug("subjectChats: {}", Arrays.stream(subjectChats).toList());

        for (String chat : subjectChats) {
            log.debug(chat.trim());
            String[] parts = chat.split("\\s+");   // 공백 기준으로 나누기
            String numberPart = parts[2];
            String contentPart = parts[3];
            Integer number = Integer.parseInt(numberPart.substring(1, numberPart.length() - 1));
            String content = contentPart.substring(1, contentPart.length() - 1);
            ChatDto chatDto = ChatDto.builder()
                    .number(number)
                    .content(content)
                    .build();

            subjectList.add(chatDto);
        }
        return subjectList;
    }

    public List<ChatDto> getFreeChat(Integer gameId, Integer round) {
        log.debug("🍳Start getting free chat data...");
        String freeChatKey = String.format("game:%d:round:%d:freechats", gameId, round);
        List<ChatDto> freeList = new ArrayList<>();

        String freeChatStr = redisTemplate.opsForValue().get(freeChatKey).toString();

        if (freeChatStr == null || freeChatStr.trim().isEmpty()) {
            log.debug("No subject data found for game:{} and round:{}", gameId, round);
            return freeList;
        }

        String[] freeChats = freeChatStr.split("\\s*\\|\\s*");
        log.debug("freeChats: {}", Arrays.stream(freeChats).toList());

        for (String chat : freeChats) {
            log.debug(chat.trim());
            String[] parts = chat.split("\\s+");   // 공백 기준으로 나누기
            String numberPart = parts[2];
            String contentPart = parts[3];
            Integer number = Integer.parseInt(numberPart.substring(1, numberPart.length() - 1));
            String content = contentPart.substring(1, contentPart.length() - 1);
            ChatDto chatDto = ChatDto.builder()
                    .number(number)
                    .content(content)
                    .build();

            freeList.add(chatDto);
        }
        return freeList;
    }
}
