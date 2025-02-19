package com.ssafy.undaied.socket.json.service;

import com.ssafy.undaied.socket.chat.util.SubjectUtil;
import com.ssafy.undaied.socket.json.dto.ChatDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;


import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
@Slf4j
public class JsonChatService {

    private final RedisTemplate redisTemplate;

    public String getSubjectTopic(Integer gameId, Integer round) {
        log.debug("🍳Start getting subject topic Id...");
        String subjectKey = String.format("game:%d:subjects", gameId);

        if (!redisTemplate.hasKey(subjectKey)) {
            log.debug("🍳No subject Key for game:{} and round:{}", gameId, round);
            return null;
        }

        Object value = redisTemplate.opsForHash().get(subjectKey, String.valueOf(round));
        if (value == null) {
            log.debug("🍳No subject data for game:{} and round:{}", gameId, round);
            return null;
        }

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

        if (!redisTemplate.hasKey(subjectChatKey)) {
            log.debug("🍳No subject chat Key for game:{} and round:{}", gameId, round);
            return subjectList;
        }

        String subjectChatStr = redisTemplate.opsForValue().get(subjectChatKey).toString();

        if (subjectChatStr == null || subjectChatStr.trim().isEmpty()) {
            log.debug("🍳No subject chat data found for game:{} and round:{}", gameId, round);
            return subjectList;
        }

        String[] subjectChats = subjectChatStr.split("\\s*\\|\\s*");
        log.debug("🍳subjectChats: {}", Arrays.stream(subjectChats).toList());

        for (String chat : subjectChats) {
            log.debug(chat.trim());

            if (chat.isEmpty()) continue;

            Pattern pattern = Pattern.compile("\\{(-?\\d+)\\} \\[(.*?)\\] <(\\d+)> \\((.*?)\\) (.*)");
            Matcher matcher = pattern.matcher(chat);

            if (matcher.find()) {
                int number = Integer.parseInt(matcher.group(3));
                String content = matcher.group(4);

                ChatDto chatDto = ChatDto.builder()
                        .number(number)
                        .content(content)
                        .build();

                subjectList.add(chatDto);
            }
        }
        return subjectList;
    }

    public List<ChatDto> getFreeChat(Integer gameId, Integer round) {
        log.debug("🍳Start getting free chat data...");
        String freeChatKey = String.format("game:%d:round:%d:freechats", gameId, round);
        List<ChatDto> freeList = new ArrayList<>();

        if (!redisTemplate.hasKey(freeChatKey)) {
            log.debug("🍳No free Chat Key for game:{} and round:{}", gameId, round);
            return freeList;
        }

        String freeChatStr = redisTemplate.opsForValue().get(freeChatKey).toString();

        if (freeChatStr == null || freeChatStr.trim().isEmpty()) {
            log.debug("🍳No free chat data found for game:{} and round:{}", gameId, round);
            return freeList;
        }

        String[] freeChats = freeChatStr.split("\\s*\\|\\s*");
        log.debug("🍳freeChats: {}", Arrays.stream(freeChats).toList());

        for (String chat : freeChats) {
            log.debug(chat.trim());

            if (chat.isEmpty()) continue;

            Pattern pattern = Pattern.compile("\\{(-?\\d+)\\} \\[(.*?)\\] <(\\d+)> \\((.*?)\\) (.*)");
            Matcher matcher = pattern.matcher(chat);

            if (matcher.find()) {
                int number = Integer.parseInt(matcher.group(3));
                String content = matcher.group(4);

                ChatDto chatDto = ChatDto.builder()
                        .number(number)
                        .content(content)
                        .build();

                freeList.add(chatDto);
            }
        }
        return freeList;
    }
}