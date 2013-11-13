package com.gercho.findmybuddies.models;

import java.util.Date;

/**
 * Created by Gercho on 11/13/13.
 */
public class FriendModel {

    private int id;
    private String nickname;
    private boolean isOnline;
    private double latitude;
    private double longitude;
    private Date coordinatesTimestamp;
    private String coordinatesTimestampDifference;
    private int distanceInMeters;
    private String distanceInKilometersAsString;
    private String distanceInMilesAsString;

    public FriendModel(int id, String nickname, boolean isOnline, double latitude, double longitude,
                       Date coordinatesTimestamp, String coordinatesTimestampDifference, int distanceInMeters,
                       String distanceInKilometersAsString, String distanceInMilesAsString) {
        this.id = id;
        this.nickname = nickname;
        this.isOnline = isOnline;
        this.latitude = latitude;
        this.longitude = longitude;
        this.coordinatesTimestamp = coordinatesTimestamp;
        this.coordinatesTimestampDifference = coordinatesTimestampDifference;
        this.distanceInMeters = distanceInMeters;
        this.distanceInKilometersAsString = distanceInKilometersAsString;
        this.distanceInMilesAsString = distanceInMilesAsString;
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

    public double getLatitude() {
        return latitude;
    }

    public void setLatitude(double latitude) {
        this.latitude = latitude;
    }

    public double getLongitude() {
        return longitude;
    }

    public void setLongitude(double longitude) {
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

    public int getDistanceInMeters() {
        return distanceInMeters;
    }

    public void setDistanceInMeters(int distanceInMeters) {
        this.distanceInMeters = distanceInMeters;
    }

    public String getDistanceInKilometersAsString() {
        return distanceInKilometersAsString;
    }

    public void setDistanceInKilometersAsString(String distanceInKilometersAsString) {
        this.distanceInKilometersAsString = distanceInKilometersAsString;
    }

    public String getDistanceInMilesAsString() {
        return distanceInMilesAsString;
    }

    public void setDistanceInMilesAsString(String distanceInMilesAsString) {
        this.distanceInMilesAsString = distanceInMilesAsString;
    }
}
