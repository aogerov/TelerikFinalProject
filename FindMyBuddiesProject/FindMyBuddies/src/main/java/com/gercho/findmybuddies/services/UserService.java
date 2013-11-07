package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;
import android.os.HandlerThread;
import android.os.IBinder;

/**
 * Created by Gercho on 11/7/13.
 */
public class UserService extends Service {

    public static String START_USER_ACTION = "com.gercho.action.START_USER_SERVICE";
    public static String STOP_USER_ACTION = "com.gercho.action.STOP_USER_SERVICE";

    private HandlerThread mHandlerThread;

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

        if (START_USER_ACTION.equalsIgnoreCase(action)) {
            this.startUserService();
        } else if (STOP_USER_ACTION.equalsIgnoreCase(action)) {
            this.stopUserService();
            this.stopSelf();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        this.mHandlerThread.quit();
        this.mHandlerThread = null;
    }

    private void startUserService() {
//        if (this.mLocationListener == null) {
//            this.mLocationListener = new MyLocationListener();
//            Looper looper = this.mHandlerThread.getLooper();
//            LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
//            lm.requestLocationUpdates(LocationManager.GPS_PROVIDER,
//                    10000, 0, this.mLocationListener, looper);
//        }
    }

    private void stopUserService() {
//        if (this.mLocationListener != null) {
//            LocationManager lm = (LocationManager) this.getSystemService(LOCATION_SERVICE);
//            lm.removeUpdates(this.mLocationListener);
//            this.mLocationListener = null;
//        }
    }
}
