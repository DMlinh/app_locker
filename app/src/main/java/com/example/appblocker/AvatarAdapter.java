package com.example.appblocker;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;

public class AvatarAdapter extends BaseAdapter {

    private Context context;
    private int[] avatars;

    public AvatarAdapter(Context context, int[] avatars) {
        this.context = context;
        this.avatars = avatars;
    }

    @Override
    public int getCount() { return avatars.length; }

    @Override
    public Object getItem(int i) { return avatars[i]; }

    @Override
    public long getItemId(int i) { return i; }

    @Override
    public View getView(int i, View convertView, ViewGroup parent) {
        ImageView img = new ImageView(context);
        img.setLayoutParams(new GridView.LayoutParams(200, 200));
        img.setScaleType(ImageView.ScaleType.CENTER_CROP);
        img.setImageResource(avatars[i]);
        return img;
    }
}

