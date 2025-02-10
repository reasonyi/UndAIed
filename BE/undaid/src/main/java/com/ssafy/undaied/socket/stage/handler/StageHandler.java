package com.ssafy.undaied.socket.stage.handler;

import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.common.util.GameTimer;
import com.ssafy.undaied.socket.stage.response.dto.RoundNotifyDto;
import com.ssafy.undaied.socket.stage.response.dto.StageNotifyDto;
import com.ssafy.undaied.socket.stage.constant.StageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class StageHandler {

    private final SocketIOServer server;
    private final GameTimer gameTimer;

//    서비스 파일들 불러봐야됨
//    private final SubjectDebateHandler subjectDebateHandler;
//    private final VoteService voteService;

    private final Map<Integer, StageType> currentStageMap = new ConcurrentHashMap<>();
    private final Map<Integer, Integer> currentRoundMap = new ConcurrentHashMap<>();

    private static final Map<StageType, Integer> STAGE_DURATIONS = Map.of(
            StageType.SUBJECT_DEBATE, 2,  // 2분
            StageType.FREE_DEBATE, 2,     // 3분
            StageType.VOTE, 2             // 30초
    );

    public void handleGameStart(Integer gameId) {
        currentRoundMap.put(gameId, 0);
        startStage(gameId, StageType.START);
    }

    private void startStage(Integer gameId, StageType currentStage) {
        // FINISH 상태일 때는 게임 종료하고 바로 리턴
        if (currentStage == StageType.FINISH) {
            gameOver(gameId);
            return;
        }

        saveCurrentStage(gameId, currentStage);

        if (currentStage == StageType.DAY) {
            // 라운드 알림
            saveCurrentRound(gameId);
            RoundNotifyDto roundNotifyDto = RoundNotifyDto.notifyRoundStart(currentRoundMap.get(gameId));
            server.getRoomOperations(String.valueOf(gameId)).sendEvent(EventType.GAME_CHAT.getValue(), roundNotifyDto);

            gameTimer.setTimer(gameId, 1, 1, () -> {
                // 낮 알림
                handleNotifyStartStage(gameId, currentStage);

                gameTimer.setTimer(gameId, 2, 2, () -> {
                    StageType nextStage = getNextStage(currentStage);
                    startStage(gameId, nextStage);
                });
            });
            return;
        }

        if (currentStage == StageType.NIGHT) {
            // 밤 알림
            handleNotifyStartStage(gameId, currentStage);

            gameTimer.setTimer(gameId, 1, 2, () -> {
                StageType nextStage = getNextStage(currentStage);
                startStage(gameId, nextStage);
            });
            return;
        }
        // 스테이지 시작 알림
        handleNotifyStartStage(gameId, currentStage);

        gameTimer.setTimer(gameId, 1, 2, () -> {
            handleStageUpdate(gameId, currentStage);

            if (STAGE_DURATIONS.containsKey(currentStage)) {
                // 스테이지 진행 (STAGE_DURATION)
                gameTimer.setTimer(gameId, 2, STAGE_DURATIONS.get(currentStage), () -> {
                    // 스테이지 종료 알림 (2초)
                    handleNotifyEndStage(gameId, currentStage);

                    gameTimer.setTimer(gameId, 3, 2, () -> {
                        StageType nextStage = getNextStage(currentStage);

                        if (currentStage == StageType.VOTE) {
                            // 투표 결과 알림 (2초)
//                            voteService.computeVoteResult(gameId);
                            System.out.println("투표 결과 알림");
                            gameTimer.setTimer(gameId, 4, 2, () -> {
                                startStage(gameId, nextStage);
                            });
                        } else if (nextStage == StageType.FINISH) {
                            gameOver(gameId);
                        } else {
                            startStage(gameId, nextStage);
                        }
                    });
                });
            } else {
                StageType nextStage = getNextStage(currentStage);
                startStage(gameId, nextStage);
            }
        });
    }


    private void handleNotifyStartStage(Integer gameId, StageType currentStage) {
        StageNotifyDto stageNotifyDto = StageNotifyDto.notifyStartStage(currentStage);
        server.getRoomOperations(String.valueOf(gameId)).sendEvent(EventType.GAME_CHAT.getValue(), stageNotifyDto);
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
//                    subjectDebateHandler.onStartSubjectDebate();
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
        server.getRoomOperations(String.valueOf(gameId)).sendEvent(EventType.GAME_CHAT.getValue(), stageNotifyDto);
    }

    private void saveCurrentStage(Integer gameId, StageType currentStage) {
        currentStageMap.put(gameId, currentStage);
    }

    public StageType getCurrentStage(Integer gameId) {
        return currentStageMap.get(gameId);
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
        currentRoundMap.put(gameId, currentRoundMap.get(gameId)+1);
    }

    private void gameOver(Integer gameId) {
        // 게임 종료 로직
        System.out.println("게임 종료");
    }

}
