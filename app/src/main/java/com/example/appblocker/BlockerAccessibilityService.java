package com.example.appblocker;

import android.accessibilityservice.AccessibilityService;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import java.util.Set;

public class BlockerAccessibilityService extends AccessibilityService {

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        boolean isBlockingActive = getSharedPreferences("AppBlockerPrefs", MODE_PRIVATE).getBoolean("isBlockingActive",false);

        if (!isBlockingActive){
            return;
        }

        if (event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = String.valueOf(event.getPackageName());

            // Lấy danh sách app bị chặn
            Set<String> blockedApps = BlockedAppsManager.getBlockedApps(this);

            if (blockedApps.contains(packageName)) {
                // Nếu app nằm trong danh sách bị chặn → đóng app
                performGlobalAction(GLOBAL_ACTION_HOME);

                Toast.makeText(this,
                        "Ứng dụng " + packageName + " đã bị chặn!",
                        Toast.LENGTH_SHORT).show();
            }
        }
    }

    @Override
    public void onInterrupt() {
        // Không cần xử lý gì đặc biệt
    }
}

