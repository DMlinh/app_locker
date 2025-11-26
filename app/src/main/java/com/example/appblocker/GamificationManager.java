package com.example.appblocker;

import android.content.Context;
import android.content.SharedPreferences;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.Calendar;

public class GamificationManager {

    private static final String PREFS_NAME = "GamificationPrefs";
    private final SharedPreferences prefs;
    private String uid = "default"; // sẽ đổi khi login
    private final Context context;
    private final String currentUser;
    private final UserDatabaseHelper dbHelper;

    public GamificationManager(Context ctx) {
        this.context = ctx;

        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);

        // Load username đang đăng nhập
        currentUser = ctx.getSharedPreferences("USER_SESSION", Context.MODE_PRIVATE)
                .getString("current_user", null);

        // Gắn DB
        dbHelper = new UserDatabaseHelper(ctx);
    }

    // ============================================================
    //  GÁN UID (gọi ngay sau login)
    // ============================================================
    public void setUser(String uid) {
        this.uid = uid.replace("@", "_").replace(".", "_");
        checkAndResetDailyQuests();
    }

    private String key(String base) {
        return base + "_" + uid;
    }

    private int getInt(String key, int def) {
        return prefs.getInt(key(key), def);
    }

    private void putInt(String key, int val) {
        prefs.edit().putInt(key(key), val).apply();
    }

    private String getString(String key, String def) {
        return prefs.getString(key(key), def);
    }

    private void putString(String key, String val) {
        prefs.edit().putString(key(key), val).apply();
    }

    private long getLong(String key, long def) {
        return prefs.getLong(key(key), def);
    }

    private void putLong(String key, long val) {
        prefs.edit().putLong(key(key), val).apply();
    }

    // ============================================================
    //  FOCUS POINTS (đồng bộ DB + SP)
    // ============================================================
    public int getFocusPoints() {
        if (currentUser == null) return 0;
        return dbHelper.getPoints(currentUser);
    }

    public void addPoints(int amount) {
        if (currentUser == null) return;

        // 1. Update DB
        dbHelper.addPoints(currentUser, amount);

        // 2. Sync SP để UI cập nhật ngay
        int newTotal = dbHelper.getPoints(currentUser);
        putInt("focus_points", newTotal);
    }

    // ============================================================
    //  RANK
    // ============================================================
    public String getRank() {
        int points = getFocusPoints();
        if (points < 100) return "Beginner";
        else if (points < 200) return "Pro";
        else if (points < 300) return "Master of Focus";
        else return "Legend";
    }

    // ============================================================
    //  DAILY QUESTS
    // ============================================================
    private void checkAndResetDailyQuests() {
        Calendar today = Calendar.getInstance();
        int todayKey = today.get(Calendar.YEAR) * 1000 + today.get(Calendar.DAY_OF_YEAR);
        int lastKey = (int) getLong("last_quest_reset", 0);

        if (todayKey != lastKey) {
            resetDailyQuests();
            putLong("last_quest_reset", todayKey);
        }
    }

    private void saveDailyQuests(JSONArray quests) {
        putString("daily_quests", quests.toString());
    }

    private void resetDailyQuests() {
        JSONArray quests = new JSONArray();
        try {
            quests.put(new JSONObject()
                    .put("id", "open_app")
                    .put("title", "Mở ứng dụng ít nhất 1 lần trong ngày")
                    .put("reward", 5)
                    .put("completed", false));

            quests.put(new JSONObject()
                    .put("id", "start_timer")
                    .put("title", "Bắt đầu 1 phiên Focus (Start Timer)")
                    .put("reward", 10)
                    .put("completed", false));

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
            String json = getString("daily_quests", "");
            if (!json.isEmpty()) return new JSONArray(json);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return new JSONArray();
    }

    public boolean isQuestCompleted(String questId) {
        JSONArray arr = getDailyQuests();
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject q = arr.getJSONObject(i);
                if (q.getString("id").equals(questId)) {
                    return q.getBoolean("completed");
                }
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

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
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        if (updated) saveDailyQuests(arr);
    }

    public String getCurrentTheme() {
        return getString("current_theme", "Dark");
    }

    // ============================================================
    // THEME
    // ============================================================
    public void setCurrentTheme(String theme) {
        putString("current_theme", theme);
    }

    public boolean isLightUnlocked() {
        return getFocusPoints() >= 100;
    }

    public boolean isGalaxyUnlocked() {
        return getFocusPoints() >= 200;
    }

    public boolean isNeonUnlocked() {
        return getFocusPoints() >= 300;
    }

    // ============================================================
    // XP / Rank Progress
    // ============================================================
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
                .remove(key("focus_points"))
                .remove(key("daily_quests"))
                .remove(key("last_quest_reset"))
                .remove(key("current_theme"))
                .apply();

        checkAndResetDailyQuests();
    }

    // Kiểm tra user có đủ điểm để dùng theme
    public boolean canUseTheme(String theme) {
        switch (theme) {
            case "Dark":
                return true;                 // luôn unlock
            case "Light":
                return isLightUnlocked();   // >=100 điểm
            case "Galaxy":
                return isGalaxyUnlocked(); // >=200 điểm
            case "Neon":
                return isNeonUnlocked();     // >=300 điểm
            default:
                return false;
        }
    }

}
