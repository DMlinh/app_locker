package com.example.appblocker;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.Calendar;

public class GamificationManager {
    private static final String PREFS_NAME = "GamificationPrefs";
    private static final String KEY_QUESTS = "daily_quests";
    private static final String KEY_LAST_RESET = "last_quest_reset";
    private final SharedPreferences prefs;

    public GamificationManager(Context context) {
        prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        checkAndResetDailyQuests();
    }

    // ===== SharedPreferences Helpers =====
    private int getInt(String key, int def) { return prefs.getInt(key, def); }
    private void putInt(String key, int val) { prefs.edit().putInt(key, val).apply(); }
    private String getString(String key, String def) { return prefs.getString(key, def); }
    private void putString(String key, String val) { prefs.edit().putString(key, val).apply(); }
    private long getLong(String key, long def) { return prefs.getLong(key, def); }
    private void putLong(String key, long val) { prefs.edit().putLong(key, val).apply(); }

    // ===== Focus Points =====
    public int getFocusPoints() { return getInt("focus_points", 0); }
    public void addPoints(int amount) { putInt("focus_points", getFocusPoints() + amount); }

    // ===== Rank System =====
    public String getRank() {
        int points = getFocusPoints();
        if (points < 100) return "Beginner";
        else if (points < 200) return "Pro";
        else if (points < 300) return "Master of Focus";
        else return "Legend";
    }

    // ===== Daily Quests =====
    private void checkAndResetDailyQuests() {
        Calendar today = Calendar.getInstance();
        int todayKey = today.get(Calendar.YEAR) * 1000 + today.get(Calendar.DAY_OF_YEAR);
        int lastKey = (int) getLong(KEY_LAST_RESET, 0);

        if (todayKey != lastKey) {
            resetDailyQuests();
            putLong(KEY_LAST_RESET, todayKey);
        }
    }

    private void saveDailyQuests(JSONArray quests) {
        putString(KEY_QUESTS, quests.toString());
    }

    /** 🔁 Reset danh sách quest mỗi ngày */
    private void resetDailyQuests() {
        JSONArray quests = new JSONArray();
        try {
            // Quest 1: Mở app 1 lần trong ngày
            quests.put(new JSONObject()
                    .put("id", "open_app")
                    .put("title", "Mở ứng dụng ít nhất 1 lần trong ngày")
                    .put("reward", 5)
                    .put("completed", false));

            // Quest 2: Bắt đầu Start Timer
            quests.put(new JSONObject()
                    .put("id", "start_timer")
                    .put("title", "Bắt đầu 1 phiên Focus (Start Timer)")
                    .put("reward", 10)
                    .put("completed", false));

            // Quest 3: Không bấm Cancel trong khi countdown
            quests.put(new JSONObject()
                    .put("id", "no_cancel")
                    .put("title", "Không bấm Cancel trong suốt phiên Focus")
                    .put("reward", 15)
                    .put("completed", false));

        } catch (JSONException e) {
            e.printStackTrace();
        }

        saveDailyQuests(quests);
    }

    public JSONArray getDailyQuests() {
        try {
            String json = getString(KEY_QUESTS, "");
            if (!json.isEmpty()) return new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    // ===== Quest Logic =====
    public boolean isQuestCompleted(String questId) {
        JSONArray arr = getDailyQuests();
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject q = arr.getJSONObject(i);
                if (q.getString("id").equals(questId)) {
                    return q.getBoolean("completed");
                }
            } catch (JSONException e) { e.printStackTrace(); }
        }
        return false;
    }

    /** 🔹 Hoàn thành quest theo id + cộng điểm */
    public void completeQuest(String questId) {
        JSONArray arr = getDailyQuests();
        boolean updated = false;

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject q = arr.getJSONObject(i);
                if (q.getString("id").equals(questId) && !q.getBoolean("completed")) {
                    q.put("completed", true);
                    addPoints(q.getInt("reward"));
                    updated = true;
                }
            } catch (JSONException e) { e.printStackTrace(); }
        }

        if (updated) saveDailyQuests(arr);
    }

    // ===== Theme unlocks =====
    public boolean isLightUnlocked() { return getFocusPoints() >= 100; }
    public boolean isGalaxyUnlocked() { return getFocusPoints() >= 200; }
    public boolean isNeonUnlocked() { return getFocusPoints() >= 300; }

    public void setCurrentTheme(String theme) { putString("current_theme", theme); }
    public String getCurrentTheme() { return getString("current_theme", "Dark"); }

    // ===== XP / Rank Progress =====
    public int getCurrentXPInRank() {
        int xp = getFocusPoints();
        if (xp < 100) return xp;
        else if (xp < 200) return xp - 100;
        else if (xp < 300) return xp - 200;
        else return 200;
    }

    public int getRequiredXPForNextRank() {
        int xp = getFocusPoints();
        if (xp < 100) return 100;
        else if (xp < 200) return 100;
        else return 0;
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
        else if (xp < 200) return "Master of Focus";
        else return "Legend";
    }

    public void resetProgress() {
        prefs.edit()
                .remove("focus_points")
                .remove(KEY_QUESTS)
                .remove(KEY_LAST_RESET)
                .apply();
        checkAndResetDailyQuests(); // tạo lại quest mặc định sau reset
    }

}
