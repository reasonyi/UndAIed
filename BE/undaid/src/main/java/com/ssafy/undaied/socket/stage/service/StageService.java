package com.ssafy.undaied.socket.stage.service;
import com.corundumstudio.socketio.SocketIONamespace;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.common.exception.SocketException;
import com.ssafy.undaied.socket.common.util.GameTimer;
import com.ssafy.undaied.socket.common.util.GameTimerConstants;
import com.ssafy.undaied.socket.infect.service.InfectService;
import com.ssafy.undaied.socket.stage.constant.StageType;
import com.ssafy.undaied.socket.stage.dto.response.RoundNotifyDto;
import com.ssafy.undaied.socket.stage.dto.response.StageNotifyDto;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class StageService {

    private final SocketIONamespace namespace;
    private final RedisTemplate redisTemplate;
    private final GameTimer gameTimer;

    //    서비스 파일들 불러봐야됨
//    private final VoteService voteService;
    private final InfectService infectService;

    private static final Map<StageType, Integer> STAGE_DURATIONS = Map.of(
            StageType.SUBJECT_DEBATE, 2,  // 2분
            StageType.FREE_DEBATE, 2,     // 3분
            StageType.VOTE, 2             // 30초
    );

    public void handleGameStart(Integer gameId) {
        String roundKey = "game:" + gameId + ":round";
        redisTemplate.opsForValue().set(roundKey, "0");
        try {
            startStage(gameId, StageType.START);
        } catch (Exception e) {
            handleGameError(gameId, e);
        }
    }

    private void startStage(Integer gameId, StageType currentStage) throws SocketException {
        // FINISH 상태일 때는 게임 종료하고 바로 리턴
        if (currentStage == StageType.FINISH) {
            gameOver(gameId);
            return;
        }

        saveCurrentStage(gameId, currentStage);

        if (currentStage == StageType.DAY) {
            String roundKey = "game:" + gameId + ":round";

            saveCurrentRound(gameId);
            String currentRound = redisTemplate.opsForValue().get(roundKey).toString();
            // 라운드 알림
            RoundNotifyDto roundNotifyDto = RoundNotifyDto.notifyRoundStart(currentRound);

            try {
                String infectedPlayerNumber = infectService.infectPlayer(gameId);
                // 감염된 플레이어 정보를 사용한 추가 로직
                namespace.getRoomOperations("game:"+gameId).sendEvent(EventType.GAME_CHAT_SEND.getValue(),
                        Map.of("number", 0,
                                "content", "밤 사이에 인간 플레이어가 AI에게 감염되었습니다."));
            } catch (SocketException e) {
                log.error("Infection stage error: {}", e.getMessage());
                throw e;  // 상위로 예외를 전파
            }

            namespace.getRoomOperations("game:"+gameId).sendEvent(EventType.GAME_CHAT_EMIT.getValue(), roundNotifyDto);

            gameTimer.setTimer(gameId, GameTimerConstants.MAIN_NOTIFY, 2, () -> {
                // 낮 알림
                handleNotifyStartStage(gameId, currentStage);
                gameTimer.setTimer(gameId, GameTimerConstants.SUB_MAIN, 1, () -> {
                    try {
                        StageType nextStage = getNextStage(currentStage);
                        startStage(gameId, nextStage);
                    } catch (Exception e) {
                        handleGameError(gameId, e);
                    }
                });
            });
            return;

        }

        if (currentStage == StageType.NIGHT) {
            // 밤 알림
            handleNotifyStartStage(gameId, currentStage);

            gameTimer.setTimer(gameId, GameTimerConstants.MAIN_NOTIFY, 2, () -> {
                try {
                    StageType nextStage = getNextStage(currentStage);
                    startStage(gameId, nextStage);
                } catch (Exception e) {
                    handleGameError(gameId, e);
                }
            });
            return;

        }

        // 스테이지 시작 알림
        handleNotifyStartStage(gameId, currentStage);

        gameTimer.setTimer(gameId, GameTimerConstants.SUB_NOTIFY, 1, () -> {
            handleStageUpdate(gameId, currentStage);

            if (STAGE_DURATIONS.containsKey(currentStage)) {
                // 스테이지 진행 (STAGE_DURATION)
                gameTimer.setTimer(gameId, GameTimerConstants.SUB_MAIN, STAGE_DURATIONS.get(currentStage), () -> {
                    // 스테이지 종료 알림
                    handleNotifyEndStage(gameId, currentStage);

                    gameTimer.setTimer(gameId, GameTimerConstants.SUB_NOTIFY, 1, () -> {
                        StageType nextStage = getNextStage(currentStage);

                        if (currentStage == StageType.VOTE) {
                            // 투표 결과 알림 (2초)
//                            voteService.computeVoteResult(gameId);
                            System.out.println("투표 결과 알림");
                            gameTimer.setTimer(gameId, GameTimerConstants.VOTE_RESULT, 2, () -> {
                                try {
                                    startStage(gameId, nextStage);
                                } catch (Exception e) {
                                    handleGameError(gameId, e);
                                }
                            });
                        } else {
                            try {
                                startStage(gameId, nextStage);
                            } catch (Exception e) {
                                handleGameError(gameId, e);
                            }                        }
                    });
                });
            } else {
                StageType nextStage = getNextStage(currentStage);
                try {
                    startStage(gameId, nextStage);
                } catch (Exception e) {
                    handleGameError(gameId, e);
                }            }
        });
    }


    private void handleNotifyStartStage(Integer gameId, StageType currentStage) {
        StageNotifyDto stageNotifyDto = StageNotifyDto.notifyStartStage(currentStage);
        namespace.getRoomOperations("game:"+gameId).sendEvent(EventType.GAME_CHAT_EMIT.getValue(), stageNotifyDto);
    }

    private void handleStageUpdate(Integer gameId, StageType currentStage) {
        switch (currentStage) {
            case DAY ->
//                boolean isGameOver = gameResultHandler.onComputeGameResult();
//                if (isGameOver) {
//                    HandleGameOver();
//                }
                    System.out.println("낮");

            case SUBJECT_DEBATE -> System.out.println("주제 토론");
            case FREE_DEBATE -> System.out.println("자유 토론");
//                    freeDebateHandler.onSTartFreeDebate();
            case VOTE -> {System.out.println("투표");}
//                voteService.computeVoteResult(gameId);}
            case INFECTION -> System.out.println("감염");
//                    infectionHandler.onStartInfectionHandler;
            case NIGHT -> System.out.println("밤");
            case FINISH -> System.out.println("게임 종료");
            case START -> System.out.println("게임 시작");
        }
    }

    private void handleNotifyEndStage(Integer gameId, StageType currentStage) {
        StageNotifyDto stageNotifyDto = StageNotifyDto.notifyEndStage(currentStage);
        namespace.getRoomOperations("game:"+gameId).sendEvent(EventType.GAME_CHAT_EMIT.getValue(), stageNotifyDto);
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

    private StageType getNextStage(StageType currentStage) {
        return switch (currentStage) {
            case START -> StageType.DAY;    // NIGHT 추가 해줘야 됨
            case DAY -> StageType.SUBJECT_DEBATE;
            case SUBJECT_DEBATE -> StageType.FREE_DEBATE;
            case FREE_DEBATE -> StageType.VOTE;
            case VOTE -> StageType.NIGHT;
            default -> StageType.FINISH;
        };
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
        System.out.println("게임 종료");
    }

}

