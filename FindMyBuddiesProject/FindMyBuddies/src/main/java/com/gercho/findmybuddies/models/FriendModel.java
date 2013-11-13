package com.gercho.findmybuddies.models;

/**
 * Created by Gercho on 11/13/13.
 */
public class FriendModel {

    private int id;
    private String nickname;
    private boolean isOnline;
    private String latitude;
    private String longitude;

    public FriendModel(int id, String nickname, boolean isOnline, String latitude, String longitude) {
        this.id = id;
        this.nickname = nickname;
        this.isOnline = isOnline;
        this.latitude = latitude;
        this.longitude = longitude;
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

    public String getLatitude() {
        return latitude;
    }

    public void setLatitude(String latitude) {
        this.latitude = latitude;
    }

    public String getLongitude() {
        return longitude;
    }

    public void setLongitude(String longitude) {
        this.longitude = longitude;
    }
}
