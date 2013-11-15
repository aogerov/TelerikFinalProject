package com.gercho.findmybuddies.broadcasts;

import android.app.Service;
import android.content.Intent;

import com.gercho.findmybuddies.enums.MeasureUnits;
import com.gercho.findmybuddies.enums.OrderByTypes;
import com.gercho.findmybuddies.services.BuddiesService;

/**
 * Created by Gercho on 11/12/13.
 */
public class BuddiesServiceBroadcast {

    private Service mService;

    public BuddiesServiceBroadcast(Service service) {
        this.mService = service;
    }

    public void sendCurrentSettings(int updateFrequency, int imagesToShowCount,
                                    OrderByTypes buddiesOrderBy, MeasureUnits measureUnits) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.UPDATE_FREQUENCY_EXTRA, updateFrequency);
        intent.putExtra(BuddiesService.IMAGES_TO_SHOW_COUNT_EXTRA, imagesToShowCount);
        intent.putExtra(BuddiesService.BUDDIES_ORDER_BY_TYPES_EXTRA, buddiesOrderBy.ordinal());
        intent.putExtra(BuddiesService.BUDDIES_MEASURE_UNITS_EXTRA, measureUnits.ordinal());
        this.mService.sendBroadcast(intent);
    }

    public void sendBuddiesInfoUpdate(String buddieModelsAsJson, MeasureUnits measureUnits) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.BUDDIES_INFO_UPDATE_EXTRA, buddieModelsAsJson);
        intent.putExtra(BuddiesService.BUDDIES_MEASURE_UNITS_EXTRA, measureUnits.ordinal());
        this.mService.sendBroadcast(intent);
    }
}
