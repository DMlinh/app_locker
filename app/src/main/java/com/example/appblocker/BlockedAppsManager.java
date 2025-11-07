package com.example.appblocker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class BlockedAppsManager {
    private static final String PREFS_NAME = "BlockedAppsPrefs";
    private static final String KEY_BLOCKED_APPS = "blocked_apps";

    // ➕ Thêm app vào danh sách bị chặn
    public static void addBlockedApp(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> blockedApps = new HashSet<>(prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>()));

        // Không cho chặn chính AppBlocker
        if (!packageName.equals(context.getPackageName())) {
            blockedApps.add(packageName);
            prefs.edit().putStringSet(KEY_BLOCKED_APPS, blockedApps).apply();
        }
    }

    // ➖ Xóa app khỏi danh sách
    public static void removeBlockedApp(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> blockedApps = new HashSet<>(prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>()));
        blockedApps.remove(packageName);
        prefs.edit().putStringSet(KEY_BLOCKED_APPS, blockedApps).apply();
    }

    // 📋 Lấy danh sách app bị chặn
    public static Set<String> getBlockedApps(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> blocked = new HashSet<>(prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>()));

        // 🔹 Xóa chính AppBlocker khỏi danh sách bị chặn
        blocked.remove(context.getPackageName());

        return blocked;
    }
}
