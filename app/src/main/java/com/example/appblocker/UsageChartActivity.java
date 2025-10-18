package com.example.appblocker;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.content.res.AppCompatResources;
import androidx.core.content.ContextCompat;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UsageChartActivity extends BaseActivity {

    private BarChart barChart;
    private LinearLayout appListLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_chart);

        barChart = findViewById(R.id.barChart);
        appListLayout = findViewById(R.id.appListLayout);

        // 🔹 Lấy dữ liệu thật
        Map<String, Long> appUsage = getAppUsageToday();
        Map<Integer, Long> hourlyUsage = getHourlyUsageToday();

        // ⚠️ Nếu không có dữ liệu → tạo test data
        if (hourlyUsage.isEmpty()) {
            for (int i = 8; i <= 12; i++) {
                hourlyUsage.put(i, (long) (Math.random() * 10 * 60_000));
            }
        }
        if (appUsage.isEmpty()) {
            appUsage.put("com.twitter.android", 18L * 60_000 + 8_000);
            appUsage.put("com.facebook.katana", 12L * 60_000 + 36_000);
            appUsage.put("com.spotify.music", 6L * 60_000 + 35_000);
            appUsage.put("com.android.deskclock", 47_000L);
            appUsage.put("com.google.android.gm", 20_000L);
        }

        // 🔹 Hiển thị
        showChart(hourlyUsage);
        showAppUsageList(appUsage);
        showTotalUsage(appUsage);
    }

    // === LẤY TỔNG THỜI GIAN SỬ DỤNG THEO APP ===
    @NonNull
    private Map<String, Long> getAppUsageToday() {
        Map<String, Long> appUsage = new HashMap<>();
        UsageStatsManager usageStatsManager =
                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) return appUsage;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();

        UsageEvents events = usageStatsManager.queryEvents(start, end);
        UsageEvents.Event event = new UsageEvents.Event();
        Map<String, Long> lastOpenTime = new HashMap<>();

        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            String pkg = event.getPackageName();
            if (pkg == null) continue;

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastOpenTime.put(pkg, event.getTimeStamp());
            } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                Long startTime = lastOpenTime.remove(pkg);
                if (startTime != null) {
                    long duration = event.getTimeStamp() - startTime;
                    appUsage.put(pkg, appUsage.getOrDefault(pkg, 0L) + duration);
                }
            }
        }
        return appUsage;
    }

    // === LẤY THỜI GIAN THEO GIỜ ===
    @NonNull
    private Map<Integer, Long> getHourlyUsageToday() {
        Map<Integer, Long> hourlyUsage = new HashMap<>();
        UsageStatsManager usageStatsManager =
                (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);

        if (usageStatsManager == null) return hourlyUsage;

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);

        long start = calendar.getTimeInMillis();
        long end = System.currentTimeMillis();

        UsageEvents events = usageStatsManager.queryEvents(start, end);
        UsageEvents.Event event = new UsageEvents.Event();
        Map<String, Long> lastForegroundByPkg = new HashMap<>();

        while (events.hasNextEvent()) {
            events.getNextEvent(event);
            String pkg = event.getPackageName();
            if (pkg == null) continue;

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                lastForegroundByPkg.put(pkg, event.getTimeStamp());
            } else if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND) {
                Long startTime = lastForegroundByPkg.remove(pkg);
                if (startTime != null && startTime > 0) {
                    long duration = event.getTimeStamp() - startTime;

                    Calendar c = Calendar.getInstance();
                    c.setTimeInMillis(startTime);
                    int hour = c.get(Calendar.HOUR_OF_DAY);

                    long prev = hourlyUsage.getOrDefault(hour, 0L);
                    hourlyUsage.put(hour, prev + duration);
                }
            }
        }
        return hourlyUsage;
    }

    // === VẼ BIỂU ĐỒ ===
    private void showChart(Map<Integer, Long> hourlyUsage) {
        List<BarEntry> entries = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            float minutes = hourlyUsage.getOrDefault(i, 0L) / 60000f;
            entries.add(new BarEntry(i, minutes));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Thời gian sử dụng (phút)");
        dataSet.setColor(getResources().getColor(R.color.white));
        dataSet.setValueTextSize(12f);
        dataSet.setValueTextColor(getResources().getColor(android.R.color.white));
        dataSet.setDrawValues(true);

        // 💬 Hiển thị "Xp" trên cột
        dataSet.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry barEntry) {
                float value = barEntry.getY();
                return value < 1 ? "" : String.format(Locale.getDefault(), "%.0fp", value);
            }
        });

        BarData data = new BarData(dataSet);
        data.setBarWidth(0.8f);

        barChart.setData(data);
        barChart.setFitBars(true);
        barChart.getDescription().setEnabled(false);

        // ⚙️ Cấu hình trục X
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(3f);
        xAxis.setLabelCount(8);
        xAxis.setTextSize(12f);
        xAxis.setDrawGridLines(false);

        // ⚙️ Giới hạn trục Y (tối đa 60 phút)
        barChart.getAxisLeft().setAxisMaximum(60f);
        barChart.getAxisLeft().setTextSize(12f);
        barChart.getAxisLeft().setGranularity(10f);
        barChart.getAxisLeft().setDrawGridLines(true);

        // Tắt trục phải & legend
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        barChart.animateY(800);
        barChart.invalidate();
    }


    private void showTotalUsage(@NonNull Map<String, Long> appUsage) {
        TextView tvTotalUsage = findViewById(R.id.tvTotalUsage);

        long totalMs = 0;
        for (long duration : appUsage.values()) {
            totalMs += duration;
        }

        long totalSeconds = totalMs / 1000;
        long hours = totalSeconds / 3600;
        long minutes = (totalSeconds % 3600) / 60;
        long seconds = totalSeconds % 60;

        String totalText;
        if (hours > 0) {
            totalText = String.format(Locale.getDefault(),
                    "Total usage: %d giờ %d phút %d giây", hours, minutes, seconds);
        } else {
            totalText = String.format(Locale.getDefault(),
                    "Total usage: %d phút %d giây", minutes, seconds);
        }

        tvTotalUsage.setText(totalText);
    }


    // === HIỂN THỊ DANH SÁCH APP ===
    private void showAppUsageList(@NonNull Map<String, Long> appUsage) {
        PackageManager pm = getPackageManager();

        List<Map.Entry<String, Long>> sorted = new ArrayList<>(appUsage.entrySet());
        sorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        long total = sorted.stream().mapToLong(Map.Entry::getValue).sum();

        for (Map.Entry<String, Long> entry : sorted) {
            String pkg = entry.getKey();
            long ms = entry.getValue();
            float mins = ms / 60000f;
            float percent = total == 0 ? 0 : (ms * 100f / total);

            String appName;
            Drawable icon;
            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                appName = pm.getApplicationLabel(info).toString();
                icon = AppCompatResources.getDrawable(this, info.icon);
            } catch (Exception e) {
                appName = pkg;
                icon = AppCompatResources.getDrawable(this, android.R.drawable.sym_def_app_icon);
            }

            // Layout chứa icon + thông tin
            LinearLayout itemLayout = new LinearLayout(this);
            itemLayout.setOrientation(LinearLayout.HORIZONTAL);
            itemLayout.setPadding(8, 16, 8, 16);
            itemLayout.setGravity(Gravity.CENTER_VERTICAL);

            ImageView iv = new ImageView(this);
            iv.setImageDrawable(icon);
            LinearLayout.LayoutParams iconParams = new LinearLayout.LayoutParams(100, 100);
            iv.setLayoutParams(iconParams);
            itemLayout.addView(iv);

            LinearLayout textLayout = new LinearLayout(this);
            textLayout.setOrientation(LinearLayout.VERTICAL);
            textLayout.setPadding(16, 0, 0, 0);
            textLayout.setLayoutParams(new LinearLayout.LayoutParams(
                    0, LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView tvName = new TextView(this);
            tvName.setText(appName);
            tvName.setTextSize(16);
            tvName.setTypeface(null, Typeface.BOLD);

            TextView tvDetail = new TextView(this);
            tvDetail.setText(String.format(Locale.getDefault(),
                    "%.0f phút (%.1f%%)", mins, percent));
            tvDetail.setTextSize(14);
            tvDetail.setTextColor(Color.DKGRAY);

            ProgressBar progress = new ProgressBar(this, null, android.R.attr.progressBarStyleHorizontal);
            progress.setProgress((int) percent);
            progress.setMax(100);
            progress.setProgressTintList(ColorStateList.valueOf(
                    ContextCompat.getColor(this, R.color.white)));
            progress.setScaleY(1.2f);

            textLayout.addView(tvName);
            textLayout.addView(progress);
            textLayout.addView(tvDetail);

            itemLayout.addView(textLayout);
            appListLayout.addView(itemLayout);
        }
    }
}
