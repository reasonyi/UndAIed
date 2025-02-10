package com.ssafy.undaied.socket.stage.handler;

import com.corundumstudio.socketio.SocketIOClient;
import com.corundumstudio.socketio.SocketIOServer;
import com.ssafy.undaied.socket.common.constant.EventType;
import com.ssafy.undaied.socket.common.util.GameTimer;
import com.ssafy.undaied.socket.chat.service.GameChatService;
import com.ssafy.undaied.socket.stage.dto.response.StageNotifyDto;
import com.ssafy.undaied.socket.stage.constant.StageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
@RequiredArgsConstructor
public class GameStageHandler {

    private final SocketIOServer server;
    private final GameTimer gameTimer;
    private final StageNotifyDto stageNotifyDto;

//    핸들러 파일들 불러봐야됨
//    private final SubjectDebateHandler subjectDebateHandler;

    private final GameChatService gameChatService;

    private final Map<Integer, StageType> currentStageMap = new ConcurrentHashMap<>();

    private static final Map<StageType, Integer> STAGE_DURATIONS = Map.of(
            StageType.SUBJECT_DEBATE, 2,  // 2분
            StageType.FREE_DEBATE, 2,     // 3분
            StageType.VOTE, 2             // 30초
    );

    public void handleGameStart(Integer userId, Integer gameId, SocketIOClient client) {
        startStage(userId, gameId, client, StageType.START);
    }

    private void startStage(Integer userId, Integer gameId, SocketIOClient client, StageType currentStage) {
        saveCurrentStage(gameId, currentStage);

        // 스테이지 시작 알림 날려야 함
        handleNotifyStartStage(gameId, currentStage);

        gameTimer.setTimer(gameId, 1, 2, () -> {
            StageType nextStage = getNextStage(currentStage);
            handleStageUpdate(userId, gameId, client, currentStage);

            if (STAGE_DURATIONS.containsKey(currentStage)) {
                gameTimer.setTimer(gameId, 2, STAGE_DURATIONS.get(currentStage), () -> {
                    handleNotifyEndStage(gameId, currentStage);
                });
            }
            // FINISH 상태로 전환되는 경우에만 다음 스테이지 호출
            if (nextStage != StageType.FINISH) {
                gameTimer.setTimer(gameId, 3, 2, () -> {
                    startStage(userId, gameId, client, nextStage);
                });
            } else {
                gameTimer.setTimer(gameId, 3, 2, () -> {
                    gameOver(gameId);
                });
            }
        });


    }

    private void handleNotifyStartStage(Integer gameId, StageType currentStage) {
        server.getRoomOperations(String.valueOf(gameId)).sendEvent(EventType.GAME_CHAT.getValue(),
                Map.of("message", stageNotifyDto.notifyStartStage(currentStage)));
    }

    private void handleStageUpdate(Integer userId, Integer gameId, SocketIOClient client, StageType currentStage) {
        switch (currentStage) {
            case DAY ->
//                boolean isGameOver = gameResultHandler.onComputeGameResult();
//                if (isGameOver) {
//                    HandleGameOver();
//                }
                    System.out.println("낮");

            case SUBJECT_DEBATE -> {
                System.out.println("주제 토론");
                gameChatService.sendSubject(gameId);
            }
            case FREE_DEBATE -> {
                System.out.println("자유 토론");
            }
            case VOTE -> System.out.println("투표");
//                    voteHandler.onStartVoteDebate();
            case INFECTION -> System.out.println("감염");
//                    infectionHandler.onStartInfectionHandler;
            case NIGHT -> System.out.println("밤");
            case FINISH -> System.out.println("게임 종료");
            case START -> System.out.println("게임 시작");
        }
    }

    private void handleNotifyEndStage(Integer gameId, StageType currentStage) {
        server.getRoomOperations(String.valueOf(gameId)).sendEvent(EventType.GAME_CHAT.getValue(),
                Map.of("message", stageNotifyDto.notifyEndStage(currentStage)));
    }

    private void saveCurrentStage(Integer gameId, StageType currentStage) {
        currentStageMap.put(gameId, currentStage);
    }
    public String getCurrentStage(Integer gameId) {
        StageType currentStage = currentStageMap.get(gameId);
        return currentStage.getValue();
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

    private void gameOver(Integer gameId) {
        // 게임 종료 로직
        System.out.println("게임 종료");
    }

}
