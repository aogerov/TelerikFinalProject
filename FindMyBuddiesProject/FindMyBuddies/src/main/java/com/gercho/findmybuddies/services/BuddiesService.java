package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.gercho.findmybuddies.broadcasts.BuddiesServiceBroadcast;
import com.gercho.findmybuddies.enums.MeasureUnits;
import com.gercho.findmybuddies.enums.OrderBy;
import com.gercho.findmybuddies.helpers.LocationUpdater;
import com.gercho.findmybuddies.helpers.LogHelper;
import com.gercho.findmybuddies.helpers.NetworkConnectionInfo;
import com.gercho.findmybuddies.helpers.ThreadSleeper;
import com.gercho.findmybuddies.helpers.ToastNotifier;
import com.gercho.findmybuddies.http.HttpRequester;
import com.gercho.findmybuddies.http.HttpResponse;
import com.gercho.findmybuddies.models.CoordinatesModel;
import com.gercho.findmybuddies.validators.BuddiesServiceValidator;
import com.google.gson.Gson;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Gercho on 11/12/13.
 */
public class BuddiesService extends Service {

    public static final String START_BUDDIES_SERVICE = "com.gercho.action.START_BUDDIES_SERVICE";
    public static final String PAUSE_BUDDIES_SERVICE = "com.gercho.action.PAUSE_BUDDIES_SERVICE";
    public static final String RESUME_BUDDIES_SERVICE = "com.gercho.action.RESUME_BUDDIES_SERVICE";
    public static final String STOP_BUDDIES_SERVICE = "com.gercho.action.STOP_BUDDIES_SERVICE";
    public static final String FORCE_UPDATING_BUDDIES_SERVICE = "com.gercho.action.FORCE_UPDATING_BUDDIES_SERVICE";
    public static final String GET_CURRENT_SETTINGS = "com.gercho.action.GET_CURRENT_SETTINGS";
    public static final String SET_UPDATE_FREQUENCY = "com.gercho.action.SET_UPDATE_FREQUENCY";
    public static final String SET_IMAGES_TO_SHOW_COUNT = "com.gercho.action.SET_IMAGES_TO_SHOW_COUNT";
    public static final String SET_BUDDIES_ORDER_BY = "com.gercho.action.SET_BUDDIES_ORDER_BY";
    public static final String SET_MEASURE_UNITS = "com.gercho.action.SET_MEASURE_UNITS";
    public static final String GET_BUDDIE_IMAGES = "com.gercho.action.GET_BUDDIE_IMAGES";
    public static final String ANDROID_CONNECTIVITY_CHANGE = "android.net.conn.ANDROID_CONNECTIVITY_CHANGE";
    public static final String ANDROID_GPS_ENABLED_CHANGE = "android.location.ANDROID_GPS_ENABLED_CHANGE";

    public static final String BUDDIES_SERVICE_BROADCAST = "BuddiesServiceBroadcast";
    public static final String UPDATE_FREQUENCY_EXTRA = "UpdateFrequencyExtra";
    public static final String IMAGES_TO_SHOW_COUNT_EXTRA = "ImagesToShowCountExtra";
    public static final String BUDDIES_ORDER_BY_EXTRA = "BuddiesOrderByExtra";
    public static final String BUDDIES_MEASURE_UNITS_EXTRA = "BuddiesMeasureUnitsExtra";
    public static final String BUDDIES_INFO_UPDATE_EXTRA = "BuddiesInfoUpdateExtra";

    private static final int UPDATING_LOCK_TIME = 1000 * 50; // 50 seconds
    private static final int UPDATE_FREQUENCY_DEFAULT = 1000 * 60 * 5; // 5 minutes
    private static final int IMAGES_TO_SHOW_COUNT_DEFAULT = 3;
    private static final OrderBy BUDDIES_ORDER_BY_DEFAULT = OrderBy.DISTANCE;
    private static final MeasureUnits MEASURE_UNITS_DEFAULT = MeasureUnits.KILOMETERS;

    private static final String ERROR_MESSAGE_NO_NETWORK = "Can't access buddies, no network available";
    private static final String ERROR_MESSAGE_NO_GPS = "Please turn on your GPS";

    private static final String BUDDIES_STORAGE = "BuddiesStorage";
    private static final String BUDDIES_STORAGE_UPDATE_FREQUENCY = "BuddiesStorageUpdateFrequency";
    private static final String BUDDIES_STORAGE_IMAGES_TO_SHOW_COUNT = "BuddiesStorageImagesToShowCount";
    private static final String BUDDIES_STORAGE_BUDDIES_ORDER_BY_AS_INT = "BuddiesStorageBuddiesOrderByAsInt";
    private static final String BUDDIES_STORAGE_MEASURE_UNITS_AS_INT = "BuddiesStorageMeasureUnitsAsInt";

    private StatusChangeReceiver mStatusChangeReceiver;
    private boolean mIsServiceAlreadyStarted;
    private boolean mIsUpdatingActive;
    private boolean mIsUpdatingAvailable;
    private boolean mIsOnPauseMode;
    private boolean mIsNetworkAvailable;
    private boolean mIsGpsAvailable;
    private BuddiesServiceBroadcast mBroadcast;
    private HandlerThread mMainHandledThread;
    private Handler mMainHandler;
    private HandlerThread mOccasionalHandlerThread;
    private Handler mOccasionalHandler;
    private Timer mUpdateTimer;
    private LocationUpdater mLocationUpdater;
    private HttpRequester mHttpRequester;
    private Gson mGson;
    private String mSessionKey;
    private int mUpdateFrequency;
    private int mImagesToShowCount;
    private OrderBy mBuddiesOrderBy;
    private MeasureUnits mMeasureUnits;
    private String mCurrentBuddiesInfo;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (START_BUDDIES_SERVICE.equalsIgnoreCase(action)) {
            this.startBuddiesService(intent);
        } else if (RESUME_BUDDIES_SERVICE.equalsIgnoreCase(action)) {
            this.resumeBuddiesService();
        } else if (PAUSE_BUDDIES_SERVICE.equalsIgnoreCase(action)) {
            this.pauseBuddiesService();
        } else if (STOP_BUDDIES_SERVICE.equalsIgnoreCase(action)) {
            this.stopBuddiesService();
        } else if (FORCE_UPDATING_BUDDIES_SERVICE.equalsIgnoreCase(action)) {
            this.forceUpdatingBuddiesService();
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
            this.getBuddieImages(intent);
        }

        return START_REDELIVER_INTENT;
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

        if (!this.mIsServiceAlreadyStarted) {
            this.mIsServiceAlreadyStarted = true;
            this.mIsUpdatingAvailable = true;

            this.mStatusChangeReceiver = new StatusChangeReceiver();
            IntentFilter statusChangeIntentFilter = new IntentFilter();
            statusChangeIntentFilter.addAction(ANDROID_CONNECTIVITY_CHANGE);
            statusChangeIntentFilter.addAction(ANDROID_GPS_ENABLED_CHANGE);
            this.registerReceiver(this.mStatusChangeReceiver, statusChangeIntentFilter);

            this.mMainHandledThread = new HandlerThread("UserServiceMainThread");
            this.mMainHandledThread.start();
            Looper mainLooper = this.mMainHandledThread.getLooper();
            if (mainLooper != null) {
                this.mMainHandler = new Handler(mainLooper);
            }

            this.mOccasionalHandlerThread = new HandlerThread("UserServiceOccasionalThread");
            this.mOccasionalHandlerThread.start();
            Looper occasionalLooper = this.mOccasionalHandlerThread.getLooper();
            if (occasionalLooper != null) {
                this.mOccasionalHandler = new Handler(occasionalLooper);
            }

            this.mUpdateTimer = new Timer("UpdateTimer");
            this.mLocationUpdater = new LocationUpdater(this);
            this.mHttpRequester = new HttpRequester();
            this.mGson = new Gson();
            this.mBroadcast = new BuddiesServiceBroadcast(this);

            this.readBuddiesStorage();
        }
    }

    private void resumeBuddiesService() {
        this.mIsOnPauseMode = false;
        this.forceUpdatingBuddiesService();
    }

    private void pauseBuddiesService() {
        this.mIsOnPauseMode = true;
    }

    private void stopBuddiesService() {
        if (this.mIsServiceAlreadyStarted) {
            this.mIsServiceAlreadyStarted = false;
            this.mIsUpdatingActive = false;

            this.unregisterReceiver(this.mStatusChangeReceiver);
            this.mStatusChangeReceiver = null;

            this.mMainHandledThread.quit();
            this.mMainHandledThread = null;
            this.mOccasionalHandlerThread.quit();
            this.mOccasionalHandlerThread = null;
            this.mUpdateTimer = null;
        }

        this.stopSelf();
    }

    private void forceUpdatingBuddiesService() {
        if (!this.mIsUpdatingAvailable) {
            this.sendBroadcastWithBuddiesInfoUpdate();
        }

        this.validateStatusAvailabilities();
        if (this.mIsUpdatingActive) {
            this.runOccasionalServiceUpdating();
        } else {
            if (this.mIsNetworkAvailable) {
                this.mIsUpdatingActive = true;
                this.runServiceUpdating();
            }
        }
    }

    private void getCurrentSettings() {
        this.mBroadcast.sendCurrentSettings(
                this.mUpdateFrequency, this.mImagesToShowCount, this.mBuddiesOrderBy, this.mMeasureUnits);
    }

    private void setUpdateFrequency(Intent intent) {
        int updateFrequency = intent.getIntExtra(UPDATE_FREQUENCY_EXTRA, Integer.MIN_VALUE);
        boolean isUpdateFrequencyValid = BuddiesServiceValidator.validateUpdateFrequency(updateFrequency);
        if (isUpdateFrequencyValid && this.mUpdateFrequency != updateFrequency) {
            this.mUpdateFrequency = updateFrequency;
            this.updateBuddiesStorage(BUDDIES_STORAGE_UPDATE_FREQUENCY, updateFrequency);
        }
    }

    private void setImagesToShowCount(Intent intent) {
        int imagesToShowCount = intent.getIntExtra(IMAGES_TO_SHOW_COUNT_EXTRA, Integer.MIN_VALUE);
        boolean isImagesToShowCountValid = BuddiesServiceValidator.validateImagesToShowCount(imagesToShowCount);
        if (isImagesToShowCountValid && this.mImagesToShowCount != imagesToShowCount) {
            this.mImagesToShowCount = imagesToShowCount;
            this.updateBuddiesStorage(BUDDIES_STORAGE_IMAGES_TO_SHOW_COUNT, imagesToShowCount);
        }
    }

    private void setBuddiesOrderBy(Intent intent) {
        int buddiesOrderByAsInt = intent.getIntExtra(BUDDIES_ORDER_BY_EXTRA, Integer.MIN_VALUE);
        boolean isBuddiesOrderByValid = BuddiesServiceValidator.validateBuddiesOrderByAsInt(buddiesOrderByAsInt);
        if (isBuddiesOrderByValid && this.mBuddiesOrderBy != OrderBy.values()[buddiesOrderByAsInt]) {
            this.mBuddiesOrderBy = OrderBy.values()[buddiesOrderByAsInt];
            this.updateBuddiesStorage(BUDDIES_STORAGE_BUDDIES_ORDER_BY_AS_INT, buddiesOrderByAsInt);
        }
    }

    private void setMeasureUnits(Intent intent) {
        int measureUnitsAsInt = intent.getIntExtra(BUDDIES_MEASURE_UNITS_EXTRA, Integer.MIN_VALUE);
        boolean areMeasureUnitsValid = BuddiesServiceValidator.validateDistanceAsInt(measureUnitsAsInt);
        if (areMeasureUnitsValid && this.mMeasureUnits != MeasureUnits.values()[measureUnitsAsInt]) {
            this.mMeasureUnits = MeasureUnits.values()[measureUnitsAsInt];
            this.updateBuddiesStorage(BUDDIES_STORAGE_MEASURE_UNITS_AS_INT, measureUnitsAsInt);
        }
    }

    private void getBuddieImages(Intent intent) {
        // TODO fill
    }

    private void runServiceUpdating() {
        this.mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                while (BuddiesService.this.mIsUpdatingActive) {
                    BuddiesService.this.updateBuddiesInfo();
                    BuddiesService.this.updateCurrentPosition();

                    BuddiesService.this.setUpdatingTemporallyUnavailable(UPDATING_LOCK_TIME);
                    ThreadSleeper.sleep(BuddiesService.this.mUpdateFrequency);
                    BuddiesService.this.validateStatusAvailabilities();
                }
            }
        });
    }

    private void runOccasionalServiceUpdating() {
        this.mOccasionalHandler.post(new Runnable() {
            @Override
            public void run() {
                BuddiesService.this.updateBuddiesInfo();
                BuddiesService.this.updateCurrentPosition();

                BuddiesService.this.setUpdatingTemporallyUnavailable(UPDATING_LOCK_TIME);
            }
        });
    }

    private void updateBuddiesInfo() {
        if (!this.mIsUpdatingAvailable || !this.mIsNetworkAvailable || this.mIsOnPauseMode) {
            return;
        }

        HttpResponse response = this.mHttpRequester.get(String.format(
                "friends/all?orderBy=%s&sessionKey=%s",
                this.mBuddiesOrderBy.toString().toLowerCase(), this.mSessionKey));

        if (response.isStatusOk()) {
            this.mCurrentBuddiesInfo = response.getMessage();
            this.sendBroadcastWithBuddiesInfoUpdate();
        }
    }

    private void updateCurrentPosition() {
        if (!this.mIsUpdatingAvailable || !this.mIsNetworkAvailable || !this.mIsGpsAvailable) {
            return;
        }

        CoordinatesModel coordinatesModel = this.mLocationUpdater.getLastKnownLocation();
        if (coordinatesModel != null) {
            String coordinatesModelAsJson = this.mGson.toJson(coordinatesModel);
            HttpResponse response = this.mHttpRequester.post(
                    "coordinates/update?sessionKey=" + this.mSessionKey, coordinatesModelAsJson);

            if (response.isStatusOk()) {
                LogHelper.logThreadId("updateCurrentPosition() error: " + response.getMessage());
            }
        }
    }

    private void validateStatusAvailabilities(){
        this.mIsNetworkAvailable = NetworkConnectionInfo.isOnline(this);
        this.mIsGpsAvailable = this.mLocationUpdater.isProviderEnabled();
    }

    private void sendBroadcastWithBuddiesInfoUpdate() {
        if (this.mCurrentBuddiesInfo != null) {
            this.mBroadcast.sendBuddiesInfoUpdate(this.mCurrentBuddiesInfo, this.mMeasureUnits);
        }
    }

    private void setUpdatingTemporallyUnavailable(long delay) {
        this.mIsUpdatingAvailable = false;
        this.mUpdateTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                BuddiesService.this.mIsUpdatingAvailable = true;
            }
        }, delay);
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
            this.mBuddiesOrderBy = OrderBy.values()[buddiesOrderByAsInt];
        } else {
            this.mBuddiesOrderBy = BUDDIES_ORDER_BY_DEFAULT;
        }

        int measureUnitsAsInt = buddiesStorage.getInt(
                BUDDIES_STORAGE_MEASURE_UNITS_AS_INT, Integer.MIN_VALUE);
        boolean areMeasureUnitsValid = BuddiesServiceValidator.validateDistanceAsInt(measureUnitsAsInt);
        if (areMeasureUnitsValid) {
            this.mMeasureUnits = MeasureUnits.values()[measureUnitsAsInt];
        } else {
            this.mMeasureUnits = MEASURE_UNITS_DEFAULT;
        }
    }

    private void updateBuddiesStorage(String storageItem, int value) {
        SharedPreferences userStorage = this.getSharedPreferences(BUDDIES_STORAGE, MODE_PRIVATE);
        SharedPreferences.Editor editor = userStorage.edit();
        editor.putInt(storageItem, value);
        editor.commit();
    }

    private class StatusChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BuddiesService.ANDROID_CONNECTIVITY_CHANGE)) {
                this.handleConnectivityChange(context);
            } else if (action != null && action.equals(BuddiesService.ANDROID_GPS_ENABLED_CHANGE)) {
                this.handleGpsEnabledChange();
            }
        }

        private void handleConnectivityChange(Context context) {
            ConnectivityManager connectivityManager = (ConnectivityManager)
                    context.getSystemService(Context.CONNECTIVITY_SERVICE);

            NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
            BuddiesService.this.mIsNetworkAvailable = activeNetworkInfo != null && activeNetworkInfo.isConnected();
            if (BuddiesService.this.mIsNetworkAvailable) {
                BuddiesService.this.forceUpdatingBuddiesService();
            } else {
                ToastNotifier.makeToast(BuddiesService.this, ERROR_MESSAGE_NO_NETWORK);
            }
        }

        private void handleGpsEnabledChange() {
            BuddiesService.this.mIsGpsAvailable = BuddiesService.this.mLocationUpdater.isProviderEnabled();
            if (!BuddiesService.this.mIsGpsAvailable) {
                ToastNotifier.makeToast(BuddiesService.this, ERROR_MESSAGE_NO_GPS);
            }
        }
    }
}