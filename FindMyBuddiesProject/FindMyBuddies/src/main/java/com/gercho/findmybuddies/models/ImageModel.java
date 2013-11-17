package com.gercho.findmybuddies.models;

/**
 * Created by Gercho on 11/16/13.
 */
public class ImageModel {

    private String url;
    private String thumbUrl;
    private String imageDateAsString;
    private String timestampDifferenceWithCoordinates;
    private String coordinatesAccuracy;
    private double latitude;
    private double longitude;

    public ImageModel(String url, String thumbUrl, String imageDateAsString,
                      String timestampDifferenceWithCoordinates, String coordinatesAccuracy,
                      double latitude, double longitude) {
        this.url = url;
        this.thumbUrl = thumbUrl;
        this.imageDateAsString = imageDateAsString;
        this.timestampDifferenceWithCoordinates = timestampDifferenceWithCoordinates;
        this.coordinatesAccuracy = coordinatesAccuracy;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getThumbUrl() {
        return thumbUrl;
    }

    public void setThumbUrl(String thumbUrl) {
        this.thumbUrl = thumbUrl;
    }

    public String getImageDateAsString() {
        return imageDateAsString;
    }

    public void setImageDateAsString(String imageDateAsString) {
        this.imageDateAsString = imageDateAsString;
    }

    public String getTimestampDifferenceWithCoordinates() {
        return timestampDifferenceWithCoordinates;
    }

    public void setTimestampDifferenceWithCoordinates(String timestampDifferenceWithCoordinates) {
        this.timestampDifferenceWithCoordinates = timestampDifferenceWithCoordinates;
    }

    public String getCoordinatesAccuracy() {
        return coordinatesAccuracy;
    }

    public void setCoordinatesAccuracy(String coordinatesAccuracy) {
        this.coordinatesAccuracy = coordinatesAccuracy;
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
}
