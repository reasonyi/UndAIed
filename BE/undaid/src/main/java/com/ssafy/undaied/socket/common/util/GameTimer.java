package com.ssafy.undaied.socket.common.util;

import com.ssafy.undaied.socket.stage.constant.StageType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class GameTimer {
    private final Map<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    private final Map<String, Integer> timerInfos = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void setTimer(Integer gameId, Integer timerId, int duration, Runnable callback) {
        String timerKey = gameId + "_" + timerId;
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
        // gameId에 해당하는 모든 타이머 키 중에서
        // 가장 최근에 설정된 타이머의 남은 시간 반환
        return timerInfos.entrySet().stream()
                .filter(entry -> entry.getKey().startsWith(gameId+"_"))
                .map(Map.Entry::getValue)
                .findFirst()
                .orElse(0); // 실행 중인 타이머가 없으면 0 반환
    }
}

