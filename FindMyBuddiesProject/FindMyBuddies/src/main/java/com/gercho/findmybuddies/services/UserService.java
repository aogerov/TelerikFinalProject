package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.os.IBinder;

/**
 * Created by Gercho on 11/7/13.
 */
public class UserService extends Service {

    public static final String START_SERVICE = "com.gercho.action.START_USER_SERVICE";
    public static final String STOP_SERVICE = "com.gercho.action.STOP_USER_SERVICE";
    public static final String LOGIN_SERVICE = "com.gercho.action.LOGIN_USER_SERVICE";
    public static final String REGISTER_SERVICE = "com.gercho.action.REGISTER_USER_SERVICE";
    public static final String LOGOUT_SERVICE = "com.gercho.action.LOGOUT_USER_SERVICE";

    private static final String USER_STORAGE = "UserStorage";
    private static final String USER_SESSION_KEY = "UserSessionKey";
    private static final int USER_SESSION_KEY_LENGTH = 50;

    private HandlerThread mHandlerThread;
    private String mSessionKey;

    public String getSessionKey() {
        return this.mSessionKey;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.mHandlerThread = new HandlerThread("UserService");
        this.mHandlerThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (START_SERVICE.equalsIgnoreCase(action)) {
            this.initSessionKey();
        } else if (STOP_SERVICE.equalsIgnoreCase(action)) {
            this.stopSelf();
        } else if (LOGIN_SERVICE.equalsIgnoreCase(action)) {
            this.login(intent);
        } else if (REGISTER_SERVICE.equalsIgnoreCase(action)) {
            this.register(intent);
        } else if (LOGOUT_SERVICE.equalsIgnoreCase(action)) {
            this.logout();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        this.mHandlerThread.quit();
        this.mHandlerThread = null;
    }

    public boolean isUserLoggedIn() {
        if (this.mSessionKey != null) {
            if (this.mSessionKey.length() == USER_SESSION_KEY_LENGTH) {
                return true;
            }
        }

        return false;
    }

    private void initSessionKey() {
        if (this.mSessionKey == null) {
            SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, 0);
            this.mSessionKey = userStorage.getString(USER_SESSION_KEY, null);
        }
    }

    private void login(Intent intent) {

    }

    private void register(Intent intent) {

    }

    private void logout() {

    }
}
