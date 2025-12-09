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
    private final UserDatabaseHelper dbHelper;
    private final String uid = "default";

    public GamificationManager(Context ctx) {
        prefs = ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        dbHelper = new UserDatabaseHelper(ctx);
    }

    private String ukey(String base) {
        return base + "_" + uid;
    }

    private void putStringU(String key, String val) {
        prefs.edit().putString(ukey(key), val).apply();
    }

    private String getStringU(String key, String def) {
        return prefs.getString(ukey(key), def);
    }

    private void putLongU(String key, long val) {
        prefs.edit().putLong(ukey(key), val).apply();
    }

    private long getLongU(String key, long def) {
        return prefs.getLong(ukey(key), def);
    }

    // ------------------------------------------------------------
    // POINTS
    // ------------------------------------------------------------
    public int getFocusPoints() {
        return dbHelper.getPoints();
    }

    public void addPoints(int amount) {
        dbHelper.addPoints(amount);
    }

    // ------------------------------------------------------------
    // RANK
    // ------------------------------------------------------------
    public String getRank() {
        int p = getFocusPoints();
        if (p < 100) return "Beginner";
        else if (p < 200) return "Pro";
        else if (p < 300) return "Master of Focus";
        else return "Legend";
    }

    // ------------------------------------------------------------
    // DAILY QUESTS
    // ------------------------------------------------------------
    private void ensureDailyQuestsExist() {
        if (getStringU("quests", null) == null) {
            resetDailyQuests();
        }
    }

    private void checkAndResetDailyQuests() {
        Calendar c = Calendar.getInstance();
        int today = c.get(Calendar.YEAR) * 1000 + c.get(Calendar.DAY_OF_YEAR);
        int last = (int) getLongU("last_reset", -1);

        if (today != last) {
            resetDailyQuests();
            putLongU("last_reset", today);
        }
    }

    private void resetDailyQuests() {
        JSONArray arr = new JSONArray();
        try {
            arr.put(new JSONObject()
                    .put("id", "open_app")
                    .put("title", "Mở ứng dụng 1 lần")
                    .put("reward", 5)
                    .put("done", false));

            arr.put(new JSONObject()
                    .put("id", "start_timer")
                    .put("title", "Bắt đầu 1 phiên Focus")
                    .put("reward", 10)
                    .put("done", false));

            arr.put(new JSONObject()
                    .put("id", "no_cancel")
                    .put("title", "Không Cancel trong suốt phiên Focus")
                    .put("reward", 15)
                    .put("done", false));
        } catch (JSONException ignored) {
        }

        putStringU("quests", arr.toString());
    }

    public JSONArray getDailyQuests() {
        try {
            String data = getStringU("quests", null);
            if (data != null) {
                JSONArray arr = new JSONArray(data);

                // Nếu bị thiếu quest → tự sửa
                if (arr.length() == 0) {
                    resetDailyQuests();
                    return getDailyQuests();
                }

                return arr;
            }
        } catch (Exception ignored) {
        }

        // Nếu JSON hỏng → tạo mới
        resetDailyQuests();
        return getDailyQuests();
    }

    public void completeQuest(String questId) {
        JSONArray arr = getDailyQuests();
        boolean changed = false;

        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject q = arr.getJSONObject(i);
                if (q.getString("id").equals(questId) && !q.getBoolean("done")) {
                    q.put("done", true);
                    addPoints(q.getInt("reward"));
                    changed = true;
                }
            } catch (Exception ignored) {
            }
        }

        if (changed) putStringU("quests", arr.toString());
    }

    public boolean isQuestCompleted(String questId) {
        JSONArray arr = getDailyQuests();
        for (int i = 0; i < arr.length(); i++) {
            try {
                JSONObject q = arr.getJSONObject(i);
                if (q.getString("id").equals(questId))
                    return q.getBoolean("done");
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    // ------------------------------------------------------------
    // THEME SYSTEM
    // ------------------------------------------------------------
    public String getCurrentTheme() {
        return getStringU("theme", "Dark");
    }

    public void setCurrentTheme(String theme) {
        putStringU("theme", theme);
    }

    public boolean canUseTheme(String theme) {
        int p = getFocusPoints();
        switch (theme) {
            case "Dark":
                return true;
            case "Light":
                return p >= 100;
            case "Galaxy":
                return p >= 200;
            case "Neon":
                return p >= 300;
        }
        return false;
    }

    // ------------------------------------------------------------
    // PROGRESS
    // ------------------------------------------------------------
    public float getProgressPercent() {
        int xp = getFocusPoints();

        if (xp < 100) return xp / 100f;
        if (xp < 200) return (xp - 100) / 100f;
        if (xp < 300) return (xp - 200) / 100f;
        return 1f;
    }

    public String getProgressText() {
        int xp = getFocusPoints();
        if (xp < 100) return "Beginner (" + xp + "/100)";
        if (xp < 200) return "Pro (" + (xp - 100) + "/100)";
        if (xp < 300) return "Master (" + (xp - 200) + "/100)";
        return "Legend (MAX)";
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

    public String getUser() {
        return getStringU("username", "User");
    }

    // ------------------------------------------------------------
    // USER SESSION
    // ------------------------------------------------------------
    public void setUser(String name) {
        if (name == null || name.trim().isEmpty()) return;

        name = name.replace(" ", "_");
        putStringU("username", name);
    }

}
