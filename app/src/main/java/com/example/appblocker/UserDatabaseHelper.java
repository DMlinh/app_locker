package com.example.appblocker;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class UserDatabaseHelper extends SQLiteOpenHelper {

    private static final String DB_NAME = "user_profile.db";
    private static final int DB_VERSION = 3;

    public UserDatabaseHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE user_profile (" +
                "id INTEGER PRIMARY KEY DEFAULT 1, " +
                "username TEXT, " +
                "points INTEGER DEFAULT 0" +
                ")");
        db.execSQL("INSERT INTO user_profile (id, username, points) VALUES (1, 'Người dùng', 0)");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        if (oldVersion < 3) {
            db.execSQL("ALTER TABLE user_profile ADD COLUMN username TEXT DEFAULT 'Người dùng'");
        }
    }

    public String getUserName() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT username FROM user_profile WHERE id=1", null);
        if (c.moveToFirst()) {
            String name = c.getString(0);
            c.close();
            return name;
        }
        c.close();
        return "Người dùng";
    }

    public void setUserName(String name) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues v = new ContentValues();
        v.put("username", name);
        db.update("user_profile", v, "id=1", null);
    }

    public int getPoints() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor c = db.rawQuery("SELECT points FROM user_profile WHERE id=1", null);
        if (c.moveToFirst()) {
            int p = c.getInt(0);
            c.close();
            return p;
        }
        c.close();
        return 0;
    }

    public void addPoints(int amount) {
        SQLiteDatabase db = getWritableDatabase();
        db.execSQL("UPDATE user_profile SET points = points + ? WHERE id=1",
                new Object[]{amount});
    }

    public Cursor getTop10Cursor() {
        SQLiteDatabase db = getReadableDatabase();
        return db.rawQuery("SELECT username, points FROM user_profile ORDER BY points DESC LIMIT 10", null);
    }

    public void updateUsername(String newName) {
        SQLiteDatabase db = this.getWritableDatabase();
        ContentValues cv = new ContentValues();
        cv.put("username", newName);

        db.update("user_profile", cv, "id=1", null);
    }


}

