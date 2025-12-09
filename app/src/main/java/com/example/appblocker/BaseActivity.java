package com.example.appblocker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public abstract class BaseActivity extends AppCompatActivity {

    private final BroadcastReceiver themeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            recreate(); // Reload khi theme đổi nếu activity đang foreground
        }
    };
    protected BottomNavigationView bottomNav;
    private String lastTheme;

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

    protected void setupBottomNav(@IdRes int currentItemId) {
        bottomNav = findViewById(R.id.bottomNavigation);
        if (bottomNav == null) return;

        // Đánh dấu item hiện tại
        bottomNav.setSelectedItemId(currentItemId);

        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();

            if (id == R.id.nav_home && currentItemId != R.id.nav_home) {
                navigateTo(MainActivity.class, R.anim.slide_in_left, R.anim.slide_out_right);
                return true;
            } else if (id == R.id.nav_apps && currentItemId != R.id.nav_apps) {
                navigateTo(AppListActivity.class, R.anim.slide_in_right, R.anim.slide_out_left);
                return true;
            } else if (id == R.id.nav_stats && currentItemId != R.id.nav_stats) {
                navigateTo(UsageChartActivity.class, R.anim.fade_in, R.anim.fade_out);
                return true;
            } else if (id == R.id.nav_profile && currentItemId != R.id.nav_profile) {
                navigateTo(ProfileActivity.class, R.anim.slide_in_up, R.anim.slide_out_down);
                return true;
            } else if (id == R.id.nav_settings) {
                startActivity(new Intent(this, SettingsActivity.class));
                overridePendingTransition(0, 0);
                return true;
            }

            return true;
        });
    }

    private void navigateTo(Class<?> targetActivity, int enterAnim, int exitAnim) {
        Intent intent = new Intent(this, targetActivity);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        overridePendingTransition(enterAnim, exitAnim);
    }
}
