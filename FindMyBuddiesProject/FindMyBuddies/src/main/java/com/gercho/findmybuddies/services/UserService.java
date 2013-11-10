package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.gercho.findmybuddies.http.HttpRequester;
import com.gercho.findmybuddies.http.HttpResponse;
import com.gercho.findmybuddies.models.UserModel;
import com.google.gson.Gson;

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

    public static final String USER_SERVICE_BROADCAST = "UserServiceBroadcastManager";
    public static final String USER_SERVICE_CONNECTING = "UserServiceConnecting";
    public static final String USER_SERVICE_IS_CONNECTED = "UserServiceIsConnected";
    public static final String USER_SERVICE_ERROR = "UserServiceError";
    public static final String USER_SERVICE_MESSAGE = "UserServiceMessage";
    public static final String USER_SERVICE_NOT_AVAILABLE_ERROR_MESSAGE = "Service is currently unavailable, please try again in few seconds";
    public static final String USERNAME = "Username";
    public static final String NICKNAME = "Nickname";
    public static final String PASSWORD = "Password";

    private static final String USER_STORAGE = "UserStorage";
    private static final String USER_STORAGE_SESSION_KEY = "UserStorageSessionKey";

    private boolean mIsServiceInitialized;
    private boolean mIsConnectingActive;
    private HandlerThread mHandledThread;
    private Handler mHandler;
    private HttpRequester mHttpRequester;
    private Gson mGson;
    private UserServiceBroadcastManager mBroadcastManager;
    private UserServiceValidator mValidator;
    private String mSessionKey;
    private String mNickname;

    @Override
    public void onCreate() {
        this.mHandledThread = new HandlerThread("UserServiceThread");
        this.mHandledThread.start();
        Looper looper = this.mHandledThread.getLooper();
        if (looper != null) {
            this.mHandler = new Handler(looper);
        }

        this.mHttpRequester = new HttpRequester();
        this.mGson = new Gson();
        this.mBroadcastManager = new UserServiceBroadcastManager(this);
        this.mValidator = new UserServiceValidator(this.mBroadcastManager);
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
        this.mHandledThread.quit();
        this.mHandledThread = null;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private void initService() {
        if (!this.mIsServiceInitialized) {
            this.mIsServiceInitialized = true;
            this.mIsConnectingActive = false;

            SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, 0);
            this.mSessionKey = userStorage.getString(USER_STORAGE_SESSION_KEY, null);

            if (this.mSessionKey != null) {
                this.initSessionKeyHttpRequest(this.mSessionKey);
            }
        }
    }

    private void login(Intent intent) {
        String username = this.mValidator.extractAndValidateUsername(intent);
        String password = this.mValidator.extractAndValidatePassword(intent);
        String authCode = this.getAuthCode(username, password);

        if (username != null && password != null) {
            this.loginHttpRequest(username, authCode);
        }
    }

    private void register(Intent intent) {
        String username = this.mValidator.extractAndValidateUsername(intent);
        String nickname = this.mValidator.extractAndValidateNickname(intent);
        String password = this.mValidator.extractAndValidatePassword(intent);
        String authCode = this.getAuthCode(username, password);

        if (username != null && nickname != null && password != null) {
            this.registerHttpRequest(username, authCode, nickname);
        }
    }

    private void logout() {
        this.mSessionKey = null;
        this.mNickname = null;

        this.updateLocalStorageSessionKey(null);
        this.logoutHttpRequest(this.mSessionKey);
    }

    private void initSessionKeyHttpRequest(String sessionKeyInput) {
        if (this.mIsConnectingActive) {
            this.mBroadcastManager.sendErrorMessage(USER_SERVICE_NOT_AVAILABLE_ERROR_MESSAGE);
            return;
        }

        this.mIsConnectingActive = true;
        final String sessionKey = sessionKeyInput;

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                UserService.this.mBroadcastManager.sendConnecting();
                HttpResponse response =
                        UserService.this.mHttpRequester.get("users/validate?sessionKey=", sessionKey);

                if (response.isStatusOk()) {
                    UserService.this.mBroadcastManager.sendIsConnected();
                } else {
                    UserService.this.mBroadcastManager.sendErrorMessage("Please login or register");
                }

                UserService.this.mIsConnectingActive = false;
            }
        });
    }

    private void loginHttpRequest(String username, String authCode) {
        if (this.mIsConnectingActive) {
            this.mBroadcastManager.sendErrorMessage(USER_SERVICE_NOT_AVAILABLE_ERROR_MESSAGE);
            return;
        }

        this.mIsConnectingActive = true;
        final Gson gson = this.mGson;
        final UserModel userModel = new UserModel(username, authCode);

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                UserService.this.mBroadcastManager.sendConnecting();
                String userModelAsJson = gson.toJson(userModel);
                HttpResponse response =
                        UserService.this.mHttpRequester.post("users/login", userModelAsJson, null);

                if (response.isStatusOk()) {
                    UserService.this.mBroadcastManager.sendIsConnected();
                    UserService.this.saveUserData(response);
                } else {
                    UserService.this.mBroadcastManager.sendErrorMessage("Invalid username or password");
                }

                UserService.this.mIsConnectingActive = false;
            }
        });
    }

    private void registerHttpRequest(String username, String authCode, String nickname) {
        if (this.mIsConnectingActive) {
            this.mBroadcastManager.sendErrorMessage(USER_SERVICE_NOT_AVAILABLE_ERROR_MESSAGE);
            return;
        }

        this.mIsConnectingActive = true;
        final Gson gson = this.mGson;
        final UserModel userModel = new UserModel(username, authCode, nickname);

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                UserService.this.mBroadcastManager.sendConnecting();
                String userModelAsJson = gson.toJson(userModel);
                HttpResponse response =
                        UserService.this.mHttpRequester.post("users/register", userModelAsJson, null);

                if (response.isStatusOk()) {
                    UserService.this.mBroadcastManager.sendIsConnected();
                    UserService.this.saveUserData(response);
                } else {
                    UserService.this.mBroadcastManager.sendErrorMessage("Invalid username or password");
                }

                UserService.this.mIsConnectingActive = false;
            }
        });
    }

    private void logoutHttpRequest(String sessionKeyInput) {
        final String sessionKey = sessionKeyInput;

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                UserService.this.mBroadcastManager.sendConnecting();
                UserService.this.mHttpRequester.get("users/logout?sessionKey=", sessionKey);
            }
        });
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

    private void saveUserData(HttpResponse response) {
        UserModel userModel = this.mGson.fromJson(response.getMessage(), UserModel.class);
        this.mNickname = userModel.getNickname();
        this.mSessionKey = userModel.getSessionKey();
        this.updateLocalStorageSessionKey(this.mSessionKey);
    }

    private void updateLocalStorageSessionKey(String sessionKey) {
        SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, 0);
        SharedPreferences.Editor editor = userStorage.edit();
        editor.putString(USER_STORAGE_SESSION_KEY, sessionKey);
        editor.commit();
    }
}
