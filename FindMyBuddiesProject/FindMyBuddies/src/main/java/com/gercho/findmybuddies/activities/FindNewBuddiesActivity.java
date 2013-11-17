package com.gercho.findmybuddies.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.helpers.LogoutAssistant;
import com.gercho.findmybuddies.helpers.NavigationDrawer;
import com.gercho.findmybuddies.helpers.ProgressBarController;
import com.gercho.findmybuddies.helpers.ServiceActions;
import com.gercho.findmybuddies.helpers.ToastNotifier;
import com.gercho.findmybuddies.models.BuddieFoundModel;
import com.gercho.findmybuddies.services.BuddiesService;
import com.google.gson.Gson;

/**
 * Created by Gercho on 11/16/13.
 */
public class FindNewBuddiesActivity extends FragmentActivity implements ListView.OnItemClickListener {

    public static final String BUDDIE_FOUND = "Buddie found";
    public static final String BUDDIE_NOT_FOUND = "Buddie not found";
    public static final String BUDDIE_REQUEST_SUCCESSFULLY_SEND = "Buddie request successfully send";
    public static final String BUDDIE_REQUEST_FAILED_ON_SEND = "Buddie request failed. Possible reason: " +
            "You have already send buddie request to this user";

    private BuddiesServiceUpdateReceiver mBuddiesServiceUpdateReceiver;
    private NavigationDrawer mNavigationDrawer;
    private ProgressBarController mProgressBarController;
    private boolean mIsConnectingActive;
    private BuddieFoundModel mBuddie;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_find_new_buddie);

        this.mNavigationDrawer = new NavigationDrawer();
        this.mNavigationDrawer.init(this, this);
        this.mNavigationDrawer.setSelection(NavigationDrawer.DRAWER_OPTION_FIND_NEW_BUDDIE);

        ProgressBar progressBar = (ProgressBar) this.findViewById(R.id.progressBar_search);
        this.mProgressBarController = new ProgressBarController(this, progressBar);

        this.setupViews();
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.mBuddiesServiceUpdateReceiver == null) {
            this.mBuddiesServiceUpdateReceiver = new BuddiesServiceUpdateReceiver();
            IntentFilter intentFilter = new IntentFilter(BuddiesService.BUDDIES_SERVICE_BROADCAST);
            this.registerReceiver(this.mBuddiesServiceUpdateReceiver, intentFilter);
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.mBuddiesServiceUpdateReceiver != null) {
            this.unregisterReceiver(this.mBuddiesServiceUpdateReceiver);
            this.mBuddiesServiceUpdateReceiver = null;
        }

        this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.logout, menu);
        return true;
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.mNavigationDrawer.syncState();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.mNavigationDrawer.handleOnPrepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_logout:
                LogoutAssistant.logout(this);
                return true;
            default:
                this.mNavigationDrawer.handleOnOptionsItemSelected(item);
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int optionsLib, long l) {
        this.mNavigationDrawer.handleSelect(optionsLib);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mNavigationDrawer.syncState();
    }

    private void setupViews() {
        this.findViewById(R.id.button_search).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FindNewBuddiesActivity.this.handleSearch();
            }
        });

        this.findViewById(R.id.button_sendRequest).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                FindNewBuddiesActivity.this.handleSendFriendRequest();
            }
        });
    }

    private void handleSearch() {
        if (this.mIsConnectingActive) {
            return;
        }

        this.mIsConnectingActive = true;
        this.mBuddie = null;
        this.mProgressBarController.startProgressBar(ProgressBarController.SEARCHING_TOAST_MESSAGE);
        EditText nicknameEditText = (EditText) this.findViewById(R.id.editText_searchNickname);
        CharSequence nickname = nicknameEditText.getText();
        if (nickname == null) {
            this.mIsConnectingActive = false;
            return;
        }

        Intent intent = new Intent();
        intent.setAction(ServiceActions.SEARCH_FOR_NEW_BUDDIE);
        intent.putExtra(BuddiesService.BUDDIE_NICKNAME_EXTRA, nickname.toString());
        this.startService(intent);
    }

    private void handleSearchResult(BuddieFoundModel buddie, boolean isStatusOk) {
        this.mIsConnectingActive = false;
        this.mProgressBarController.stopProgressBar();

        if (buddie != null && isStatusOk) {
            this.mBuddie = buddie;
            this.setFriendRequestUiVisibility(true);
        } else {
            this.mProgressBarController.changeActiveToastMessage(BUDDIE_NOT_FOUND);
            ToastNotifier.makeToast(this, BUDDIE_NOT_FOUND);
            this.setFriendRequestUiVisibility(false);
        }
    }

    private void setFriendRequestUiVisibility(boolean isVisible) {
        TextView searchResultTextView = (TextView) this.findViewById(R.id.textView_searchResult);
        Button sendRequestButton = (Button) this.findViewById(R.id.button_sendRequest);

        if (isVisible) {
            searchResultTextView.setText(BUDDIE_FOUND + ": " + this.mBuddie.getNickname());
            searchResultTextView.setVisibility(View.VISIBLE);
            sendRequestButton.setVisibility(View.VISIBLE);
        } else {
            searchResultTextView.setVisibility(View.INVISIBLE);
            sendRequestButton.setVisibility(View.INVISIBLE);
        }
    }

    private void handleSendFriendRequest() {
        if (!this.mIsConnectingActive && this.mBuddie != null) {
            Intent intent = new Intent();
            intent.setAction(ServiceActions.SEND_BUDDIE_REQUEST);
            intent.putExtra(BuddiesService.BUDDIE_ID_EXTRA, this.mBuddie.getId());
            intent.putExtra(BuddiesService.BUDDIE_NICKNAME_EXTRA, this.mBuddie.getNickname());
            this.startService(intent);
        }
    }

    private void handleSendFriendRequestResponse(boolean isStatusOk) {
        if (isStatusOk) {
            ToastNotifier.makeToast(this, BUDDIE_REQUEST_SUCCESSFULLY_SEND);
        } else {
            ToastNotifier.makeToast(this, BUDDIE_REQUEST_FAILED_ON_SEND);
        }

        this.setFriendRequestUiVisibility(false);
    }

    private class BuddiesServiceUpdateReceiver extends BroadcastReceiver {

        private Gson mGson;

        private BuddiesServiceUpdateReceiver() {
            this.mGson = new Gson();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BuddiesService.BUDDIES_SERVICE_BROADCAST)) {
                boolean isStatusOk = intent.getBooleanExtra(BuddiesService.IS_HTTP_STATUS_OK_EXTRA, false);
                String buddieSearchResultAsJson = intent.getStringExtra(BuddiesService.BUDDIE_SEARCH_RESULT_EXTRA);
                String responseMessage = intent.getStringExtra(BuddiesService.REQUESTS_SEND_RESULT_EXTRA);

                if (buddieSearchResultAsJson != null) {
                    BuddieFoundModel buddie = this.mGson.fromJson(buddieSearchResultAsJson, BuddieFoundModel.class);
                    FindNewBuddiesActivity.this.handleSearchResult(buddie, isStatusOk);
                } else if (responseMessage != null) {
                    FindNewBuddiesActivity.this.handleSendFriendRequestResponse(isStatusOk);
                }
            }
        }
    }
}