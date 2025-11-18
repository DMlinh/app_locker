package com.example.appblocker;

import android.app.usage.UsageEvents;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import androidx.annotation.NonNull;

import java.util.*;

public class UsageAnalyzer {

    private final Context context;
    private final UsageStatsManager usm;
    private final PackageManager pm;

    // Danh sách app muốn loại trừ (launcher, system UI)
    private final Set<String> excludedPackages = new HashSet<>(Arrays.asList(
            "com.google.android.apps.nexuslauncher", // Pixel Launcher
            "com.android.systemui",
            "com.android.settings"
    ));

    public UsageAnalyzer(Context ctx) {
        this.context = ctx;
        this.usm = (UsageStatsManager) ctx.getSystemService(Context.USAGE_STATS_SERVICE);
        this.pm = ctx.getPackageManager();
    }

    private long getStartOfDay() {
        Calendar c = Calendar.getInstance();
        c.set(Calendar.HOUR_OF_DAY, 0);
        c.set(Calendar.MINUTE, 0);
        c.set(Calendar.SECOND, 0);
        c.set(Calendar.MILLISECOND, 0);
        return c.getTimeInMillis();
    }

    private boolean isUserApp(String packageName) {
        if (excludedPackages.contains(packageName)) return false;
        try {
            ApplicationInfo ai = pm.getApplicationInfo(packageName, 0);
            return (ai.flags & ApplicationInfo.FLAG_SYSTEM) == 0;
        } catch (PackageManager.NameNotFoundException e) {
            return false;
        }
    }

    @NonNull
    public Map<Integer, Long> getHourlyUsage() {
        long start = getStartOfDay();
        long end = System.currentTimeMillis();

        UsageEvents events = usm.queryEvents(start, end);
        Map<Integer, Long> hourly = new HashMap<>();

        for (int i = 0; i < 24; i++) hourly.put(i, 0L);

        UsageEvents.Event event = new UsageEvents.Event();
        String foregroundApp = null;
        long startTime = 0;

        while (events.hasNextEvent()) {
            events.getNextEvent(event);

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND &&
                    isUserApp(event.getPackageName())) {
                foregroundApp = event.getPackageName();
                startTime = event.getTimeStamp();
            }

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND &&
                    foregroundApp != null &&
                    isUserApp(foregroundApp)) {

                long duration = event.getTimeStamp() - startTime;
                Calendar c = Calendar.getInstance();
                c.setTimeInMillis(startTime);
                int hour = c.get(Calendar.HOUR_OF_DAY);
                hourly.put(hour, hourly.get(hour) + duration);

                foregroundApp = null;
            }
        }

        return hourly;
    }

    @NonNull
    public Map<String, Long> getAppUsage() {
        long start = getStartOfDay();
        long end = System.currentTimeMillis();

        UsageEvents events = usm.queryEvents(start, end);

        Map<String, Long> usage = new HashMap<>();
        Map<String, Long> startTimes = new HashMap<>();

        UsageEvents.Event event = new UsageEvents.Event();

        while (events.hasNextEvent()) {
            events.getNextEvent(event);

            String pkg = event.getPackageName();
            if (!isUserApp(pkg)) continue;

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_FOREGROUND) {
                startTimes.put(pkg, event.getTimeStamp());
            }

            if (event.getEventType() == UsageEvents.Event.MOVE_TO_BACKGROUND &&
                    startTimes.containsKey(pkg)) {

                long duration = event.getTimeStamp() - startTimes.get(pkg);
                usage.put(pkg, usage.getOrDefault(pkg, 0L) + duration);

                startTimes.remove(pkg);
            }
        }

        return usage;
    }
}
