package com.gercho.findmybuddies;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.gercho.findmybuddies.helpers.ProgressBarHelper;
import com.gercho.findmybuddies.services.UserService;

public class MainActivity extends Activity {

    private ProgressBarHelper mProgressBarHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar_main);
        this.mProgressBarHelper = new ProgressBarHelper(this, progressBar);

        this.startServices();
        this.setupButtons();

        // TODO: bind to user service and use it at onResume() to check user status
    }

    @Override
    protected void onResume() {
        super.onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    private void startServices() {
        Intent intent = new Intent();
        intent.setAction(UserService.START_USER_SERVICE);
        this.startService(intent);
    }

    private void setupButtons() {
        this.findViewById(R.id.btn_startProgressBar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.mProgressBarHelper.startProgressBar();
            }
        });

        this.findViewById(R.id.btn_stopProgressBar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.mProgressBarHelper.stopProgressBar();
            }
        });
    }
}
