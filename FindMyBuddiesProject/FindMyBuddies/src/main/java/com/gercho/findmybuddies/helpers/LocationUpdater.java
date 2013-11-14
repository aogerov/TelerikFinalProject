package com.gercho.findmybuddies.helpers;

import android.app.Service;
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
public class LocationUpdater implements LocationListener {

    private static final int TIME_REVERENCE = 1000 * 60 * 2; // 2 minute
    private static final int UPDATES_MIN_TIME = 1000 * 30; // 30 seconds
    private static final int UPDATES_MIN_DISTANCE = 10; // 10 meters

    Service mService;
    LocationManager mLocationManager;

    public LocationUpdater(Service service) {
        this.mService = service;
        this.mLocationManager = (LocationManager) this.mService.getSystemService(Context.LOCATION_SERVICE);
        this.mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, UPDATES_MIN_TIME, UPDATES_MIN_DISTANCE, this);
    }

    public CoordinatesModel getLastKnownLocation() {
        Location location = mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        if (location != null) {
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();
            long timeDifferenceInMilliseconds = Calendar.getInstance().getTimeInMillis() - location.getTime();
            if (timeDifferenceInMilliseconds < TIME_REVERENCE) {
                return new CoordinatesModel(latitude, longitude, timeDifferenceInMilliseconds);
            }
        }

        return null;
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
