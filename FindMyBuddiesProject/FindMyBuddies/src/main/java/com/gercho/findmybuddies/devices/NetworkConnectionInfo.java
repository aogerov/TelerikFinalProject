package com.gercho.findmybuddies.devices;

import android.content.Context;
import android.net.ConnectivityManager;

/**
 * Created by Gercho on 11/14/13.
 */
public class NetworkConnectionInfo {

    public static boolean isOnline(Context context) {
        ConnectivityManager connectivityManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        android.net.NetworkInfo activeNetworkInfo = connectivityManager.getActiveNetworkInfo();
        boolean isConnected = activeNetworkInfo != null && activeNetworkInfo.isConnected();
        if (isConnected) {
            return true;
        } else {
            return false;
        }
    }
}
