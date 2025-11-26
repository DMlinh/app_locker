package com.example.appblocker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "users.db";
    private static final int DB_VERSION = 2;

    public UserDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE users (" +
                "id INTEGER PRIMARY KEY AUTOINCREMENT, " +
                "username TEXT UNIQUE, " +
                "password TEXT, " +
                "points INTEGER DEFAULT 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS users");
        onCreate(db);
    }

    // Đăng ký user
    public boolean registerUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", username);
        v.put("password", password);
        v.put("points", 0);
        return db.insert("users", null, v) != -1;
    }

    // Login
    public boolean loginUser(String username, String password) {
        SQLiteDatabase db = getReadableDatabase();

        Cursor cur = db.rawQuery(
                "SELECT * FROM users WHERE username=? AND password=?",
                new String[]{username, password});

        boolean ok = cur.getCount() > 0;
        cur.close();
        return ok;
    }

    // Lấy điểm
    public int getPoints(String username) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT points FROM users WHERE username=?",
                new String[]{username});

        if (c.moveToFirst()) {
            int p = c.getInt(0);
            c.close();
            return p;
        }
        c.close();
        return 0;
    }

    // Cộng điểm
    public void addPoints(String username, int amount) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE users SET points = points + ? WHERE username=?",
                new Object[]{amount, username});
    }

    // Lấy top 10
    public Cursor getTop10Cursor() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery(
                "SELECT username, points FROM users ORDER BY points DESC LIMIT 10",
                null
        );
    }
}
