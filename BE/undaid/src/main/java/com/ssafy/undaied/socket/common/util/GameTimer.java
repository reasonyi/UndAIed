package com.ssafy.undaied.socket.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class GameTimer {
    private final Map<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    private final Map<String, Integer> timerInfos = new ConcurrentHashMap<>();
    private final Map<Integer, String> currentTimerKeys = new ConcurrentHashMap<>();  // gameId -> currentTimerKey

    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void setTimer(Integer gameId, Integer timerId, int duration, Runnable callback) {
        String timerKey = gameId + "_" + timerId;
        currentTimerKeys.put(gameId, timerKey);  // 현재 타이머 키 저장
        cancelTimer(timerKey);

        // 타이머 정보 Map에 저장
        timerInfos.put(timerKey, duration);

        // 1초마다 실행되는 타이머
        ScheduledFuture<?> future = scheduler.scheduleAtFixedRate(() -> {
                    // 현재 타이머 조회
                    Integer remainingTime = timerInfos.get(timerKey);
                    // 남은 시간이 있으면 1초 감소시키고 정보 업데이트
                    if (remainingTime > 0) {
                        timerInfos.put(timerKey, remainingTime - 1);
                    } else {
                        cancelTimer(timerKey);
                        callback.run();
                    }
                    // 초기 딜레이 0초, 이후 1초마다 실행
                }, 0, 1, TimeUnit.SECONDS
        );
        timers.put(timerKey, future);
    }

    public void cancelTimer(String timerKey) {
        ScheduledFuture<?> future = timers.remove(timerKey);
        if (future != null) {
            future.cancel(false);
        }
    }

    public Integer getRemainingTime(Integer gameId) {
        String currentKey = currentTimerKeys.get(gameId);
        return currentKey != null ? timerInfos.getOrDefault(currentKey, 0) : 0;
    }

    public Integer getCurrentTimerId(Integer gameId) {
        String currentKey = currentTimerKeys.get(gameId);
        if (currentKey != null) {
            // timerKey : "gameId_timerId"
            String[] parts = currentKey.split("_");
            if (parts.length == 2) {
                return Integer.parseInt(parts[1]);
            }
        }
        return null;
    }

    public boolean isMainStage(Integer gameId) {
        Integer currentTimerId = getCurrentTimerId(gameId);
        return currentTimerId != null && currentTimerId.equals(GameTimerConstants.STAGE_MAIN);
    }

    // 게임 종료 시 해당 게임의 모든 타이머 정리
    public void cleanupGame(Integer gameId) {
        // 게임의 모든 타이머 찾아서 취소
        String prefix = gameId + "_";
        timers.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(prefix))
                .forEach(entry -> {
                    entry.getValue().cancel(false);
                    timers.remove(entry.getKey());
                    timerInfos.remove(entry.getKey());
                });

        // currentTimerKeys에서도 제거
        currentTimerKeys.remove(gameId);
    }
}

