package com.gercho.findmybuddies.broadcasts;

import android.app.Service;
import android.content.Intent;

import com.gercho.findmybuddies.helpers.EnumOrderBy;
import com.gercho.findmybuddies.services.BuddiesService;

/**
 * Created by Gercho on 11/12/13.
 */
public class BuddiesServiceBroadcast {

    private Service mService;

    public BuddiesServiceBroadcast(Service service) {
        this.mService = service;
    }

    public void sendCurrentSettings(int updateFrequency, int imagesToShowCount, EnumOrderBy buddiesOrderBy) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.UPDATE_FREQUENCY_EXTRA, updateFrequency);
        intent.putExtra(BuddiesService.IMAGES_TO_SHOW_COUNT_EXTRA, imagesToShowCount);
        intent.putExtra(BuddiesService.BUDDIES_ORDER_BY_EXTRA, buddiesOrderBy);
        this.mService.sendBroadcast(intent);
    }

    public void sendBuddiesInfoUpdate(String friendModels) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.BUDDIES_INFO_UPDATE_EXTRA, friendModels);
        this.mService.sendBroadcast(intent);
    }
}
