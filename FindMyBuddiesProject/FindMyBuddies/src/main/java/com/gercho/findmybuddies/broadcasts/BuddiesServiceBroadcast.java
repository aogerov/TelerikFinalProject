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

    public void sendBuddiesInfoUpdate(String buddieModelsAsJson, MeasureUnits measureUnits, int newBuddieRequestsCount) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.BUDDIES_INFO_UPDATE_EXTRA, buddieModelsAsJson);
        intent.putExtra(BuddiesService.BUDDIES_MEASURE_UNITS_EXTRA, measureUnits.ordinal());
        intent.putExtra(BuddiesService.NEW_BUDDIE_REQUESTS_EXTRA, newBuddieRequestsCount);
        this.mService.sendBroadcast(intent);
    }

    public void sendBroadcastWithBuddieSearchResult(String buddieSearchResult) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.BUDDIE_SEARCH_RESULT_EXTRA, buddieSearchResult);
        this.mService.sendBroadcast(intent);
    }

    public void sendBroadcastWithBuddieRemoveResult(boolean isStatusOk, int buddieId, String buddieNickname) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.BUDDIE_REMOVED_RESULT_EXTRA, true);
        intent.putExtra(BuddiesService.BUDDIE_REMOVE_IS_STATUS_OK_EXTRA, isStatusOk);
        intent.putExtra(BuddiesService.BUDDIE_ID_EXTRA, buddieId);
        intent.putExtra(BuddiesService.BUDDIE_NICKNAME_EXTRA, buddieNickname);
        this.mService.sendBroadcast(intent);
    }
}
