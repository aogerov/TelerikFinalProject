package com.gercho.findmybuddies;

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ProgressBar;

import com.gercho.findmybuddies.helpers.ToastHelper;
import com.gercho.findmybuddies.services.UserService;

public class MainActivity extends Activity {

    private static final int MAX_PROGRESS_BAR_WRITES = 1000;

    private boolean mIsProgressBarActive;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

        this.startServices();
        this.setupButtons();
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
                MainActivity.this.startProgressBar();
            }
        });

        this.findViewById(R.id.btn_stopProgressBar).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MainActivity.this.stopProgressBar();
            }
        });
//
//        this.findViewById(R.id.btnSendMessageToHandler).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MainActivity.this.btnSendMessageToHandlerOnClick();
//            }
//        });
//
//        this.findViewById(R.id.btnCallRunnableOnHandler).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MainActivity.this.btnCallRunnableOnHandlerOnClick();
//            }
//        });
//
//        this.findViewById(R.id.btnStartLocationMonitoring).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MainActivity.this.btnStartLocationMonitoringOnClick();
//            }
//        });
//
//        this.findViewById(R.id.btnStopLocationMonitoring).setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                MainActivity.this.btnStopLocationMonitoringOnClick();
//            }
//        });
    }

    private void startServices() {
        Intent intent = new Intent();
        intent.setAction(UserService.START_USER_SERVICE);
        this.startService(intent);
    }

    private void startProgressBar() {
        ToastHelper.makeToast(this, "Logging, please wait...");
        this.mIsProgressBarActive = true;

        final ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar_main);
        progressBar.setMax(MAX_PROGRESS_BAR_WRITES);
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        new AsyncTask<String, Integer, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                while (MainActivity.this.mIsProgressBarActive) {
                    for (int i = 1; i <= MAX_PROGRESS_BAR_WRITES; i++) {
                        try {
                            this.publishProgress(i);
                            Thread.sleep(5, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!MainActivity.this.mIsProgressBarActive) {
                            break;
                        }
                    }
                }

                return null;
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                int progressValue = values[0];
                progressBar.setProgress(progressValue);
            }

            @Override
            protected void onPostExecute(Void aVoid) {
                progressBar.setVisibility(View.INVISIBLE);
                ToastHelper.makeToast(MainActivity.this, "Login successful");
            }
        }.execute();
    }

    private void stopProgressBar() {
        this.mIsProgressBarActive = false;
    }
}
