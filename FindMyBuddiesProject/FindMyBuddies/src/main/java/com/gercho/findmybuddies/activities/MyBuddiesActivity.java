package com.gercho.findmybuddies.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.enums.MeasureUnits;
import com.gercho.findmybuddies.helpers.LogHelper;
import com.gercho.findmybuddies.helpers.NavigationDrawer;
import com.gercho.findmybuddies.helpers.ServiceActions;
import com.gercho.findmybuddies.helpers.ToastNotifier;
import com.gercho.findmybuddies.models.BuddieFoundModel;
import com.gercho.findmybuddies.models.BuddieModel;
import com.gercho.findmybuddies.models.ImageModel;
import com.gercho.findmybuddies.models.RequestModel;
import com.gercho.findmybuddies.services.BuddiesService;
import com.google.gson.Gson;

/**
 * Created by Gercho on 11/8/13.
 */
public class MyBuddiesActivity extends Activity implements ListView.OnItemClickListener {

    private NavigationDrawer mNavigationDrawer;
    private BuddiesServiceUpdateReceiver mBuddiesServiceUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_my_buddies);

        this.mNavigationDrawer = new NavigationDrawer();
        this.mNavigationDrawer.init(this, this);
        this.mNavigationDrawer.setSelection(NavigationDrawer.DRAWER_OPTION_MY_BUDDIES);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.mBuddiesServiceUpdateReceiver == null) {
            this.mBuddiesServiceUpdateReceiver = new BuddiesServiceUpdateReceiver();
            IntentFilter intentFilter = new IntentFilter(BuddiesService.BUDDIES_SERVICE_BROADCAST);
            this.registerReceiver(this.mBuddiesServiceUpdateReceiver, intentFilter);
        }

        Intent buddiesServiceIntent = new Intent();
        buddiesServiceIntent.setAction(ServiceActions.RESUME_BUDDIES_SERVICE);
        this.startService(buddiesServiceIntent);
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (this.mBuddiesServiceUpdateReceiver != null) {
            this.unregisterReceiver(this.mBuddiesServiceUpdateReceiver);
            this.mBuddiesServiceUpdateReceiver = null;
        }

        Intent buddiesServiceIntent = new Intent();
        buddiesServiceIntent.setAction(ServiceActions.PAUSE_BUDDIES_SERVICE);
        this.startService(buddiesServiceIntent);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main, menu);
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
            case R.id.action_forceUpdate:
                this.forceUpdate();
                return true;
            case R.id.action_logout:
                this.logout();
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

    private void forceUpdate() {
        Intent userServiceIntent = new Intent();
        userServiceIntent.setAction(ServiceActions.FORCE_UPDATING_BUDDIES_SERVICE);
        this.startService(userServiceIntent);
    }

    private void logout() {
        Intent buddiesServiceIntent = new Intent();
        buddiesServiceIntent.setAction(ServiceActions.STOP_BUDDIES_SERVICE);
        this.startService(buddiesServiceIntent);

        Intent userServiceIntent = new Intent();
        userServiceIntent.setAction(ServiceActions.LOGOUT);
        this.startService(userServiceIntent);

        Intent mainActivityIntent = new Intent(this, LoginRegisterActivity.class);
        this.startActivity(mainActivityIntent);
    }

    private class BuddiesServiceUpdateReceiver extends BroadcastReceiver {

        Gson mGson;

        private BuddiesServiceUpdateReceiver() {
            this.mGson = new Gson();
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BuddiesService.BUDDIES_SERVICE_BROADCAST)) {
                String buddieModelsAsJson = intent.getStringExtra(BuddiesService.BUDDIES_INFO_UPDATE_EXTRA);
                int measureUnitsAsInt = intent.getIntExtra(BuddiesService.BUDDIES_MEASURE_UNITS_EXTRA, Integer.MIN_VALUE);
                int newBuddieRequestsCount = intent.getIntExtra(BuddiesService.NEW_BUDDIE_REQUESTS_EXTRA, 0);
                if (buddieModelsAsJson != null && measureUnitsAsInt != Integer.MIN_VALUE) {
                    this.handleBuddiesUpdated(buddieModelsAsJson, measureUnitsAsInt, newBuddieRequestsCount);
                    return;
                }

                boolean isStatusOk = intent.getBooleanExtra(BuddiesService.IS_HTTP_STATUS_OK_EXTRA, false);

                String buddieSearchResultAsJson = intent.getStringExtra(BuddiesService.BUDDIE_SEARCH_RESULT_EXTRA);
                if (buddieSearchResultAsJson != null) {
                    this.handleBuddieSearchResult(buddieSearchResultAsJson, isStatusOk);
                    return;
                }

                String allRequestsAsJson = intent.getStringExtra(BuddiesService.ALL_REQUESTS_EXTRA);
                if (allRequestsAsJson != null) {
                    this.handleAllRequestsResult(allRequestsAsJson, isStatusOk);
                    return;
                }

                boolean buddieRemovedResult = intent.getBooleanExtra(BuddiesService.BUDDIE_REMOVED_RESULT_EXTRA, false);
                if (buddieRemovedResult) {
                    int buddieId = intent.getIntExtra(BuddiesService.BUDDIE_ID_EXTRA, Integer.MIN_VALUE);
                    String buddieNickname = intent.getStringExtra(BuddiesService.BUDDIE_NICKNAME_EXTRA);
                    this.handleBuddieRemovedResult(buddieId, buddieNickname, isStatusOk);
                    return;
                }

                String requestSendResponseMessage = intent.getStringExtra(BuddiesService.REQUESTS_SEND_RESULT_EXTRA);
                if (requestSendResponseMessage != null) {
                    this.handleRequestSendResult(isStatusOk);
                    return;
                }

                String responseToRequestResponseMessage = intent.getStringExtra(BuddiesService.RESPONSE_TO_REQUEST_EXTRA);
                if (responseToRequestResponseMessage != null) {
                    this.handleResponseToRequestResult(isStatusOk);
                    return;
                }

                String buddieImagesAsJson = intent.getStringExtra(BuddiesService.BUDDIE_IMAGES_EXTRA);
                if (buddieImagesAsJson != null) {
                    this.handleBuddieImagesResult(buddieImagesAsJson, isStatusOk);
                    return;
                }

                String infoMessage = intent.getStringExtra(BuddiesService.INFO_MESSAGE_EXTRA);
                if (infoMessage != null) {
                    this.handleInfoMessage(infoMessage);
                }
            }
        }

        private void handleBuddiesUpdated(String buddieModelsAsJson, int measureUnitsAsInt, int newBuddieRequestsCount) {
            BuddieModel[] buddies = this.mGson.fromJson(buddieModelsAsJson, BuddieModel[].class);
            MeasureUnits measureUnits = MeasureUnits.values()[measureUnitsAsInt];
            if (buddies != null && buddies.length > 0) {
                LogHelper.logThreadId("buddies count: " + buddies.length + ", measure units: " + measureUnits + ", new requests: " + newBuddieRequestsCount);

            }
        }

        private void handleBuddieRemovedResult(int buddieId, String buddieNickname, boolean isStatusOk) {
            if (isStatusOk) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, buddieNickname + " with id " + buddieId + " successfully removed");
            } else {
                ToastNotifier.makeToast(MyBuddiesActivity.this, buddieNickname + " with id " + buddieId + " was not removed");
            }
        }

        private void handleBuddieSearchResult(String buddieSearchResultAsJson, boolean isStatusOk) {
            if (!isStatusOk) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "Occurred error in connecting the database");
                return;
            }

            BuddieFoundModel buddie = this.mGson.fromJson(buddieSearchResultAsJson, BuddieFoundModel.class);
            if (buddie != null) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "buddie nickname - " + buddie.getNickname());
            } else {
                // TODO validate before sending the buddie, if its not in buddie list already
                ToastNotifier.makeToast(MyBuddiesActivity.this, "no matches found");
            }
        }

        private void handleAllRequestsResult(String allRequestsAsJson, boolean isStatusOk) {
            if (!isStatusOk) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "Occurred error in connecting the database");
                return;
            }

            RequestModel[] allRequests = this.mGson.fromJson(allRequestsAsJson, RequestModel[].class);
            if (allRequests != null) { // && allRequests.length > 0
                ToastNotifier.makeToast(MyBuddiesActivity.this, "there are " + allRequests.length + " current requests");
            }
        }

        private void handleRequestSendResult(boolean isStatusOk) {
            if (isStatusOk) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "request successfully send");
            } else {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "request was unable to be send");
            }
        }

        private void handleResponseToRequestResult(boolean isStatusOk) {
            if (isStatusOk) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "response successfully send");
            } else {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "response failed, please try again");
            }
        }

        private void handleBuddieImagesResult(String buddieImagesAsJson, boolean isStatusOk) {
            if (!isStatusOk) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "Occurred error in connecting the database");
                return;
            }

            ImageModel[] images = this.mGson.fromJson(buddieImagesAsJson, ImageModel[].class);
            if (images != null && images.length > 0) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "images count: " + images.length);
            }
        }

        private void handleInfoMessage(String infoMessage) {
            ToastNotifier.makeToast(MyBuddiesActivity.this, infoMessage);
        }
    }
}