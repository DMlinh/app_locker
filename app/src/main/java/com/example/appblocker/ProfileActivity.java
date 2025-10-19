package com.example.appblocker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


public class ProfileActivity extends BaseActivity {
    private GamificationManager gm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        //Reset Điểm và chuỗi
        SharedPreferences prefs = getSharedPreferences("GamificationPrefs", MODE_PRIVATE);
        prefs.edit()
                .putInt("focus_points", 0)
                .putInt("streak", 0)
                .apply();
        Toast.makeText(this, "Đã reset điểm và chuỗi ngày!", Toast.LENGTH_SHORT).show();


        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        gm = new GamificationManager(this);

        TextView tvPoints = findViewById(R.id.tvPoints);
        TextView tvStreak = findViewById(R.id.tvStreak);
        TextView tvRank = findViewById(R.id.tvRank);
        LinearLayout themeList = findViewById(R.id.themeList);
        TextView rankText = findViewById(R.id.tvProgressLabel);
        ProgressBar xpBar = findViewById(R.id.progressRank);

        gm.addPoints(99);

        tvPoints.setText("🎯 Điểm tập trung: " + gm.getFocusPoints());
        tvStreak.setText("🔥 Chuỗi ngày: " + gm.getStreak());
        tvRank.setText("🏆 Cấp bậc: " + gm.getRank());

        themeList.addView(createThemeItem("Dark", true));
        themeList.addView(createThemeItem("Light", gm.isLightUnlocked()));
        themeList.addView(createThemeItem("Galaxy", gm.isGalaxyUnlocked()));
        themeList.addView(createThemeItem("Neon", gm.isNeonUnlocked()));


        rankText.setText(gm.getProgressText()+"->"+gm.getNextRankName()); // ví dụ: "Beginner (45/100)"
        xpBar.setProgress((int) (gm.getProgressPercent() * 100)); // thanh 0-100%
    }

    private View createThemeItem(String name, boolean unlocked) {
        View item = getLayoutInflater().inflate(R.layout.item_theme, null);
        TextView tvName = item.findViewById(R.id.tvThemeName);
        TextView tvIcon = item.findViewById(R.id.tvThemeIcon); // thêm dòng này
        ImageView ivLock = item.findViewById(R.id.ivLock);

        // set tên
        tvName.setText(name);

        // set icon tương ứng
        switch (name) {
            case "Dark":
                tvIcon.setText("🌑");
                break;
            case "Light":
                tvIcon.setText("☀️");
                break;
            case "Galaxy":
                tvIcon.setText("🌌");
                break;
            case "Neon":
                tvIcon.setText("🌈");
                break;
            default:
                tvIcon.setText("🎨");
                break;
        }

        // khóa / mở
        ivLock.setVisibility(unlocked ? View.GONE : View.VISIBLE);

        if (unlocked) {
            item.setOnClickListener(v -> {
                ThemeManager.setTheme(this, name);
                recreate();
                Toast.makeText(this, "Đã chọn theme: " + name, Toast.LENGTH_SHORT).show();
            });
        } else {
            item.setAlpha(0.4f);
            item.setOnClickListener(v ->
                    Toast.makeText(this, "Cần thêm điểm để mở khóa!", Toast.LENGTH_SHORT).show());
        }

        return item;
    }
}
