package com.gercho.findmybuddies;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.gercho.findmybuddies.helpers.LogHelper;
import com.gercho.findmybuddies.helpers.ProgressBarHelper;
import com.gercho.findmybuddies.helpers.ToastHelper;
import com.gercho.findmybuddies.services.UserService;
import com.gercho.findmybuddies.services.UserServiceBinder;

public class MainActivity extends Activity {

    private static final String IS_PROGRESS_BAR_ACTIVE = "IsProgressBarActive";

    private UserService mUserService;
    private boolean mIsBoundToUserService;
    private ServiceConnection mUserServiceConnection;

    private ProgressBarHelper mProgressBarHelper;
    private boolean mIsProgressBarActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        LogHelper.logThreadId("onCreate");

        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar_main);
        this.mProgressBarHelper = new ProgressBarHelper(this, progressBar);

        if (savedInstanceState != null) {
            this.mIsProgressBarActive = savedInstanceState.getBoolean(IS_PROGRESS_BAR_ACTIVE, false);
        }

        this.setupButtons();
        this.startServices();
        this.connectToServices();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Intent intent = new Intent(this, UserService.class);
        bindService(intent, mUserServiceConnection, Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onResume() {
        LogHelper.logThreadId("onResume");

        super.onResume();
        if (this.mIsProgressBarActive) {
            this.mProgressBarHelper.startProgressBar();
        }
    }

    @Override
    protected void onPause() {
        LogHelper.logThreadId("onPause");

        super.onPause();
        if (this.mIsProgressBarActive) {
            this.mProgressBarHelper.stopProgressBar();
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        if (mIsBoundToUserService) {
            unbindService(mUserServiceConnection);
            mIsBoundToUserService = false;
        }
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        LogHelper.logThreadId("onSaveInstanceState");

        super.onSaveInstanceState(outState);
        outState.putBoolean(IS_PROGRESS_BAR_ACTIVE, this.mIsProgressBarActive);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void setupButtons() {
        this.findViewById(R.id.btn_startProgressBar).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                MainActivity.this.startProgressBarHelper();
//                ToastHelper.makeToast(MainActivity.this, MainActivity.this.mUserService.getIsUserLoggedIn() + "");
                MainActivity.this.mUserService.changeUserLoginStatus();
            }
        });

        this.findViewById(R.id.btn_stopProgressBar).setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {
//                MainActivity.this.stopProgressBarHelper();
                ToastHelper.makeToast(MainActivity.this, MainActivity.this.mUserService.getIsUserLoggedIn() + "");
            }
        });
    }

    private void startServices() {
        this.startUserService();
    }

    private void connectToServices() {
        this.connectToUserService();
    }

    private void startUserService() {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(UserService.START_USER_SERVICE);
        this.startService(serviceIntent);
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

    private void startProgressBarHelper() {
        this.mProgressBarHelper.startProgressBar();
        this.mIsProgressBarActive = true;
    }

    private void stopProgressBarHelper() {
        this.mProgressBarHelper.stopProgressBar();
        this.mIsProgressBarActive = false;
        ToastHelper.makeToast(MainActivity.this, "Successful connected");
    }
}
