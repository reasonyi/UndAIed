package com.ssafy.undaied.socket.common.util;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.concurrent.*;

@Component
@RequiredArgsConstructor
public class GameTimer {
    private final Map<String, ScheduledFuture<?>> timers = new ConcurrentHashMap<>();
    private final ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

    public void setTimer(Integer gameId, Integer timerId, int duration, Runnable callback) {
        String timerKey = gameId + "_" + timerId;
        cancelTimer(timerKey);

        ScheduledFuture<?> future = scheduler.schedule(callback, duration, TimeUnit.SECONDS);
        timers.put(timerKey, future);
    }

    public void cancelTimer(String gameId) {
        ScheduledFuture<?> future = timers.remove(gameId);
        if ( future != null ) {
            future.cancel(false);
        }
    }
}
