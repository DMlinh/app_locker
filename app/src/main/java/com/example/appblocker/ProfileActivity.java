package com.example.appblocker;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


public class ProfileActivity extends BaseActivity {
    private GamificationManager gm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        gm = new GamificationManager(this);

        TextView tvPoints = findViewById(R.id.tvPoints);
        TextView tvStreak = findViewById(R.id.tvStreak);
        TextView tvRank = findViewById(R.id.tvRank);
        LinearLayout themeList = findViewById(R.id.themeList);

        tvPoints.setText("🎯 Điểm tập trung: " + gm.getFocusPoints());
        tvStreak.setText("🔥 Chuỗi ngày: " + gm.getStreak());
        tvRank.setText("🏆 Cấp bậc: " + gm.getRank());

        themeList.addView(createThemeItem("Dark", true));
        themeList.addView(createThemeItem("Light", gm.isLightUnlocked()));
        themeList.addView(createThemeItem("Galaxy", gm.isGalaxyUnlocked()));
        themeList.addView(createThemeItem("Neon", gm.isNeonUnlocked()));

    }

    private View createThemeItem(String name, boolean unlocked) {
        View item = getLayoutInflater().inflate(R.layout.item_theme, null);
        TextView tvName = item.findViewById(R.id.tvThemeName);
        ImageView ivLock = item.findViewById(R.id.ivLock);

        tvName.setText(name);
        ivLock.setVisibility(unlocked ? View.GONE : View.VISIBLE);

        if (unlocked) {
            item.setOnClickListener(v -> {
                ThemeManager.setTheme(this, name);
                recreate();
                Toast.makeText(this, "Đã chọn theme: " + name, Toast.LENGTH_SHORT).show();
            });
        }
        else {
            item.setAlpha(0.4f);
            item.setOnClickListener(v ->
                    Toast.makeText(this, "Cần thêm điểm để mở khóa!", Toast.LENGTH_SHORT).show());
        }

        return item;
    }
}
