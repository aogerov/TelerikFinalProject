package com.gercho.findmybuddies.services;

import android.os.Binder;

/**
 * Created by Gercho on 11/8/13.
 */
public class UserServiceBinder extends Binder {

    private UserService mService;

    public UserServiceBinder(UserService service) {
        this.mService = service;
    }

    public UserService getService() {
        return mService;
    }
}