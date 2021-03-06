package com.gercho.findmybuddies.helpers;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Handler;
import android.view.View;
import android.widget.ProgressBar;

/**
 * Created by Gercho on 11/8/13.
 */
public class ProgressBarController {

    public static final String DEFAULT_TOAST_MESSAGE = "Connecting, please wait...";
    public static final String SEARCHING_TOAST_MESSAGE = "Searching, please wait...";

    private static final int MAX_PROGRESS_BAR_WRITES = 300;

    private Context mContext;
    private ProgressBar mProgressBar;
    private boolean mIsProgressBarActive;
    private String mToastMessage;

    public ProgressBarController(Context context, ProgressBar progressBar) {
        this.mContext = context;
        this.mProgressBar = progressBar;
        this.mProgressBar.setMax(MAX_PROGRESS_BAR_WRITES);
        this.mProgressBar.setVisibility(View.INVISIBLE);
    }

    public void startProgressBar(String toastMessage) {
        this.mToastMessage = toastMessage;
        this.mIsProgressBarActive = true;
        this.executeProcess();
    }

    public void stopProgressBar() {
        this.mIsProgressBarActive = false;
    }

    public void changeActiveToastMessage(String message) {
        this.mToastMessage = message;
    }

    private void executeProcess() {
        final Handler handler = new Handler(this.mContext.getMainLooper());
        final ProgressBar progressBar = this.mProgressBar;
        progressBar.setProgress(0);
        progressBar.setVisibility(View.VISIBLE);

        new AsyncTask<String, Integer, Void>() {
            @Override
            protected Void doInBackground(String... strings) {
                while (ProgressBarController.this.mIsProgressBarActive) {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            ToastNotifier.makeToast(ProgressBarController.this.mContext,
                                    ProgressBarController.this.mToastMessage);
                        }
                    });

                    for (int i = 1; i <= MAX_PROGRESS_BAR_WRITES; i++) {
                        try {
                            this.publishProgress(i);
                            Thread.sleep(5, 0);
                        } catch (InterruptedException e) {
                            e.printStackTrace();
                        }

                        if (!ProgressBarController.this.mIsProgressBarActive) {
                            return null;
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
            }
        }.execute();
    }
}
