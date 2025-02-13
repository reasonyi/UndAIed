package com.ssafy.undaied.socket.infect.service;

import com.ssafy.undaied.global.common.exception.BaseException;
import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class InfectService {

    private final RedisTemplate redisTemplate;

    public String infectPlayer(Integer gameId) throws SocketException {
        try {
            String statusKey = "game:" + gameId + ":player_status";
            String aiKey = "game:" + gameId + ":ai_numbers";

            List<String> eligiblePlayers = new ArrayList<>();
            Set<String> aiPlayers = redisTemplate.opsForSet().members(aiKey);
            Map<Object, Object> playerStatus = redisTemplate.opsForHash().entries(statusKey);

            for (Map.Entry<Object, Object> entry : playerStatus.entrySet()) {
                String playerNumber = entry.getKey().toString();
                String status = entry.getValue().toString();
                if (!aiPlayers.contains(playerNumber) && !status.contains("isDied=true") && !status.contains("isInfected=true")) {
                    eligiblePlayers.add(playerNumber);
                }
            }

            if (eligiblePlayers.isEmpty()) {
                throw new SocketException(SocketErrorCode.INFECT_PLAYER_NOT_FOUND);
            }
            // 랜덤 선택
            String infectedPlayerNumber = eligiblePlayers.get(new Random().nextInt(eligiblePlayers.size()));

            // 감염 처리(상태 변경)
            String newStatus = playerStatus.get(infectedPlayerNumber).toString().replace("isInfected=false", "isInfected=true");
            redisTemplate.opsForHash().put(statusKey, infectedPlayerNumber, newStatus);

            String roundKey = "game:" + gameId + ":round";
            String currentRound = redisTemplate.opsForValue().get(roundKey).toString();
            String eventKey = "game:" + gameId + ":round" + currentRound + ":events";
            String userNameKey = "game:" + gameId + ":number_nicknames";

            String infectedPlayerName = redisTemplate.opsForHash().get(userNameKey, infectedPlayerNumber).toString();


            redisTemplate.opsForList().rightPush(eventKey, String.format("{infection} [null] <null> (%s) ~%s~ %s |",
                    infectedPlayerName, infectedPlayerNumber, LocalDateTime.now())
            );

            return infectedPlayerNumber;
        } catch (SocketException e) {
            log.error("Infection error - No eligible players: {}", e.getMessage());
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during infection process: ", e);
            throw new SocketException(SocketErrorCode.INFECT_FAILED);
        }

    }
}
