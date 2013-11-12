package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.gercho.findmybuddies.broadcasts.BuddiesServiceBroadcast;
import com.gercho.findmybuddies.helpers.OrderBy;
import com.gercho.findmybuddies.http.HttpRequester;
import com.gercho.findmybuddies.validators.BuddiesServiceValidator;
import com.google.gson.Gson;

/**
 * Created by Gercho on 11/12/13.
 */
public class BuddiesService extends Service {

    public static final String START_BUDDIES_SERVICE = "com.gercho.action.START_BUDDIES_SERVICE";
    public static final String STOP_BUDDIES_SERVICE = "com.gercho.action.STOP_BUDDIES_SERVICE";
    public static final String GET_CURRENT_SETTINGS = "com.gercho.action.GET_CURRENT_SETTINGS";
    public static final String SET_UPDATE_FREQUENCY = "com.gercho.action.SET_UPDATE_FREQUENCY";
    public static final String SET_IMAGES_TO_SHOW_COUNT = "com.gercho.action.SET_IMAGES_TO_SHOW_COUNT";
    public static final String SET_BUDDIES_ORDER_BY = "com.gercho.action.SET_BUDDIES_ORDER_BY";

    public static final String BUDDIES_SERVICE_BROADCAST = "BuddiesServiceBroadcast";
    public static final String UPDATE_FREQUENCY_EXTRA = "UpdateFrequencyExtra";
    public static final String IMAGES_TO_SHOW_COUNT_EXTRA = "ImagesToShowCountExtra";
    public static final String BUDDIES_ORDER_BY_EXTRA = "BuddiesOrderByExtra";

    public static final int UPDATE_FREQUENCY_DEFAULT = 1000 * 60; // 1 minute
    public static final int IMAGES_TO_SHOW_COUNT_DEFAULT = 3;
    public static final OrderBy BUDDIES_ORDER_BY_DEFAULT = OrderBy.DISTANCE;

    private static final String BUDDIES_STORAGE = "BuddiesStorage";
    private static final String BUDDIES_STORAGE_UPDATE_FREQUENCY = "BuddiesStorageUpdateFrequency";
    private static final String BUDDIES_STORAGE_IMAGES_TO_SHOW_COUNT = "BuddiesStorageImagesToShowCount";
    private static final String BUDDIES_STORAGE_BUDDIES_ORDER_BY_AS_INT = "BuddiesStorageBuddiesOrderByAsInt";

    private boolean mIsServiceAlreadyStarted;
    private HandlerThread mHandledThread;
    private Handler mHandler;
    private HttpRequester mHttpRequester;
    private Gson mGson;
    private BuddiesServiceBroadcast mBroadcast;
    private BuddiesServiceValidator mValidator;
    private String mSessionKey;
    private int mUpdateFrequency;
    private int mImagesToShowCount;
    private OrderBy mBuddiesOrderBy;

    @Override
    public void onCreate() {
        this.mHandledThread = new HandlerThread("UserServiceThread");
        this.mHandledThread.start();
        Looper looper = this.mHandledThread.getLooper();
        if (looper != null) {
            this.mHandler = new Handler(looper);
        }

        this.mHttpRequester = new HttpRequester();
        this.mGson = new Gson();
        this.mBroadcast = new BuddiesServiceBroadcast(this);
        this.mValidator = new BuddiesServiceValidator(this.mBroadcast);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (START_BUDDIES_SERVICE.equalsIgnoreCase(action)) {
            this.startService(intent);
        } else if (STOP_BUDDIES_SERVICE.equalsIgnoreCase(action)) {
            this.stopSelf();
        } else if (GET_CURRENT_SETTINGS.equalsIgnoreCase(action)) {
            this.getCurrentSettings();
        } else if (SET_UPDATE_FREQUENCY.equalsIgnoreCase(action)) {
            this.setUpdateFrequency(intent);
        } else if (SET_IMAGES_TO_SHOW_COUNT.equalsIgnoreCase(action)) {
            this.setImagesToShowCount(intent);
        } else if (SET_BUDDIES_ORDER_BY.equalsIgnoreCase(action)) {
            this.setBuddiesOrderBy(intent);
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        this.mHandledThread.quit();
        this.mHandledThread = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startService(Intent intent) {
        String sessionKey = intent.getStringExtra(UserService.SESSION_KEY_EXTRA);
        boolean isSessionKeyValid = this.mValidator.validateSesionKey(sessionKey);
        if (isSessionKeyValid) {
            this.mSessionKey = sessionKey;
        }

        if (!this.mIsServiceAlreadyStarted) {
            this.mIsServiceAlreadyStarted = true;
            SharedPreferences buddiesStorage = this.getSharedPreferences(BUDDIES_STORAGE, 0);

            int updateFrequency = buddiesStorage.getInt(
                    BUDDIES_STORAGE_UPDATE_FREQUENCY, Integer.MIN_VALUE);
            boolean isUpdateFrequencyValid = this.mValidator.validateUpdateFrequency(updateFrequency);
            if (isUpdateFrequencyValid) {
                this.mUpdateFrequency = updateFrequency;
            } else {
                this.mUpdateFrequency = UPDATE_FREQUENCY_DEFAULT;
            }

            int imagesToShowCount = buddiesStorage.getInt(
                    BUDDIES_STORAGE_IMAGES_TO_SHOW_COUNT, Integer.MIN_VALUE);
            boolean isImagesToShowCountValid = this.mValidator.validateImagesToShowCount(imagesToShowCount);
            if (isImagesToShowCountValid) {
                this.mImagesToShowCount = imagesToShowCount;
            } else {
                this.mImagesToShowCount = IMAGES_TO_SHOW_COUNT_DEFAULT;
            }

            int buddiesOrderByAsInt = buddiesStorage.getInt(
                    BUDDIES_STORAGE_BUDDIES_ORDER_BY_AS_INT, Integer.MIN_VALUE);
            boolean isBuddiesOrderByValid = this.mValidator.validateBuddiesOrderByAsInt(buddiesOrderByAsInt);
            if (isBuddiesOrderByValid) {
                this.mBuddiesOrderBy = OrderBy.values()[buddiesOrderByAsInt];
            } else {
                this.mBuddiesOrderBy = BUDDIES_ORDER_BY_DEFAULT;
            }
        }
    }

    private void getCurrentSettings() {

    }

    private void setUpdateFrequency(Intent intent) {

    }

    private void setImagesToShowCount(Intent intent) {

    }

    private void setBuddiesOrderBy(Intent intent) {

    }
}
