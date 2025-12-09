package com.example.appblocker;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONObject;

public class ProfileActivity extends BaseActivity {

    private static final int PICK_IMAGE = 1001;
    private final String avatarKeyPrefix = "avatar_uri_";
    private final String nameKey = "profile_name";
    private ImageView imgAvatar;
    private EditText edtName;
    private SharedPreferences avatarPrefs;
    private SharedPreferences profilePrefs;
    private GamificationManager gm;
    private String currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_profile);
        setupBottomNav(R.id.nav_profile);

        gm = new GamificationManager(this);
        gm.addPoints(300);
        // ƯU TIÊN lấy từ ProfilePrefs
        profilePrefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);
        String savedName = profilePrefs.getString(nameKey, null);

        if (savedName != null && !savedName.isEmpty()) {
            gm.setUser(savedName);
            currentUser = savedName;
        } else {
            currentUser = gm.getUser(); // nếu chưa có → lấy trong Gamification
        }

        avatarPrefs = getSharedPreferences("AvatarPrefs", MODE_PRIVATE);
        profilePrefs = getSharedPreferences("ProfilePrefs", MODE_PRIVATE);

        imgAvatar = findViewById(R.id.imgAvatar);
        edtName = findViewById(R.id.edtProfileName);

        // Load avatar
        loadAvatar();

        // Load tên hiển thị
        edtName.setText(gm.getUser());

        edtName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus) saveProfileName();
        });

        edtName.setOnEditorActionListener((v, actionId, event) -> {
            saveProfileName();
            edtName.clearFocus();
            return true;
        });

        imgAvatar.setOnClickListener(v -> openAvatarChooser());

        // UI RANK – XP – POINTS
        TextView tvPoints = findViewById(R.id.tvPoints);
        TextView tvRank = findViewById(R.id.tvRank);
        ProgressBar bar = findViewById(R.id.progressRank);

        tvPoints.setText("🎯 Điểm tập trung: " + gm.getFocusPoints());
        tvRank.setText("🏆 Cấp bậc: " + gm.getRank());
        bar.setProgress((int) (gm.getProgressPercent() * 100));

        findViewById(R.id.btnRanking).setOnClickListener(v ->
                startActivity(new Intent(this, RankingActivity.class))
        );

        // Load quest
        loadQuests();

        // Load theme với đúng logic createThemeItem()
        loadThemes();

    }

    // ------------------- AVATAR -------------------

    private void loadAvatar() {
        String key = avatarKeyPrefix + currentUser;
        String uri = avatarPrefs.getString(key, null);

        if (uri == null) return;

        if (uri.startsWith("res:")) {
            imgAvatar.setImageResource(Integer.parseInt(uri.substring(4)));
        } else {
            imgAvatar.setImageURI(Uri.parse(uri));
        }
    }

    private void openAvatarChooser() {
        String[] options = {"Chọn từ thư viện", "Chọn avatar có sẵn"};

        new AlertDialog.Builder(this)
                .setTitle("Chọn Avatar")
                .setItems(options, (d, w) -> {
                    if (w == 0) pickFromGallery();
                    else showDefaultAvatars();
                })
                .show();
    }

    private void pickFromGallery() {
        Intent i = new Intent(Intent.ACTION_PICK);
        i.setType("image/*");
        startActivityForResult(i, PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int req, int res, @Nullable Intent data) {
        super.onActivityResult(req, res, data);

        if (req == PICK_IMAGE && res == RESULT_OK && data != null) {
            Uri uri = data.getData();
            imgAvatar.setImageURI(uri);

            avatarPrefs.edit()
                    .putString(avatarKeyPrefix + currentUser, uri.toString())
                    .apply();
        }
    }

    private void showDefaultAvatars() {
        int[] list = {
                R.drawable.avatar1,
                R.drawable.avatar2,
                R.drawable.avatar3,
                R.drawable.avatar4,
                R.drawable.avatar5
        };

        GridView grid = new GridView(this);
        grid.setNumColumns(3);
        grid.setAdapter(new AvatarAdapter(this, list));

        AlertDialog dialog = new AlertDialog.Builder(this)
                .setTitle("Chọn Avatar")
                .setView(grid)
                .create();

        grid.setOnItemClickListener((p, v, pos, id) -> {
            int res = list[pos];
            imgAvatar.setImageResource(res);

            avatarPrefs.edit()
                    .putString(avatarKeyPrefix + currentUser, "res:" + res)
                    .apply();

            dialog.dismiss();
        });

        dialog.show();
    }

    // ------------------- QUEST UI -------------------

    private void loadQuests() {
        LinearLayout questList = findViewById(R.id.questList);
        questList.removeAllViews();

        JSONArray quests = gm.getDailyQuests();

        if (quests == null || quests.length() == 0) {
            TextView tv = new TextView(this);
            tv.setText("🎯 Không có nhiệm vụ hôm nay!");
            tv.setTextSize(16);
            tv.setPadding(8, 16, 8, 16);
            questList.addView(tv);
            return;
        }

        for (int i = 0; i < quests.length(); i++) {
            try {
                JSONObject q = quests.getJSONObject(i);

                View questItem = getLayoutInflater()
                        .inflate(R.layout.item_quest, questList, false);

                TextView tvQuest = questItem.findViewById(R.id.tvQuestTitle);
                TextView tvReward = questItem.findViewById(R.id.tvQuestReward);
                ImageView ivCheck = questItem.findViewById(R.id.ivQuestDone);

                tvQuest.setText(q.getString("title"));
                tvReward.setText("+" + q.getInt("reward") + " điểm");

                boolean done = q.getBoolean("done");  // ← SỬA Ở ĐÂY

                ivCheck.setVisibility(done ? View.VISIBLE : View.INVISIBLE);
                questItem.setAlpha(done ? 0.6f : 1f);

                questList.addView(questItem);

            } catch (Exception ignored) {
            }
        }
    }


    // ------------------- THEME UI (KEEP LOGIC createThemeItem) -------------------

    private void loadThemes() {
        LinearLayout themeList = findViewById(R.id.themeList);
        themeList.removeAllViews();

        themeList.addView(createThemeItem("Dark", true));
        themeList.addView(createThemeItem("Light", gm.isLightUnlocked()));
        themeList.addView(createThemeItem("Galaxy", gm.isGalaxyUnlocked()));
        themeList.addView(createThemeItem("Neon", gm.isNeonUnlocked()));
    }

    private View createThemeItem(String name, boolean unlocked) {
        View item = getLayoutInflater().inflate(R.layout.item_theme, null);

        TextView tvName = item.findViewById(R.id.tvThemeName);
        TextView tvIcon = item.findViewById(R.id.tvThemeIcon);
        ImageView ivLock = item.findViewById(R.id.ivLock);

        tvName.setText(name);

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

            int required = getRequiredPointsForTheme(name);
            int remain = Math.max(required - gm.getFocusPoints(), 0);

            String msg = "🔒 Cần " + required + " điểm để mở khóa theme " + name +
                    " (thiếu " + remain + " điểm).";

            item.setOnClickListener(v -> Toast.makeText(this, msg, Toast.LENGTH_SHORT).show());
        }

        return item;
    }

    private int getRequiredPointsForTheme(String name) {
        switch (name) {
            case "Light":
                return 0; // mở sẵn

            case "Galaxy":
                return 100;

            case "Neon":
                return 200;

            case "Dark":
                return 300;

            default:
                return 9999;
        }
    }


    // ------------------- SAVE NAME -------------------

    private void saveProfileName() {
        String newName = edtName.getText().toString().trim();
        if (newName.isEmpty()) return;

        String oldName = gm.getUser();   // lấy tên cũ

        // Lưu vào SharedPreferences
        profilePrefs.edit().putString(nameKey, newName).apply();

        // Lưu vào Gamification (SharedPreferences)
        gm.setUser(newName);

        // Cập nhật vào Database (để Ranking đọc đúng)
        UserDatabaseHelper db = new UserDatabaseHelper(this);
        db.updateUsername(newName);

        Toast.makeText(this, "Đã lưu tên!", Toast.LENGTH_SHORT).show();
    }


}
