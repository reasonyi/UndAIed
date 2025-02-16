package com.ssafy.undaied.socket.stage.service;

import com.corundumstudio.socketio.SocketIONamespace;
import com.ssafy.undaied.socket.chat.dto.response.GameChatResponseDto;
import com.ssafy.undaied.socket.chat.service.GameChatService;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.common.util.GameTimer;
import com.ssafy.undaied.socket.common.util.GameTimerConstants;
import com.ssafy.undaied.socket.infect.service.InfectService;
import com.ssafy.undaied.socket.init.service.GameInitService;
import com.ssafy.undaied.socket.result.service.GameResultService;
import com.ssafy.undaied.socket.stage.constant.StageType;
import com.ssafy.undaied.socket.stage.dto.response.RoundNotifyDto;
import com.ssafy.undaied.socket.stage.dto.response.StageNotifyDto;
import com.ssafy.undaied.socket.vote.dto.response.VoteResultResponseDto;
import com.ssafy.undaied.socket.vote.service.VoteService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StageService {

    private final SocketIONamespace namespace;
    private final RedisTemplate redisTemplate;
    private final GameTimer gameTimer;

    private final VoteService voteService;
    private final InfectService infectService;
    private final GameChatService gameChatService;
    private final GameResultService gameResultService;
    private final GameInitService gameInitService;

    private static final Map<String, Integer> STAGE_DURATIONS = Map.of(
            "notify", 1,
            "result", 2,
            StageType.SUBJECT_DEBATE.getRedisValue(), 15,  // 2분
            StageType.FREE_DEBATE.getRedisValue(), 10,     // 3분
            StageType.VOTE.getRedisValue(), 10             // 30초
    );

    public void handleGameStart(Integer gameId) {
        String roundKey = "game:" + gameId + ":round";
        redisTemplate.opsForValue().set(roundKey, "0");

        handleNotifyStartStage(gameId, StageType.START);
        gameTimer.setTimer(gameId, GameTimerConstants.STAGE_START_NOTIFY, STAGE_DURATIONS.get("notify"), () -> {
            try {
                startStage(gameId, StageType.DAY);
            } catch (Exception e) {
                handleGameError(gameId, e);
            }
        });
    }

    private void startStage(Integer gameId, StageType currentStage) throws SocketException {
        log.debug("Starting stage: {}", currentStage.getRedisValue());

        try {
            // 현재 스테이지 상태 저장
            saveCurrentStage(gameId, currentStage);

            // 테스트에서 2라운드까지만 진행하고 종료함
            if (currentStage == StageType.NIGHT && getCurrentRound(gameId).equals("2")) {
                gameOver(gameId);
                return;
            }
            // 낮인 경우에만 라운드 알림
            if (currentStage == StageType.DAY) {
                saveCurrentRound(gameId);
                // 라운드 알림
                RoundNotifyDto roundNotifyDto = RoundNotifyDto.notifyRoundStart(getCurrentRound(gameId));
                namespace.getRoomOperations("game:" + gameId).sendEvent(EventType.GAME_CHAT_SEND.getValue(), roundNotifyDto);
            }

            // 2라운드 종료 체크
            if (currentStage == StageType.NIGHT && getCurrentRound(gameId).equals("2")) {
                gameOver(gameId);
                return;
            }

            // 스테이지 시작 알림
            gameTimer.setTimer(gameId, GameTimerConstants.STAGE_START_NOTIFY, STAGE_DURATIONS.get("notify"), () -> {
                handleNotifyStartStage(gameId, currentStage);

                // 스테이지별 메인 로직 실행
                handleStageUpdate(gameId, currentStage);

            });
        } catch (Exception e) {
            log.error("Error in startStage: {}", e.getMessage());
            handleGameError(gameId, e);
//            throw new SocketException(SocketErrorCode.STAGE_START_FAILED);
        }
    }

    private void handleNotifyStartStage(Integer gameId, StageType currentStage) {
        StageNotifyDto stageNotifyDto = StageNotifyDto.notifyStartStage(currentStage);
        namespace.getRoomOperations("game:" + gameId).sendEvent(EventType.GAME_CHAT_SEND.getValue(), stageNotifyDto);
    }

    private void handleNotifyEndStage(Integer gameId, StageType currentStage) {
        StageNotifyDto stageNotifyDto = StageNotifyDto.notifyEndStage(currentStage);
        namespace.getRoomOperations("game:" + gameId).sendEvent(EventType.GAME_CHAT_SEND.getValue(), stageNotifyDto);
    }

    private void handleStageUpdate(Integer gameId, StageType currentStage) {
        gameInitService.sendGameInfo(gameId);
        switch (currentStage) {
            case START -> handleGameStart(gameId);
            case DAY -> handleDayStage(gameId);
            case SUBJECT_DEBATE -> handleSubjectDebate(gameId);
            case FREE_DEBATE -> handleFreeDebate(gameId);
            case VOTE -> handleVote(gameId);
            case NIGHT -> handleNight(gameId);
            case FINISH -> handleGameEnd(gameId);
        }
    }

    private void handleDayStage(Integer gameId) {
        // 2라운드부터 감염 처리
        if (Integer.parseInt(getCurrentRound(gameId)) > 1) {
            handleInfection(gameId);
        }

        gameTimer.setTimer(gameId, GameTimerConstants.STAGE_END_NOTIFY, STAGE_DURATIONS.get("notify"), () -> {
            try {
                startStage(gameId, StageType.SUBJECT_DEBATE);
            } catch (Exception e) {
                handleGameError(gameId, e);
            }
        });
    }

    private void handleInfection(Integer gameId) {
        try {
            String infectedPlayerNumber = infectService.infectPlayer(gameId);
            log.info("InfectedPlayerNumber: {}", infectedPlayerNumber);
            namespace.getRoomOperations("game:" + gameId).sendEvent(
                    EventType.GAME_CHAT_SEND.getValue(),
                    Map.of("number", 0, "content", "밤 사이에 인간 플레이어가 AI에게 감염되었습니다.")
            );
        } catch (Exception e) {
            log.error("Infection stage error: {}", e.getMessage());
        }
    }

    private void handleSubjectDebate(Integer gameId) {
        gameTimer.setTimer(gameId, GameTimerConstants.STAGE_START_NOTIFY, STAGE_DURATIONS.get("notify"), () -> {
            gameChatService.sendSubject(gameId);

            gameTimer.setTimer(gameId, GameTimerConstants.STAGE_MAIN, STAGE_DURATIONS.get(StageType.SUBJECT_DEBATE.getRedisValue()), () -> {
                handleNotifyEndStage(gameId, StageType.SUBJECT_DEBATE);

                gameTimer.setTimer(gameId, GameTimerConstants.EVENT_RESULT, STAGE_DURATIONS.get("result"), () -> {
                    String currentRound = getCurrentRound(gameId);
                    List<GameChatResponseDto> subjectChatList =
                            gameChatService.getSubjectDebateChats(gameId, currentRound);
                    namespace.getRoomOperations("game:" + gameId)
                            .sendEvent(EventType.CHAT_SUBJECT_SEND.getValue(), subjectChatList);

                    try {
                        startStage(gameId, StageType.FREE_DEBATE);
                    } catch (Exception e) {
                        handleGameError(gameId, e);
                    }
                });
            });
        });
    }

    private void handleFreeDebate(Integer gameId) {
        gameTimer.setTimer(gameId, GameTimerConstants.STAGE_START_NOTIFY, STAGE_DURATIONS.get("notify"), () -> {
            gameTimer.setTimer(gameId, GameTimerConstants.STAGE_MAIN, STAGE_DURATIONS.get(StageType.FREE_DEBATE.getRedisValue()), () -> {
                handleNotifyEndStage(gameId, StageType.FREE_DEBATE);

                gameTimer.setTimer(gameId, GameTimerConstants.STAGE_END_NOTIFY, 1, () -> {
                    try {
                        startStage(gameId, StageType.VOTE);
                    } catch (Exception e) {
                        handleGameError(gameId, e);
                    }
                });
            });
        });
    }

    private void handleVote(Integer gameId) {
        gameTimer.setTimer(gameId, GameTimerConstants.STAGE_START_NOTIFY, STAGE_DURATIONS.get("notify"), () -> {
            gameTimer.setTimer(gameId, GameTimerConstants.STAGE_MAIN, STAGE_DURATIONS.get(StageType.VOTE.getRedisValue()), () -> {
                handleNotifyEndStage(gameId, StageType.VOTE);

                gameTimer.setTimer(gameId, GameTimerConstants.EVENT_RESULT, STAGE_DURATIONS.get("result"), () -> {
                    VoteResultResponseDto responseDto = voteService.computeVoteResult(gameId);
                    namespace.getRoomOperations("game:" + gameId)
                            .sendEvent(EventType.GAME_CHAT_SEND.getValue(), responseDto);

                    try {
                        startStage(gameId, StageType.NIGHT);
                    } catch (Exception e) {
                        handleGameError(gameId, e);
                    }
                });
            });
        });
    }

    private void handleNight(Integer gameId) {
        if (getCurrentRound(gameId).equals("2")) {
            gameOver(gameId);
            return;
        }

        gameTimer.setTimer(gameId, GameTimerConstants.STAGE_START_NOTIFY, STAGE_DURATIONS.get("notify"), () -> {
            try {
                startStage(gameId, StageType.DAY);
            } catch (Exception e) {
                handleGameError(gameId, e);
            }
        });
    }

    private void handleGameEnd(Integer gameId) {
        gameTimer.setTimer(gameId, GameTimerConstants.GAME_END, STAGE_DURATIONS.get("notify"), () -> {
            gameOver(gameId);
        });
    }

    private void saveCurrentStage(Integer gameId, StageType currentStage) {
        String stageKey = "game:" + gameId + ":stage";
        redisTemplate.opsForValue().set(stageKey, currentStage.getRedisValue());
    }

    public String getCurrentStage(Integer gameId) {
        String stageKey = "game:" + gameId + ":stage";
        String currentStage = redisTemplate.opsForValue().get(stageKey).toString();

        return currentStage;
    }

    private void saveCurrentRound(Integer gameId) {
        String roundKey = "game:" + gameId + ":round";
        redisTemplate.opsForValue().increment(roundKey);
    }

    public String getCurrentRound(Integer gameId) {
        String roundKey = "game:" + gameId + ":round";
        String currentRound = redisTemplate.opsForValue().get(roundKey).toString();

        return currentRound;
    }

    private void handleGameError(Integer gameId, Exception e) {
        log.error("Game error in gameId {}: {}", gameId, e.getMessage());
        // 게임 에러 발생 시 클라이언트에게 알림
//        namespace.getRoomOperations("game:" + gameId)
//                .sendEvent(EventType.GAME_ERROR.getValue(),
//                        e instanceof SocketException ?
//                                ((SocketException)e).getErrorCode().getMessage() :
//                                "게임 진행 중 오류가 발생했습니다");
    }

    private void gameOver(Integer gameId) {
        // 게임 종료 로직
        // gameTimer에서 타이머 데이터 삭제
        gameTimer.cleanupGame(gameId);
    }

}

