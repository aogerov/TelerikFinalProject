package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.HandlerThread;
import android.os.IBinder;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by Gercho on 11/7/13.
 */
public class UserService extends Service {

    public static final String START_SERVICE = "com.gercho.action.START_USER_SERVICE";
    public static final String STOP_SERVICE = "com.gercho.action.STOP_USER_SERVICE";
    public static final String LOGIN_SERVICE = "com.gercho.action.LOGIN_USER_SERVICE";
    public static final String REGISTER_SERVICE = "com.gercho.action.REGISTER_USER_SERVICE";
    public static final String LOGOUT_SERVICE = "com.gercho.action.LOGOUT_USER_SERVICE";

    private static final String USER_STORAGE = "UserStorage";
    private static final String USER_SESSION_KEY = "UserSessionKey";
    private static final String USER_LOGGED_IN = "UserLoggedIn";
    private static final String USERNAME = "Username";
    private static final String NICKNAME = "Nickname";
    private static final String PASSWORD = "Password";
    private static final int MIN_USERNAME_AND_NICKNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_INPUT_FIELDS_LENGTH = 30;

    private HandlerThread mHandlerThread;
    private boolean mIsUserLoggedIn;
    private String mSessionKey;

    public boolean getIsUserLoggedIn() {
        return this.mIsUserLoggedIn;
    }

    public String getSessionKey() {
        return this.mSessionKey;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        this.mHandlerThread = new HandlerThread("UserService");
        this.mHandlerThread.start();
        this.getAuthCode("Fanta", "12049uas");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();

        if (START_SERVICE.equalsIgnoreCase(action)) {
            this.readDataFromStorage();
        } else if (STOP_SERVICE.equalsIgnoreCase(action)) {
            this.stopSelf();
        } else if (LOGIN_SERVICE.equalsIgnoreCase(action)) {
            this.login(intent);
        } else if (REGISTER_SERVICE.equalsIgnoreCase(action)) {
            this.register(intent);
        } else if (LOGOUT_SERVICE.equalsIgnoreCase(action)) {
            this.logout();
        }

        return START_REDELIVER_INTENT;
    }

    @Override
    public void onDestroy() {
        this.mHandlerThread.quit();
        this.mHandlerThread = null;
    }

    private void readDataFromStorage() {
        SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, 0);
        this.mIsUserLoggedIn = userStorage.getBoolean(USER_LOGGED_IN, false);
        if (this.mIsUserLoggedIn) {
            this.mSessionKey = userStorage.getString(USER_SESSION_KEY, null);
            if (this.mSessionKey == null) {
                this.mIsUserLoggedIn = false;
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

        SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, 0);
        SharedPreferences.Editor editor = userStorage.edit();
        editor.putBoolean(USER_LOGGED_IN, false);
        editor.putString(USER_SESSION_KEY, null);
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
}
