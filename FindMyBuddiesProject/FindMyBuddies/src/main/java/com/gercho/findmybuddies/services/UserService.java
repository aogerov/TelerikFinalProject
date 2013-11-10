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

    public static final String USER_SERVICE_BROADCAST = "UserServiceBroadcast";
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
    private static final int MIN_USERNAME_AND_NICKNAME_LENGTH = 3;
    private static final int MIN_PASSWORD_LENGTH = 6;
    private static final int MAX_INPUT_FIELDS_LENGTH = 30;

    private boolean mIsServiceInitialized;
    private boolean mIsConnectingActive;
    private HandlerThread mHandledThread;
    private Handler mHandler;
    private HttpRequester mHttpRequester;
    private Gson mGson;

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
//        if (!this.mIsServiceInitialized) {
//            this.mIsServiceInitialized = true;
//            this.mIsConnectingActive = false;
//
//            SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, 0);
//            this.mSessionKey = userStorage.getString(USER_STORAGE_SESSION_KEY, null);
//
//            if (this.mSessionKey != null) {
//                this.sendConnectingBroadast();
//                this.initSessionKeyHttpRequest(this.mSessionKey);
//            }
//        }

        // remove this shit, its just for testing
        this.loginHttpRequest("gercho", "63e58d4a85f451b10dc26dddb5a78e7e1728edb0");
    }

    private void login(Intent intent) {
        String username = this.extractAndValidateUsername(intent);
        String password = this.extractAndValidatePassword(intent);
        String authCode = this.getAuthCode(username, password);

        if (username != null && password != null) {
            this.sendConnectingBroadast();
            this.loginHttpRequest(username, authCode);
        }
    }

    private void register(Intent intent) {
        String username = this.extractAndValidateUsername(intent);
        String nickname = this.extractAndValidateNickname(intent);
        String password = this.extractAndValidatePassword(intent);
        String authCode = this.getAuthCode(username, password);

        if (username != null && nickname != null && password != null) {
            this.sendConnectingBroadast();
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
            this.sendErrorMessageBroadcast(USER_SERVICE_NOT_AVAILABLE_ERROR_MESSAGE);
            return;
        }

        this.mIsConnectingActive = true;
        final String sessionKey = sessionKeyInput;

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                UserService.this.sendConnectingBroadast();
                HttpResponse response =
                        UserService.this.mHttpRequester.get("users/validate?sessionKey=", sessionKey);

                if (response.isStatusOk()) {
                    UserService.this.sendIsConnectedBroadast();
                } else {
                    UserService.this.sendErrorMessageBroadcast("Please login or register");
                }
            }
        });
    }

    private void loginHttpRequest(String username, String authCode) {
        if (this.mIsConnectingActive) {
            this.sendErrorMessageBroadcast(USER_SERVICE_NOT_AVAILABLE_ERROR_MESSAGE);
            return;
        }

        this.mIsConnectingActive = true;
        UserModel userModel = new UserModel(username, authCode, null);
        final String json = this.mGson.toJson(userModel);

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                UserService.this.sendConnectingBroadast();
                HttpResponse response =
                        UserService.this.mHttpRequester.post("users/login", json, null);

                if (response.isStatusOk()) {
                    UserService.this.sendIsConnectedBroadast();
                    // TODO parse data from server
                } else {
                    UserService.this.sendErrorMessageBroadcast("Invalid username or password");
                }
            }
        });
    }

    private void registerHttpRequest(String usernameInput, String authCodeInput, String nicknameInput) {
        if (this.mIsConnectingActive) {
            this.sendErrorMessageBroadcast(USER_SERVICE_NOT_AVAILABLE_ERROR_MESSAGE);
            return;
        }

        this.mIsConnectingActive = true;
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
    }

    private void logoutHttpRequest(String sessionKeyInput) {
        this.mHandler.post(new Runnable() {
            @Override
            public void run() {

            }
        });
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

        this.sendErrorMessageBroadcast(
                String.format("Username must be min %d and max %d chars long",
                        MIN_USERNAME_AND_NICKNAME_LENGTH, MAX_INPUT_FIELDS_LENGTH));
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

        this.sendErrorMessageBroadcast(
                String.format("Nickname must be min %d and max %d chars long",
                        MIN_USERNAME_AND_NICKNAME_LENGTH, MAX_INPUT_FIELDS_LENGTH));
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

        this.sendErrorMessageBroadcast(
                String.format("Password must be min %d and max %d chars long",
                        MIN_PASSWORD_LENGTH, MAX_INPUT_FIELDS_LENGTH));
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

    private void updateLocalStorageSessionKey(String sessionKey) {
        SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, 0);
        SharedPreferences.Editor editor = userStorage.edit();
        editor.putString(USER_STORAGE_SESSION_KEY, sessionKey);
        editor.commit();
    }

    private void sendConnectingBroadast() {
        Intent intent = new Intent(USER_SERVICE_BROADCAST);
        intent.putExtra(USER_SERVICE_CONNECTING, true);
        this.sendBroadcast(intent);
    }

    private void sendIsConnectedBroadast() {
        Intent intent = new Intent(USER_SERVICE_BROADCAST);
        intent.putExtra(USER_SERVICE_IS_CONNECTED, true);
        this.sendBroadcast(intent);
    }

    private void sendErrorMessageBroadcast(String message) {
        Intent intent = new Intent(USER_SERVICE_BROADCAST);
        intent.putExtra(USER_SERVICE_ERROR, true);
        intent.putExtra(USER_SERVICE_MESSAGE, message);
        this.sendBroadcast(intent);
    }
}
