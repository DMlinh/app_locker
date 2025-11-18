package com.example.appblocker;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.provider.Settings;
import android.text.InputType;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

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
    private GamificationManager gm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setupBottomNav(R.id.nav_home);

        gm = new GamificationManager(this);

        // Hoàn thành quest 1 khi mở app
        gm.completeQuest("open_app");

        Spinner spinnerHours = findViewById(R.id.spinnerHours);
        Spinner spinnerMinutes = findViewById(R.id.spinnerMinutes);
        tvTimer = findViewById(R.id.tvPermission);
        tvQuote = findViewById(R.id.tvQuote);
        Button btnStart = findViewById(R.id.btnStart);
        Button btnCancel = findViewById(R.id.btnCancel);
        bottomNav = findViewById(R.id.bottomNavigation);

        // Quote ngẫu nhiên
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

        // Nút START
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

            // Hoàn thành quest 2
            gm.completeQuest("start_timer");

            // Bật chế độ chặn
            getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)
                    .edit().putBoolean("isBlockingActive", true).apply();

            // Start timer và lưu vào TimerManager
            TimerManager.getInstance().start(timeLimit);
            startTimerFromManager(btnStart);
        });

        // Nút CANCEL
        btnCancel.setOnClickListener(v -> {
            if (!isRunning) {
                Toast.makeText(this, R.string.no_timer_running, Toast.LENGTH_SHORT).show();
                return;
            }

            SharedPreferences prefs = getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE);
            boolean requirePin = prefs.getBoolean("require_pin", false);

            if (requirePin) {
                String savedPin = prefs.getString("pin_code", "");
                showPinConfirmDialog(savedPin);
            } else {
                cancelTimer();
                prefs.edit().putBoolean("isBlockingActive", false).apply();
                Toast.makeText(this, R.string.timer_cancelled, Toast.LENGTH_SHORT).show();
            }
        });
    }

    // --- Spinner setup ---
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

    // --- Timer ---
    private void startTimerFromManager(Button btnStart) {
        long remaining = TimerManager.getInstance().getRemaining();
        if (remaining <= 0) {
            tvTimer.setText(R.string.time_up);
            isRunning = false;
            btnStart.setEnabled(true);
            return;
        }

        isRunning = true;
        btnStart.setEnabled(false);

        countDownTimer = new CountDownTimer(remaining, 1000) {
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

            @Override
            public void onFinish() {
                TimerManager.getInstance().cancel();
                tvTimer.setText(R.string.time_up);
                isRunning = false;
                btnStart.setEnabled(true);

                // Hoàn thành quest nếu user không cancel
                gm.completeQuest("no_cancel");

                getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)
                        .edit().putBoolean("isBlockingActive", false).apply();
            }
        }.start();
    }

    private void cancelTimer() {
        if (countDownTimer != null) countDownTimer.cancel();
        TimerManager.getInstance().cancel();
        tvTimer.setText(R.string.cancelled);
        isRunning = false;
        findViewById(R.id.btnStart).setEnabled(true);
    }

    private void updateTimeLimit() {
        timeLimit = (selectedHours * 60L + selectedMinutes) * 60 * 1000;
        if (timeLimit == 0) timeLimit = 30 * 1000;
    }

    // --- Permissions ---
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
    protected void onResume() {
        super.onResume();
        // Khôi phục timer nếu còn chạy
        if (TimerManager.getInstance().isRunning()) {
            startTimerFromManager(findViewById(R.id.btnStart));
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (countDownTimer != null) countDownTimer.cancel();
    }

    // --- PIN ---
    private void showPinConfirmDialog(String savedPin) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_NUMBER);

        new AlertDialog.Builder(this)
                .setTitle("Nhập mã PIN để Cancel")
                .setView(input)
                .setPositiveButton("Xác nhận", (dialog, which) -> {
                    if (input.getText().toString().equals(savedPin)) {
                        cancelTimer();
                    } else {
                        Toast.makeText(this, "Sai mã PIN!", Toast.LENGTH_SHORT).show();
                    }
                })
                .setNegativeButton("Hủy", null)
                .show();
    }
}
