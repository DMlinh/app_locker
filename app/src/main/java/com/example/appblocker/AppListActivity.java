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

        RecyclerView recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        PackageManager pm = getPackageManager();
        List<ApplicationInfo> apps = pm.getInstalledApplications(PackageManager.GET_META_DATA);

        List<AppInfo> userApps = new ArrayList<>();

        for (ApplicationInfo appInfo : apps) {
            try {
                // Bỏ qua app hệ thống, chỉ lấy app user cài
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {
                    String appName = pm.getApplicationLabel(appInfo).toString();
                    Drawable icon = pm.getApplicationIcon(appInfo);
                    String packageName = appInfo.packageName;

                    userApps.add(new AppInfo(appName, packageName, icon));
                }
                // Nếu muốn hiển thị Play Store hoặc các app hệ thống quan trọng, thêm điều kiện riêng
                else if (appInfo.packageName.equals("com.android.vending") // Google Play Store
                        || appInfo.packageName.equals("com.google.android.gms")) { // Google Play Services
                    String appName = pm.getApplicationLabel(appInfo).toString();
                    Drawable icon = pm.getApplicationIcon(appInfo);
                    String packageName = appInfo.packageName;

                    userApps.add(new AppInfo(appName, packageName, icon));
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Gán vào biến toàn cục
        this.appList = userApps;

        // Load danh sách chặn đã lưu
        Set<String> blockedApps = BlockedAppsManager.getBlockedApps(this);

        // Tạo adapter
        AppAdapter adapter = new AppAdapter(this, this.appList, blockedApps);
        recyclerView.setAdapter(adapter);
    }
}


