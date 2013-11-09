package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;

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

    public static final String USER_SERVICE_UPDATE = "UserServiceUpdate";
    public static final String SERVICE_INIT_LOGIN = "ServiceInitLogin";
    public static final String SERVER_RESPONSE_MESSAGE = "ServerResponseMessage";
    public static final String IS_CONNECTED = "IsConnected";
    public static final String USERNAME = "Username";
    public static final String NICKNAME = "Nickname";
    public static final String PASSWORD = "Password";

    private static final String USER_STORAGE = "UserStorage";
    private static final String USER_STORAGE_USERNAME = "UserStorageUsername";
    private static final String USER_STORAGE_NICKNAME = "UserStorageNickname";
    private static final String USER_STORAGE_PASSWORD = "UserStoragePassword";
    private static final int MIN_USERNAME_AND_NICKNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_INPUT_FIELDS_LENGTH = 30;

    private boolean mIsServiceInitialized;
    private boolean mIsConnectingActive;
    private HandlerThread mUserThread;

    private boolean mIsConnected;
    private String mSessionKey;
    private String mUsername;
    private String mNickname;
    private String mPassword;

    @Override
    public void onCreate() {
        this.mIsConnected = false;
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
        return null;
    }

    private void initService() {
        if (!this.mIsServiceInitialized) {
            this.mIsServiceInitialized = true;

            SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, 0);
            this.mUsername = userStorage.getString(USER_STORAGE_USERNAME, null);
            this.mNickname = userStorage.getString(USER_STORAGE_NICKNAME, null);
            this.mPassword = userStorage.getString(USER_STORAGE_PASSWORD, null);

            if (this.mUsername != null && this.mPassword != null) {
                this.sendInitLoginBroadcast();

                String authCode = this.getAuthCode(this.mUsername, this.mPassword);
                this.loginHttpRequest(this.mUsername, authCode);
            }
        }
    }

    private void login(Intent intent) {
        String username = this.extractAndValidateUsername(intent);
        String password = this.extractAndValidatePassword(intent);

        if (username != null && password != null) {
            String authCode = this.getAuthCode(username, password);
            this.loginHttpRequest(username, authCode);
        }
    }

    private void register(Intent intent) {
        String username = this.extractAndValidateUsername(intent);
        String nickname = this.extractAndValidateNickname(intent);
        String password = this.extractAndValidatePassword(intent);

        if (username != null && nickname != null && password != null) {
            String authCode = this.getAuthCode(username, password);
            this.registerHttpRequest(username, authCode, nickname);
        }
    }

    private void logout() {
        this.mIsConnected = false;
        this.mSessionKey = null;
        this.mUsername = null;
        this.mNickname = null;
        this.mPassword = null;

        this.updateLocalStorage(this.mUsername, this.mNickname, this.mPassword);
        this.logoutHttpRequest(this.mSessionKey);
    }

    private void loginHttpRequest(String username, String authCode) {
        if (this.mIsConnectingActive) {
            return;
        }

        this.mIsConnectingActive = true;
        this.mIsConnected = false;
        this.mSessionKey = null;

        Handler userHandler = new Handler(this.mUserThread.getLooper());
        userHandler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void registerHttpRequest(String username, String authCode, String nickname) {
        if (this.mIsConnectingActive) {
            return;
        }

        this.mIsConnectingActive = true;
        this.mIsConnected = false;
        this.mSessionKey = null;
    }

    private void logoutHttpRequest(String sessionKey) {

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

        this.sendConnectionErrorBroadcast("Username is invalid");
        return null;
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

        this.sendConnectionErrorBroadcast("Nickname is invalid");
        return null;
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

        this.sendConnectionErrorBroadcast("Password is invalid");
        return null;
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
            StringBuilder authCode = new StringBuilder();
            for (int i = 0; i < bytes.length; i++) {
                String tmp = Integer.toString((bytes[i] & 0xff) + 0x100, 16).substring(1);
                authCode.append(tmp);
            }

            return authCode.toString();
        }

        throw new NumberFormatException("AuthCode failed on create");
    }

    private void updateLocalStorage(String username, String nickname, String password) {
        SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, 0);
        SharedPreferences.Editor editor = userStorage.edit();
        editor.putString(USER_STORAGE_USERNAME, username);
        editor.putString(USER_STORAGE_NICKNAME, nickname);
        editor.putString(USER_STORAGE_PASSWORD, password);
    }

    private void sendInitLoginBroadcast(){
        Intent intent = this.getBroadcastIntent();
        intent.putExtra(SERVICE_INIT_LOGIN, true);
        this.sendBroadcast(intent);
    }

    private void sendConnectionErrorBroadcast(String message) {
        Intent intent = this.getBroadcastIntent();
        intent.putExtra(SERVER_RESPONSE_MESSAGE, message);
        this.sendBroadcast(intent);
    }

    private Intent getBroadcastIntent() {
        Intent intent = new Intent(USER_SERVICE_UPDATE);
        intent.putExtra(IS_CONNECTED, UserService.this.mIsConnected);
        return intent;
    }
}
