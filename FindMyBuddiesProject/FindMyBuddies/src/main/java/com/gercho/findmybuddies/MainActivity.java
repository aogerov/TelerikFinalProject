package com.gercho.findmybuddies;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gercho.findmybuddies.helpers.ProgressBarController;
import com.gercho.findmybuddies.helpers.ToastNotifier;
import com.gercho.findmybuddies.services.UserService;

public class MainActivity extends Activity {

    private static final String IS_CONNECTING_ACTIVE = "IsConnectingActive";

    private UserServiceUpdateReceiver mUserServiceUpdateReceiver;
    private ProgressBarController mProgressBarController;
    private boolean mIsConnectingActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar_main);
        this.mProgressBarController = new ProgressBarController(this, progressBar);

        if (savedInstanceState != null) {
            this.mIsConnectingActive = savedInstanceState.getBoolean(IS_CONNECTING_ACTIVE, false);
        }

        this.setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.mIsConnectingActive) {
            this.mProgressBarController.startProgressBar();
        }

        if (this.mUserServiceUpdateReceiver == null) {
            this.mUserServiceUpdateReceiver = new UserServiceUpdateReceiver();
            IntentFilter intentFilter = new IntentFilter(UserService.USER_SERVICE_BROADCAST);
            this.registerReceiver(this.mUserServiceUpdateReceiver, intentFilter);

            Intent userServiceIntent = new Intent();
            userServiceIntent.setAction(UserService.START_USER_SERVICE);
            this.startService(userServiceIntent);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.mIsConnectingActive) {
            this.mProgressBarController.stopProgressBar();
        }

        if (this.mUserServiceUpdateReceiver != null) {
            this.unregisterReceiver(this.mUserServiceUpdateReceiver);
            this.mUserServiceUpdateReceiver = null;
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
            Intent loginServiceIntent = new Intent();
            loginServiceIntent.setAction(UserService.LOGIN_USER_SERVICE);
            String username = this.getTextFromTextView(R.id.editText_username);
            loginServiceIntent.putExtra(UserService.USERNAME, username);
            String password = this.getTextFromTextView(R.id.editText_password);
            loginServiceIntent.putExtra(UserService.PASSWORD, password);
            this.startService(loginServiceIntent);
        }
    }

    private void handleRegister() {
        if (!this.mIsConnectingActive) {
            Intent registerServiceIntent = new Intent();
            registerServiceIntent.setAction(UserService.REGISTER_USER_SERVICE);
            String username = this.getTextFromTextView(R.id.editText_username);
            registerServiceIntent.putExtra(UserService.USERNAME, username);
            String password = this.getTextFromTextView(R.id.editText_password);
            registerServiceIntent.putExtra(UserService.PASSWORD, password);
            String nickname = this.getTextFromTextView(R.id.editText_nickname);
            registerServiceIntent.putExtra(UserService.NICKNAME, nickname);
            this.startService(registerServiceIntent);
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

    private String getTextFromTextView(int id) {
        TextView textView = (TextView) this.findViewById(id);
        CharSequence text = textView.getText();
        if (text != null) {
            return text.toString();
        }

        return null;
    }

    private void startConnectMessaging() {
        this.mProgressBarController.startProgressBar();
        this.mIsConnectingActive = true;
    }

    private void stopConnectMessaging() {
        this.mProgressBarController.stopProgressBar();
        this.mIsConnectingActive = false;
    }

    private class UserServiceUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(UserService.USER_SERVICE_BROADCAST)) {
                boolean isConnectingActive = intent.getBooleanExtra(UserService.USER_SERVICE_CONNECTING, false);
                boolean isConnected = intent.getBooleanExtra(UserService.USER_SERVICE_IS_CONNECTED, false);
                boolean isResponseMessageReceived = intent.getBooleanExtra(UserService.USER_SERVICE_RESPONSE_MESSAGE, false);

                if (isConnectingActive) {
                    MainActivity.this.startConnectMessaging();
                }else if (isConnected) {
                    MainActivity.this.stopConnectMessaging();
                    Intent buddiesIntent = new Intent(MainActivity.this, BuddiesActivity.class);
                    MainActivity.this.startActivity(buddiesIntent);
                }else if (isResponseMessageReceived) {
                    MainActivity.this.stopConnectMessaging();
                    String message = intent.getStringExtra(UserService.USER_SERVICE_MESSAGE_TEXT);
                    MainActivity.this.mProgressBarController.changeActiveToastMessage(message);
                    ToastNotifier.makeToast(MainActivity.this, message);
                }
            }
        }
    }
}
