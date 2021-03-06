package com.gercho.findmybuddies.models;

/**
 * Created by Gercho on 11/15/13.
 */
public class BuddyFoundModel {

    private int id;
    private String nickname;
    private boolean isOnline;

    public BuddyFoundModel(int id, String nickname, boolean isOnline) {
        this.id = id;
        this.nickname = nickname;
        this.isOnline = isOnline;
    }

    public BuddyFoundModel(int id, String nickname) {
        this.id = id;
        this.nickname = nickname;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getNickname() {
        return nickname;
    }

    public void setNickname(String nickname) {
        this.nickname = nickname;
    }

    public boolean isOnline() {
        return isOnline;
    }

    public void setOnline(boolean isOnline) {
        this.isOnline = isOnline;
    }
}
