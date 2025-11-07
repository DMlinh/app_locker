package com.example.appblocker;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends BaseActivity {
    private TextView tvTimer, tvQuote;
    private CountDownTimer countDownTimer;
    private long timeLimit = 30 * 1000; // mặc định 30s
    private boolean isRunning = false;
    private int selectedHours = 0, selectedMinutes = 0;
    private BottomNavigationView bottomNav;
    private GamificationManager gm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        gm = new GamificationManager(this);

        // ✅ Hoàn thành quest 1 khi mở app
        gm.completeQuest("open_app");

        Spinner spinnerHours = findViewById(R.id.spinnerHours);
        Spinner spinnerMinutes = findViewById(R.id.spinnerMinutes);
        tvTimer = findViewById(R.id.tvPermission);
        tvQuote = findViewById(R.id.tvQuote);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnCancel = findViewById(R.id.btnCancel);
        bottomNav = findViewById(R.id.bottomNavigation);

        // 🌟 Quote ngẫu nhiên
        String[] quotes = getResources().getStringArray(R.array.time_quotes);
        tvQuote.setText(quotes[new Random().nextInt(quotes.length)]);
        Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(6000);
        fadeIn.setFillAfter(true);
        tvQuote.startAnimation(fadeIn);

        // Spinner setup
        setupSpinners(spinnerHours, spinnerMinutes);

        // Kiểm tra quyền
        if (!PermissionUtils.hasUsageStatsPermission(this)) {
            PermissionUtils.requestUsageStatsPermission(this);
        }

        // === Nút START ===
        btnStart.setOnClickListener(v -> {
            if (!hasUsageStatsPermission()) {
                Toast.makeText(this, R.string.need_usage_permission, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
                return;
            }
            if (!isAccessibilityServiceEnabled()) {
                Toast.makeText(this, R.string.need_accessibility_service, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
                return;
            }

            // ✅ Đánh dấu đã bắt đầu timer (quest 2)
            gm.completeQuest("start_timer");

            // ✅ Bật chế độ chặn
            getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)
                    .edit().putBoolean("isBlockingActive", true).apply();

            startTimer(btnStart);
        });

        // === Nút CANCEL ===
        btnCancel.setOnClickListener(v -> {
            if (isRunning) {
                cancelTimer();

                // ✅ Tắt chế độ chặn khi người dùng bấm cancel
                getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)
                        .edit().putBoolean("isBlockingActive", false).apply();
            } else {
                Toast.makeText(this, R.string.no_timer_running, Toast.LENGTH_SHORT).show();
            }
        });

        // === BOTTOM NAVIGATION ===
        bottomNav.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_home) return true;
            else if (id == R.id.nav_apps) {
                startActivity(new Intent(this, AppListActivity.class));
                return true;
            } else if (id == R.id.nav_stats) {
                startActivity(new Intent(this, UsageChartActivity.class));
                return true;
            } else if (id == R.id.nav_profile) {
                startActivity(new Intent(this, ProfileActivity.class));
                return true;
            }
            return false;
        });
    }

    // === SETUP SPINNERS ===
    private void setupSpinners(Spinner spinnerHours, Spinner spinnerMinutes) {
        List<String> hours = new ArrayList<>();
        for (int i = 0; i <= 12; i++) hours.add(String.valueOf(i));

        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i += 5)
            minutes.add(String.format(Locale.getDefault(), "%02d", i));

        ArrayAdapter<String> hoursAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hours);
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHours.setAdapter(hoursAdapter);

        ArrayAdapter<String> minutesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, minutes);
        minutesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMinutes.setAdapter(minutesAdapter);

        spinnerHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
                selectedHours = Integer.parseInt(hours.get(position));
                updateTimeLimit();
            }
            @Override public void onNothingSelected(@NonNull AdapterView<?> parent) {}
        });

        spinnerMinutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
                selectedMinutes = Integer.parseInt(minutes.get(position));
                updateTimeLimit();
            }
            @Override public void onNothingSelected(@NonNull AdapterView<?> parent) {}
        });
    }

    // === TIMER LOGIC ===
    private void startTimer(Button btnStart) {
        isRunning = true;
        btnStart.setEnabled(false);

        countDownTimer = new CountDownTimer(timeLimit, 1000) {
            @Override
            public void onFinish() {
                Toast.makeText(MainActivity.this, R.string.time_up, Toast.LENGTH_LONG).show();
                isRunning = false;
                btnStart.setEnabled(true);

                // ✅ Hoàn thành quest 3 nếu user không cancel
                gm.completeQuest("no_cancel");

                // ✅ Tắt chặn khi hết thời gian
                getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)
                        .edit().putBoolean("isBlockingActive", false).apply();
            }

            @Override
            public void onTick(long millisUntilFinished) {
                long totalSeconds = millisUntilFinished / 1000;
                long hours = totalSeconds / 3600;
                long minutes = (totalSeconds % 3600) / 60;
                long seconds = totalSeconds % 60;

                String totalText;
                if (hours > 0)
                    totalText = String.format(Locale.getDefault(),
                            "⏳ Còn lại: %d giờ %d phút %d giây", hours, minutes, seconds);
                else if (minutes > 0)
                    totalText = String.format(Locale.getDefault(),
                            "⏳ Còn lại: %d phút %d giây", minutes, seconds);
                else
                    totalText = String.format(Locale.getDefault(),
                            "⏳ Còn lại: %d giây", seconds);

                tvTimer.setText(totalText);
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        tvTimer.setText(R.string.cancelled);
        isRunning = false;
        findViewById(R.id.btnStart).setEnabled(true);
    }

    private void updateTimeLimit() {
        timeLimit = (selectedHours * 60L + selectedMinutes) * 60 * 1000;
        if (timeLimit == 0) timeLimit = 30 * 1000;
    }

    private boolean hasUsageStatsPermission() {
        try {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(), getPackageName());
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am != null) {
            for (AccessibilityServiceInfo serviceInfo :
                    am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
                ComponentName enabledService = ComponentName.unflattenFromString(serviceInfo.getId());
                if (enabledService != null &&
                        enabledService.getPackageName().equals(getPackageName())) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }
}
