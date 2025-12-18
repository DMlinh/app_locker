package com.example.appblocker;

import android.accessibilityservice.AccessibilityService;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Set;

public class BlockerAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        int type = event.getEventType();
        if (type != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED
                && type != AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED)
            return;

        if (event.getPackageName() == null) return;
        String pkg = event.getPackageName().toString();
        if (isSystemPackage(pkg)) return;

        boolean timer = getSharedPreferences(
                "AppBlockerPrefs", MODE_PRIVATE
        ).getBoolean("isBlockingActive", false);

        boolean timeEnabled = AllowedTimeManager.isEnabled(this);
        boolean nowAllowed = AllowedTimeManager.isNowAllowed(this);

        Log.d("BLOCK_DEBUG",
                "pkg=" + pkg
                        + " | timer=" + timer
                        + " | timeEnabled=" + timeEnabled
                        + " | nowAllowed=" + nowAllowed
        );

        Set<String> blocked = BlockedAppsManager.getBlockedApps(this);
        if (!blocked.contains(pkg)) return;

        if (timeEnabled && nowAllowed) return;

        if (timeEnabled && !nowAllowed) {
            block();
            return;
        }

        if (timer) {
            block();
        }
    }


    private void block() {
        performGlobalAction(GLOBAL_ACTION_HOME);
        Toast.makeText(this, "App bị khóa", Toast.LENGTH_SHORT).show();
    }



    private boolean isSystemPackage(String pkg) {
        return pkg.startsWith("com.android")
                || pkg.startsWith("com.google.android")
                || pkg.contains("launcher")
                || pkg.contains("systemui")
                || pkg.equals(getPackageName()); // không tự chặn chính app
    }


    @Override
    public void onInterrupt() {
    }
}

