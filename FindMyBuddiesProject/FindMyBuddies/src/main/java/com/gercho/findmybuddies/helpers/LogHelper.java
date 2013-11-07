package com.gercho.findmybuddies.helpers;

import android.util.Log;

/**
 * Created by Gercho on 11/7/13.
 */
public class LogHelper {

    public static void logThreadId(String message) {
        long processId = android.os.Process.myPid();
        long threadId = Thread.currentThread().getId();
        Log.d("BackgroundWork", String.format("[ Process: %d | Thread: %d] %s", processId, threadId, message));
    }
}
