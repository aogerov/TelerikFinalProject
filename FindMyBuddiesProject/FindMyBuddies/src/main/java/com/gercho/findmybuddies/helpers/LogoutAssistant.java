package com.gercho.findmybuddies.helpers;

import android.content.Context;
import android.content.Intent;

import com.gercho.findmybuddies.LoginRegisterActivity;

/**
 * Created by Gercho on 11/16/13.
 */
public class LogoutAssistant {

    public static void logout(Context context) {
        Intent buddiesServiceIntent = new Intent();
        buddiesServiceIntent.setAction(ServiceActions.STOP_BUDDIES_SERVICE);
        context.startService(buddiesServiceIntent);

        Intent userServiceIntent = new Intent();
        userServiceIntent.setAction(ServiceActions.LOGOUT);
        context.startService(userServiceIntent);

        Intent mainActivityIntent = new Intent(context, LoginRegisterActivity.class);
        context.startActivity(mainActivityIntent);
    }
}
