package com.ssafy.undaied.socket.vote.service;

import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.stage.constant.StageType;
import com.ssafy.undaied.socket.vote.dto.request.VoteSubmitRequestDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteResultResponseDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteSubmitResponseDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class VoteService {

    private final RedisTemplate<String, String> redisTemplate;

    // 투표 제출
    public VoteSubmitResponseDto submitVote(Integer voterUserId, Integer gameId, VoteSubmitRequestDto voteSubmitRequestDto)
            throws SocketException {
        String userNumberKey = "game:" + gameId + ":number_mapping";

        try {
            String gameKey = "game:" + gameId;
            if (!redisTemplate.hasKey(gameKey)) {
                throw new SocketException(SocketErrorCode.GAME_NOT_FOUND);
            }

            Object voterNumberObj = redisTemplate.opsForHash().get(userNumberKey, voterUserId.toString());

            if (voterNumberObj == null) {
                throw new SocketException(SocketErrorCode.PLAYER_NOT_IN_GAME);
            }

            String voterNumber = voterNumberObj.toString();
            String targetNumber = voteSubmitRequestDto.getTarget().toString();
            log.debug("voterNumber: " + voterNumber + ", targetNumber: " + targetNumber);

            if (!isValidTarget(gameId, targetNumber))
                throw new SocketException(SocketErrorCode.VOTE_INVALID_TARGET);

            if (!isValidVote(gameId, voterNumber))
                throw new SocketException(SocketErrorCode.VOTE_INVALID_PLAYER);

            String stageKey = "game:" + gameId + ":stage";
            String currentStage = redisTemplate.opsForValue().get(stageKey);

            if (!currentStage.equals(StageType.VOTE.getRedisValue()))
                throw new SocketException(SocketErrorCode.VOTE_STAGE_INVALID);

            String roundKey = "game:" + gameId + ":round";
            String currentRound = redisTemplate.opsForValue().get(roundKey);
            String eventKey = "game:" + gameId + ":round:" + currentRound + ":events";

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
            redisTemplate.opsForValue().append(eventKey, voteEvent);

            VoteSubmitResponseDto responseDto = VoteSubmitResponseDto.builder()
                    .number(Integer.parseInt(targetNumber))
                    .build();

            return responseDto;
        } catch (SocketException e) {
            log.error("Error in submitVote: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while creating room: {}", e.getMessage());
            throw new SocketException(SocketErrorCode.VOTE_SUBMIT_FAILED);
        }
    }


    public boolean isValidVote(Integer gameId, String voterNumber) {
        String statusKey = "game:" + gameId + ":player_status";
        String aiKey = "game:" + gameId + ":ai_numbers";

        String statusStr = redisTemplate.opsForHash().get(statusKey, voterNumber).toString();
        boolean isDied = statusStr.contains("isDied=true");
        boolean isInfected = statusStr.contains("isDied=true");
        boolean isInGame = statusStr.contains("isInGame=true");
        boolean isAI = redisTemplate.opsForSet().isMember(aiKey, voterNumber);

        return !isDied && !isInfected && isInGame && !isAI;
    }

    public boolean isValidTarget(Integer gameId, String targetNumber) {
        String statusKey = "game:" + gameId + ":player_status";
        String statusStr = redisTemplate.opsForHash().get(statusKey, targetNumber).toString();
        boolean isDied = statusStr.contains("isDied=true");

        return !isDied;
    }

    public boolean hasVoted(String eventKey, String voterNumber) {
        String events = redisTemplate.opsForValue().get(eventKey);
        if (events == null) return false;

        return Arrays.stream(events.split("\\|"))
                .anyMatch(event -> event.trim().contains("{vote}")
                        && event.contains(String.format("<%s>", voterNumber)));
    }


    // 투표 산출
    public VoteResultResponseDto computeVoteResult(Integer gameId) {
        log.debug("Starting vote computation for game {}", gameId);

        // 현재 라운드 조회
        String roundKey = "game:" + gameId + ":round";
        String currentRound = redisTemplate.opsForValue().get(roundKey);
        log.debug("Current round: {}", currentRound);

        // 유효 플레이어 수 계산
        int playerCount = countValidPlayers(gameId);
        log.debug("Valid player count: {}", playerCount);

        // 투표를 카운트할 배열 : 인덱스가 익명 number, 값은 익명 number가 받은 투표 수
        int[] voteCounts = new int[playerCount + 1];

        // 투표 이벤트 조회
        String eventKey = "game:" + gameId + ":round:" + currentRound + ":events";

        if ( redisTemplate.hasKey(eventKey) && !redisTemplate.opsForValue().get(eventKey).trim().isEmpty()) {
            String allEvents = redisTemplate.opsForValue().get(eventKey).toString();
            String[] events = allEvents.split("\\s*\\|\\s*");
            log.debug("events: {}", Arrays.stream(events).toList());

            // 투표 집계
            for (String event : events) {
                log.debug(event.trim());
                if (event.trim().startsWith("{vote}")) {
                    log.debug("Start vote computing");
                    String[] parts = event.split("\\s+");   // 공백 기준으로 나누기
                    String targetPart = parts[4];
                    int targetNumber = Integer.parseInt(targetPart.substring(1, targetPart.length() - 1));
                    voteCounts[targetNumber]++;
                }
            }
            log.debug("VoteCounts: {}", Arrays.stream(voteCounts).toArray());
            log.debug("Vote counting completed");
        }

        // AI 투표 처리
        // 가장 많은 표를 받은 사람 중 랜덤으로 타켓 선정
        int randomTarget = randomVoteTargetAI(voteCounts);
        log.debug("RandoTarget: {}", randomTarget);

        int AICount = countValidAIs(gameId);
        voteCounts[randomTarget] += AICount;
        log.debug("AI votes ({} votes) added to player {}", AICount, randomTarget);

        // 최다 득표자 찾기
        List<Integer> maxVotedCandidates = new ArrayList<>();
        int maxVotes = Arrays.stream(voteCounts).max().orElse(0);
        log.debug("Maximum votes: {}", maxVotes);

        for (int i = 1; i < voteCounts.length; i++) {
            if (voteCounts[i] == maxVotes) {
                maxVotedCandidates.add(i);
                log.debug("Player {} has maximum votes", i);
            }
        }
        log.debug("Found {} players with maximum votes", maxVotedCandidates.size());

        if (maxVotedCandidates.size() > 1) {
            // 비기는 경우
            log.info("Vote resulted in a draw between {} players", maxVotedCandidates.size());

            // 투표 이벤트 저장 (모든 데이터 null)
            String voteEvent = String.format("{vote_result} [null] <null> (null) ~null~ %s | ",
                    LocalDateTime.now());

            redisTemplate.opsForValue().append(eventKey, voteEvent);
            return VoteResultResponseDto.notifyDraw(maxVotedCandidates, maxVotes);
        } else {
            String eliminatedNumber = String.valueOf(maxVotedCandidates.get(0));
            log.info("Player {} eliminated with {} votes", eliminatedNumber, maxVotes);

            String statusKey = "game:" + gameId + ":player_status";
            String aiKey = "game:" + gameId + ":ai_numbers";

            String statusStr = redisTemplate.opsForHash().get(statusKey, eliminatedNumber).toString();
            log.debug("statusStr: {}", statusStr);
            boolean isInfected = statusStr.contains("isInfected=true");
            log.debug("EliminatedPlayer is Infected: {}", isInfected);
            boolean isAI = redisTemplate.opsForSet().isMember(aiKey, eliminatedNumber);
            log.debug("EliminatedPlayer is AI: {}", isAI);

            log.debug("Eliminated player status - AI: {}, Infected: {}", isAI, isInfected);

            // 투표 이벤트 저장
            String userNameKey = "game:" + gameId + ":number_nicknames";
            String eliminatedName = redisTemplate.opsForHash().get(userNameKey, eliminatedNumber).toString();

            String voteEvent = String.format("{vote_result} [null] <null> (%s) ~%s~ %s | ",
                    eliminatedName, eliminatedNumber, LocalDateTime.now());
            redisTemplate.opsForValue().append(eventKey, voteEvent);
            return VoteResultResponseDto.notifyVoteResult(eliminatedNumber, maxVotes, isAI, isInfected);
        }
    }


    /**
     * 유효 투표자 수 구하기
     **/
    public int countValidPlayers(Integer gameId) {
        log.debug("Counting valid players for game {}", gameId);
        String statusKey = "game:" + gameId + ":player_status";

        Map<Object, Object> playerStatuses = redisTemplate.opsForHash().entries(statusKey);
        int count = 0;
        for (Map.Entry<Object, Object> entry : playerStatuses.entrySet()) {
            String status = entry.getValue().toString();
            if (!status.contains("isDied=true") && !status.contains("isInfected=true")) count++;
        }

        log.debug("Valid players count: {}", count);
        return count;
    }

    /**
     * 유효 AI 수 구하기
     **/
    public int countValidAIs(Integer gameId) {
        String statusKey = "game:" + gameId + ":player_status";
        String aiKey = "game:" + gameId + ":ai_numbers";

        Map<Object, Object> playerStatuses = redisTemplate.opsForHash().entries(statusKey);
        Set<String> aiSet = redisTemplate.opsForSet().members(aiKey);

        int count = 0;
        for (Map.Entry<Object, Object> entry : playerStatuses.entrySet()) {
            String status = entry.getValue().toString();
            if (!status.contains("isDied=true") && !status.contains("isInfected=true")
                    && aiSet.contains(entry.getKey())) count++;
        }

        log.debug("Valid AIs count: {}", count);
        return count;
    }

    /**
     * 유효 AI 수만큼 최다 득표자에게(여러 명일 경우 랜덤) 투표 하기
     **/
    public Integer randomVoteTargetAI(int[] voteCounts) {
        log.debug("Start randomVoteTargetAI method");
        List<Integer> randomVoteList = new ArrayList<>();
        int maxVotes = Arrays.stream(voteCounts).max().orElse(0);   // 최다 득표수
        log.debug("maxVotes: {}", maxVotes);
        for (int i = 1; i < voteCounts.length; i++) {
            if (voteCounts[i] == maxVotes) {
                randomVoteList.add(i);
            }
        }

        int randomIndex = (int) (Math.random() * randomVoteList.size());
        Integer randomTarget = randomVoteList.get(randomIndex);
        log.debug("RandomTarget: {}", randomTarget);
        return randomTarget;
    }
}



