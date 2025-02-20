package com.ssafy.undaied.socket.stage.service;

import com.corundumstudio.socketio.SocketIONamespace;
import com.ssafy.undaied.socket.chat.dto.response.GameChatResponseDto;
import com.ssafy.undaied.socket.chat.service.GameChatService;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.common.util.GameTimer;
import com.ssafy.undaied.socket.common.util.GameTimerConstants;
import com.ssafy.undaied.socket.infect.dto.InfectResponseDto;
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
            StageType.SUBJECT_DEBATE.getRedisValue(), 20,  // 20초
            StageType.FREE_DEBATE.getRedisValue(), 40,     // 40초
            StageType.VOTE.getRedisValue(), 10                 // 10초
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
        gameInitService.sendGameInfo(gameId);
    }

    private void startStage(Integer gameId, StageType currentStage) throws SocketException {
        log.debug("Starting stage: {}", currentStage.getRedisValue());

        try {
            // 현재 스테이지 상태 저장
            saveCurrentStage(gameId, currentStage);
            if (currentStage.equals(StageType.DAY)) {
                // 라운드 알림
                saveCurrentRound(gameId);
                RoundNotifyDto roundNotifyDto = RoundNotifyDto.notifyRoundStart(getCurrentRound(gameId));
                namespace.getRoomOperations("game:" + gameId).sendEvent(EventType.GAME_CHAT_SEND.getValue(), roundNotifyDto);
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
        switch (currentStage) {
            case DAY -> handleDayStage(gameId);
            case SUBJECT_DEBATE -> handleSubjectDebate(gameId);
            case FREE_DEBATE -> handleFreeDebate(gameId);
            case VOTE -> handleVote(gameId);
            case NIGHT -> handleNight(gameId);
        }
    }

    private void handleDayStage(Integer gameId) {

        // 2라운드부터 감염 처리
        if (Integer.parseInt(getCurrentRound(gameId)) > 1) {
            gameTimer.setTimer(gameId, GameTimerConstants.STAGE_START_NOTIFY, 1, () -> {
                try {
                    handleInfection(gameId);
                    gameInitService.sendGameInfo(gameId);

                    String winner = gameResultService.checkGameResult(gameId);
                    if (winner != null) {
                        handleGameEnd(gameId, winner);
                    } else {
                        // 승자가 없는 경우 다음 스테이지로
                        // 시작 시 5초
                        gameTimer.setTimer(gameId, GameTimerConstants.STAGE_END_NOTIFY, 5, () -> {
                            try {
                                startStage(gameId, StageType.SUBJECT_DEBATE);
                            } catch (Exception e) {
                                handleGameError(gameId, e);
                            }
                        });
                        gameInitService.sendGameInfo(gameId);
                    }
                } catch (SocketException e) {
                    log.error("🍳게임 결과 처리 중 오류 : {} ", e.getMessage());
                }
                // 시작 시 5초
                gameTimer.setTimer(gameId, GameTimerConstants.STAGE_END_NOTIFY, 5, () -> {
                    try {
                        startStage(gameId, StageType.SUBJECT_DEBATE);
                    } catch (Exception e) {
                        handleGameError(gameId, e);
                    }
                });
                gameInitService.sendGameInfo(gameId);
            });
        }

        try {
            String winner = gameResultService.checkGameResult(gameId);
            if (winner != null) {
                handleGameEnd(gameId, winner);
            }
        } catch (SocketException e) {
            log.error("🍳게임 결과 처리 중 오류 : {} ", e.getMessage());
        }
        // 시작 시 5초
        gameTimer.setTimer(gameId, GameTimerConstants.STAGE_END_NOTIFY, 5, () -> {
            try {
                startStage(gameId, StageType.SUBJECT_DEBATE);
            } catch (Exception e) {
                handleGameError(gameId, e);
            }
        });
        gameInitService.sendGameInfo(gameId);

    }

    private void handleInfection(Integer gameId) {
        try {
            InfectResponseDto responseDto = infectService.infectPlayer(gameId);
            namespace.getRoomOperations("game:" + gameId).sendEvent(
                    EventType.GAME_CHAT_SEND.getValue(), responseDto
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
                            log.debug("주제토론 뭉치 넘기기");
                    namespace.getRoomOperations("game:" + gameId)
                            .sendEvent(EventType.CHAT_SUBJECT_SEND.getValue(), subjectChatList);

                    try {
                        startStage(gameId, StageType.FREE_DEBATE);
                    } catch (Exception e) {
                        handleGameError(gameId, e);
                    }
                });
            });
            gameInitService.sendGameInfo(gameId);
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
            gameInitService.sendGameInfo(gameId);
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
                        log.debug("🍳vote event end");
                        gameInitService.sendGameInfo(gameId);

                        try {
                            String winner = gameResultService.checkGameResult(gameId);
                            if (winner != null) {
                                handleGameEnd(gameId, winner);
                            }
                        } catch (SocketException e) {
                            log.error("🍳게임 결과 처리 중 오류 : {} ", e.getMessage());
                        }

                        startStage(gameId, StageType.NIGHT);
                    } catch (Exception e) {
                        handleGameError(gameId, e);
                    }
                });
            });
            gameInitService.sendGameInfo(gameId);
        });
    }

    private void handleNight(Integer gameId) {
        // 밤 진행 시간 5초
        gameTimer.setTimer(gameId, GameTimerConstants.STAGE_START_NOTIFY, 5, () -> {
            try {
                startStage(gameId, StageType.DAY);
            } catch (Exception e) {
                handleGameError(gameId, e);
            }
        });
        gameInitService.sendGameInfo(gameId);
    }

    private void handleGameEnd(Integer gameId, String winner) {
        saveCurrentStage(gameId, StageType.FINISH);
        gameInitService.sendGameInfo(gameId);

        handleNotifyEndStage(gameId, StageType.FINISH);
        gameTimer.setTimer(gameId, GameTimerConstants.GAME_END, STAGE_DURATIONS.get("notify"), () -> {
            gameOver(gameId, winner);
        });
        gameInitService.sendGameInfo(gameId);
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

    private void gameOver(Integer gameId, String winner) {
        // 게임 종료 로직
        try {
            gameResultService.gameEnd(gameId, winner);
        } catch (SocketException e) {
            log.error(e.getMessage());
        }
        // gameTimer에서 타이머 데이터 삭제
        gameTimer.cleanupGame(gameId);
    }

}

