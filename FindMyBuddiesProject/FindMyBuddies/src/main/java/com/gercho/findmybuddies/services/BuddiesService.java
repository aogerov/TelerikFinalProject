package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.gercho.findmybuddies.broadcasts.BuddiesServiceBroadcast;
import com.gercho.findmybuddies.data.DataPersister;
import com.gercho.findmybuddies.data.HttpResponse;
import com.gercho.findmybuddies.devices.LocationInfo;
import com.gercho.findmybuddies.devices.NetworkConnectionInfo;
import com.gercho.findmybuddies.enums.MeasureUnits;
import com.gercho.findmybuddies.enums.OrderByTypes;
import com.gercho.findmybuddies.helpers.ServiceActions;
import com.gercho.findmybuddies.helpers.LogHelper;
import com.gercho.findmybuddies.helpers.ThreadSleeper;
import com.gercho.findmybuddies.models.BuddieFoundModel;
import com.gercho.findmybuddies.models.BuddieModel;
import com.gercho.findmybuddies.models.CoordinatesModel;
import com.gercho.findmybuddies.models.ResponseModel;
import com.gercho.findmybuddies.validators.BuddiesValidator;
import com.google.gson.Gson;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Gercho on 11/12/13.
 */
public class BuddiesService extends Service {

    public static final String BUDDIES_SERVICE_BROADCAST = "BuddiesServiceBroadcast";
    public static final String BUDDIES_INFO_UPDATE_EXTRA = "BuddiesInfoUpdateExtra";
    public static final String NEW_BUDDIE_REQUESTS_EXTRA = "NewBuddieRequestsExtra";
    public static final String BUDDIE_SEARCH_RESULT_EXTRA = "BuddieSearchResultExtra";
    public static final String BUDDIE_REMOVED_RESULT_EXTRA = "BuddieRemovedResultExtra";
    public static final String ALL_REQUESTS_EXTRA = "AllRequestsExtra";
    public static final String REQUESTS_SEND_RESULT_EXTRA = "RequestSendResultExtra";
    public static final String REQUESTS_IS_ACCEPTED_EXTRA = "RequestIsAcceptedExtra";
    public static final String REQUESTS_IS_LEFT_FOR_LATER_EXTRA = "RequestIsLeftForLaterExtra";
    public static final String RESPONSE_TO_REQUEST_EXTRA = "ResponseToRequestExtra";
    public static final String BUDDIE_IMAGES_EXTRA = "BuddieImagesExtra";
    public static final String NEW_IMAGE_URI_EXTRA = "NewImageUriExtra";
    public static final String IS_HTTP_STATUS_OK_EXTRA = "HttpIsStatusOkExtra";
    public static final String BUDDIE_ID_EXTRA = "BuddieIdExtra";
    public static final String BUDDIE_NICKNAME_EXTRA = "BuddieNicknameExtra";
    public static final String UPDATE_FREQUENCY_EXTRA = "UpdateFrequencyExtra";
    public static final String IMAGES_TO_SHOW_COUNT_EXTRA = "ImagesToShowCountExtra";
    public static final String BUDDIES_ORDER_BY_TYPES_EXTRA = "BuddiesOrderByTypesExtra";
    public static final String BUDDIES_MEASURE_UNITS_EXTRA = "BuddiesMeasureUnitsExtra";
    public static final String INFO_MESSAGE_EXTRA = "InfoMessageExtra";

    private static final String ANDROID_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    private static final String ANDROID_GPS_ENABLED_CHANGE = "android.location.GPS_ENABLED_CHANGE";

    private static final int UPDATING_LOCK_TIME = 1000 * 50; // 50 seconds
    private static final int UPDATE_FREQUENCY_DEFAULT = 1000 * 60 * 5; // 5 minutes
    private static final int IMAGES_TO_SHOW_COUNT_DEFAULT = 3;
    private static final OrderByTypes BUDDIES_ORDER_BY_DEFAULT = OrderByTypes.DISTANCE;
    private static final MeasureUnits MEASURE_UNITS_DEFAULT = MeasureUnits.KILOMETERS;

    private static final String MESSAGE_SYNCHRONIZING = "Synchronizing";
    private static final String ERROR_MESSAGE_NO_NETWORK = "Find My Buddies: Can't access buddies, no network available";
    private static final String ERROR_MESSAGE_NO_GPS = "Find My Buddies: Please turn on your GPS";

    private static final String BUDDIES_STORAGE = "BuddiesStorage";
    private static final String BUDDIES_STORAGE_UPDATE_FREQUENCY = "BuddiesStorageUpdateFrequency";
    private static final String BUDDIES_STORAGE_IMAGES_TO_SHOW_COUNT = "BuddiesStorageImagesToShowCount";
    private static final String BUDDIES_STORAGE_BUDDIES_ORDER_BY_AS_INT = "BuddiesStorageBuddiesOrderByAsInt";
    private static final String BUDDIES_STORAGE_MEASURE_UNITS_AS_INT = "BuddiesStorageMeasureUnitsAsInt";

    private StatusChangeReceiver mStatusChangeReceiver;
    private boolean mIsServiceAlreadyStarted;
    private boolean mIsAutomaticUpdatingActive;
    private boolean mIsUpdatingAvailable;
    private boolean mIsOnPauseMode;
    private boolean mIsNetworkAvailable;
    private boolean mIsGpsAvailable;
    private BuddiesServiceBroadcast mBroadcast;
    private HandlerThread mMainHandledThread;
    private HandlerThread mOccasionalHandlerThread;
    private HandlerThread mUserHandlerThread;
    private Handler mMainHandler;
    private Handler mOccasionalHandler;
    private Handler mUserHandler;
    private Timer mUpdateTimer;
    private LocationInfo mLocationInfo;
    private Gson mGson;
    private String mSessionKey;
    private int mUpdateFrequency;
    private int mImagesToShowCount;
    private OrderByTypes mOrderByTypes;
    private MeasureUnits mMeasureUnits;
    private String mCurrentBuddiesInfo;
    private int mNewBuddieRequestsCount;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (ServiceActions.START_BUDDIES_SERVICE.equals(action)) {
            this.startBuddiesService(intent);
        } else if (ServiceActions.RESUME_BUDDIES_SERVICE.equals(action)) {
            this.resumeBuddiesService();
        } else if (ServiceActions.FORCE_UPDATING_BUDDIES_SERVICE.equals(action)) {
            this.forceUpdatingBuddiesService();
        } else if (ServiceActions.PAUSE_BUDDIES_SERVICE.equals(action)) {
            this.pauseBuddiesService();
        } else if (ServiceActions.STOP_BUDDIES_SERVICE.equals(action)) {
            this.stopBuddiesService();
        } else if (ServiceActions.SEARCH_FOR_NEW_BUDDIE.equals(action)) {
            this.searchForNewBuddie(intent);
        } else if (ServiceActions.REMOVE_EXISTING_BUDDIE.equals(action)) {
            this.removeExistingBuddie(intent);
        } else if (ServiceActions.GET_ALL_REQUESTS.equals(action)) {
            this.getAllRequests();
        } else if (ServiceActions.SEND_BUDDIE_REQUEST.equals(action)) {
            this.sendBuddieRequest(intent);
        } else if (ServiceActions.RESPOND_TO_BUDDIE_REQUEST.equals(action)) {
            this.respondToBuddieRequest(intent);
        } else if (ServiceActions.SEND_NEW_IMAGE.equals(action)) {
            this.sendNewImage(intent);
        } else if (ServiceActions.GET_BUDDIE_IMAGES.equals(action)) {
            this.getBuddieImages(intent);
        } else if (ServiceActions.GET_CURRENT_SETTINGS.equals(action)) {
            this.getCurrentSettings();
        } else if (ServiceActions.SET_UPDATE_FREQUENCY.equals(action)) {
            this.setUpdateFrequency(intent);
        } else if (ServiceActions.SET_IMAGES_TO_SHOW_COUNT.equals(action)) {
            this.setImagesToShowCount(intent);
        } else if (ServiceActions.SET_BUDDIES_ORDER_BY.equals(action)) {
            this.setBuddiesOrderBy(intent);
        } else if (ServiceActions.SET_MEASURE_UNITS.equals(action)) {
            this.setMeasureUnits(intent);
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void startBuddiesService(Intent intent) {
        String sessionKey = intent.getStringExtra(UserService.SESSION_KEY_EXTRA);
        boolean isSessionKeyValid = BuddiesValidator.validateSessionKey(sessionKey);
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

            this.mMainHandledThread = new HandlerThread("BuddiesServiceMainThread");
            this.mMainHandledThread.start();
            Looper mainLooper = this.mMainHandledThread.getLooper();
            if (mainLooper != null) {
                this.mMainHandler = new Handler(mainLooper);
            }

            this.mOccasionalHandlerThread = new HandlerThread("BuddiesServiceOccasionalThread");
            this.mOccasionalHandlerThread.start();
            Looper occasionalLooper = this.mOccasionalHandlerThread.getLooper();
            if (occasionalLooper != null) {
                this.mOccasionalHandler = new Handler(occasionalLooper);
            }

            this.mUserHandlerThread = new HandlerThread("BuddiesServiceUserThread");
            this.mUserHandlerThread.start();
            Looper userLooper = this.mUserHandlerThread.getLooper();
            if (userLooper != null) {
                this.mUserHandler = new Handler(userLooper);
            }

            this.mUpdateTimer = new Timer("UpdateTimer");
            this.mLocationInfo = new LocationInfo(this);
            this.mGson = new Gson();
            this.mBroadcast = new BuddiesServiceBroadcast(this);

            this.readBuddiesStorage();
        }
    }

    private void resumeBuddiesService() {
        this.mIsOnPauseMode = false;
        this.mNewBuddieRequestsCount = 0;
        this.forceUpdatingBuddiesService();
    }

    private void forceUpdatingBuddiesService() {
        if (!this.mIsUpdatingAvailable) {
            this.sendBroadcastWithBuddiesInfoUpdate();
        }

        if (this.mIsAutomaticUpdatingActive) {
            this.runOccasionalServiceUpdating();
        } else {
            this.mIsNetworkAvailable = NetworkConnectionInfo.isOnline(this);
            if (this.mIsNetworkAvailable) {
                this.mIsAutomaticUpdatingActive = true;
                this.startAutomaticUpdating();
            }
        }
    }

    private void pauseBuddiesService() {
        this.mIsOnPauseMode = true;
    }

    private void stopBuddiesService() {
        if (this.mIsServiceAlreadyStarted) {
            this.mIsServiceAlreadyStarted = false;
            this.mIsAutomaticUpdatingActive = false;

            this.unregisterReceiver(this.mStatusChangeReceiver);
            this.mStatusChangeReceiver = null;

            this.mMainHandledThread.quit();
            this.mMainHandledThread = null;
            this.mOccasionalHandlerThread.quit();
            this.mOccasionalHandlerThread = null;
            this.mUserHandlerThread.quit();
            this.mUserHandlerThread = null;
            this.mUpdateTimer = null;
        }

        this.stopSelf();
    }

    private void startAutomaticUpdating() {
        this.mMainHandler.post(new Runnable() {
            @Override
            public void run() {
                while (BuddiesService.this.mIsAutomaticUpdatingActive) {
                    BuddiesService.this.updateCurrentPosition();
                    BuddiesService.this.updateBuddiesInfo();

                    BuddiesService.this.setUpdatingTemporallyUnavailable(UPDATING_LOCK_TIME);
                    ThreadSleeper.sleep(BuddiesService.this.mUpdateFrequency);
                }
            }
        });
    }

    private void runOccasionalServiceUpdating() {
        this.mOccasionalHandler.post(new Runnable() {
            @Override
            public void run() {
                BuddiesService.this.updateCurrentPosition();
                BuddiesService.this.updateBuddiesInfo();

                BuddiesService.this.setUpdatingTemporallyUnavailable(UPDATING_LOCK_TIME);
            }
        });
    }

    private void updateCurrentPosition() {
        if (!this.mIsUpdatingAvailable || !this.mIsNetworkAvailable || !this.mIsGpsAvailable) {
            return;
        }

        CoordinatesModel coordinatesModel = this.mLocationInfo.getLastKnownLocation();
        if (coordinatesModel != null) {
            String coordinatesModelAsJson = this.mGson.toJson(coordinatesModel);
            HttpResponse response = DataPersister.updateCurrentPosition(this.mSessionKey, coordinatesModelAsJson);

            if (response.isStatusOk()) {
                LogHelper.logThreadId("updateCurrentPosition() error: " + response.getMessage());
            }
        }
    }

    private void updateBuddiesInfo() {
        if (!this.mIsUpdatingAvailable || !this.mIsNetworkAvailable || this.mIsOnPauseMode) {
            return;
        }

        this.mBroadcast.sendInfoMessage(MESSAGE_SYNCHRONIZING);

        HttpResponse allBuddies = DataPersister.getAllBuddies(this.mOrderByTypes, this.mSessionKey);
        HttpResponse newBuddieRequests = DataPersister.getNewRequests(this.mSessionKey);

        if (newBuddieRequests.isStatusOk()) {
            this.mNewBuddieRequestsCount = this.mGson.fromJson(newBuddieRequests.getMessage(), Integer.class);
        }

        if (allBuddies.isStatusOk()) {
            this.mCurrentBuddiesInfo = allBuddies.getMessage();
            this.sendBroadcastWithBuddiesInfoUpdate();
        }
    }

    private void sendBroadcastWithBuddiesInfoUpdate() {
        if (this.mCurrentBuddiesInfo != null) {
            this.mBroadcast.sendBuddiesInfoUpdate(
                    this.mCurrentBuddiesInfo, this.mMeasureUnits, this.mNewBuddieRequestsCount);
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

    private void removeExistingBuddie(Intent intent) {
        final int buddieId = intent.getIntExtra(BUDDIE_ID_EXTRA, Integer.MIN_VALUE);
        final String buddieNickname = intent.getStringExtra(BUDDIE_NICKNAME_EXTRA);
        if (buddieId != Integer.MIN_VALUE && buddieNickname != null) {
            BuddieModel buddieModel = new BuddieModel(buddieId, buddieNickname);
            final String buddieAsJson = this.mGson.toJson(buddieModel);

            this.mUserHandler.post(new Runnable() {
                @Override
                public void run() {
                    HttpResponse response = DataPersister.removeExistingBuddie(
                            BuddiesService.this.mSessionKey, buddieAsJson);

                    BuddiesService.this.mBroadcast.sendBuddieRemoveResult(
                            buddieId, buddieNickname, response.isStatusOk());
                }
            });
        }
    }

    private void searchForNewBuddie(Intent intent) {
        final String buddieNickname = intent.getStringExtra(BUDDIE_NICKNAME_EXTRA);
        if (buddieNickname != null) {
            this.mUserHandler.post(new Runnable() {
                @Override
                public void run() {
                    HttpResponse response = DataPersister.searchForNewBuddie(
                            buddieNickname, BuddiesService.this.mSessionKey);

                    BuddiesService.this.mBroadcast.sendBuddieSearchResult(
                            response.getMessage(), response.isStatusOk());
                }
            });
        }
    }

    private void getAllRequests() {
        this.mUserHandler.post(new Runnable() {
            @Override
            public void run() {
                HttpResponse response = DataPersister.getAllRequests(
                        BuddiesService.this.mSessionKey);

                BuddiesService.this.mBroadcast.sendAllRequests(
                        response.getMessage(), response.isStatusOk());
            }
        });
    }

    private void sendBuddieRequest(Intent intent) {
        int buddieId = intent.getIntExtra(BUDDIE_ID_EXTRA, Integer.MIN_VALUE);
        String buddieNickname = intent.getStringExtra(BUDDIE_NICKNAME_EXTRA);
        if (buddieId != Integer.MIN_VALUE && buddieNickname != null) {
            BuddieFoundModel buddieFoundModel = new BuddieFoundModel(buddieId, buddieNickname);
            final String buddieAsJson = this.mGson.toJson(buddieFoundModel);

            this.mUserHandler.post(new Runnable() {
                @Override
                public void run() {
                    HttpResponse response = DataPersister.sendBuddieRequest(
                            BuddiesService.this.mSessionKey, buddieAsJson);

                    BuddiesService.this.mBroadcast.sendBuddieRequestSendResult(
                            response.getMessage(), response.isStatusOk());
                }
            });
        }
    }

    private void respondToBuddieRequest(Intent intent) {
        int buddieId = intent.getIntExtra(BUDDIE_ID_EXTRA, Integer.MIN_VALUE);
        String buddieNickname = intent.getStringExtra(BUDDIE_NICKNAME_EXTRA);
        boolean isAccepted = intent.getBooleanExtra(REQUESTS_IS_ACCEPTED_EXTRA, false);
        boolean isLeftForLater = intent.getBooleanExtra(REQUESTS_IS_LEFT_FOR_LATER_EXTRA, false);
        if (buddieId != Integer.MIN_VALUE && buddieNickname != null) {
            ResponseModel responseModel = new ResponseModel(buddieId, buddieNickname, isAccepted, isLeftForLater);
            final String responseAsJson = this.mGson.toJson(responseModel);

            this.mUserHandler.post(new Runnable() {
                @Override
                public void run() {
                    HttpResponse response = DataPersister.respondToBuddieRequest(
                            BuddiesService.this.mSessionKey, responseAsJson);

                    BuddiesService.this.mBroadcast.sendResponseToBuddieRequest(
                            response.getMessage(), response.isStatusOk());
                }
            });
        }
    }

    private void sendNewImage(Intent intent) {
        Uri photoPath = intent.getParcelableExtra(NEW_IMAGE_URI_EXTRA);
        String result = photoPath.toString();
    }

    private void getBuddieImages(Intent intent) {
        int buddieId = intent.getIntExtra(BUDDIE_ID_EXTRA, Integer.MIN_VALUE);
        String buddieNickname = intent.getStringExtra(BUDDIE_NICKNAME_EXTRA);
        if (buddieId != Integer.MIN_VALUE && buddieNickname != null) {
            BuddieModel buddieModel = new BuddieModel(buddieId, buddieNickname);
            final String buddieAsJson = this.mGson.toJson(buddieModel);

            this.mUserHandler.post(new Runnable() {
                @Override
                public void run() {
                    HttpResponse response = DataPersister.getBuddieImages(
                            BuddiesService.this.mImagesToShowCount, BuddiesService.this.mSessionKey, buddieAsJson);

                    BuddiesService.this.mBroadcast.sendBuddieImages(
                            response.getMessage(), response.isStatusOk());
                }
            });
        }
    }

    private void getCurrentSettings() {
        this.mBroadcast.sendCurrentSettings(
                this.mUpdateFrequency, this.mImagesToShowCount, this.mOrderByTypes, this.mMeasureUnits);
    }

    private void setUpdateFrequency(Intent intent) {
        int updateFrequency = intent.getIntExtra(UPDATE_FREQUENCY_EXTRA, Integer.MIN_VALUE);
        boolean isUpdateFrequencyValid = BuddiesValidator.validateUpdateFrequency(updateFrequency);
        if (isUpdateFrequencyValid && this.mUpdateFrequency != updateFrequency) {
            this.mUpdateFrequency = updateFrequency;
            this.updateBuddiesStorage(BUDDIES_STORAGE_UPDATE_FREQUENCY, updateFrequency);
        }
    }

    private void setImagesToShowCount(Intent intent) {
        int imagesToShowCount = intent.getIntExtra(IMAGES_TO_SHOW_COUNT_EXTRA, Integer.MIN_VALUE);
        boolean isImagesToShowCountValid = BuddiesValidator.validateImagesToShowCount(imagesToShowCount);
        if (isImagesToShowCountValid && this.mImagesToShowCount != imagesToShowCount) {
            this.mImagesToShowCount = imagesToShowCount;
            this.updateBuddiesStorage(BUDDIES_STORAGE_IMAGES_TO_SHOW_COUNT, imagesToShowCount);
        }
    }

    private void setBuddiesOrderBy(Intent intent) {
        int buddiesOrderByAsInt = intent.getIntExtra(BUDDIES_ORDER_BY_TYPES_EXTRA, Integer.MIN_VALUE);
        boolean isBuddiesOrderByValid = BuddiesValidator.validateBuddiesOrderByAsInt(buddiesOrderByAsInt);
        if (isBuddiesOrderByValid && this.mOrderByTypes != OrderByTypes.values()[buddiesOrderByAsInt]) {
            this.mOrderByTypes = OrderByTypes.values()[buddiesOrderByAsInt];
            this.updateBuddiesStorage(BUDDIES_STORAGE_BUDDIES_ORDER_BY_AS_INT, buddiesOrderByAsInt);
        }
    }

    private void setMeasureUnits(Intent intent) {
        int measureUnitsAsInt = intent.getIntExtra(BUDDIES_MEASURE_UNITS_EXTRA, Integer.MIN_VALUE);
        boolean areMeasureUnitsValid = BuddiesValidator.validateDistanceAsInt(measureUnitsAsInt);
        if (areMeasureUnitsValid && this.mMeasureUnits != MeasureUnits.values()[measureUnitsAsInt]) {
            this.mMeasureUnits = MeasureUnits.values()[measureUnitsAsInt];
            this.updateBuddiesStorage(BUDDIES_STORAGE_MEASURE_UNITS_AS_INT, measureUnitsAsInt);
        }
    }

    private void readBuddiesStorage() {
        SharedPreferences buddiesStorage = this.getSharedPreferences(BUDDIES_STORAGE, MODE_PRIVATE);

        int updateFrequency = buddiesStorage.getInt(
                BUDDIES_STORAGE_UPDATE_FREQUENCY, Integer.MIN_VALUE);
        boolean isUpdateFrequencyValid = BuddiesValidator.validateUpdateFrequency(updateFrequency);
        if (isUpdateFrequencyValid) {
            this.mUpdateFrequency = updateFrequency;
        } else {
            this.mUpdateFrequency = UPDATE_FREQUENCY_DEFAULT;
        }

        int imagesToShowCount = buddiesStorage.getInt(
                BUDDIES_STORAGE_IMAGES_TO_SHOW_COUNT, Integer.MIN_VALUE);
        boolean isImagesToShowCountValid = BuddiesValidator.validateImagesToShowCount(imagesToShowCount);
        if (isImagesToShowCountValid) {
            this.mImagesToShowCount = imagesToShowCount;
        } else {
            this.mImagesToShowCount = IMAGES_TO_SHOW_COUNT_DEFAULT;
        }

        int buddiesOrderByAsInt = buddiesStorage.getInt(
                BUDDIES_STORAGE_BUDDIES_ORDER_BY_AS_INT, Integer.MIN_VALUE);
        boolean isBuddiesOrderByValid = BuddiesValidator.validateBuddiesOrderByAsInt(buddiesOrderByAsInt);
        if (isBuddiesOrderByValid) {
            this.mOrderByTypes = OrderByTypes.values()[buddiesOrderByAsInt];
        } else {
            this.mOrderByTypes = BUDDIES_ORDER_BY_DEFAULT;
        }

        int measureUnitsAsInt = buddiesStorage.getInt(
                BUDDIES_STORAGE_MEASURE_UNITS_AS_INT, Integer.MIN_VALUE);
        boolean areMeasureUnitsValid = BuddiesValidator.validateDistanceAsInt(measureUnitsAsInt);
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
            if (action != null && action.equals(ANDROID_CONNECTIVITY_CHANGE)) {
                this.handleConnectivityChange();
            } else if (action != null && action.equals(ANDROID_GPS_ENABLED_CHANGE)) {
                this.handleGpsEnabledChange();
            }
        }

        private void handleConnectivityChange() {
            BuddiesService.this.mIsNetworkAvailable = NetworkConnectionInfo.isOnline(BuddiesService.this);
            if (BuddiesService.this.mIsNetworkAvailable) {
                BuddiesService.this.forceUpdatingBuddiesService();
            } else {
                BuddiesService.this.mBroadcast.sendInfoMessage(ERROR_MESSAGE_NO_NETWORK);
            }
        }

        private void handleGpsEnabledChange() {
            BuddiesService.this.mIsGpsAvailable = BuddiesService.this.mLocationInfo.isProviderEnabled();
            if (!BuddiesService.this.mIsGpsAvailable) {
                BuddiesService.this.mBroadcast.sendInfoMessage(ERROR_MESSAGE_NO_GPS);
            }
        }
    }
}