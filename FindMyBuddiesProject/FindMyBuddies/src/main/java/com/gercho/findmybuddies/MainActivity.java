package com.gercho.findmybuddies;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.gercho.findmybuddies.helpers.LogHelper;
import com.gercho.findmybuddies.helpers.ProgressBarHelper;
import com.gercho.findmybuddies.helpers.ToastHelper;
import com.gercho.findmybuddies.services.UserService;

public class MainActivity extends Activity {

    private static final String IS_PROGRESS_BAR_ACTIVE = "IsProgressBarActive";

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

        this.startServices();
        this.setupButtons();

        // TODO: bind to user service and use it at onResume() to check user status
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

    private void startServices() {
        this.startUserService();
    }

    private void setupButtons() {
        this.findViewById(R.id.btn_startProgressBar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.startProgressBarHelper();
            }
        });

        this.findViewById(R.id.btn_stopProgressBar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.stopProgressBarHelper();
            }
        });
    }

    private void startUserService() {
        Intent serviceIntent = new Intent();
        serviceIntent.setAction(UserService.START_USER_SERVICE);
        this.startService(serviceIntent);
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
