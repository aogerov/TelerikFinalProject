package com.gercho.findmybuddies.helpers;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Gercho on 11/8/13.
 */
public class ToastHelper {

    private static Toast mToast;

    public static void makeToast(Context context, String message) {
        if (mToast != null) {
            mToast.cancel();
        }

        mToast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        mToast.show();
    }
}
