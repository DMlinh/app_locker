package com.example.appblocker;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class AppListActivity extends BaseActivity {

    private RecyclerView recyclerView;
    private AppAdapter adapter;
    private List<AppInfo> appList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_list);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);
        List<AppInfo> userApps = new ArrayList<>();

        String myPackage = getPackageName(); // 🔹 Lấy tên gói của chính app AppBlocker

        for (ApplicationInfo appInfo : apps) {
            try {
                String packageName = appInfo.packageName;

                // 🔹 Bỏ qua chính AppBlocker để không tự hiển thị trong danh sách
                if (packageName.equals(myPackage)) continue;

                // 🔹 Chỉ hiển thị app người dùng cài hoặc một số hệ thống quan trọng
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0
                        || packageName.equals("com.android.vending")
                        || packageName.equals("com.google.android.gms")) {

                    String appName = pm.getApplicationLabel(appInfo).toString();
                    Drawable icon = pm.getApplicationIcon(appInfo);
                    userApps.add(new AppInfo(appName, packageName, icon));
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Gán vào biến toàn cục
        this.appList = userApps;

        // Load danh sách app bị chặn đã lưu
        Set<String> blockedApps = BlockedAppsManager.getBlockedApps(this);

        // Tạo adapter
        adapter = new AppAdapter(this, this.appList, blockedApps);
        recyclerView.setAdapter(adapter);
    }
}
