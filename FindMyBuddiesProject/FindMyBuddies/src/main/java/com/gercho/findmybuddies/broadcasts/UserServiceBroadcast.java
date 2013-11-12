package com.gercho.findmybuddies.broadcasts;

import android.app.Service;
import android.content.Intent;

import com.gercho.findmybuddies.services.UserService;

/**
 * Created by Gercho on 11/10/13.
 */
public class UserServiceBroadcast {

    private Service mService;

    public UserServiceBroadcast(Service service) {
        this.mService = service;
    }

    public void sendConnecting() {
        Intent intent = new Intent(UserService.USER_SERVICE_BROADCAST);
        intent.putExtra(UserService.CONNECTING_EXTRA, true);
        this.mService.sendBroadcast(intent);
    }

    public void sendIsConnected(String nickname) {
        Intent intent = new Intent(UserService.USER_SERVICE_BROADCAST);
        intent.putExtra(UserService.IS_CONNECTED_EXTRA, true);
        intent.putExtra(UserService.NICKNAME_EXTRA, nickname);
        this.mService.sendBroadcast(intent);
    }

    public void sendResponseMessage(String message) {
        Intent intent = new Intent(UserService.USER_SERVICE_BROADCAST);
        intent.putExtra(UserService.RESPONSE_MESSAGE_EXTRA, true);
        intent.putExtra(UserService.MESSAGE_TEXT_EXTRA, message);
        this.mService.sendBroadcast(intent);
    }
}
