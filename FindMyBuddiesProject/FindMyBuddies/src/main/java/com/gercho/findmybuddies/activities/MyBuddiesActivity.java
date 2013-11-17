package com.gercho.findmybuddies.activities;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.adapters.MyBuddiesArrayAdapter;
import com.gercho.findmybuddies.enums.MeasureUnits;
import com.gercho.findmybuddies.helpers.LogoutAssistant;
import com.gercho.findmybuddies.helpers.NavigationDrawer;
import com.gercho.findmybuddies.helpers.ServiceActions;
import com.gercho.findmybuddies.helpers.ToastNotifier;
import com.gercho.findmybuddies.models.BuddyModel;
import com.gercho.findmybuddies.models.ImageModel;
import com.gercho.findmybuddies.services.BuddiesService;
import com.google.gson.Gson;

/**
 * Created by Gercho on 11/8/13.
 */
public class MyBuddiesActivity extends FragmentActivity implements ListView.OnItemClickListener {

    private static final int SHOW_ON_MAP = 0;
    private static final int SHOW_IMAGES = 1;
    private static final int NO_MORE_BUDDIES = 3;

    private NavigationDrawer mNavigationDrawer;
    private BuddyModel[] mBuddies;
    private MyBuddiesArrayAdapter mMyBuddiesArrayAdapter;
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

        this.overridePendingTransition(android.R.anim.fade_in, android.R.anim.fade_out);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.update, menu);
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
            case R.id.action_forceUpdate:
                this.forceUpdate();
                return true;
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
            case SHOW_ON_MAP:
                this.handleShowOnMap(this.mBuddies[info.position]);
                return true;
            case SHOW_IMAGES:
                this.handleShowImages(this.mBuddies[info.position]);
                return true;
            case NO_MORE_BUDDIES:
                this.handleNoMoreBuddies(this.mBuddies[info.position]);
                return true;
            default:
                return super.onContextItemSelected(item);
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

    @Override
    public void onCreateContextMenu(ContextMenu menu, View view, ContextMenu.ContextMenuInfo menuInfo) {
        if (view.getId() == R.id.list_buddies) {
            AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
            menu.setHeaderTitle(this.mBuddies[info.position].getNickname());
            String[] menuItems = getResources().getStringArray(R.array.buddy_commands_array);

            for (int i = 0; i < menuItems.length; i++) {
                menu.add(Menu.NONE, i, i, menuItems[i]);
            }
        }
    }

    private void forceUpdate() {
        Intent userServiceIntent = new Intent();
        userServiceIntent.setAction(ServiceActions.FORCE_UPDATING_BUDDIES_SERVICE);
        this.startService(userServiceIntent);
    }

    private void handleShowOnMap(BuddyModel buddy) {
        Intent intent = new Intent(this, MapActivity.class);
        intent.putExtra(BuddiesService.BUDDY_ID_EXTRA, buddy.getId());
        this.startActivity(intent);
    }

    private void handleShowImages(BuddyModel buddy) {
        Intent intent = this.getBuddyIntent(buddy);
        intent.setAction(ServiceActions.GET_BUDDY_IMAGES);
        this.startService(intent);
    }

    private void handleNoMoreBuddies(BuddyModel buddy) {
        Intent intent = this.getBuddyIntent(buddy);
        intent.setAction(ServiceActions.REMOVE_EXISTING_BUDDY);
        this.startService(intent);
    }

    private Intent getBuddyIntent(BuddyModel buddy){
        Intent intent = new Intent();
        intent.putExtra(BuddiesService.BUDDY_ID_EXTRA, buddy.getId());
        intent.putExtra(BuddiesService.BUDDY_NICKNAME_EXTRA, buddy.getNickname());
        return intent;
    }

    private void handleBuddiesUpdated(BuddyModel[] buddies, MeasureUnits measureUnits, int newBuddyRequestsCount) {
        this.mBuddies = buddies;
        this.mMyBuddiesArrayAdapter = new MyBuddiesArrayAdapter(
                this, R.layout.item_row_buddies_list, this.mBuddies, measureUnits);

        ListView buddiesList = (ListView) this.findViewById(R.id.list_buddies);
        buddiesList.setAdapter(this.mMyBuddiesArrayAdapter);

        this.registerForContextMenu(buddiesList);
        this.updateNewRequests(newBuddyRequestsCount);
    }

    private void updateNewRequests(int newBuddyRequestsCount) {
        TextView newRequestsTextView = (TextView) this.findViewById(R.id.textView_newRequests);
        if (newBuddyRequestsCount <= 0){
            newRequestsTextView.setText("");
        } else if(newBuddyRequestsCount == 1) {
            newRequestsTextView.setText(String.format("You have %d new buddy request", newBuddyRequestsCount));
        } else {
            newRequestsTextView.setText(String.format("You have %d new buddy requests", newBuddyRequestsCount));
        }
    }

    private void handleBuddyImages(ImageModel[] images) {
        Uri uri = Uri.parse(images[0].getUrl());
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        this.startActivity(intent);
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
                String buddYModelsAsJson = intent.getStringExtra(BuddiesService.BUDDIES_INFO_UPDATE_EXTRA);
                int measureUnitsAsInt = intent.getIntExtra(BuddiesService.BUDDIES_MEASURE_UNITS_EXTRA, Integer.MIN_VALUE);
                int newBuddyRequestsCount = intent.getIntExtra(BuddiesService.NEW_BUDDY_REQUESTS_EXTRA, 0);
                if (buddYModelsAsJson != null && measureUnitsAsInt != Integer.MIN_VALUE) {
                    this.handleBuddiesUpdated(buddYModelsAsJson, measureUnitsAsInt, newBuddyRequestsCount);
                    return;
                }

                boolean isStatusOk = intent.getBooleanExtra(BuddiesService.IS_HTTP_STATUS_OK_EXTRA, false);

                boolean buddyRemovedResult = intent.getBooleanExtra(BuddiesService.BUDDY_REMOVED_RESULT_EXTRA, false);
                if (buddyRemovedResult) {
                    int buddyId = intent.getIntExtra(BuddiesService.BUDDY_ID_EXTRA, Integer.MIN_VALUE);
                    String buddyNickname = intent.getStringExtra(BuddiesService.BUDDY_NICKNAME_EXTRA);
                    this.handleBuddyRemovedResult(buddyId, buddyNickname, isStatusOk);
                    return;
                }

                String responseToRequestResponseMessage = intent.getStringExtra(BuddiesService.RESPONSE_TO_REQUEST_EXTRA);
                if (responseToRequestResponseMessage != null) {
                    this.handleResponseToRequestResult(isStatusOk);
                    return;
                }

                String buddyImagesAsJson = intent.getStringExtra(BuddiesService.BUDDY_IMAGES_EXTRA);
                if (buddyImagesAsJson != null) {
                    this.handleBuddyImagesResult(buddyImagesAsJson, isStatusOk);
                    return;
                }

                String infoMessage = intent.getStringExtra(BuddiesService.INFO_MESSAGE_EXTRA);
                if (infoMessage != null) {
                    this.handleInfoMessage(infoMessage);
                }
            }
        }

        private void handleBuddiesUpdated(String buddyModelsAsJson, int measureUnitsAsInt, int newBuddyRequestsCount) {
            BuddyModel[] buddies = this.mGson.fromJson(buddyModelsAsJson, BuddyModel[].class);
            MeasureUnits measureUnits = MeasureUnits.values()[measureUnitsAsInt];
            if (buddies != null && buddies.length > 0) {
                MyBuddiesActivity.this.handleBuddiesUpdated(buddies, measureUnits, newBuddyRequestsCount);
            }
        }

        private void handleBuddyRemovedResult(int buddyId, String buddyNickname, boolean isStatusOk) {
            if (isStatusOk) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, buddyNickname + " with id " + buddyId + " successfully removed");
            } else {
                ToastNotifier.makeToast(MyBuddiesActivity.this, buddyNickname + " with id " + buddyId + " was not removed");
            }
        }

        private void handleResponseToRequestResult(boolean isStatusOk) {
            if (isStatusOk) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "response successfully send");
            } else {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "response failed, please try again");
            }
        }

        private void handleBuddyImagesResult(String buddyImagesAsJson, boolean isStatusOk) {
            if (!isStatusOk) {
                ToastNotifier.makeToast(MyBuddiesActivity.this, "Occurred error in connecting the database");
                return;
            }

            ImageModel[] images = this.mGson.fromJson(buddyImagesAsJson, ImageModel[].class);
            if (images != null && images.length > 0) {
                MyBuddiesActivity.this.handleBuddyImages(images);
            }
        }

        private void handleInfoMessage(String infoMessage) {
            ToastNotifier.makeToast(MyBuddiesActivity.this, infoMessage);
        }
    }
}