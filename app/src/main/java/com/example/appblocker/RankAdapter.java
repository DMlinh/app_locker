package com.example.appblocker;

import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
public class RankAdapter extends RecyclerView.Adapter<RankAdapter.ViewHolder> {

    private ArrayList<UserRank> list;

    public RankAdapter(ArrayList<UserRank> list) {
        this.list = list;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_rank, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder h, int position) {
        UserRank u = list.get(position);

        h.txtRank.setText("#" + (position + 1));
        h.txtName.setText(u.name);
        h.txtPoints.setText(u.points + " pts");

        if (u.avatarUri != null) {
            if (u.avatarUri.startsWith("res:")) {
                int resId = Integer.parseInt(u.avatarUri.substring(4));
                h.imgAvatar.setImageResource(resId);
            } else {
                h.imgAvatar.setImageURI(Uri.parse(u.avatarUri));
            }
        } else {
            h.imgAvatar.setImageResource(R.drawable.avatar1);
        }
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
        TextView txtRank, txtName, txtPoints;
        ImageView imgAvatar;  // thêm ImageView

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            txtRank = itemView.findViewById(R.id.txtRank);
            txtName = itemView.findViewById(R.id.txtName);
            txtPoints = itemView.findViewById(R.id.txtPoints);
            imgAvatar = itemView.findViewById(R.id.imgAvatar); // bind ImageView
        }
    }
}
