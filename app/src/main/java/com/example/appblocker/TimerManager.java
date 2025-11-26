package com.example.appblocker;

public class TimerManager {
    private static TimerManager instance;
    private long endTime = 0; // thời gian kết thúc thực tế
    private boolean isRunning = false;

    private TimerManager() {
    }

    public static TimerManager getInstance() {
        if (instance == null) instance = new TimerManager();
        return instance;
    }

    public void start(long durationMillis) {
        endTime = System.currentTimeMillis() + durationMillis;
        isRunning = true;
    }

    public void cancel() {
        isRunning = false;
        endTime = 0;
    }

    public boolean isRunning() {
        return isRunning && System.currentTimeMillis() < endTime;
    }

    public long getRemaining() {
        if (!isRunning()) return 0;
        long remaining = endTime - System.currentTimeMillis();
        if (remaining <= 0) {
            cancel();
            return 0;
        }
        return remaining;
    }
}
