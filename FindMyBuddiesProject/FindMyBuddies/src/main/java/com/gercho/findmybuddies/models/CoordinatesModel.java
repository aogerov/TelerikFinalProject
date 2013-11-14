package com.gercho.findmybuddies.models;

/**
 * Created by Gercho on 11/14/13.
 */
public class CoordinatesModel {

    private double latitude;
    private double longitude;
    private long timeDifferenceInMilliseconds;

    public CoordinatesModel(double latitude, double longitude, long timeDifferenceInMilliseconds) {
        this.latitude = latitude;
        this.longitude = longitude;
        this.timeDifferenceInMilliseconds = timeDifferenceInMilliseconds;
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

    public long getTimeDifferenceInMilliseconds() {
        return timeDifferenceInMilliseconds;
    }

    public void setTimeDifferenceInMilliseconds(long timeDifferenceInMilliseconds) {
        this.timeDifferenceInMilliseconds = timeDifferenceInMilliseconds;
    }
}
