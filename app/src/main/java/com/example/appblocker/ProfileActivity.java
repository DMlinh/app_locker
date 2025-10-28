package com.example.appblocker;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

public class ProfileActivity extends BaseActivity {
    private GamificationManager gm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);

        gm = new GamificationManager(this);

        TextView tvPoints = findViewById(R.id.tvPoints);
        TextView tvRank = findViewById(R.id.tvRank);
        LinearLayout themeList = findViewById(R.id.themeList);
        TextView rankText = findViewById(R.id.tvProgressLabel);
        ProgressBar xpBar = findViewById(R.id.progressRank);
        LinearLayout questList = findViewById(R.id.questList);

        // 🔹 Hiển thị điểm & cấp bậc
        tvPoints.setText("🎯 Điểm tập trung: " + gm.getFocusPoints());
        tvRank.setText("🏆 Cấp bậc: " + gm.getRank());
        rankText.setText(gm.getProgressText() + " → " + gm.getNextRankName());
        xpBar.setProgress((int) (gm.getProgressPercent() * 100));

        // 🔹 Hiển thị theme
        themeList.addView(createThemeItem("Dark", true));
        themeList.addView(createThemeItem("Light", gm.isLightUnlocked()));
        themeList.addView(createThemeItem("Galaxy", gm.isGalaxyUnlocked()));
        themeList.addView(createThemeItem("Neon", gm.isNeonUnlocked()));

        // 🔹 Hiển thị danh sách quest
        displayDailyQuests(questList);

        // 💡 TEST: Hoàn thành quest đầu tiên (tự cộng điểm)
        JSONArray quests = gm.getDailyQuests();
        if (quests.length() > 0) {
            try {
                String questId = quests.getJSONObject(0).getString("id");
                gm.completeQuest(questId);
                Toast.makeText(this, "✅ Đã hoàn thành quest: " + questId, Toast.LENGTH_SHORT).show();
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }

        // Cập nhật lại giao diện sau khi hoàn thành
        displayDailyQuests(questList);
        tvPoints.setText("🎯 Điểm tập trung: " + gm.getFocusPoints());
        tvRank.setText("🏆 Cấp bậc: " + gm.getRank());
        rankText.setText(gm.getProgressText() + " → " + gm.getNextRankName());
        xpBar.setProgress((int) (gm.getProgressPercent() * 100));
    }
    /**
     * Hiển thị danh sách nhiệm vụ hàng ngày
     */
    private void displayDailyQuests(LinearLayout questList) {
        questList.removeAllViews();

        JSONArray quests = gm.getDailyQuests();
        if (quests == null || quests.length() == 0) {
            TextView emptyView = new TextView(this);
            emptyView.setText("🎯 Không có nhiệm vụ hôm nay!");
            emptyView.setTextColor(getResources().getColor(android.R.color.darker_gray));
            emptyView.setTextSize(16);
            emptyView.setPadding(8, 16, 8, 16);
            questList.addView(emptyView);
            return;
        }

        for (int i = 0; i < quests.length(); i++) {
            try {
                JSONObject q = quests.getJSONObject(i);
                View questItem = getLayoutInflater().inflate(R.layout.item_quest, questList, false);

                TextView tvQuest = questItem.findViewById(R.id.tvQuestTitle);
                TextView tvReward = questItem.findViewById(R.id.tvQuestReward);
                ImageView ivCheck = questItem.findViewById(R.id.ivQuestDone);

                tvQuest.setText(q.getString("title"));
                tvReward.setText("+" + q.getInt("reward") + " điểm");

                boolean done = q.getBoolean("completed");
                ivCheck.setVisibility(done ? View.VISIBLE : View.INVISIBLE);
                questItem.setAlpha(done ? 0.6f : 1f);

                questList.addView(questItem);
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Tạo view hiển thị từng theme
     */
    private View createThemeItem(String name, boolean unlocked) {
        View item = getLayoutInflater().inflate(R.layout.item_theme, null);
        TextView tvName = item.findViewById(R.id.tvThemeName);
        TextView tvIcon = item.findViewById(R.id.tvThemeIcon);
        ImageView ivLock = item.findViewById(R.id.ivLock);

        tvName.setText(name);

        switch (name) {
            case "Dark": tvIcon.setText("🌑"); break;
            case "Light": tvIcon.setText("☀️"); break;
            case "Galaxy": tvIcon.setText("🌌"); break;
            case "Neon": tvIcon.setText("🌈"); break;
            default: tvIcon.setText("🎨");
        }

        ivLock.setVisibility(unlocked ? View.GONE : View.VISIBLE);
        item.setAlpha(unlocked ? 1f : 0.4f);

        if (unlocked) {
            item.setOnClickListener(v -> {
                ThemeManager.setTheme(this, name);
                recreate();
                Toast.makeText(this, "Đã chọn theme: " + name, Toast.LENGTH_SHORT).show();
            });
        } else {
            item.setOnClickListener(v ->
                    Toast.makeText(this, "Cần thêm điểm để mở khóa!", Toast.LENGTH_SHORT).show());
        }

        return item;
    }
}
