package com.example.appblocker;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.GridView;
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
    private static final int PICK_IMAGE = 1001;
    private ImageView imgAvatar;
    private SharedPreferences prefs;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNav(R.id.nav_profile);

        gm = new GamificationManager(this);

        // load current user
        currentUser = getSharedPreferences("USER_SESSION", MODE_PRIVATE)
                .getString("current_user", null);
        gm.setUser(currentUser);

        // set theme theo user hiện tại
        String theme = ThemeManager.getUserTheme(this, currentUser);
        if (!gm.canUseTheme(theme)) {
            theme = "Dark"; // fallback nếu chưa đủ điểm
        }
        ThemeManager.setTheme(this, theme);

        TextView tvHello = findViewById(R.id.tvHello);
        ImageView btnLeaderboard = findViewById(R.id.btnLeaderboard);
        bottomNav = findViewById(R.id.bottomNavigation);
        TextView tvPoints = findViewById(R.id.tvPoints);
        TextView tvRank = findViewById(R.id.tvRank);
        LinearLayout themeList = findViewById(R.id.themeList);
        TextView rankText = findViewById(R.id.tvProgressLabel);
        ProgressBar xpBar = findViewById(R.id.progressRank);
        LinearLayout questList = findViewById(R.id.questList);

        // hiển thị điểm & cấp bậc
        tvHello.setText("Xin chào " + (currentUser != null ? currentUser : "Người dùng") + "!");
        tvPoints.setText("🎯 Điểm tập trung: " + gm.getFocusPoints());
        tvRank.setText("🏆 Cấp bậc: " + gm.getRank());
        rankText.setText(gm.getProgressText() + " → " + gm.getNextRankName());
        xpBar.setProgress((int) (gm.getProgressPercent() * 100));

        // hiển thị theme
        themeList.addView(createThemeItem("Dark", true));
        themeList.addView(createThemeItem("Light", gm.isLightUnlocked()));
        themeList.addView(createThemeItem("Galaxy", gm.isGalaxyUnlocked()));
        themeList.addView(createThemeItem("Neon", gm.isNeonUnlocked()));

        // hiển thị quest
        displayDailyQuests(questList);

        // cập nhật lại UI
        tvPoints.setText("🎯 Điểm tập trung: " + gm.getFocusPoints());
        tvRank.setText("🏆 Cấp bậc: " + gm.getRank());
        rankText.setText(gm.getProgressText() + " → " + gm.getNextRankName());
        xpBar.setProgress((int) (gm.getProgressPercent() * 100));

        btnLeaderboard.setOnClickListener(v -> {
            v.animate()
                    .scaleX(0.85f).scaleY(0.85f).setDuration(120)
                    .withEndAction(() -> {
                        v.animate().scaleX(1f).scaleY(1f).setDuration(120).start();
                        startActivity(new Intent(ProfileActivity.this, RankingActivity.class));
                    }).start();
        });

        imgAvatar = findViewById(R.id.imgAvatar);
        prefs = getSharedPreferences("AvatarPrefs", MODE_PRIVATE);

        // load avatar theo user
        String savedAvatar = prefs.getString("avatar_uri_" + currentUser, null);
        if (savedAvatar != null) {
            if (savedAvatar.startsWith("res:")) {
                int resId = Integer.parseInt(savedAvatar.replace("res:", ""));
                imgAvatar.setImageResource(resId);
            } else {
                imgAvatar.setImageURI(Uri.parse(savedAvatar));
            }
        }

        imgAvatar.setOnClickListener(v -> openAvatarChooser());
    }

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
                ThemeManager.setUserTheme(this, currentUser, name);
                ThemeManager.setTheme(this, name);
                recreate();
                Toast.makeText(this, "Đã chọn theme: " + name, Toast.LENGTH_SHORT).show();
            });
        } else {
            int requiredPoints = 0;
            switch (name) {
                case "Light": requiredPoints = 100; break;
                case "Galaxy": requiredPoints = 200; break;
                case "Neon": requiredPoints = 300; break;
            }
            int current = gm.getFocusPoints();
            int remaining = Math.max(requiredPoints - current, 0);
            String message = requiredPoints > 0
                    ? "🔒 Cần " + requiredPoints + " điểm để mở khóa theme " + name
                    + " (thiếu " + remaining + " điểm)"
                    : "🔒 Theme này chưa khả dụng.";
            final String toastMessage = message;
            item.setOnClickListener(v -> Toast.makeText(this, toastMessage, Toast.LENGTH_SHORT).show());
        }

        return item;
    }

    private void openAvatarChooser() {
        String[] options = {"Chọn từ thư viện", "Chọn avatar có sẵn"};

        new AlertDialog.Builder(this)
                .setTitle("Chọn Avatar")
                .setItems(options, (dialog, which) -> {
                    if (which == 0) pickFromGallery();
                    else showDefaultAvatars();
                })
                .show();
    }

    private void pickFromGallery() {
        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE && resultCode == RESULT_OK && data != null) {
            Uri uri = data.getData();
            imgAvatar.setImageURI(uri);
            prefs.edit().putString("avatar_uri_" + currentUser, uri.toString()).apply();
        }
    }

    private void showDefaultAvatars() {
        int[] avatarRes = {
                R.drawable.avatar1,
                R.drawable.avatar2,
                R.drawable.avatar3,
                R.drawable.avatar4,
                R.drawable.avatar5
        };

        GridView grid = new GridView(this);
        grid.setNumColumns(3);
        grid.setAdapter(new AvatarAdapter(this, avatarRes));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chọn Avatar")
                .setView(grid)
                .create();

        grid.setOnItemClickListener((parent, view, position, id) -> {
            imgAvatar.setImageResource(avatarRes[position]);
            prefs.edit().putString("avatar_uri_" + currentUser, "res:" + avatarRes[position]).apply();
            dialog.dismiss();
        });

        dialog.show();
    }
}
