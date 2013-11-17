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

    public void sendBuddiesInfoUpdate(String buddyModelsAsJson, MeasureUnits measureUnits, int newBuddyRequestsCount) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.BUDDIES_INFO_UPDATE_EXTRA, buddyModelsAsJson);
        intent.putExtra(BuddiesService.BUDDIES_MEASURE_UNITS_EXTRA, measureUnits.ordinal());
        intent.putExtra(BuddiesService.NEW_BUDDY_REQUESTS_EXTRA, newBuddyRequestsCount);
        this.mService.sendBroadcast(intent);
    }

    public void sendBuddySearchResult(String buddySearchResultAsJson, boolean isStatusOk) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.BUDDY_SEARCH_RESULT_EXTRA, buddySearchResultAsJson);
        intent.putExtra(BuddiesService.IS_HTTP_STATUS_OK_EXTRA, isStatusOk);
        this.mService.sendBroadcast(intent);
    }

    public void sendBuddyRemoveResult(int buddyId, String buddyNickname, boolean isStatusOk) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.BUDDY_REMOVED_RESULT_EXTRA, true);
        intent.putExtra(BuddiesService.BUDDY_ID_EXTRA, buddyId);
        intent.putExtra(BuddiesService.BUDDY_NICKNAME_EXTRA, buddyNickname);
        intent.putExtra(BuddiesService.IS_HTTP_STATUS_OK_EXTRA, isStatusOk);
        this.mService.sendBroadcast(intent);
    }

    public void sendAllRequests(String allRequestsAsJson, boolean isStatusOk) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.ALL_REQUESTS_EXTRA, allRequestsAsJson);
        intent.putExtra(BuddiesService.IS_HTTP_STATUS_OK_EXTRA, isStatusOk);
        this.mService.sendBroadcast(intent);
    }

    public void sendBuddyRequestSendResult(String responseMessage, boolean isStatusOk) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.REQUESTS_SEND_RESULT_EXTRA, responseMessage);
        intent.putExtra(BuddiesService.IS_HTTP_STATUS_OK_EXTRA, isStatusOk);
        this.mService.sendBroadcast(intent);
    }

    public void sendResponseToBuddyRequest(String responseMessage, boolean isStatusOk) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.RESPONSE_TO_REQUEST_EXTRA, responseMessage);
        intent.putExtra(BuddiesService.IS_HTTP_STATUS_OK_EXTRA, isStatusOk);
        this.mService.sendBroadcast(intent);
    }

    public void sendBuddyImages(String buddyImagesAsJson, boolean isStatusOk) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.BUDDY_IMAGES_EXTRA, buddyImagesAsJson);
        intent.putExtra(BuddiesService.IS_HTTP_STATUS_OK_EXTRA, isStatusOk);
        this.mService.sendBroadcast(intent);
    }

    public void sendInfoMessage(String infoMessage) {
        Intent intent = new Intent(BuddiesService.BUDDIES_SERVICE_BROADCAST);
        intent.putExtra(BuddiesService.INFO_MESSAGE_EXTRA, infoMessage);
        this.mService.sendBroadcast(intent);
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
}
