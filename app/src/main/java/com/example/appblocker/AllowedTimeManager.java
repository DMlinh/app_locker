package com.example.appblocker;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Calendar;

public class AllowedTimeManager {

    private static final String PREF = "AllowedTimePrefs";

    public static void saveWindow(Context c, int sh, int sm, int eh, int em, boolean enabled) {
        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        p.edit()
                .putInt("sh", sh)
                .putInt("sm", sm)
                .putInt("eh", eh)
                .putInt("em", em)
                .putBoolean("enabled", enabled)
                .apply();
    }

    public static boolean isEnabled(Context c) {
        return c.getSharedPreferences(PREF, Context.MODE_PRIVATE)
                .getBoolean("enabled", false);
    }

    public static int[] getStart(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return new int[]{p.getInt("sh", 0), p.getInt("sm", 0)};
    }

    public static int[] getEnd(Context c) {
        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);
        return new int[]{p.getInt("eh", 23), p.getInt("em", 59)};
    }

    // ================================================================
    // THIS IS THE IMPORTANT PART — FIXED
    // ================================================================
    public static boolean isNowAllowed(Context c) {

        SharedPreferences p = c.getSharedPreferences(PREF, Context.MODE_PRIVATE);

        if (!p.getBoolean("enabled", false))
            return true;   // nếu không bật time window → luôn cho phép

        int sh = p.getInt("sh", 0);
        int sm = p.getInt("sm", 0);
        int eh = p.getInt("eh", 23);
        int em = p.getInt("em", 59);

        Calendar now = Calendar.getInstance();
        int nh = now.get(Calendar.HOUR_OF_DAY);
        int nm = now.get(Calendar.MINUTE);

        // convert về phút để so sánh dễ
        int start = sh * 60 + sm;
        int end = eh * 60 + em;
        int curr = nh * 60 + nm;

        // TRƯỜNG HỢP 1: khung giờ bình thường (start < end)
        if (start <= end) {
            return curr >= start && curr <= end;
        }

        // TRƯỜNG HỢP 2: khung giờ qua đêm (vd 22:00 → 06:00)
        return curr >= start || curr <= end;
    }

    public static String formatWindow(Context c) {
        int[] st = getStart(c);
        int[] en = getEnd(c);
        return String.format(
                "%02d:%02d – %02d:%02d",
                st[0], st[1], en[0], en[1]
        );
    }
}

