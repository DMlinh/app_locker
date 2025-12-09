package com.example.appblocker;

import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

public class RankingActivity extends BaseActivity {

    private RecyclerView recyclerRank;
    private RankAdapter adapter;
    private ArrayList<UserRank> rankList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_ranking);

        recyclerRank = findViewById(R.id.recyclerRank);
        recyclerRank.setLayoutManager(new LinearLayoutManager(this));

        rankList = loadTop10Users();
        adapter = new RankAdapter(rankList);
        recyclerRank.setAdapter(adapter);
    }

    private ArrayList<UserRank> loadTop10Users() {
        ArrayList<UserRank> list = new ArrayList<>();
        UserDatabaseHelper helper = new UserDatabaseHelper(this);

        // SharedPreferences lưu avatar của user hiện tại
        SharedPreferences prefs = getSharedPreferences("AvatarPrefs", MODE_PRIVATE);

        Cursor cur = helper.getTop10Cursor();
        while (cur.moveToNext()) {
            String name = cur.getString(cur.getColumnIndexOrThrow("username"));
            int pts = cur.getInt(cur.getColumnIndexOrThrow("points"));

            // Lấy avatar từ SharedPreferences
            String avatarUri = prefs.getString("avatar_uri_" + name, null);
            // Nếu muốn chỉ dùng avatar của current user thì dùng: prefs.getString("avatar_uri", null);

            list.add(new UserRank(name, pts, avatarUri));
        }
        cur.close();
        return list;
    }

}
