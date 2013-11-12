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
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gercho.findmybuddies.helpers.ProgressBarController;
import com.gercho.findmybuddies.helpers.ToastNotifier;
import com.gercho.findmybuddies.services.UserService;

public class MainActivity extends Activity {

    private static final String CONNECTING_STATUS = "ConnectingStatus";
    private static final String REGISTER_WINDOW_VISIBILITY = "RegisterWindowVisibility";

    private UserServiceUpdateReceiver mUserServiceUpdateReceiver;
    private ProgressBarController mProgressBarController;
    private boolean mIsRegisterWindowVisible;
    private boolean mIsConnectingActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar_main);
        this.mProgressBarController = new ProgressBarController(this, progressBar);

        if (savedInstanceState != null) {
            this.mIsRegisterWindowVisible = savedInstanceState.getBoolean(REGISTER_WINDOW_VISIBILITY, true);
            this.mIsConnectingActive = savedInstanceState.getBoolean(CONNECTING_STATUS, false);
        }

        this.setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.mIsConnectingActive) {
            this.startProgressBar();
            this.hideUi();
        } else {
            this.showUi();
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
            this.stopProgressBar();
        }

        if (this.mUserServiceUpdateReceiver != null) {
            this.unregisterReceiver(this.mUserServiceUpdateReceiver);
            this.mUserServiceUpdateReceiver = null;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putBoolean(REGISTER_WINDOW_VISIBILITY, this.mIsRegisterWindowVisible);
        outState.putBoolean(CONNECTING_STATUS, this.mIsConnectingActive);
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
                MainActivity.this.handleSwitchLoginRegister();
            }
        });
    }

    private void handleLogin() {
        Intent loginServiceIntent = new Intent();
        loginServiceIntent.setAction(UserService.LOGIN);
        String username = this.getTextFromTextView(R.id.editText_username);
        loginServiceIntent.putExtra(UserService.USERNAME_EXTRA, username);
        String password = this.getTextFromTextView(R.id.editText_password);
        loginServiceIntent.putExtra(UserService.PASSWORD_EXTRA, password);
        this.startService(loginServiceIntent);
    }

    private void handleRegister() {
        Intent registerServiceIntent = new Intent();
        registerServiceIntent.setAction(UserService.REGISTER);
        String username = this.getTextFromTextView(R.id.editText_username);
        registerServiceIntent.putExtra(UserService.USERNAME_EXTRA, username);
        String password = this.getTextFromTextView(R.id.editText_password);
        registerServiceIntent.putExtra(UserService.PASSWORD_EXTRA, password);
        String nickname = this.getTextFromTextView(R.id.editText_nickname);
        registerServiceIntent.putExtra(UserService.NICKNAME_EXTRA, nickname);
        this.startService(registerServiceIntent);
    }

    private void handleSwitchLoginRegister() {
        this.mIsRegisterWindowVisible = !this.mIsRegisterWindowVisible;
        this.showUi();
    }

    private String getTextFromTextView(int id) {
        TextView textView = (TextView) this.findViewById(id);
        CharSequence text = textView.getText();
        if (text != null) {
            return text.toString();
        }

        return null;
    }

    private void setConnectingActive() {
        this.mIsConnectingActive = true;
    }

    private void setConnectingInactive() {
        this.mIsConnectingActive = false;
    }

    private void startProgressBar() {
        this.mProgressBarController.startProgressBar();
    }

    private void stopProgressBar() {
        this.mProgressBarController.stopProgressBar();
    }

    private void changeActiveToastMessage(String message) {
        this.mProgressBarController.changeActiveToastMessage(message);
        ToastNotifier.makeToast(this, message);
    }

    private void showUi() {
        Button switchButton = (Button) this.findViewById(R.id.button_switch);
        switchButton.setVisibility(View.VISIBLE);
        this.findViewById(R.id.editText_username).setVisibility(View.VISIBLE);
        this.findViewById(R.id.editText_password).setVisibility(View.VISIBLE);

        if (this.mIsRegisterWindowVisible) {
            switchButton.setText(this.getString(R.string.button_login));
            this.findViewById(R.id.button_login).setVisibility(View.INVISIBLE);
            this.findViewById(R.id.button_register).setVisibility(View.VISIBLE);
            this.findViewById(R.id.editText_nickname).setVisibility(View.VISIBLE);
        } else {
            switchButton.setText(this.getString(R.string.button_register));
            this.findViewById(R.id.button_login).setVisibility(View.VISIBLE);
            this.findViewById(R.id.button_register).setVisibility(View.INVISIBLE);
            this.findViewById(R.id.editText_nickname).setVisibility(View.INVISIBLE);
        }
    }

    private void hideUi() {
        this.findViewById(R.id.button_login).setVisibility(View.INVISIBLE);
        this.findViewById(R.id.button_register).setVisibility(View.INVISIBLE);
        this.findViewById(R.id.button_switch).setVisibility(View.INVISIBLE);
        this.findViewById(R.id.editText_username).setVisibility(View.INVISIBLE);
        this.findViewById(R.id.editText_password).setVisibility(View.INVISIBLE);
        this.findViewById(R.id.editText_nickname).setVisibility(View.INVISIBLE);
    }

    private class UserServiceUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(UserService.USER_SERVICE_BROADCAST)) {
                boolean isConnectingActive = intent.getBooleanExtra(UserService.CONNECTING_EXTRA, false);
                boolean isResponseMessageReceived = intent.getBooleanExtra(UserService.RESPONSE_MESSAGE_EXTRA, false);
                boolean isConnected = intent.getBooleanExtra(UserService.IS_CONNECTED_EXTRA, false);

                if (isConnectingActive) {
                    this.handleConnecting();
                } else if (isResponseMessageReceived) {
                    this.handleResponseMessage(intent);
                } else if (isConnected) {
                    this.handleConnected(intent);
                }
            }
        }

        private void handleConnecting() {
            MainActivity.this.setConnectingActive();
            MainActivity.this.startProgressBar();
            MainActivity.this.hideUi();
        }

        private void handleResponseMessage(Intent intent) {
            MainActivity.this.setConnectingInactive();
            MainActivity.this.stopProgressBar();
            MainActivity.this.showUi();

            String message = intent.getStringExtra(UserService.MESSAGE_TEXT_EXTRA);
            MainActivity.this.changeActiveToastMessage(message);
        }

        private void handleConnected(Intent intent) {
            MainActivity.this.setConnectingInactive();
            MainActivity.this.stopProgressBar();

            String nickname = intent.getStringExtra(UserService.NICKNAME_EXTRA);
            MainActivity.this.changeActiveToastMessage("Welcome " + nickname);

            this.startAdditionalServices();

            Intent buddiesIntent = new Intent(MainActivity.this, FindMyBuddiesActivity.class);
            buddiesIntent.putExtra(UserService.NICKNAME_EXTRA, nickname);
            MainActivity.this.startActivity(buddiesIntent);
        }

        private void startAdditionalServices() {
            Intent additionalServicesIntent = new Intent();
            additionalServicesIntent.setAction(UserService.START_ADDITIONAL_SERVICES);
            MainActivity.this.startService(additionalServicesIntent);
        }
    }
}