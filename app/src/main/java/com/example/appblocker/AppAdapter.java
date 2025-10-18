package com.example.appblocker;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;
import java.util.Set;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.AppViewHolder> {
    private Context context;
    private List<AppInfo> appList;
    private Set<String> blockedApps;

    public AppAdapter(Context context, List<AppInfo> appList, Set<String> blockedApps) {
        this.context = context;
        this.appList = appList;
        this.blockedApps = blockedApps;
    }

    @NonNull
    @Override
    public AppViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_app, parent, false);
        return new AppViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AppViewHolder holder, int position) {
        AppInfo app = appList.get(position);
        holder.tvAppName.setText(app.appName);
        holder.imgAppIcon.setImageDrawable(app.icon);
        holder.cbBlock.setChecked(blockedApps.contains(app.packageName));

        holder.cbBlock.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (isChecked) {
                BlockedAppsManager.addBlockedApp(context, app.packageName);
            } else {
                BlockedAppsManager.removeBlockedApp(context, app.packageName);
            }
        });
    }

    @Override
    public int getItemCount() {
        return appList.size();
    }

    static class AppViewHolder extends RecyclerView.ViewHolder {
        ImageView imgAppIcon;
        TextView tvAppName;
        CheckBox cbBlock;

        public AppViewHolder(@NonNull View itemView) {
            super(itemView);
            imgAppIcon = itemView.findViewById(R.id.imgAppIcon);
            tvAppName = itemView.findViewById(R.id.tvAppName);
            cbBlock = itemView.findViewById(R.id.cbBlock);
        }
    }
}

