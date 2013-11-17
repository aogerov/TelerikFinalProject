package com.gercho.findmybuddies.devices;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.gercho.findmybuddies.models.CoordinatesModel;

import java.util.Calendar;

/**
 * Created by Gercho on 11/14/13.
 */
public class LocationInfo implements LocationListener {

    private static final long TIME_REVERENCE_IN_MILLISECONDS = 1000 * 60 * 60 * 24 * 20; // 20 days
    private static final int UPDATES_MIN_TIME = 1000 * 20; // 20 seconds
    private static final int UPDATES_MIN_DISTANCE = 5; // 5 meters

    Context mContext;
    LocationManager mLocationManager;

    public LocationInfo(Context context) {
        this.mContext = context;
        this.mLocationManager = (LocationManager) this.mContext.getSystemService(Context.LOCATION_SERVICE);
        this.mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, UPDATES_MIN_TIME, UPDATES_MIN_DISTANCE, this);
    }

    public CoordinatesModel getLastKnownLocation() {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            long timeDifferenceInMilliseconds = Calendar.getInstance().getTimeInMillis() - location.getTime();
            if (timeDifferenceInMilliseconds < TIME_REVERENCE_IN_MILLISECONDS) {
                return new CoordinatesModel(latitude, longitude, timeDifferenceInMilliseconds);
            }
        }

        return null;
    }

    public boolean isProviderEnabled() {
        return this.mLocationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
    }

    @Override
    public void onLocationChanged(Location location) {

    }

    @Override
    public void onStatusChanged(String s, int i, Bundle bundle) {

    }

    @Override
    public void onProviderEnabled(String s) {

    }

    @Override
    public void onProviderDisabled(String s) {

    }
}
