package com.example.appblocker;

import android.content.Context;
import android.content.SharedPreferences;

public class TimerManager {

    private static final String PREFS_NAME = "TimerPrefs";
    private static final String KEY_END_TIME = "end_time";
    private static final String KEY_IS_RUNNING = "is_running";
    private static TimerManager instance;
    private final SharedPreferences prefs;

    private TimerManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    public static synchronized TimerManager getInstance(Context context) {
        if (instance == null) {
            instance = new TimerManager(context.getApplicationContext());
        }
        return instance;
    }

    // Bắt đầu đếm giờ
    public void start(long durationMillis) {
        long endTime = System.currentTimeMillis() + durationMillis;

        prefs.edit()
                .putLong(KEY_END_TIME, endTime)
                .putBoolean(KEY_IS_RUNNING, true)
                .apply();
    }

    // Hủy đếm giờ
    public void cancel() {
        prefs.edit()
                .putBoolean(KEY_IS_RUNNING, false)
                .putLong(KEY_END_TIME, 0)
                .apply();
    }

    // Thời gian còn lại
    public long getRemaining() {
        if (!isRunning()) return 0;

        long end = prefs.getLong(KEY_END_TIME, 0);
        long remaining = end - System.currentTimeMillis();

        return Math.max(remaining, 0);
    }

    // Trạng thái
    public boolean isRunning() {
        boolean running = prefs.getBoolean(KEY_IS_RUNNING, false);
        long end = prefs.getLong(KEY_END_TIME, 0);

        // Nếu đã hết giờ thì auto cancel
        if (running && System.currentTimeMillis() > end) {
            cancel();
            return false;
        }

        return running;
    }

    public void reset() {
        prefs.edit().clear().apply();
    }
}
