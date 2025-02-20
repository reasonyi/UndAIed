package com.ssafy.undaied.socket.vote.service;

import com.ssafy.undaied.socket.common.exception.SocketErrorCode;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.common.util.GameTimer;
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
    private final GameTimer gameTimer;
    private final int PLAYER = 8;

    // 투표 제출
    public VoteSubmitResponseDto submitVote(Integer voterUserId, Integer gameId, VoteSubmitRequestDto voteSubmitRequestDto)
            throws SocketException {
        String userNumberKey = "game:" + gameId + ":number_mapping";
        String roundKey = String.format("game:%d:round", gameId);
        String currentRound = redisTemplate.opsForValue().get(roundKey);

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
            log.debug("Round: " + currentRound +", voterNumber: " + voterNumber + ", targetNumber: " + targetNumber);

            String statusKey = "game:" + gameId + ":player_status";
            String statusStr = redisTemplate.opsForHash().get(statusKey, voterNumber).toString();

            // 죽은 사람에게 투표했을 때
            if (!isValidTarget(gameId, targetNumber))
                throw new SocketException(SocketErrorCode.VOTE_INVALID_TARGET);
            // 죽은 플레이어가 투표에 참여했을 때
            if (isVoterDied(statusStr))
                throw new SocketException(SocketErrorCode.VOTE_DIED_PLAYER);
            // 플레이어가 참여 중이 아닐 때
            if (!isVoterInGame(statusStr))
                throw new SocketException(SocketErrorCode.PLAYER_NOT_IN_GAME);
            // 본인에게 투표했을 때
            if (voterNumber.equals(targetNumber))
                throw new SocketException(SocketErrorCode.VOTE_SELF_TARGET);


            // 현재 투표 시간이 아닌 경우
            String stageKey = "game:" + gameId + ":stage";
            String currentStage = redisTemplate.opsForValue().get(stageKey);
            log.debug("🍳currentRound: "+ currentRound+ ", currentStage: " + currentStage + ", VOTE in redis: " + StageType.VOTE.getRedisValue());

            if (!currentStage.equals(StageType.VOTE.getRedisValue()) || !gameTimer.isMainStage(gameId))
                throw new SocketException(SocketErrorCode.VOTE_STAGE_INVALID);

            String eventKey = "game:" + gameId + ":round:" + currentRound + ":events";

            // 이미 투표한 경우
            if (hasVoted(eventKey, voterNumber))
                throw new SocketException(SocketErrorCode.VOTE_ALREADY_SUBMITTED);

            storeVoteSubmitEvent(gameId, voterNumber, targetNumber, eventKey);

            VoteSubmitResponseDto responseDto = VoteSubmitResponseDto.builder()
                    .number(Integer.parseInt(targetNumber))
                    .build();
            return responseDto;

        } catch (SocketException e) {
            log.error("Error in submitVote: ", e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error while voting : {}", e.getMessage());
            throw new SocketException(SocketErrorCode.VOTE_SUBMIT_FAILED);
        }
    }


    public boolean isVoterDied(String statusStr) {
        boolean isDied = statusStr.contains("isDied=true");
        return isDied;
    }

    public boolean isVoterInGame(String statusKey) {
        boolean isVoterInGame = statusKey.contains("isInGame=true");
        return isVoterInGame;
    }

    public boolean isValidTarget(Integer gameId, String targetNumber) {
        String statusKey = "game:" + gameId + ":player_status";
        String statusStr = redisTemplate.opsForHash().get(statusKey, targetNumber).toString();
        boolean isDied = statusStr.contains("isDied=true");

        return !isDied;
    }

    public boolean hasVoted(String eventKey, String voterNumber) {
        log.debug("🍳Check voter has Voted ...");

        String events = redisTemplate.opsForValue().get(eventKey);
        if (events == null || events.trim().isEmpty()) return false;

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

        String statusKey = "game:" + gameId + ":player_status";
        String aiKey = "game:" + gameId + ":ai_numbers";

//        // 유효 플레이어 수 계산
//        int playerCount = countValidPlayers(gameId, statusKey);
//        log.debug("Valid player count: {}", playerCount);

        // 투표를 카운트할 배열 : 인덱스가 익명 number, 값은 익명 number가 받은 투표 수
        int[] voteCounts = new int[PLAYER + 1];

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
            log.debug("🍳Round: {}, VoteCounts: {}", currentRound, Arrays.stream(voteCounts).toArray());
            log.debug("🍳Round: {}, Vote counting completed", currentRound);
        }

         // AI가 아닌 최다 득표수 찾기
        log.debug("🍳Start find maxVotes excepting AI");
        Set<String> aiSet = redisTemplate.opsForSet().members(aiKey);
        log.debug("🍳AI set : {}", aiSet.stream().toArray());

        int maxVotes = 0;
        for (int i=1; i< voteCounts.length; i++) {
            if (!aiSet.contains(String.valueOf(i)) && maxVotes < voteCounts[i]){
                maxVotes = voteCounts[i];
            }
        }
        log.debug("🍳Max Vote Counts: {}", maxVotes);

        // AI가 아니면서 최다 득표수와 같은 플레이어 찾기
        List<Integer> randomTargetCandidates = new ArrayList<>();

        for (int i = 1; i < voteCounts.length; i++) {
            if (!aiSet.contains(String.valueOf(i)) && voteCounts[i] == maxVotes) {
                String statusStr = redisTemplate.opsForHash().get(statusKey, String.valueOf(i)).toString();
                if (!statusStr.contains("isDied=true"))
                    randomTargetCandidates.add(i);
                log.debug("Player {} has maximum votes", i);
            }
        }
        log.debug("Found {} players with maximum votes", randomTargetCandidates.size());

        // AI 투표 처리
        // 가장 많은 표를 받은 사람 중 랜덤으로 타켓 선정
        int randomTarget = randomVoteTargetAI(randomTargetCandidates, statusKey, aiKey);
        log.debug("--------RandomTarget: {}", randomTarget);

        int AICount = countValidAIs(gameId, statusKey, aiKey);
        voteCounts[randomTarget] += AICount;
        log.debug("AI votes ({} votes) added to player {}", AICount, randomTarget);

        // 살아있는 AI만 필터링
        for (String aiNumber : aiSet) {
            String statusStr = redisTemplate.opsForHash().get(statusKey, aiNumber).toString();
            if (!statusStr.contains("isDied=true")) {
                storeVoteSubmitEvent(gameId, aiNumber, String.valueOf(randomTarget), eventKey);
            }
        }
        log.debug("🍳AI vote events stored for {} AIs targeting player {}", AICount, randomTarget);

        // AI 투표까지 종료 후 최다 득표자 찾기 => 최다 득표자가 여러 명일 경우 비김
        List<Integer> maxVotedCandidates = new ArrayList<>();
        int finalMaxVotes = Arrays.stream(voteCounts)
                .max()
                .orElse(0);
        log.debug("🍳Final maximum votes {}", finalMaxVotes);
        for (int i = 1; i < voteCounts.length; i++) {
            if (voteCounts[i] == finalMaxVotes) {
                maxVotedCandidates.add(i);
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
            return VoteResultResponseDto.notifyDraw(maxVotedCandidates, finalMaxVotes);
        } else {
            String eliminatedNumber = String.valueOf(maxVotedCandidates.get(0));
            log.info("Player {} eliminated with {} votes", eliminatedNumber, finalMaxVotes);

            String statusStr = redisTemplate.opsForHash().get(statusKey, eliminatedNumber).toString();
            log.debug("statusStr: {}", statusStr);
            boolean isInfected = statusStr.contains("isInfected=true");
            log.debug("EliminatedPlayer is Infected: {}", isInfected);
            boolean isAI = redisTemplate.opsForSet().isMember(aiKey, eliminatedNumber);
            log.debug("EliminatedPlayer is AI: {}", isAI);

            log.debug("Eliminated player status - AI: {}, Infected: {}", isAI, isInfected);

            // player_status 변경
            Map<Object, Object> playerStatus = redisTemplate.opsForHash().entries(statusKey);

            String newStatus = playerStatus.get(eliminatedNumber).toString().replace("isDied=false", "isDied=true");
            redisTemplate.opsForHash().put(statusKey, eliminatedNumber, newStatus);
            log.debug("🍳Store new player_status data in Redis : {}", redisTemplate.opsForHash().get(statusKey, eliminatedNumber));

            // 처형자가 AI인 경우 평가지표를 위해 redis에 따로 저장함
            if (isAI) {
                String aiId = findAIIdByNumber(gameId, eliminatedNumber);
                String aiDiedKey = String.format("game:%d:ai_died", gameId);
                redisTemplate.opsForHash().put(aiDiedKey, aiId, currentRound);
                log.debug("🍳AI died: AI Id : {}, Round : {}", aiId, currentRound);
            }

            // 투표 이벤트 저장
            String userNameKey = "game:" + gameId + ":number_nicknames";
            String eliminatedName = redisTemplate.opsForHash().get(userNameKey, eliminatedNumber).toString();

            String voteEvent = String.format("{vote_result} [null] <null> (%s) ~%s~ %s | ",
                    eliminatedName, eliminatedNumber, LocalDateTime.now());
            redisTemplate.opsForValue().append(eventKey, voteEvent);
            log.debug("🍳Store vote event data in Redis");
            return VoteResultResponseDto.notifyVoteResult(eliminatedNumber, finalMaxVotes, isAI, isInfected);
        }
    }


    /**
     * 유효 투표자 수 구하기
     **/
    public int countValidPlayers(Integer gameId, String statusKey) {
        log.debug("Counting valid players for game {}", gameId);

        Map<Object, Object> playerStatuses = redisTemplate.opsForHash().entries(statusKey);
        int count = 0;
        for (Map.Entry<Object, Object> entry : playerStatuses.entrySet()) {
            String status = entry.getValue().toString();
            if (!status.contains("isDied=true")) count++;
        }

        log.debug("Valid players count: {}", count);
        return count;
    }

    /**
     * 유효 AI 수 구하기
     **/
    public int countValidAIs(Integer gameId, String statusKey, String aiKey) {
        Map<Object, Object> playerStatuses = redisTemplate.opsForHash().entries(statusKey);
        Set<String> aiSet = redisTemplate.opsForSet().members(aiKey);

        int count = 0;
        for (Map.Entry<Object, Object> entry : playerStatuses.entrySet()) {
            String status = entry.getValue().toString();
            if (!status.contains("isDied=true")
                    && aiSet.contains(entry.getKey().toString())) count++;
        }

        log.debug("Valid AIs count: {}", count);
        return count;
    }

    /**
     * 유효 AI 수만큼 최다 득표자에게(여러 명일 경우 랜덤) 투표 하기
     **/
    public Integer randomVoteTargetAI(List<Integer> randomTargetCandidates, String statusKey, String aiKey) {
        log.debug("Start randomVoteTargetAI method : Candidates : {}", randomTargetCandidates.size());

        int randomIndex = (int) (Math.random() * randomTargetCandidates.size());
        Integer randomTarget = randomTargetCandidates.get(randomIndex);
        log.debug("RandomTarget: {}", randomTarget);

        return randomTarget;
    }

    // AI 익명 number로 AI Id 찾기
    public String findAIIdByNumber(Integer gameId, String eliminatedNumber) {
        log.debug("🍳Starting find AI Id by eliminatedNumber");
        String userNumberKey = String.format("game:%d:number_mapping", gameId);

        Map<Object, Object> userMapping = redisTemplate.opsForHash().entries(userNumberKey);
        // userMapping 에서 키를 찾아야 함
        for (Map.Entry<Object, Object> entry : userMapping.entrySet()) {
            if (entry.getValue().toString().equals(eliminatedNumber)) {
                String aiId = entry.getKey().toString();
                log.debug("🍳eliminated AI id: {}", aiId);

                if(aiId.startsWith("-")) {
                    return aiId.substring(1);
                }
            }
        }
        return null;
    }

    public void storeVoteSubmitEvent(Integer gameId, String voterNumber, String targetNumber, String eventKey) {
        // 투표 이벤트 저장을 위한 닉네임 찾기
        String userNameKey = "game:" + gameId + ":number_nicknames";
        String voteUserName = redisTemplate.opsForHash().get(userNameKey, voterNumber).toString();
        String targetUserName = redisTemplate.opsForHash().get(userNameKey, targetNumber).toString();
        log.info("voteUserName: " + voteUserName + ", targetUserName: " + targetUserName);
        String voteEvent = String.format("{vote} [%s] <%s> (%s) ~%s~ %s | ",
                voteUserName, voterNumber, targetUserName, targetNumber, LocalDateTime.now());

        // 투표 이벤트 저장
        redisTemplate.opsForValue().append(eventKey, voteEvent);
    }
}



