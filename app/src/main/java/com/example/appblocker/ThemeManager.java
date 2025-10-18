package com.example.appblocker;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.widget.Button;

import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public class ThemeManager {
    private static final String PREFS_NAME = "GamificationPrefs";
    public static final String ACTION_THEME_CHANGED = "com.example.appblocker.THEME_CHANGED";

    // Lưu theme hiện tại
    public static void setTheme(Context context, String themeName) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        prefs.edit().putString("current_theme", themeName).apply();

        // Gửi broadcast nội bộ
        Intent intent = new Intent(ACTION_THEME_CHANGED);
        LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
    }

    // Lấy theme hiện tại
    public static String getTheme(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        return prefs.getString("current_theme", "Dark");
    }

    // ✅ Hàm hỗ trợ đổi màu nút theo theme
    public static void applyButtonTheme(Context context, Button button) {
        String theme = getTheme(context);

        int colorResId;
        switch (theme) {
            case "Light":
                colorResId = R.color.btn_text_light;   // 🎨 file trong res/color/
                break;
            case "Galaxy":
                colorResId = R.color.btn_background_galaxy;
                break;
            case "Neon":
                colorResId = R.drawable.btn_neon;
                break;
            default:
                colorResId = R.color.btn_background_dark;
                break;
        }

        int color = ContextCompat.getColor(context, colorResId);
        button.setBackgroundTintList(ColorStateList.valueOf(color));
    }
}
