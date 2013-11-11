package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.gercho.findmybuddies.helpers.Encryptor;
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
    public static final String USER_SERVICE_RESPONSE_MESSAGE = "UserServiceResponseMessage";
    public static final String USER_SERVICE_MESSAGE_TEXT = "UserServiceMessageText";
    public static final String ERROR_MESSAGE_NOT_AVAILABLE = "Service is currently unavailable, please try again in few seconds";
    public static final String ERROR_MESSAGE_INIT_FAILED = "Please login or register";
    public static final String ERROR_MESSAGE_LOGIN_FAILED = "Invalid username or password";
    public static final String ERROR_MESSAGE_REGISTER_FAILED = "Registration failed, try with another username and/or nickname";
    public static final String SESSION_KEY_ENCRYPTED = "SessionKeyEncrypted";
    public static final String SESSION_KEY = "SessionKey";
    public static final String USERNAME = "Username";
    public static final String NICKNAME = "Nickname";
    public static final String PASSWORD = "Password";

    private static final String USER_STORAGE = "UserStorage";
    private static final String USER_STORAGE_SESSION_KEY_ENCRYPTED = "UserStorageSessionKeyEncrypted";
    private static final String SESSION_KEY_ENCRYPTION = "il6su3df23no3cn8wy4cpt98wtp3ncq3r0";

    private boolean mIsServiceInitialized;
    private boolean mIsConnectingActive;
    private HandlerThread mHandledThread;
    private Handler mHandler;
    private HttpRequester mHttpRequester;
    private Gson mGson;
    private UserServiceBroadcastManager mBroadcastManager;
    private UserServiceValidator mValidator;
    private String mSessionKey;
    private String mSessionKeyEncrypted;
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
            this.mSessionKeyEncrypted = userStorage.getString(USER_STORAGE_SESSION_KEY_ENCRYPTED, null);

            if (this.mSessionKeyEncrypted != null) {
                this.mSessionKey = Encryptor.decrypt(this.mSessionKeyEncrypted, SESSION_KEY_ENCRYPTION);
                this.initSessionKeyHttpRequest(this.mSessionKey);
            }
        }

        if (this.mSessionKey != null && this.mNickname != null && this.mSessionKeyEncrypted != null) {
            this.mBroadcastManager.sendIsConnected(this.mNickname, this.mSessionKeyEncrypted);
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
        this.logoutHttpRequest(this.mSessionKey);

        this.updateLocalStorageSessionKeyEncrypted(null);
        this.mSessionKey = null;
        this.mNickname = null;
    }

    private void initSessionKeyHttpRequest(String sessionKey) {
        if (this.mIsConnectingActive) {
            this.mBroadcastManager.sendResponseMessage(ERROR_MESSAGE_NOT_AVAILABLE);
            return;
        }

        this.mIsConnectingActive = true;
        this.mBroadcastManager.sendConnecting();
        final String sessionKeyAsString = sessionKey;

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                HttpResponse response =
                        UserService.this.mHttpRequester.get("users/validate?sessionKey=", sessionKeyAsString);

                UserService.this.processHttpResponse(response, ERROR_MESSAGE_INIT_FAILED);
            }
        });
    }

    private void loginHttpRequest(String username, String authCode) {
        if (this.mIsConnectingActive) {
            this.mBroadcastManager.sendResponseMessage(ERROR_MESSAGE_NOT_AVAILABLE);
            return;
        }

        this.mIsConnectingActive = true;
        this.mBroadcastManager.sendConnecting();
        UserModel userModel = new UserModel(username, authCode);
        final String userModelAsJson = this.mGson.toJson(userModel);

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                HttpResponse response =
                        UserService.this.mHttpRequester.post("users/login", userModelAsJson, null);

                UserService.this.processHttpResponse(response, ERROR_MESSAGE_LOGIN_FAILED);
            }
        });
    }

    private void registerHttpRequest(String username, String authCode, String nickname) {
        if (this.mIsConnectingActive) {
            this.mBroadcastManager.sendResponseMessage(ERROR_MESSAGE_NOT_AVAILABLE);
            return;
        }

        this.mIsConnectingActive = true;
        this.mBroadcastManager.sendConnecting();
        UserModel userModel = new UserModel(username, authCode, nickname);
        final String userModelAsJson = this.mGson.toJson(userModel);

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                HttpResponse response =
                        UserService.this.mHttpRequester.post("users/register", userModelAsJson, null);

                UserService.this.processHttpResponse(response, ERROR_MESSAGE_REGISTER_FAILED);
            }
        });
    }

    private void logoutHttpRequest(String sessionKey) {
        final String sessionKeyAsString = sessionKey;

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                UserService.this.mHttpRequester.get("users/logout?sessionKey=", sessionKeyAsString);
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

    private void processHttpResponse(HttpResponse response, String errorMessage) {
        boolean isResponseValid = true;
        if (response.isStatusOk()) {
            isResponseValid = this.tryUpdateUserStatus(response);
            if (isResponseValid) {
                this.mBroadcastManager.sendIsConnected(this.mNickname, this.mSessionKeyEncrypted);
            }
        }

        if (!response.isStatusOk() || !isResponseValid){
            this.mBroadcastManager.sendResponseMessage(errorMessage);
        }

        this.mIsConnectingActive = false;
    }

    private boolean tryUpdateUserStatus(HttpResponse response) {
        boolean isResponseValid = this.mValidator.validateHttpResponse(response);
        if (!isResponseValid) {
            return false;
        }

        UserModel userModel = this.mGson.fromJson(response.getMessage(), UserModel.class);
        boolean isModelValid = this.mValidator.validateUserModel(userModel);
        if (!isModelValid) {
            return false;
        }

        this.mNickname = userModel.getNickname();
        this.mSessionKey = userModel.getSessionKey();
        this.mSessionKeyEncrypted = Encryptor.encrypt(this.mSessionKey, SESSION_KEY_ENCRYPTION);
        this.updateLocalStorageSessionKeyEncrypted(this.mSessionKeyEncrypted);
        return true;
    }

    private void updateLocalStorageSessionKeyEncrypted(String sessionKeyEncrypted) {
        SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, 0);
        SharedPreferences.Editor editor = userStorage.edit();
        editor.putString(USER_STORAGE_SESSION_KEY_ENCRYPTED, sessionKeyEncrypted);
        editor.commit();
    }
}
