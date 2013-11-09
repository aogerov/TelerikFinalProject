package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

import com.gercho.findmybuddies.helpers.ThreadSleeper;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Gercho on 11/7/13.
 */
public class UserService extends Service {

    public static final String START_USER_SERVICE = "com.gercho.action.START_USER_SERVICE";
    public static final String STOP_USER_SERVICE = "com.gercho.action.STOP_USER_SERVICE";
    public static final String LOGIN_USER_SERVICE = "com.gercho.action.LOGIN_USER_SERVICE";
    public static final String REGISTER_USER_SERVICE = "com.gercho.action.REGISTER_USER_SERVICE";
    public static final String LOGOUT_USER_SERVICE = "com.gercho.action.LOGOUT_USER_SERVICE";

    public static final String USERNAME = "Username";
    public static final String NICKNAME = "Nickname";
    public static final String PASSWORD = "Password";

    private static final String STORAGE = "Storage";
    private static final String SESSION_KEY = "SessionKey";
    private static final String IS_LOGGED_IN = "IsLoggedIn";
    private static final int MIN_USERNAME_AND_NICKNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_INPUT_FIELDS_LENGTH = 30;

    private boolean mIsServiceInitialized;
    private IBinder mBinder;
    private HandlerThread mUserThread;
    private boolean mIsUserLoggedIn;
    private String mSessionKey;
    private boolean mIsConnectingActive;

    public boolean getIsUserLoggedIn() {
        return this.mIsUserLoggedIn;
    }

    public String getSessionKey() {
        return this.mSessionKey;
    }

    @Override
    public void onCreate() {
        this.mBinder = new UserServiceBinder(this);
        this.mUserThread = new HandlerThread("UserServiceThread");
        this.mUserThread.start();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (START_USER_SERVICE.equalsIgnoreCase(action)) {
            this.initService();
        } else if (STOP_USER_SERVICE.equalsIgnoreCase(action)) {
            this.stopSelf();
        } else if (LOGIN_USER_SERVICE.equalsIgnoreCase(action)) {
            this.login(intent);
        } else if (REGISTER_USER_SERVICE.equalsIgnoreCase(action)) {
            this.register(intent);
        } else if (LOGOUT_USER_SERVICE.equalsIgnoreCase(action)) {
            this.logout();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        this.mUserThread.quit();
        this.mUserThread = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return this.mBinder;
    }

    private void initService() {
        if (!this.mIsServiceInitialized) {
            this.mIsServiceInitialized = true;

            SharedPreferences userStorage = this.getSharedPreferences(STORAGE, 0);
            this.mIsUserLoggedIn = userStorage.getBoolean(IS_LOGGED_IN, false);
            if (this.mIsUserLoggedIn) {
                this.mSessionKey = userStorage.getString(SESSION_KEY, null);
                if (this.mSessionKey == null) {
                    this.mIsUserLoggedIn = false;
                }
            }
        }
    }

    private void login(Intent intent) {
        String username = this.extractAndValidateUsername(intent);
        String password = this.extractAndValidatePassword(intent);
        String authCode = this.getAuthCode(username, password);

        // TODO Http request
    }

    private void register(Intent intent) {
        String username = this.extractAndValidateUsername(intent);
        String nickname = this.extractAndValidateNickname(intent);
        String password = this.extractAndValidatePassword(intent);
        String authCode = this.getAuthCode(username, password);

        // TODO Http request
    }

    private void logout() {
        this.mIsUserLoggedIn = false;
        this.mSessionKey = null;

        SharedPreferences userStorage = this.getSharedPreferences(STORAGE, 0);
        SharedPreferences.Editor editor = userStorage.edit();
        editor.putBoolean(IS_LOGGED_IN, false);
        editor.putString(SESSION_KEY, null);

        // TODO Http request
    }

    private String extractAndValidateUsername(Intent intent) {
        String username = intent.getStringExtra(USERNAME);
        if (username != null) {
            String usernameTrimmed = username.trim();
            if (usernameTrimmed.length() >= MIN_USERNAME_AND_NICKNAME_LENGTH &&
                    usernameTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return usernameTrimmed;
            }
        }

        throw new IllegalArgumentException("Username is invalid");
    }

    private String extractAndValidateNickname(Intent intent) {
        String nickname = intent.getStringExtra(NICKNAME);
        if (nickname != null) {
            String nicknameTrimmed = nickname.trim();
            if (nicknameTrimmed.length() >= MIN_USERNAME_AND_NICKNAME_LENGTH &&
                    nicknameTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return nicknameTrimmed;
            }
        }

        throw new IllegalArgumentException("Nickname is invalid");
    }

    private String extractAndValidatePassword(Intent intent) {
        String password = intent.getStringExtra(PASSWORD);
        if (password != null) {
            String passwordTrimmed = password.trim();
            if (passwordTrimmed.length() >= MIN_PASSWORD_LENGTH &&
                    passwordTrimmed.length() <= MAX_INPUT_FIELDS_LENGTH) {
                return passwordTrimmed;
            }
        }

        throw new IllegalArgumentException("Password is invalid");
    }

    private String getAuthCode(String username, String password) {
        MessageDigest messageDigest = null;
        try {
            messageDigest = MessageDigest.getInstance("SHA-1");
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        if (messageDigest != null) {
            messageDigest.update((username + password).getBytes());
            byte[] bytes = messageDigest.digest();
            StringBuffer buffer = new StringBuffer();
            for (int i = 0; i < bytes.length; i++) {
                String tmp = Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
                buffer.append(tmp);
            }

            String authCode = buffer.toString();
            return authCode;
        }

        throw new NumberFormatException("AuthCode failed on create");
    }

    // this is just test method, remove it on release
    public void changeUserLoginStatus() {
        if (this.mIsConnectingActive) {
            return;
        }

        this.mIsConnectingActive = true;
        this.mIsUserLoggedIn = false;
        this.mSessionKey = null;

        Handler userHandler = new Handler(this.mUserThread.getLooper());
        userHandler.post(new Runnable() {
            @Override
            public void run() {
                ThreadSleeper.sleep(15000);
                UserService.this.mIsConnectingActive = false;
                UserService.this.mIsUserLoggedIn = true;
            }
        });
    }
}
