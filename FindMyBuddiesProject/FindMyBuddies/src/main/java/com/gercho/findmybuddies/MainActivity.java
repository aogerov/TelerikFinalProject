package com.gercho.findmybuddies;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gercho.findmybuddies.helpers.ProgressBarHelper;
import com.gercho.findmybuddies.helpers.ThreadSleeper;
import com.gercho.findmybuddies.helpers.ToastHelper;
import com.gercho.findmybuddies.services.UserService;
import com.gercho.findmybuddies.services.UserServiceBinder;

public class MainActivity extends Activity {

    private static final String IS_CONNECTING_ACTIVE = "IsConnectingActive";
    private static final int THREAD_SLEEP_TIME = 100;
    private static final int MAX_SERVICE_CALLBACKS = 300;

    private UserService mUserService;
    private boolean mIsBoundToUserService;
    private ServiceConnection mUserServiceConnection;

    private ProgressBarHelper mProgressBarHelper;
    private boolean mIsConnectingActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar_main);
        this.mProgressBarHelper = new ProgressBarHelper(this, progressBar);

        if (savedInstanceState != null) {
            this.mIsConnectingActive = savedInstanceState.getBoolean(IS_CONNECTING_ACTIVE, false);
        }

        this.startUserService();
        this.connectToUserService();
        this.setupButtons();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent userServiceIntent = new Intent(this, UserService.class);
        this.bindService(userServiceIntent, this.mUserServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (this.mIsConnectingActive) {
            this.mProgressBarHelper.startProgressBar();
            this.connectToServer();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (this.mIsConnectingActive) {
            this.mProgressBarHelper.stopProgressBar();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (this.mIsBoundToUserService) {
            this.unbindService(this.mUserServiceConnection);
            this.mIsBoundToUserService = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_CONNECTING_ACTIVE, this.mIsConnectingActive);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void startUserService() {
        Intent userServiceIntent = new Intent();
        userServiceIntent.setAction(UserService.START_USER_SERVICE);
        this.startService(userServiceIntent);
    }

    private void connectToUserService() {
        this.mUserServiceConnection = new ServiceConnection() {

            @Override
            public void onServiceConnected(ComponentName className, IBinder service) {
                UserServiceBinder binder = (UserServiceBinder) service;
                MainActivity.this.mUserService = binder.getService();
                MainActivity.this.mIsBoundToUserService = true;
            }

            @Override
            public void onServiceDisconnected(ComponentName arg0) {
                MainActivity.this.mIsBoundToUserService = false;
            }
        };
    }

    private void setupButtons() {
        this.findViewById(R.id.button_login).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.handleLogin();
            }
        });

        this.findViewById(R.id.button_register).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.handleRegister();
            }
        });

        this.findViewById(R.id.button_switch).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.handleSwitchLoginRegister((Button) view);
            }
        });
    }

    private void handleLogin() {
        if (!this.mIsConnectingActive) {
            this.setConnectingStart();

            Intent loginServiceIntent = new Intent();
            loginServiceIntent.setAction(UserService.LOGIN_USER_SERVICE);
            String username = this.getTextFromTextView(R.id.editText_username);
            loginServiceIntent.putExtra(UserService.USERNAME, username);
            String password = this.getTextFromTextView(R.id.editText_password);
            loginServiceIntent.putExtra(UserService.PASSWORD, password);
            this.startService(loginServiceIntent);

            this.connectToServer();
        }
    }

    private void handleRegister() {
        if (!this.mIsConnectingActive) {
            this.setConnectingStart();

            Intent loginServiceIntent = new Intent();
            loginServiceIntent.setAction(UserService.REGISTER_USER_SERVICE);
            String username = this.getTextFromTextView(R.id.editText_username);
            loginServiceIntent.putExtra(UserService.USERNAME, username);
            String password = this.getTextFromTextView(R.id.editText_password);
            loginServiceIntent.putExtra(UserService.PASSWORD, password);
            String nickname = this.getTextFromTextView(R.id.editText_nickname);
            loginServiceIntent.putExtra(UserService.NICKNAME, nickname);
            this.startService(loginServiceIntent);

            this.connectToServer();
        }
    }

    private void handleSwitchLoginRegister(Button button) {
        String buttonText = button.getText().toString();
        String loginText = this.getString(R.string.button_login);
        String registerText = this.getString(R.string.button_register);

        if (buttonText.equals(loginText)) {
            button.setText(R.string.button_register);
            Button loginButton = (Button) this.findViewById(R.id.button_login);
            loginButton.setVisibility(View.VISIBLE);
            Button registerButton = (Button) this.findViewById(R.id.button_register);
            registerButton.setVisibility(View.INVISIBLE);
            EditText nicknameEditText = (EditText) this.findViewById(R.id.editText_nickname);
            nicknameEditText.setVisibility(View.INVISIBLE);
        }

        if (buttonText.equals(registerText)) {
            button.setText(R.string.button_login);
            Button loginButton = (Button) this.findViewById(R.id.button_login);
            loginButton.setVisibility(View.INVISIBLE);
            Button registerButton = (Button) this.findViewById(R.id.button_register);
            registerButton.setVisibility(View.VISIBLE);
            EditText nicknameEditText = (EditText) this.findViewById(R.id.editText_nickname);
            nicknameEditText.setVisibility(View.VISIBLE);
        }
    }

    private void connectToServer() {
        HandlerThread handlerThread = new HandlerThread("ConnectToServerThread");
        handlerThread.start();
        Handler userHandler = new Handler(handlerThread.getLooper());
        userHandler.post(new Runnable() {
            @Override
            public void run() {
                for (int i = 0; i < MAX_SERVICE_CALLBACKS; i++) {
                    if (MainActivity.this.mUserService.getIsUserLoggedIn()) {
                        break;
                    } else {
                        ThreadSleeper.sleep(THREAD_SLEEP_TIME);
                    }
                }

                if (!MainActivity.this.mIsConnectingActive) {
                    return;
                }

                MainActivity.this.setConnectingStop();
                if (MainActivity.this.mUserService.getIsUserLoggedIn()) {
                    ToastHelper.makeToast(MainActivity.this, "Connected successfully");
                } else {
                    ToastHelper.makeToast(MainActivity.this, "Connecting failed");
                }
            }
        });
    }

    private String getTextFromTextView(int id) {
        TextView textView = (TextView) this.findViewById(id);
        CharSequence text = textView.getText();
        if (text != null) {
            return text.toString();
        }

        return null;
    }

    private void setConnectingStart() {
        this.mProgressBarHelper.startProgressBar();
        this.mIsConnectingActive = true;
    }

    private void setConnectingStop() {
        this.mProgressBarHelper.stopProgressBar();
        this.mIsConnectingActive = false;
    }
}
