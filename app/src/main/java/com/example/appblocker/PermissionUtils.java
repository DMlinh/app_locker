package com.example.appblocker;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.provider.Settings;
import android.view.accessibility.AccessibilityManager;

import java.util.List;

public class PermissionUtils {

    // 🔹 Kiểm tra quyền Usage Stats (theo dõi thời gian sử dụng)
    public static boolean hasUsageStatsPermission(Context context) {
        try {
            AppOpsManager appOps = (AppOpsManager) context.getSystemService(Context.APP_OPS_SERVICE);
            ApplicationInfo info = context.getApplicationInfo();
            int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                    info.uid, info.packageName);
            return mode == AppOpsManager.MODE_ALLOWED;
        } catch (Exception e) {
            return false;
        }
    }

    // 🔹 Mở trang yêu cầu quyền Usage Stats
    public static void requestUsageStatsPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // 🔹 Kiểm tra quyền Accessibility đã bật cho service chưa
    public static boolean isAccessibilityServiceEnabled(Context context, Class<?> serviceClass) {
        AccessibilityManager am = (AccessibilityManager) context.getSystemService(Context.ACCESSIBILITY_SERVICE);
        if (am == null) return false;

        List<AccessibilityServiceInfo> enabledServices =
                am.getEnabledAccessibilityServiceList(AccessibilityServiceInfo.FEEDBACK_ALL_MASK);

        for (AccessibilityServiceInfo serviceInfo : enabledServices) {
            String id = serviceInfo.getId();
            if (id.contains(context.getPackageName()) && id.contains(serviceClass.getSimpleName())) {
                return true;
            }
        }
        return false;
    }

    // 🔹 Mở trang yêu cầu quyền Accessibility
    public static void requestAccessibilityPermission(Context context) {
        Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }
}
