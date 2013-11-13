package com.gercho.findmybuddies.services;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.os.Looper;

import com.gercho.findmybuddies.broadcasts.UserServiceBroadcast;
import com.gercho.findmybuddies.helpers.AuthCodeGenerator;
import com.gercho.findmybuddies.helpers.Encryptor;
import com.gercho.findmybuddies.http.HttpRequester;
import com.gercho.findmybuddies.http.HttpResponse;
import com.gercho.findmybuddies.models.UserModel;
import com.gercho.findmybuddies.validators.UserServiceValidator;
import com.google.gson.Gson;

/**
 * Created by Gercho on 11/7/13.
 */
public class UserService extends Service {

    public static final String START_USER_SERVICE = "com.gercho.action.START_USER_SERVICE";
    public static final String STOP_USER_SERVICE = "com.gercho.action.STOP_USER_SERVICE";
    public static final String LOGIN = "com.gercho.action.LOGIN";
    public static final String REGISTER = "com.gercho.action.REGISTER";
    public static final String LOGOUT = "com.gercho.action.LOGOUT";
    public static final String START_ADDITIONAL_SERVICES = "com.gercho.action.START_ADDITIONAL_SERVICES";

    public static final String USER_SERVICE_BROADCAST = "UserServiceBroadcast";
    public static final String CONNECTING_EXTRA = "ConnectingExtra";
    public static final String IS_CONNECTED_EXTRA = "IsConnectedExtra";
    public static final String RESPONSE_MESSAGE_EXTRA = "ResponseMessageExtra";
    public static final String MESSAGE_TEXT_EXTRA = "MessageTextExtra";
    public static final String USERNAME_EXTRA = "UsernameExtra";
    public static final String NICKNAME_EXTRA = "NicknameExtra";
    public static final String PASSWORD_EXTRA = "PasswordExtra";
    public static final String SESSION_KEY_EXTRA = "SessionKeyExtra";

    private static final String ERROR_MESSAGE_NOT_AVAILABLE = "Service is currently unavailable, please try again in few seconds";
    private static final String ERROR_MESSAGE_INIT_FAILED = "Please login or register";
    private static final String ERROR_MESSAGE_LOGIN_FAILED = "Invalid username or password";
    private static final String ERROR_MESSAGE_REGISTER_FAILED = "Registration failed, try with another username and/or nickname";

    private static final String USER_STORAGE = "UserStorage";
    private static final String USER_STORAGE_SESSION_KEY_ENCRYPTED = "UserStorageSessionKeyEncrypted";
    private static final String SESSION_KEY_ENCRYPTION = "il6su3df23no3cn8wy4cpt98wtp3ncq3r0";

    private boolean mIsServiceAlreadyStarted;
    private boolean mIsConnectingActive;
    private UserServiceBroadcast mBroadcast;
    private HandlerThread mHandledThread;
    private Handler mHandler;
    private HttpRequester mHttpRequester;
    private Gson mGson;
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
        this.mBroadcast = new UserServiceBroadcast(this);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        String action = intent.getAction();
        if (START_USER_SERVICE.equalsIgnoreCase(action)) {
            this.startUserService();
        } else if (STOP_USER_SERVICE.equalsIgnoreCase(action)) {
            this.stopUserService();
        } else if (LOGIN.equalsIgnoreCase(action)) {
            this.login(intent);
        } else if (REGISTER.equalsIgnoreCase(action)) {
            this.register(intent);
        } else if (LOGOUT.equalsIgnoreCase(action)) {
            this.logout();
        } else if (START_ADDITIONAL_SERVICES.equalsIgnoreCase(action)) {
            this.startAdditionalServices();
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

    private void startUserService() {
        if (!this.mIsServiceAlreadyStarted) {
            this.mIsServiceAlreadyStarted = true;
            this.mIsConnectingActive = false;
            this.readUserStorage();
        } else if (this.mNickname != null && this.mSessionKey != null && this.mSessionKeyEncrypted != null) {
            this.mBroadcast.sendIsConnected(this.mNickname);
        }
    }

    private void stopUserService() {
        this.stopSelf();
    }

    private void login(Intent intent) {
        String username = this.extractAndValidateUsername(intent);
        String password = this.extractAndValidatePassword(intent);
        String authCode = AuthCodeGenerator.getAuthCode(username, password);

        if (username != null && password != null) {
            this.loginHttpRequest(username, authCode);
        }
    }

    private void register(Intent intent) {
        String username = this.extractAndValidateUsername(intent);
        String nickname = this.extractAndValidateNickname(intent);
        String password = this.extractAndValidatePassword(intent);
        String authCode = AuthCodeGenerator.getAuthCode(username, password);

        if (username != null && nickname != null && password != null) {
            this.registerHttpRequest(username, authCode, nickname);
        }
    }

    private void logout() {
        this.logoutHttpRequest(this.mSessionKey);

        this.updateUserStorage(null);
        this.mSessionKey = null;
        this.mNickname = null;
    }

    private void startAdditionalServices() {
        Intent userServiceIntent = new Intent();
        userServiceIntent.setAction(UserService.START_USER_SERVICE);
        userServiceIntent.putExtra(SESSION_KEY_EXTRA, this.mSessionKey);
        this.startService(userServiceIntent);
    }

    private void initSessionKeyHttpRequest(String sessionKey) {
        if (this.mIsConnectingActive) {
            this.mBroadcast.sendResponseMessage(ERROR_MESSAGE_NOT_AVAILABLE);
            return;
        }

        this.mIsConnectingActive = true;
        this.mBroadcast.sendConnecting();
        final String sessionKeyAsString = sessionKey;

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                HttpResponse response = UserService.this.mHttpRequester.get("users/validate?sessionKey=", sessionKeyAsString);
                UserService.this.processHttpResponse(response, ERROR_MESSAGE_INIT_FAILED);
            }
        });
    }

    private void loginHttpRequest(String username, String authCode) {
        if (this.mIsConnectingActive) {
            this.mBroadcast.sendResponseMessage(ERROR_MESSAGE_NOT_AVAILABLE);
            return;
        }

        this.mIsConnectingActive = true;
        this.mBroadcast.sendConnecting();
        UserModel userModel = new UserModel(username, authCode);
        final String userModelAsJson = this.mGson.toJson(userModel);

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                HttpResponse response = UserService.this.mHttpRequester.post("users/login", userModelAsJson, null);
                UserService.this.processHttpResponse(response, ERROR_MESSAGE_LOGIN_FAILED);
            }
        });
    }

    private void registerHttpRequest(String username, String authCode, String nickname) {
        if (this.mIsConnectingActive) {
            this.mBroadcast.sendResponseMessage(ERROR_MESSAGE_NOT_AVAILABLE);
            return;
        }

        this.mIsConnectingActive = true;
        this.mBroadcast.sendConnecting();
        UserModel userModel = new UserModel(username, authCode, nickname);
        final String userModelAsJson = this.mGson.toJson(userModel);

        this.mHandler.post(new Runnable() {
            @Override
            public void run() {
                HttpResponse response = UserService.this.mHttpRequester.post("users/register", userModelAsJson, null);
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

    private void processHttpResponse(HttpResponse response, String errorMessage) {
        boolean isResponseValid = true;
        if (response.isStatusOk()) {
            isResponseValid = this.tryUpdateUserStatus(response);
            if (isResponseValid) {
                this.mBroadcast.sendIsConnected(this.mNickname);
            }
        }

        if (!response.isStatusOk() || !isResponseValid) {
            this.mBroadcast.sendResponseMessage(errorMessage);
        }

        this.mIsConnectingActive = false;
    }

    private boolean tryUpdateUserStatus(HttpResponse response) {
        boolean isResponseValid = UserServiceValidator.validateHttpResponse(response);
        if (!isResponseValid) {
            return false;
        }

        UserModel userModel = this.mGson.fromJson(response.getMessage(), UserModel.class);
        boolean isModelValid = UserServiceValidator.validateUserModel(userModel);
        if (!isModelValid) {
            return false;
        }

        this.mNickname = userModel.getNickname();
        this.mSessionKey = userModel.getSessionKey();
        this.mSessionKeyEncrypted = Encryptor.encrypt(this.mSessionKey, SESSION_KEY_ENCRYPTION);
        this.updateUserStorage(this.mSessionKeyEncrypted);
        return true;
    }

    private String extractAndValidateUsername(Intent intent) {
        String username = intent.getStringExtra(UserService.USERNAME_EXTRA);
        boolean isUsernameValid = UserServiceValidator.validateUsername(username);
        if (isUsernameValid && username != null) {
            return username.trim();
        } else {
            this.mBroadcast.sendResponseMessage(
                    String.format("Username must be min %d and max %d chars long",
                            UserServiceValidator.MIN_USERNAME_AND_NICKNAME_LENGTH,
                            UserServiceValidator.MAX_INPUT_FIELDS_LENGTH));
            return null;
        }
    }

    private String extractAndValidateNickname(Intent intent) {
        String nickname = intent.getStringExtra(UserService.NICKNAME_EXTRA);
        boolean isNicknameValid = UserServiceValidator.validateNickname(nickname);
        if (isNicknameValid && nickname != null) {
            return nickname.trim();
        } else {
            this.mBroadcast.sendResponseMessage(
                    String.format("Nickname must be min %d and max %d chars long",
                            UserServiceValidator.MIN_USERNAME_AND_NICKNAME_LENGTH,
                            UserServiceValidator.MAX_INPUT_FIELDS_LENGTH));
            return null;
        }
    }

    private String extractAndValidatePassword(Intent intent) {
        String password = intent.getStringExtra(UserService.PASSWORD_EXTRA);
        boolean isPasswordValid = UserServiceValidator.validatePassword(password);
        if (isPasswordValid && password != null) {
            return password.trim();
        } else {
            this.mBroadcast.sendResponseMessage(
                    String.format("Password must be min %d and max %d chars long",
                            UserServiceValidator.MIN_PASSWORD_LENGTH,
                            UserServiceValidator.MAX_INPUT_FIELDS_LENGTH));
            return null;
        }
    }

    private void readUserStorage() {
        SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, MODE_PRIVATE);

        String sessionKeyEncrypted = userStorage.getString(USER_STORAGE_SESSION_KEY_ENCRYPTED, null);
        if (sessionKeyEncrypted != null) {
            String sessionKey = Encryptor.decrypt(sessionKeyEncrypted, SESSION_KEY_ENCRYPTION);
            this.initSessionKeyHttpRequest(sessionKey);
        }
    }

    private void updateUserStorage(String sessionKeyEncrypted) {
        SharedPreferences userStorage = this.getSharedPreferences(USER_STORAGE, MODE_PRIVATE);
        SharedPreferences.Editor editor = userStorage.edit();
        editor.putString(USER_STORAGE_SESSION_KEY_ENCRYPTED, sessionKeyEncrypted);
        editor.commit();
    }
}