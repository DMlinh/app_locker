package com.example.appblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

public abstract class BaseActivity extends AppCompatActivity {

    private String lastTheme;

    private final BroadcastReceiver themeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recreate(); // Reload khi theme đổi nếu activity đang foreground
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        applyTheme(); // ✅ Gọi trước super
        lastTheme = ThemeManager.getTheme(this);
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();
        LocalBroadcastManager.getInstance(this)
                .registerReceiver(themeReceiver, new IntentFilter(ThemeManager.ACTION_THEME_CHANGED));
    }

    @Override
    protected void onStop() {
        super.onStop();
        LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(themeReceiver);
    }

    @Override
    protected void onResume() {
        super.onResume();
        String currentTheme = ThemeManager.getTheme(this);
        if (!currentTheme.equals(lastTheme)) {
            lastTheme = currentTheme;
            recreate(); // 🔄 Nếu quay lại activity mà theme đổi → reload
        }
    }

    private void applyTheme() {
        String theme = ThemeManager.getTheme(this);
        switch (theme) {
            case "Light":
                setTheme(R.style.AppTheme_Light);
                break;
            case "Galaxy":
                setTheme(R.style.AppTheme_Galaxy);
                break;
            case "Neon":
                setTheme(R.style.AppTheme_Neon);
                break;
            default:
                setTheme(R.style.Theme_Applocker);
                break;
        }
    }
}
