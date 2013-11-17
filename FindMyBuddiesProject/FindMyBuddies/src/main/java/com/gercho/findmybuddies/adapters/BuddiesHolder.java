package com.gercho.findmybuddies.adapters;

import android.graphics.Color;
import android.view.View;
import android.widget.TextView;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.enums.MeasureUnits;
import com.gercho.findmybuddies.models.BuddyModel;

/**
 * Created by Gercho on 11/17/13.
 */
public class BuddiesHolder {

    private TextView mNickname;
    private TextView mIsOnline;
    private TextView mCoordinatesAccuracy;
    private TextView mLatitude;
    private TextView mLongitude;
    private TextView mDistance;

    public BuddiesHolder (View row) {
        this.mNickname = (TextView) row.findViewById(R.id.textView_buddyNickname);
        this.mIsOnline = (TextView) row.findViewById(R.id.textView_isOnline);
        this.mCoordinatesAccuracy = (TextView) row.findViewById(R.id.textView_coordinatesAccuracy);
        this.mLatitude = (TextView) row.findViewById(R.id.textView_latitude);
        this.mLongitude = (TextView) row.findViewById(R.id.textView_longitude);
        this.mDistance = (TextView) row.findViewById(R.id.textView_distance);
    }

    public void setValues(BuddyModel buddy, MeasureUnits measureUnits) {

        this.mNickname.setText(buddy.getNickname());
        this.mCoordinatesAccuracy.setText(buddy.getCoordinatesTimestampDifference());
        this.mLatitude.setText(String.valueOf(buddy.getLatitude()));
        this.mLongitude.setText(String.valueOf(buddy.getLongitude()));

        if (buddy.isOnline()) {
            this.mIsOnline.setText("online");
            this.mIsOnline.setTextColor(Color.GREEN);
        } else {
            this.mIsOnline.setText("offline");
            this.mIsOnline.setTextColor(Color.RED);
        }

        if (measureUnits == MeasureUnits.KILOMETERS) {
            this.mDistance.setText(buddy.getDistanceInKilometersAsString());
        } else {
            this.mDistance.setText(buddy.getDistanceInMilesAsString());
        }
    }
}