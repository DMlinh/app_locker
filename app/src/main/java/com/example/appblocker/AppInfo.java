package com.example.appblocker;

import android.graphics.drawable.Drawable;

public class AppInfo {
    String appName;
    String packageName;
    Drawable icon;

    public AppInfo(String appName, String packageName, Drawable icon) {
        this.appName = appName;
        this.packageName = packageName;
        this.icon = icon;
    }
}

