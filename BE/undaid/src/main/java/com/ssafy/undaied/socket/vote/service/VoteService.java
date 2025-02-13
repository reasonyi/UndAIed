package com.ssafy.undaied.socket.vote.service;

import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.stage.constant.StageType;
import com.ssafy.undaied.socket.stage.handler.StageHandler;
import com.ssafy.undaied.socket.vote.dto.request.VoteSubmitRequestDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteResultResponseDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteSubmitResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteService {

    private final RedisTemplate redisTemplate;

    // 투표 제출
    @Transactional
    public VoteSubmitResponseDto submitVote(Integer voterUserId, Integer gameId, VoteSubmitRequestDto voteSubmitRequestDto)
        throws SocketException {
        String userNumberKey = "game:" + gameId + ":number_mapping";

        try {
            String gameKey = "game:" + gameId;
            if(!redisTemplate.hasKey(gameKey)) {
                throw new SocketException(SocketErrorCode.GAME_NOT_FOUND);
            }

            Object voterNumberObj = redisTemplate.opsForHash().get(userNumberKey, voterUserId.toString());
            if (voterNumberObj == null) {
                throw new SocketException(SocketErrorCode.PLAYER_NOT_IN_GAME);
            }

            String voterNumber = voterNumberObj.toString();
            String targetNumber = voteSubmitRequestDto.getTarget().toString();

            if (!isValidTarget(gameId, targetNumber))
                throw new SocketException(SocketErrorCode.VOTE_INVALID_TARGET);

            if (!isValidVote(gameId, voterNumber))
                throw new SocketException(SocketErrorCode.VOTE_INVALID_PLAYER);

            String stageKey = "game:" + gameId + ":stage";
            String currentStage = redisTemplate.opsForValue().get(stageKey).toString();

            if (currentStage.equals(StageType.VOTE.getRedisValue()))
                throw new SocketException(SocketErrorCode.VOTE_STAGE_INVALID);

            String roundKey = "game:" + gameId + "round";
            String currentRound = redisTemplate.opsForValue().get(roundKey).toString();
            String eventKey = "game:" + gameId + ":round" + currentRound + ":events";

            if (hasVoted(eventKey, voterNumber))
                throw new SocketException(SocketErrorCode.VOTE_ALREADY_SUBMITTED);

            // 투표 이벤트 저장을 위한 닉네임 찾기
            String userNameKey = "game:" + gameId + ":number_nicknames";
            String voteUserName = redisTemplate.opsForHash().get(userNameKey, voterNumber).toString();
            String targetUserName = redisTemplate.opsForHash().get(userNameKey, targetNumber).toString();
            log.info("voteUserName: " + voteUserName + ", targetUserName: " + targetUserName);
            String voteEvent = String.format("{vote} [%s] <%s> (%s) ~%s~ %s | ",
                    voteUserName, voterNumber, targetUserName, targetNumber, LocalDateTime.now());

            // 투표 이벤트 저장
            redisTemplate.opsForList().rightPush(eventKey, voteEvent);

            VoteSubmitResponseDto responseDto = VoteSubmitResponseDto.builder()
                    .number(Integer.parseInt(targetNumber))
                    .build();

            return responseDto;
        } catch (SocketException e) {
            log.error("Error in submitVote: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating room: {}", e.getMessage());
            throw new SocketException(SocketErrorCode.VOTE_SUBMIT_FAILED);        }
    }


    public boolean isValidVote(Integer gameId, String voterNumber) {
        String statusKey = "game:" + gameId + ":player_status";
        String aiKey = "game:" + gameId + ":ai_numbers";

        String statusStr = redisTemplate.opsForHash().get(statusKey, voterNumber).toString();
        boolean isDied = statusStr.contains("isDied=true");
        boolean isInfected = statusStr.contains("isDied=true");
        boolean isAI = redisTemplate.opsForSet().isMember(aiKey, voterNumber);

        return !isDied && !isInfected && !isAI;
    }

    public boolean isValidTarget(Integer gameId, String targetNumber) {
        String statusKey = "game:" + gameId + ":player_status";
        String statusStr = redisTemplate.opsForHash().get(statusKey, targetNumber).toString();
        boolean isDied = statusStr.contains("isDied=true");

        return !isDied;
    }

    public boolean hasVoted(String eventKey, String voterNumber) {
        return redisTemplate.opsForList().range(eventKey, 0, -1).stream()
                .anyMatch(event -> event.toString().contains(String.format("{%s}", "vote"))
                        && event.toString().contains(String.format("<%s>", voterNumber)));
    }

    // 투표 산출
    public VoteResultResponseDto computeVoteResult(Integer gameId) {

        String roundKey = "game:" + gameId + ":round";
        String currentRound = redisTemplate.opsForValue().get(roundKey).toString();

        String eventKey = "game:" + gameId + ":round" + currentRound + ":events";
        List<String> allEvents = redisTemplate.opsForList().range(eventKey, 0, -1);

        int playerCount = countValidPlayers(gameId);
        int[] voteCounts = new int[playerCount + 1];

        // redis 데이터에서 '~%s~' 값이 투표 대상자임
        for (String event : allEvents) {
            if (event.startsWith("{vote}")) {
                String[] parts = event.split("\\s+");   // 공백 기준으로 나누기
                String targetPart = parts[4];
                int targetNumber = Integer.parseInt(targetPart.substring(1, targetPart.length() - 1));
                voteCounts[targetNumber]++;
            }
        }

        int randomTarget = randomVoteTargetAI(gameId, voteCounts);
        int AICount = countValidAIs(gameId);

        voteCounts[randomTarget] += AICount;

        List<Integer> maxVotedCandidates = new ArrayList<>();
        int maxVotes = Arrays.stream(voteCounts).max().orElse(0);

        for (int i = 1; i < voteCounts.length; i++) {
            if (voteCounts[i] == maxVotes) {
                maxVotedCandidates.add(i);
            }
        }

        if (maxVotedCandidates.size() > 1)
            // 투표 수가 같은 경우 -> 무효

            return VoteResultResponseDto.notifyDraw(maxVotedCandidates, maxVotes);
        else {
            // 최다 득표자 -> 처형
            int eliminatedNumber = maxVotedCandidates.get(0);

            String statusKey = "game:" + gameId + ":player_status";
            String aiKey = "game:" + gameId + ":ai_numbers";
            String statusStr = redisTemplate.opsForHash().get(statusKey, eliminatedNumber).toString();
            boolean isInfected = statusStr.contains("isInfected=true");
            boolean isAI = redisTemplate.opsForSet().isMember(aiKey, eliminatedNumber);

            return VoteResultResponseDto.notifyVoteResult(eliminatedNumber, maxVotes, isAI,isInfected);
        }
    }

    /**
     * 유효 투표자 수 구하기
     **/
    public int countValidPlayers(Integer gameId) {
        String statusKey = "game:" + gameId + ":player_status";

        String script = "local count = 0 " +
                "for _, field in ipairs(redis.call('HKEYS', KEYS[1])) do " +    // statusKey에 해당하는 해시의 모든 필드(Key)를 가져와서 순회
                "  local status = redis.call('HGET', KEYS[1], field) " + // 해당 번호의 플레이어 상태를 가져옴
                "  if status and not status:match('isDied=true') and not status:match('isInfected=true') then " +
                "    count = count + 1 " +
                "  end " +
                "end " +
                "return count";

        return (int) redisTemplate.execute(new DefaultRedisScript<>(script, long.class),
                Arrays.asList(statusKey));
    }

    /**
     * 유효 AI 수 구하기
     **/
    public int countValidAIs(Integer gameId) {
        String statusKey = "game:" + gameId + ":player_status";
        String aiKey = "game:" + gameId + ":ai_numbers";

        String script = "local count = 0 " +
                "local aiSet = redis.call('SMEMBERS', KEYS[2])" +
                "for _, field in ipairs(aiSet) do " +
                " local status = redis.call('HGET', KEYS[1], field) " +
                " if status and not status:match('isDied=true') and not status:match('isInfected=true') then " +
                "  count = count + 1 " +
                " end" +
                "end" +
                "return count";

        return (int) redisTemplate.execute(new DefaultRedisScript<>(script, long.class),
                Arrays.asList(statusKey, aiKey));

    }

    /**
     * 유효 AI 수만큼 최다 득표자에게(여러 명일 경우 랜덤) 투표 하기
     **/
    public Integer randomVoteTargetAI(Integer gameId, int[] voteCounts) {
        List<Integer> randomVoteList = new ArrayList<>();
        int maxVotes = Arrays.stream(voteCounts).max().orElse(0);   // 최다 득표수

        for (int i = 1; i < voteCounts.length + 1; i++) {
            if (voteCounts[i] == maxVotes) {
                randomVoteList.add(i);
            }
        }

        int randomIndex = (int) (Math.random() * randomVoteList.size());
        return randomVoteList.get(randomIndex);
    }
}



