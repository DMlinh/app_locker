package com.example.appblocker;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.appcompat.content.res.AppCompatResources;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;
import com.google.android.material.color.MaterialColors;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class UsageChartActivity extends BaseActivity {

    private BarChart barChart;
    private LinearLayout appList;
    private UsageAnalyzer analyzer;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_usage_chart);
        setupBottomNav(R.id.nav_stats);

        analyzer = new UsageAnalyzer(this);

        barChart = findViewById(R.id.barChart);
        appList = findViewById(R.id.appListLayout);

        Map<Integer, Long> hourly = analyzer.getHourlyUsage();
        Map<String, Long> apps = analyzer.getAppUsage();

        showChart(hourly);
        showAppList(apps);
        showTotal(apps);
    }

    // === BIỂU ĐỒ ===
    private void showChart(Map<Integer, Long> hourly) {
        List<BarEntry> list = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            list.add(new BarEntry(i, hourly.get(i) / 60000f));
        }

        int barColor = MaterialColors.getColor(this, R.attr.chartBarColor, Color.WHITE);
        int textColor = MaterialColors.getColor(this, R.attr.chartTextColor, Color.WHITE);

        BarDataSet set = new BarDataSet(list, "");
        set.setColor(barColor);
        set.setValueTextColor(textColor);
        set.setValueTextSize(10f);
        set.setValueTypeface(Typeface.DEFAULT_BOLD);

        set.setValueFormatter(new ValueFormatter() {
            @Override
            public String getBarLabel(BarEntry e) {
                return e.getY() < 1 ? "" : ((int) e.getY()) + "p";
            }
        });

        BarData data = new BarData(set);
        data.setBarWidth(0.8f);

        barChart.setData(data);
        barChart.getDescription().setEnabled(false);
        barChart.getAxisRight().setEnabled(false);
        barChart.getLegend().setEnabled(false);

        barChart.getAxisLeft().setAxisMinimum(0f);
        barChart.getAxisLeft().setAxisMaximum(60f);
        barChart.getAxisLeft().setTextColor(textColor);

        XAxis x = barChart.getXAxis();
        x.setPosition(XAxis.XAxisPosition.BOTTOM);
        x.setTextColor(textColor);
        x.setGranularity(3f);
        x.setLabelCount(8);

        barChart.animateY(800);
        barChart.invalidate();
    }

    // === TỔNG GIỜ DÙNG ===
    private void showTotal(Map<String, Long> apps) {
        TextView tv = findViewById(R.id.tvTotalUsage);

        long totalMs = 0;
        for (long t : apps.values()) totalMs += t;

        long h = totalMs / 3600000;
        long m = (totalMs % 3600000) / 60000;
        long s = (totalMs % 60000) / 1000;

        tv.setText(String.format(Locale.getDefault(),
                "Tổng mức sử dụng: %d giờ %d phút %d giây", h, m, s));
    }

    // === DANH SÁCH APP ===
    private void showAppList(Map<String, Long> apps) {
        PackageManager pm = getPackageManager();
        long total = apps.values().stream().mapToLong(v -> v).sum();

        List<Map.Entry<String, Long>> sorted =
                new ArrayList<>(apps.entrySet());
        sorted.sort(Map.Entry.comparingByValue(Comparator.reverseOrder()));

        for (Map.Entry<String, Long> entry : sorted) {
            String pkg = entry.getKey();
            long ms = entry.getValue();

            String name;
            Drawable icon;

            try {
                ApplicationInfo info = pm.getApplicationInfo(pkg, 0);
                name = pm.getApplicationLabel(info).toString();
                icon = pm.getApplicationIcon(pkg);
            } catch (Exception e) {
                name = pkg;
                icon = AppCompatResources.getDrawable(this, android.R.drawable.sym_def_app_icon);
            }

            float minutes = ms / 60000f;
            float percent = total == 0 ? 0 : ms * 100f / total;

            LinearLayout row = new LinearLayout(this);
            row.setOrientation(LinearLayout.HORIZONTAL);
            row.setPadding(8, 16, 8, 16);
            row.setGravity(Gravity.CENTER_VERTICAL);

            ImageView iv = new ImageView(this);
            iv.setImageDrawable(icon);
            iv.setLayoutParams(new LinearLayout.LayoutParams(100, 100));
            row.addView(iv);

            LinearLayout col = new LinearLayout(this);
            col.setOrientation(LinearLayout.VERTICAL);
            col.setPadding(16, 0, 0, 0);
            col.setLayoutParams(new LinearLayout.LayoutParams(0,
                    LinearLayout.LayoutParams.WRAP_CONTENT, 1));

            TextView tvName = new TextView(this);
            tvName.setText(name);
            tvName.setTextSize(16);
            tvName.setTypeface(null, Typeface.BOLD);

            TextView tvDetail = new TextView(this);
            tvDetail.setText(String.format(Locale.getDefault(),
                    "%.0f phút (%.1f%%)", minutes, percent));
            tvDetail.setTextSize(14);
            tvDetail.setTextColor(Color.DKGRAY);

            TypedValue v = new TypedValue();
            getTheme().resolveAttribute(R.attr.chartBarColor, v, true);

            ProgressBar p = new ProgressBar(this, null,
                    android.R.attr.progressBarStyleHorizontal);
            p.setProgress((int) percent);
            p.setMax(100);
            p.setProgressTintList(android.content.res.ColorStateList.valueOf(v.data));
            p.setScaleY(1.2f);

            col.addView(tvName);
            col.addView(p);
            col.addView(tvDetail);

            row.addView(col);
            appList.addView(row);
        }
    }
}
