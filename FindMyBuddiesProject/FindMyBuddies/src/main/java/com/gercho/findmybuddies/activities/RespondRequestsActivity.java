package com.gercho.findmybuddies.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.adapters.RequestsArrayAdapter;
import com.gercho.findmybuddies.helpers.LogoutAssistant;
import com.gercho.findmybuddies.helpers.NavigationDrawer;
import com.gercho.findmybuddies.helpers.ServiceActions;
import com.gercho.findmybuddies.helpers.ToastNotifier;
import com.gercho.findmybuddies.models.RequestModel;
import com.gercho.findmybuddies.services.BuddiesService;
import com.google.gson.Gson;

/**
 * Created by Gercho on 11/18/13.
 */
public class RespondRequestsActivity extends FragmentActivity implements ListView.OnItemClickListener {

    public static final int ACCEPT_REQUEST = 0;
    public static final int DECLINE_REQUEST = 1;
    public static final int LEAVE_FOR_LATER = 2;
    public static final String ERROR_CONNECTING_DATABASE = "Occurred error in connecting the database";

    private NavigationDrawer mNavigationDrawer;
    private RequestModel[] mAllRequests;
    private RequestsArrayAdapter mRequestsArrayAdapter;
    private BuddiesServiceUpdateReceiver mBuddiesServiceUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_respond_requests);

        this.mNavigationDrawer = new NavigationDrawer();
        this.mNavigationDrawer.init(this, this);
        this.mNavigationDrawer.setSelection(NavigationDrawer.DRAWER_OPTION_RESPOND_REQUESTS);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (this.mBuddiesServiceUpdateReceiver == null) {
            this.mBuddiesServiceUpdateReceiver = new BuddiesServiceUpdateReceiver();
            IntentFilter intentFilter = new IntentFilter(BuddiesService.BUDDIES_SERVICE_BROADCAST);
            this.registerReceiver(this.mBuddiesServiceUpdateReceiver, intentFilter);
        }

        this.getRequestsFromServer();
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
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case ACCEPT_REQUEST:
                this.handleAcceptRequest(this.mAllRequests[info.position]);
                return true;
            case DECLINE_REQUEST:
                this.handleDeclineRequest(this.mAllRequests[info.position]);
                return true;
            case LEAVE_FOR_LATER:
                this.handleLeaveForLaterRequest(this.mAllRequests[info.position]);
                return true;
            default:
                return super.onContextItemSelected(item);
        }
    }

    private void getRequestsFromServer() {
        Intent buddiesServiceIntent = new Intent();
        buddiesServiceIntent.setAction(ServiceActions.GET_ALL_REQUESTS);
        this.startService(buddiesServiceIntent);
    }

    private void handleAcceptRequest(RequestModel request) {
        Intent intent = this.getRequestResponseIntent(request);
        intent.putExtra(BuddiesService.REQUESTS_IS_ACCEPTED_EXTRA, true);
        intent.putExtra(BuddiesService.REQUESTS_IS_LEFT_FOR_LATER_EXTRA, false);
        this.startService(intent);
        this.getRequestsFromServer();
    }

    private void handleDeclineRequest(RequestModel request) {
        Intent intent = this.getRequestResponseIntent(request);
        intent.putExtra(BuddiesService.REQUESTS_IS_ACCEPTED_EXTRA, false);
        intent.putExtra(BuddiesService.REQUESTS_IS_LEFT_FOR_LATER_EXTRA, false);
        this.startService(intent);
        this.getRequestsFromServer();
    }

    private void handleLeaveForLaterRequest(RequestModel request) {
        Intent intent = this.getRequestResponseIntent(request);
        intent.putExtra(BuddiesService.REQUESTS_IS_ACCEPTED_EXTRA, false);
        intent.putExtra(BuddiesService.REQUESTS_IS_LEFT_FOR_LATER_EXTRA, true);
        this.startService(intent);
        this.getRequestsFromServer();
    }

    private Intent getRequestResponseIntent(RequestModel request) {
        Intent intent = new Intent();
        intent.setAction(ServiceActions.RESPOND_TO_BUDDIE_REQUEST);
        intent.putExtra(BuddiesService.BUDDIE_ID_EXTRA, request.getFromUserId());
        intent.putExtra(BuddiesService.BUDDIE_NICKNAME_EXTRA, request.getFromUserNickname());
        return intent;
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if (view.getId() == R.id.list_requests) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(this.mAllRequests[info.position].getFromUserNickname());
            String[] menuItems = getResources().getStringArray(R.array.respond_requests_array);

            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    private void handleAllRequestsResult(RequestModel[] allRequests) {
        this.mAllRequests = allRequests;
        this.mRequestsArrayAdapter = new RequestsArrayAdapter(
                this, R.layout.item_row_respond_request, this.mAllRequests);

        ListView requestsList = (ListView) this.findViewById(R.id.list_requests);
        requestsList.setAdapter(this.mRequestsArrayAdapter);

        this.registerForContextMenu(requestsList);
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

                String allRequestsAsJson = intent.getStringExtra(BuddiesService.ALL_REQUESTS_EXTRA);
                if (allRequestsAsJson != null && isStatusOk) {
                    RequestModel[] allRequests = this.mGson.fromJson(allRequestsAsJson, RequestModel[].class);
                    if (allRequests != null) {
                        RespondRequestsActivity.this.handleAllRequestsResult(allRequests);
                    }
                }

                String infoMessage = intent.getStringExtra(BuddiesService.RESPONSE_TO_REQUEST_EXTRA);
                if (infoMessage != null && !isStatusOk) {
                    ToastNotifier.makeToast(RespondRequestsActivity.this, ERROR_CONNECTING_DATABASE);
                }
            }
        }
    }
}