package com.example.appblocker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ThemeManager {
    private static final String PREFS_NAME = "GamificationPrefs";
    public static final String ACTION_THEME_CHANGED = "com.example.appblocker.THEME_CHANGED";

    public static void setTheme(Context context, String themeName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("current_theme", themeName).apply();

        Intent intent = new Intent(ACTION_THEME_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    public static String getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("current_theme", "Dark");
    }

    // theme lưu theo user
    public static void setUserTheme(Context context, String user, String theme) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("theme_" + user, theme).apply();
    }

    public static String getUserTheme(Context context, String user) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("theme_" + user, "Dark");
    }
}
