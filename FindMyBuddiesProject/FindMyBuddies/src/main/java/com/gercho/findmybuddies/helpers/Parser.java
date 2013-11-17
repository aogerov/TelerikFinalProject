package com.gercho.findmybuddies.helpers;

import android.content.Context;
import android.text.format.DateFormat;

import com.gercho.findmybuddies.devices.LocationInfo;
import com.gercho.findmybuddies.models.CoordinatesModel;
import com.gercho.findmybuddies.models.ImageModel;
import com.gercho.findmybuddies.models.UploadsImDataModel;
import com.gercho.findmybuddies.models.UploadsImResponseModel;

import java.util.Date;

/**
 * Created by Gercho on 11/17/13.
 */
public class Parser {

    public static final long MILLISECONDS_PER_ONE_DAY = 1000 * 60 * 60 * 24;
    public static final long MILLISECONDS_PER_FIVE_MINUTES = 1000 * 60 * 5;
    public static final long MILLISECONDS_PER_THREE_MINUTES = 1000 * 60 * 3;
    public static final long MILLISECONDS_PER_ONE_MINUTE = 1000 * 60 * 1;
    public static final String ACCURACY_POOR = "Accuracy of coordinates: poor";
    public static final String ACCURACY_MEDIUM = "Accuracy of coordinates: medium";
    public static final String ACCURACY_GOOD = "Accuracy of coordinates: good";
    public static final String ACCURACY_EXCELLENT = "Accuracy of coordinates: excellent";

    public static ImageModel parseImResponseModelToImageModel(
            Context context, UploadsImResponseModel uploadsImResponseModel) {

        UploadsImDataModel data = uploadsImResponseModel.getData();
        LocationInfo locationInfo = new LocationInfo(context);
        CoordinatesModel coordinates = locationInfo.getLastKnownLocation();

        String url = data.getImg_url();
        String thumbUrl = data.getThumb_url();
        String imageDateAsString = DateFormat.format("yyyy-MM-dd", new Date()).toString();
        String timestampDifferenceWithCoordinates = getTimeStampDifferenceWithCoordinates(coordinates);
        String coordinatesAccuracy = getCoordinatesAccuracy(coordinates);
        double latitude = coordinates.getLatitude();
        double longitude = coordinates.getLongitude();

        return new ImageModel(url, thumbUrl, imageDateAsString, timestampDifferenceWithCoordinates,
                coordinatesAccuracy, latitude, longitude);
    }

    private static String getTimeStampDifferenceWithCoordinates(CoordinatesModel coordinates) {
        long milliseconds = coordinates.getTimeDifferenceInMilliseconds();
        if (milliseconds < MILLISECONDS_PER_ONE_DAY) {
            return DateFormat.format("hh:mm:ss", new Date(milliseconds)).toString();
        } else {
            return "more than 24 hours";
        }
    }

    private static String getCoordinatesAccuracy(CoordinatesModel coordinates) {
        long milliseconds = coordinates.getTimeDifferenceInMilliseconds();
        if (milliseconds < MILLISECONDS_PER_ONE_MINUTE) {
            return ACCURACY_EXCELLENT;
        } else if (milliseconds < MILLISECONDS_PER_THREE_MINUTES) {
            return ACCURACY_GOOD;
        } else if (milliseconds < MILLISECONDS_PER_FIVE_MINUTES) {
            return ACCURACY_MEDIUM;
        } else {
            return ACCURACY_POOR;
        }
    }
}
