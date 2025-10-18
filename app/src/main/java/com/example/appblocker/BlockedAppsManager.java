package com.example.appblocker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.HashSet;
import java.util.Set;

public class BlockedAppsManager {
    private static final String PREFS_NAME = "BlockedAppsPrefs";
    private static final String KEY_BLOCKED_APPS = "blocked_apps";

    public static void addBlockedApp(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> blockedApps = new HashSet<>(prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>()));
        blockedApps.add(packageName);
        prefs.edit().putStringSet(KEY_BLOCKED_APPS, blockedApps).apply();
    }

    public static void removeBlockedApp(Context context, String packageName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        Set<String> blockedApps = new HashSet<>(prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>()));
        blockedApps.remove(packageName);
        prefs.edit().putStringSet(KEY_BLOCKED_APPS, blockedApps).apply();
    }

    public static Set<String> getBlockedApps(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getStringSet(KEY_BLOCKED_APPS, new HashSet<>());
    }
}

