package com.gercho.findmybuddies.models;

import java.util.Date;

/**
 * Created by Gercho on 11/13/13.
 */
public class FriendModel {

    private int id;
    private String nickname;
    private boolean isOnline;
    private String latitude;
    private String longitude;
    private Date coordinatesTimestamp;
    private String coordinatesTimestampDifference;
    private double distance;

    public FriendModel(int id, String nickname, boolean isOnline, String latitude, String longitude, Date coordinatesTimestamp, String coordinatesTimestampDifference, int distance) {
        this.id = id;
        this.nickname = nickname;
        this.isOnline = isOnline;
        this.latitude = latitude;
        this.longitude = longitude;
        this.coordinatesTimestamp = coordinatesTimestamp;
        this.coordinatesTimestampDifference = coordinatesTimestampDifference;
        this.distance = distance;
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

    public Date getCoordinatesTimestamp() {
        return coordinatesTimestamp;
    }

    public void setCoordinatesTimestamp(Date coordinatesTimestamp) {
        this.coordinatesTimestamp = coordinatesTimestamp;
    }

    public String getCoordinatesTimestampDifference() {
        return coordinatesTimestampDifference;
    }

    public void setCoordinatesTimestampDifference(String coordinatesTimestampDifference) {
        this.coordinatesTimestampDifference = coordinatesTimestampDifference;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(int distance) {
        this.distance = distance;
    }
}
