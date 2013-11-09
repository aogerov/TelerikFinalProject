package com.gercho.findmybuddies.helpers;

/**
 * Created by Gercho on 11/9/13.
 */
public class ThreadSleeper {

    public static void sleep(int millisecond) {
        try {
            Thread.sleep(millisecond);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
