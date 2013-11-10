package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;

/**
 * Created by Gercho on 11/10/13.
 */
public class UserServiceBroadcastManager {

    private Service mService;

    public UserServiceBroadcastManager(Service service) {
        this.mService = service;
    }

    public void sendConnecting() {
        Intent intent = new Intent(UserService.USER_SERVICE_BROADCAST);
        intent.putExtra(UserService.USER_SERVICE_CONNECTING, true);
        this.mService.sendBroadcast(intent);
    }

    public void sendIsConnected() {
        Intent intent = new Intent(UserService.USER_SERVICE_BROADCAST);
        intent.putExtra(UserService.USER_SERVICE_IS_CONNECTED, true);
        this.mService.sendBroadcast(intent);
    }

    public void sendErrorMessage(String message) {
        Intent intent = new Intent(UserService.USER_SERVICE_BROADCAST);
        intent.putExtra(UserService.USER_SERVICE_ERROR, true);
        intent.putExtra(UserService.USER_SERVICE_MESSAGE, message);
        this.mService.sendBroadcast(intent);
    }
}
