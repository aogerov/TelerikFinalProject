package com.gercho.findmybuddies.helpers;

/**
 * Created by Gercho on 11/15/13.
 */
public class AppActions {

    // UserService actions
    public static final String START_USER_SERVICE = "com.gercho.action.START_USER_SERVICE";
    public static final String STOP_USER_SERVICE = "com.gercho.action.STOP_USER_SERVICE";
    public static final String LOGIN = "com.gercho.action.LOGIN";
    public static final String REGISTER = "com.gercho.action.REGISTER";
    public static final String LOGOUT = "com.gercho.action.LOGOUT";
    public static final String START_ADDITIONAL_SERVICES = "com.gercho.action.START_ADDITIONAL_SERVICES";

    // BuddiesService actions
    public static final String START_BUDDIES_SERVICE = "com.gercho.action.START_BUDDIES_SERVICE";
    public static final String PAUSE_BUDDIES_SERVICE = "com.gercho.action.PAUSE_BUDDIES_SERVICE";
    public static final String RESUME_BUDDIES_SERVICE = "com.gercho.action.RESUME_BUDDIES_SERVICE";
    public static final String STOP_BUDDIES_SERVICE = "com.gercho.action.STOP_BUDDIES_SERVICE";
    public static final String FORCE_UPDATING_BUDDIES_SERVICE = "com.gercho.action.FORCE_UPDATING_BUDDIES_SERVICE";
    public static final String SEARCH_FOR_NEW_BUDDIE = "com.gercho.action.SEARCH_FOR_NEW_BUDDIE";
    public static final String REMOVE_EXISTING_BUDDIE = "com.gercho.action.REMOVE_EXISTING_BUDDIE";
    public static final String GET_ALL_REQUESTS = "com.gercho.action.GET_ALL_REQUESTS";
    public static final String GET_ALL_NEW_REQUESTS = "com.gercho.action.GET_ALL_NEW_REQUESTS";
    public static final String SEND_BUDDIE_REQUEST = "com.gercho.action.SEND_BUDDIE_REQUEST";
    public static final String RESPOND_TO_BUDDIE_REQUEST = "com.gercho.action.RESPOND_TO_BUDDIE_REQUEST";
    public static final String SEND_NEW_IMAGE = "com.gercho.action.SEND_NEW_IMAGE";
    public static final String GET_BUDDIE_IMAGES = "com.gercho.action.GET_BUDDIE_IMAGES";
    public static final String GET_CURRENT_SETTINGS = "com.gercho.action.GET_CURRENT_SETTINGS";
    public static final String SET_UPDATE_FREQUENCY = "com.gercho.action.SET_UPDATE_FREQUENCY";
    public static final String SET_IMAGES_TO_SHOW_COUNT = "com.gercho.action.SET_IMAGES_TO_SHOW_COUNT";
    public static final String SET_BUDDIES_ORDER_BY = "com.gercho.action.SET_BUDDIES_ORDER_BY";
    public static final String SET_MEASURE_UNITS = "com.gercho.action.SET_MEASURE_UNITS";

    // Android broadcasts
    public static final String ANDROID_CONNECTIVITY_CHANGE = "android.net.conn.CONNECTIVITY_CHANGE";
    public static final String ANDROID_GPS_ENABLED_CHANGE = "android.location.GPS_ENABLED_CHANGE";
}
