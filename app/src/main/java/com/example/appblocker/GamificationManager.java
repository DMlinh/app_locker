package com.example.appblocker;

import android.content.Context;
import android.content.SharedPreferences;
import java.util.Calendar;

public class GamificationManager {
    private static final String PREFS_NAME = "GamificationPrefs";
    private final SharedPreferences prefs;
    private static final String PREF_NAME = "GamificationPrefs";
    private static final String KEY_POINTS = "focus_points";
    private static final String KEY_STREAK = "streak";

    public GamificationManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    // --- Helpers ---
    private int getInt(String key, int def) { return prefs.getInt(key, def); }
    private void putInt(String key, int val) { prefs.edit().putInt(key, val).apply(); }
    private long getLong(String key, long def) { return prefs.getLong(key, def); }
    private void putLong(String key, long val) { prefs.edit().putLong(key, val).apply(); }
    private void putString(String key, String val) { prefs.edit().putString(key, val).apply(); }
    private String getString(String key, String def) { return prefs.getString(key, def); }

    // --- Points & Streak ---
    public int getFocusPoints() { return getInt("focus_points", 0); }
    private void setFocusPoints(int v) { putInt("focus_points", v); }
    public void addPoints(int amount) { setFocusPoints(getFocusPoints() + amount); }

    public int getStreak() { return getInt("focus_streak", 0); }
    private void setStreak(int v) { putInt("focus_streak", v); }

    public String getRank() {
        int points = getFocusPoints();
        if (points < 100) return "Beginner";
        else if (points < 200) return "Pro";
        else return "Master of Focus";
    }

    // --- Quest / daily mechanics ---
    // lastQuestDay: store dayOfYear when last quest was completed
    public boolean isQuestCompletedToday() {
        long last = getLong("last_quest_day", 0);
        if (last == 0) return false;
        Calendar c = Calendar.getInstance();
        return c.get(Calendar.YEAR) == (int)(last >> 16) && c.get(Calendar.DAY_OF_YEAR) == (int)(last & 0xFFFF);
    }

    private void setQuestCompletedToday() {
        Calendar c = Calendar.getInstance();
        long packed = ((long)c.get(Calendar.YEAR) << 16) | (c.get(Calendar.DAY_OF_YEAR) & 0xFFFF);
        putLong("last_quest_day", packed);
    }

    /**
     * Hoạt động khi user hoàn thành daily quest hôm nay (tổng thời gian dùng app bị chặn < 60 phút).
     * Nếu streak >= 7 thì bonusPerDay = 20, ngược lại 10.
     */
    public void completeDailyQuest() {
        if (isQuestCompletedToday()) return; // tránh cộng 2 lần trong ngày
        int streak = getStreak();
        int bonus = (streak >= 7) ? 20 : 10;
        addPoints(bonus);
        // tăng streak (hàm updateStreakWithSuccess xử lý ngày liên tiếp)
        updateStreakWithSuccess();
        setQuestCompletedToday();
    }

    // Call khi ngày hôm nay **không** hoàn thành quest (hoặc user đã vi phạm)
    public void failDailyQuest() {
        // reset streak
        setStreak(0);
        // không trừ điểm, chỉ gãy streak
    }

    // Gọi khi user hoàn thành quest trong 1 ngày → update streak logic
    private void updateStreakWithSuccess() {
        long lastPacked = getLong("last_streak_day", 0);
        Calendar now = Calendar.getInstance();
        if (lastPacked == 0) {
            setStreak(1);
        } else {
            int lastYear = (int)(lastPacked >> 16);
            int lastDay = (int)(lastPacked & 0xFFFF);
            Calendar last = Calendar.getInstance();
            last.set(Calendar.YEAR, lastYear);
            last.set(Calendar.DAY_OF_YEAR, lastDay);

            int dayDiff = now.get(Calendar.DAY_OF_YEAR) - last.get(Calendar.DAY_OF_YEAR);
            // Lưu ý: chưa xử lý đổi năm, đây là đơn giản; bạn có thể mở rộng nếu cần
            if (dayDiff == 1 && now.get(Calendar.YEAR) == last.get(Calendar.YEAR)) {
                setStreak(getStreak() + 1);
            } else if (now.get(Calendar.DAY_OF_YEAR) == last.get(Calendar.DAY_OF_YEAR) && now.get(Calendar.YEAR) == last.get(Calendar.YEAR)) {
                // cùng ngày (không tăng)
            } else {
                // khởi lại streak
                setStreak(1);
            }
        }
        // lưu ngày streak
        long packed = ((long)now.get(Calendar.YEAR) << 16) | (now.get(Calendar.DAY_OF_YEAR) & 0xFFFF);
        putLong("last_streak_day", packed);
    }


    // Nếu bạn muốn force set streak (ví dụ fail)
    public void setStreakZero() { setStreak(0); }

    // --- Theme unlocks ---
    // Quy ước: Dark mở ở 100đ, Galaxy mở ở 200đ, Neon mở ở 300đ
    public boolean isLightUnlocked() { return getFocusPoints() >= 100; }
    public boolean isGalaxyUnlocked() { return getFocusPoints() >= 200; }
    public boolean isNeonUnlocked() { return getFocusPoints() >= 300; }

    // Lưu theme hiện tại (Light, Dark, Galaxy, Neon)
    public void setCurrentTheme(String theme) { putString("current_theme", theme); }
    public String getCurrentTheme() { return getString("current_theme", "Dark"); }

    // --- XP Progression ---
    public int getCurrentXPInRank() {
        int xp = getFocusPoints();
        if (xp < 100) return xp;             // Beginner
        else if (xp < 200) return xp - 100;  // Pro
        else if (xp < 300) return xp - 200;  // Master (giới hạn)
        else return 200;                     // Max cap
    }

    public int getRequiredXPForNextRank() {
        int xp = getFocusPoints();
        if (xp < 100) return 100;            // Beginner -> Pro
        else if (xp < 100) return 100;       // Pro -> Master
        else if (xp < 200) return 100;       // Master -> max cap
        else return 0;                       // full XP
    }

    public String getProgressText() {
        int current = getCurrentXPInRank();
        int required = getRequiredXPForNextRank();
        String rank = getRank();
        if (required == 0) return rank + " (MAX)";
        return rank + " (" + current + "/" + required + ")";
    }

    public float getProgressPercent() {
        int current = getCurrentXPInRank();
        int required = getRequiredXPForNextRank();
        if (required == 0) return 1f;
        return (float) current / required;
    }

    public String getNextRankName() {
        int xp = getFocusPoints();
        if (xp < 100) return "Pro";
        else return "Master of Focus";
    }
}