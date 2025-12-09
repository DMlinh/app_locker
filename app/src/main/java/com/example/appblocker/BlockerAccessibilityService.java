package com.example.appblocker;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Set;

public class BlockerAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {

        boolean isBlockingActive = getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE)
                .getBoolean("isBlockingActive", false);

        boolean isTimeRestricted = !AllowedTimeManager.isNowAllowed(this);

        // Nếu không bật timer và không hạn chế giờ → không chặn
        if (!isBlockingActive && !isTimeRestricted) return;

        // Chỉ xử lý khi thay đổi cửa sổ app
        if (event.getEventType() != AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) return;

        String pkg = String.valueOf(event.getPackageName());
        if (pkg == null) return;

        // Bỏ qua launcher và system UI
        if (isSystemPackage(pkg)) return;

        Set<String> blocked = BlockedAppsManager.getBlockedApps(this);

        if (blocked.contains(pkg)) {
            // timer đang chạy HOẶC ngoài giờ cho phép
            if (isBlockingActive || isTimeRestricted) {
                performGlobalAction(GLOBAL_ACTION_HOME);
                Toast.makeText(this, "App bị khóa", Toast.LENGTH_SHORT).show();
            }
        }
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

