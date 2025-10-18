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
import android.view.ViewGroup;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Random;

public class MainActivity extends BaseActivity {
    private TextView tvTimer;
    private CountDownTimer countDownTimer;
    private long timeLimit = 30 * 1000; // 30 giây
    private boolean isRunning = false;
    private int selectedHours = 0, selectedMinutes = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            savedInstanceState.remove("android:viewHierarchyState");
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Spinner spinnerHours = findViewById(R.id.spinnerHours);
        Spinner spinnerMinutes = findViewById(R.id.spinnerMinutes);
        tvTimer = findViewById(R.id.tvPermission);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnCancel = findViewById(R.id.btnCancel);
        Button btnChooseApps = findViewById(R.id.btnChooseApps);
        Button btnMyUsage = findViewById(R.id.btnMyUsage);
        ImageView ivRankIcon = findViewById(R.id.ivRankIcon);
        TextView tvQuote = findViewById(R.id.tvQuote);

        // Áp dụng theme cho button
        ThemeManager.applyButtonTheme(this, btnStart);
        ThemeManager.applyButtonTheme(this, btnCancel);
        ThemeManager.applyButtonTheme(this, btnChooseApps);
        ThemeManager.applyButtonTheme(this, btnMyUsage);

        // Điều hướng giữa các activity
        btnChooseApps.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, AppListActivity.class)));
        btnMyUsage.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, UsageChartActivity.class)));
        ivRankIcon.setOnClickListener(v ->
                startActivity(new Intent(MainActivity.this, ProfileActivity.class)));

        // Kiểm tra quyền
        if (!PermissionUtils.hasUsageStatsPermission(this)) {
            PermissionUtils.requestUsageStatsPermission(this);
        }

        // --- QUOTES NGẪU NHIÊN ---
        String[] quotes = getResources().getStringArray(R.array.time_quotes);
        tvQuote.setText(quotes[new Random().nextInt(quotes.length)]);
        Animation fadeIn = new AlphaAnimation(0f, 1f);
        fadeIn.setDuration(7000);
        fadeIn.setFillAfter(true);
        tvQuote.startAnimation(fadeIn);

        // --- SPINNER DỮ LIỆU ---
        List<String> hours = new ArrayList<>();
        for (int i = 0; i <= 12; i++) hours.add(String.valueOf(i));

        List<String> minutes = new ArrayList<>();
        for (int i = 0; i < 60; i += 5)
            minutes.add(String.format(Locale.getDefault(), "%02d", i));

        // --- SPINNER ADAPTER (giờ) ---
        ArrayAdapter<String> hoursAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, hours) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.spinner_text_color));
                return tv;
            }

            @NonNull
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.spinner_dropdown_text_color));
                tv.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.spinner_dropdown_bg));
                return tv;
            }
        };
        hoursAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerHours.setAdapter(hoursAdapter);

        // --- SPINNER ADAPTER (phút) ---
        ArrayAdapter<String> minutesAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_spinner_item, minutes) {
            @NonNull
            @Override
            public View getView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getView(position, convertView, parent);
                tv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.spinner_text_color));
                return tv;
            }

            @NonNull
            @Override
            public View getDropDownView(int position, View convertView, @NonNull ViewGroup parent) {
                TextView tv = (TextView) super.getDropDownView(position, convertView, parent);
                tv.setTextColor(ContextCompat.getColor(MainActivity.this, R.color.spinner_dropdown_text_color));
                tv.setBackgroundColor(ContextCompat.getColor(MainActivity.this, R.color.spinner_dropdown_bg));
                return tv;
            }
        };
        minutesAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMinutes.setAdapter(minutesAdapter);

        // --- SPINNER LISTENERS ---
        spinnerHours.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
                selectedHours = Integer.parseInt(hours.get(position));
                updateTimeLimit();
            }

            @Override
            public void onNothingSelected(@NonNull AdapterView<?> parent) { }
        });

        spinnerMinutes.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(@NonNull AdapterView<?> parent, @NonNull View view, int position, long id) {
                selectedMinutes = Integer.parseInt(minutes.get(position));
                updateTimeLimit();
            }

            @Override
            public void onNothingSelected(@NonNull AdapterView<?> parent) { }
        });

        // --- BUTTON EVENTS ---
        btnCancel.setOnClickListener(v -> {
            if (isRunning) {
                cancelTimer();
            } else {
                Toast.makeText(this, R.string.no_timer_running, Toast.LENGTH_SHORT).show();
            }
        });

        btnStart.setOnClickListener(v -> {
            if (!hasUsageStatsPermission()) {
                Toast.makeText(this, R.string.need_usage_permission, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS));
            } else if (!isAccessibilityServiceEnabled()) {
                Toast.makeText(this, R.string.need_accessibility_service, Toast.LENGTH_SHORT).show();
                startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
            } else {
                startTimer(btnStart);
            }
        });
    }

    private void startTimer(Button btnStart) {
        isRunning = true;
        btnStart.setEnabled(false);
        saveBlockingState(true);

        countDownTimer = new CountDownTimer(timeLimit, 1000) {
            @Override
            public void onFinish() {
                Toast.makeText(MainActivity.this, R.string.time_up, Toast.LENGTH_LONG).show();
                isRunning = false;
                saveBlockingState(false);
                btnStart.setEnabled(true);
            }

            @Override
            public void onTick(long millisUntilFinished) {
                long seconds = millisUntilFinished / 1000;
                tvTimer.setText(getString(R.string.time_left, seconds));
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) {
            countDownTimer.cancel();
            tvTimer.setText(R.string.cancelled);
            isRunning = false;
            saveBlockingState(false);
        }
    }

    private void updateTimeLimit() {
        timeLimit = (selectedHours * 60L + selectedMinutes) * 60 * 1000;
        if (timeLimit == 0) timeLimit = 30 * 1000;
    }

    private void saveBlockingState(boolean active) {
        getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)
                .edit()
                .putBoolean("isBlockingActive", active)
                .apply();
    }

    private boolean hasUsageStatsPermission() {
        try {
            AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
            int mode = appOps.checkOpNoThrow(
                    AppOpsManager.OPSTR_GET_USAGE_STATS,
                    android.os.Process.myUid(),
                    getPackageName()
            );
            return (mode == AppOpsManager.MODE_ALLOWED);
        } catch (Exception e) {
            return false;
        }
    }

    private boolean isAccessibilityServiceEnabled() {
        AccessibilityManager am = (AccessibilityManager) getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am != null) {
            List<AccessibilityServiceInfo> enabledServices =
                    am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);
            for (AccessibilityServiceInfo serviceInfo : enabledServices) {
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
