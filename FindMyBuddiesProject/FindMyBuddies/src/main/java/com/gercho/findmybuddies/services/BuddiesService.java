package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.gercho.findmybuddies.broadcasts.BuddiesServiceBroadcast;
import com.gercho.findmybuddies.helpers.EnumMeasureUnits;
import com.gercho.findmybuddies.helpers.EnumOrderBy;
import com.gercho.findmybuddies.helpers.ThreadSleeper;
import com.gercho.findmybuddies.http.HttpRequester;
import com.gercho.findmybuddies.http.HttpResponse;
import com.gercho.findmybuddies.models.FriendModels;
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
    public static final String SET_MEASURE_UNITS = "com.gercho.action.SET_MEASURE_UNITS";
    public static final String GET_BUDDIE_IMAGES = "com.gercho.action.GET_BUDDIE_IMAGES";

    public static final String BUDDIES_SERVICE_BROADCAST = "BuddiesServiceBroadcast";
    public static final String UPDATE_FREQUENCY_EXTRA = "UpdateFrequencyExtra";
    public static final String IMAGES_TO_SHOW_COUNT_EXTRA = "ImagesToShowCountExtra";
    public static final String BUDDIES_ORDER_BY_EXTRA = "BuddiesOrderByExtra";
    public static final String BUDDIES_MEASURE_UNITS_EXTRA = "BuddiesMeasureUnitsExtra";
    public static final String BUDDIES_INFO_UPDATE_EXTRA = "BuddiesInfoUpdateExtra";

    private static final int UPDATE_FREQUENCY_DEFAULT = 1000 * 60; // 1 minute
    private static final int IMAGES_TO_SHOW_COUNT_DEFAULT = 3;
    private static final EnumOrderBy BUDDIES_ORDER_BY_DEFAULT = EnumOrderBy.DISTANCE;
    private static final EnumMeasureUnits MEASURE_UNITS_DEFAULT = EnumMeasureUnits.KILOMETERS;

    private static final String BUDDIES_STORAGE = "BuddiesStorage";
    private static final String BUDDIES_STORAGE_UPDATE_FREQUENCY = "BuddiesStorageUpdateFrequency";
    private static final String BUDDIES_STORAGE_IMAGES_TO_SHOW_COUNT = "BuddiesStorageImagesToShowCount";
    private static final String BUDDIES_STORAGE_BUDDIES_ORDER_BY_AS_INT = "BuddiesStorageBuddiesOrderByAsInt";
    private static final String BUDDIES_STORAGE_MEASURE_UNITS_AS_INT = "BuddiesStorageMeasureUnitsAsInt";

    private boolean mIsServiceAlreadyStarted;
    private boolean mIsUpdatingActive;
    private boolean mIsNetworkAvailable;
    private boolean mIsGpsAvailable;
    private BuddiesServiceBroadcast mBroadcast;
    private HandlerThread mHandledThread;
    private Handler mHandler;
    private HttpRequester mHttpRequester;
    private Gson mGson;
    private String mSessionKey;
    private int mUpdateFrequency;
    private int mImagesToShowCount;
    private EnumOrderBy mBuddiesOrderBy;
    private EnumMeasureUnits mMeasureUnits;

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
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (START_BUDDIES_SERVICE.equalsIgnoreCase(action)) {
            this.startBuddiesService(intent);
        } else if (STOP_BUDDIES_SERVICE.equalsIgnoreCase(action)) {
            this.stopBuddiesService();
        } else if (GET_CURRENT_SETTINGS.equalsIgnoreCase(action)) {
            this.getCurrentSettings();
        } else if (SET_UPDATE_FREQUENCY.equalsIgnoreCase(action)) {
            this.setUpdateFrequency(intent);
        } else if (SET_IMAGES_TO_SHOW_COUNT.equalsIgnoreCase(action)) {
            this.setImagesToShowCount(intent);
        } else if (SET_BUDDIES_ORDER_BY.equalsIgnoreCase(action)) {
            this.setBuddiesOrderBy(intent);
        } else if (SET_MEASURE_UNITS.equalsIgnoreCase(action)) {
            this.setMeasureUnits(intent);
        } else if (GET_BUDDIE_IMAGES.equalsIgnoreCase(action)) {
            this.getBuddieImages();
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

    private void startBuddiesService(Intent intent) {
        String sessionKey = intent.getStringExtra(UserService.SESSION_KEY_EXTRA);
        boolean isSessionKeyValid = BuddiesServiceValidator.validateSessionKey(sessionKey);
        if (isSessionKeyValid) {
            this.mSessionKey = sessionKey;
        }

        // TODO fix the shit below, commented jist for testing, uncomment on release
//        if (!this.mIsServiceAlreadyStarted) {
//            this.mIsServiceAlreadyStarted = true;
            this.mIsUpdatingActive = false;
            this.mIsNetworkAvailable = true;
            this.mIsGpsAvailable = true;
            this.readBuddiesStorage();
            this.runServiceUpdating();
//        }
    }

    private void stopBuddiesService() {
        this.mIsUpdatingActive = false;
        this.stopSelf();
    }

    private void getCurrentSettings() {
        this.mBroadcast.sendCurrentSettings(this.mUpdateFrequency, this.mImagesToShowCount, this.mBuddiesOrderBy);
    }

    private void setUpdateFrequency(Intent intent) {
        int updateFrequency = intent.getIntExtra(UPDATE_FREQUENCY_EXTRA, Integer.MIN_VALUE);
        boolean isUpdateFrequencyValid = BuddiesServiceValidator.validateUpdateFrequency(updateFrequency);
        if (isUpdateFrequencyValid) {
            this.mUpdateFrequency = updateFrequency;
        }
    }

    private void setImagesToShowCount(Intent intent) {
        int imagesToShowCount = intent.getIntExtra(IMAGES_TO_SHOW_COUNT_EXTRA, Integer.MIN_VALUE);
        boolean isImagesToShowCountValid = BuddiesServiceValidator.validateImagesToShowCount(imagesToShowCount);
        if (isImagesToShowCountValid) {
            this.mImagesToShowCount = imagesToShowCount;
        }
    }

    private void setBuddiesOrderBy(Intent intent) {
        int buddiesOrderByAsInt = intent.getIntExtra(BUDDIES_ORDER_BY_EXTRA, Integer.MIN_VALUE);
        boolean isBuddiesOrderByValid = BuddiesServiceValidator.validateBuddiesOrderByAsInt(buddiesOrderByAsInt);
        if (isBuddiesOrderByValid) {
            this.mBuddiesOrderBy = EnumOrderBy.values()[buddiesOrderByAsInt];
        }
    }

    private void setMeasureUnits(Intent intent) {
        int measureUnitsAsInt = intent.getIntExtra(BUDDIES_MEASURE_UNITS_EXTRA, Integer.MIN_VALUE);
        boolean areMeasureUnitsValid = BuddiesServiceValidator.validateDistanceAsInt(measureUnitsAsInt);
        if (areMeasureUnitsValid) {
            this.mMeasureUnits = EnumMeasureUnits.values()[measureUnitsAsInt];
        }
    }

    private void getBuddieImages() {

    }

    private void runServiceUpdating() {
        this.mIsUpdatingActive = true;
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                while (BuddiesService.this.mIsUpdatingActive) {
                    if (BuddiesService.this.mIsNetworkAvailable && BuddiesService.this.mIsGpsAvailable) {
                        BuddiesService.this.updateCurrentPosition();
                    }

                    if (BuddiesService.this.mIsNetworkAvailable) {
                        BuddiesService.this.updateBuddiesInfo();
                    }

                    ThreadSleeper.sleep(BuddiesService.this.mUpdateFrequency);
                }
            }
        });
    }

    private void updateCurrentPosition() {

    }

    private void updateBuddiesInfo() {
        // TODO Buddies might not have any images and return empty list or null
        // TODO buddies list is with 2 lists online and offline
        HttpResponse response = this.mHttpRequester.get(String.format(
                "friends/all?orderBy=%s&sessionKey=%s",
                this.mBuddiesOrderBy.toString().toLowerCase(), this.mSessionKey));

        String TEST = this.mBuddiesOrderBy.toString();
        if (response.isStatusOk()) {
            FriendModels friendModels = this.mGson.fromJson(response.getMessage(), FriendModels.class);
            boolean areModelsValid = BuddiesServiceValidator.validateFriendModels(friendModels);
            if (!areModelsValid) {
                return;
            }

            this.mBroadcast.sendBuddiesInfoUpdate(response.getMessage());
        }
    }

    private void readBuddiesStorage() {
        SharedPreferences buddiesStorage = this.getSharedPreferences(BUDDIES_STORAGE, MODE_PRIVATE);

        int updateFrequency = buddiesStorage.getInt(
                BUDDIES_STORAGE_UPDATE_FREQUENCY, Integer.MIN_VALUE);
        boolean isUpdateFrequencyValid = BuddiesServiceValidator.validateUpdateFrequency(updateFrequency);
        if (isUpdateFrequencyValid) {
            this.mUpdateFrequency = updateFrequency;
        } else {
            this.mUpdateFrequency = UPDATE_FREQUENCY_DEFAULT;
        }

        int imagesToShowCount = buddiesStorage.getInt(
                BUDDIES_STORAGE_IMAGES_TO_SHOW_COUNT, Integer.MIN_VALUE);
        boolean isImagesToShowCountValid = BuddiesServiceValidator.validateImagesToShowCount(imagesToShowCount);
        if (isImagesToShowCountValid) {
            this.mImagesToShowCount = imagesToShowCount;
        } else {
            this.mImagesToShowCount = IMAGES_TO_SHOW_COUNT_DEFAULT;
        }

        int buddiesOrderByAsInt = buddiesStorage.getInt(
                BUDDIES_STORAGE_BUDDIES_ORDER_BY_AS_INT, Integer.MIN_VALUE);
        boolean isBuddiesOrderByValid = BuddiesServiceValidator.validateBuddiesOrderByAsInt(buddiesOrderByAsInt);
        if (isBuddiesOrderByValid) {
            this.mBuddiesOrderBy = EnumOrderBy.values()[buddiesOrderByAsInt];
        } else {
            this.mBuddiesOrderBy = BUDDIES_ORDER_BY_DEFAULT;
        }

        int measureUnitsAsInt = buddiesStorage.getInt(
                BUDDIES_STORAGE_MEASURE_UNITS_AS_INT, Integer.MIN_VALUE);
        boolean areMeasureUnitsValid = BuddiesServiceValidator.validateDistanceAsInt(measureUnitsAsInt);
        if (areMeasureUnitsValid) {
            this.mMeasureUnits = EnumMeasureUnits.values()[measureUnitsAsInt];
        } else {
            this.mMeasureUnits = MEASURE_UNITS_DEFAULT;
        }
    }

    private void updateBuddiesStorage(int updateFrequency, int imagesToShowCount, EnumOrderBy buddiesOrderBy, EnumOrderBy measureUnits) {
        SharedPreferences userStorage = this.getSharedPreferences(BUDDIES_STORAGE, MODE_PRIVATE);
        SharedPreferences.Editor editor = userStorage.edit();
        editor.putInt(BUDDIES_STORAGE_UPDATE_FREQUENCY, updateFrequency);
        editor.putInt(BUDDIES_STORAGE_IMAGES_TO_SHOW_COUNT, imagesToShowCount);
        editor.putInt(BUDDIES_STORAGE_BUDDIES_ORDER_BY_AS_INT, buddiesOrderBy.ordinal());
        editor.putInt(BUDDIES_STORAGE_MEASURE_UNITS_AS_INT, measureUnits.ordinal());
        editor.commit();
    }
}