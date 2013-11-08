package com.gercho.findmybuddies.helpers;

import android.content.Context;
import android.widget.Toast;

/**
 * Created by Gercho on 11/8/13.
 */
public class ToastHelper {

    public static void makeToast(Context context, String message) {
        Toast toast = Toast.makeText(context, message, Toast.LENGTH_LONG);
        toast.show();
    }
}
