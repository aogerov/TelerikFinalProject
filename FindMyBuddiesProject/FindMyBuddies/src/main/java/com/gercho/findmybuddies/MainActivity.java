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

import com.gercho.findmybuddies.helpers.ProgressBarHelper;
import com.gercho.findmybuddies.helpers.ToastHelper;
import com.gercho.findmybuddies.services.UserService;

public class MainActivity extends Activity {

    private static final String IS_CONNECTING_ACTIVE = "IsConnectingActive";

    private UserServiceUpdateReceiver mUserServiceUpdateReceiver;
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
        this.setupButtons();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.mIsConnectingActive) {
            this.mProgressBarHelper.startProgressBar();
        }

        if (this.mUserServiceUpdateReceiver == null) {
            this.mUserServiceUpdateReceiver = new UserServiceUpdateReceiver();
        }

        IntentFilter intentFilter = new IntentFilter(UserService.USER_SERVICE_UPDATE);
        this.registerReceiver(this.mUserServiceUpdateReceiver, intentFilter);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.mIsConnectingActive) {
            this.mProgressBarHelper.stopProgressBar();
        }

        if (this.mUserServiceUpdateReceiver != null) {
            this.unregisterReceiver(this.mUserServiceUpdateReceiver);
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
            this.setStartConnecting();
        }
    }

    private void handleRegister() {
        if (!this.mIsConnectingActive) {
            Intent loginServiceIntent = new Intent();
            loginServiceIntent.setAction(UserService.REGISTER_USER_SERVICE);
            String username = this.getTextFromTextView(R.id.editText_username);
            loginServiceIntent.putExtra(UserService.USERNAME, username);
            String password = this.getTextFromTextView(R.id.editText_password);
            loginServiceIntent.putExtra(UserService.PASSWORD, password);
            String nickname = this.getTextFromTextView(R.id.editText_nickname);
            loginServiceIntent.putExtra(UserService.NICKNAME, nickname);
            this.startService(loginServiceIntent);
            this.setStartConnecting();
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

    private void setStartConnecting() {
        this.mProgressBarHelper.startProgressBar();
        this.mIsConnectingActive = true;
    }

    private void setStopConnecting() {
        this.mProgressBarHelper.stopProgressBar();
        this.mIsConnectingActive = false;
    }

    private class UserServiceUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null) {
                if (action.equals(UserService.USER_SERVICE_UPDATE)) {
                    MainActivity.this.setStopConnecting();

                    String serverResponseMessage = intent.getStringExtra(UserService.SERVER_RESPONSE_MESSAGE);
                    ToastHelper.makeToast(MainActivity.this, serverResponseMessage);

                    boolean isConnected = intent.getBooleanExtra(UserService.IS_CONNECTED, false);
                    if (isConnected) {
                        Intent buddiesIntent = new Intent(MainActivity.this, BuddiesActivity.class);
                        MainActivity.this.startActivity(buddiesIntent);
                    }

                    boolean isServiceInitLoginActive = intent.getBooleanExtra(UserService.SERVICE_INIT_LOGIN, false);
                    if (isServiceInitLoginActive) {
                        MainActivity.this.setStartConnecting();
                    }
                }
            }
        }
    }
}
