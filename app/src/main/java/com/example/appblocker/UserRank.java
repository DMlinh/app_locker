package com.example.appblocker;

public class UserRank {
    public String name;
    public int points;
    public String avatarUri; // "res:ID" hoặc URI từ gallery

    public UserRank(String name, int points, String avatarUri) {
        this.name = name;
        this.points = points;
        this.avatarUri = avatarUri;
    }
}
