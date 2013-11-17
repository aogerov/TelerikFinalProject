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
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.Spinner;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.helpers.LogoutAssistant;
import com.gercho.findmybuddies.helpers.NavigationDrawer;
import com.gercho.findmybuddies.helpers.ServiceActions;
import com.gercho.findmybuddies.services.BuddiesService;

/**
 * Created by Gercho on 11/16/13.
 */
public class SettingsActivity extends FragmentActivity implements ListView.OnItemClickListener {

    private NavigationDrawer mNavigationDrawer;
    private BuddiesServiceUpdateReceiver mBuddiesServiceUpdateReceiver;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_settings);

        this.mNavigationDrawer = new NavigationDrawer();
        this.mNavigationDrawer.init(this, this);
        this.mNavigationDrawer.setSelection(NavigationDrawer.DRAWER_OPTION_SETTING);
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

    private void setupViews(int updateFrequency, int imagesToShowCount, int buddiesOrderByAsInt, int measureUnitsAsInt) {
        Spinner spinner = (Spinner) findViewById(R.id.spinner_imagesCount);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.images_count_array, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        if (updateFrequency != Integer.MIN_VALUE) {

        }

        if (imagesToShowCount != Integer.MIN_VALUE) {

        }

        if (buddiesOrderByAsInt != Integer.MIN_VALUE) {

        }

        if (measureUnitsAsInt != Integer.MIN_VALUE) {

        }
    }

    private class BuddiesServiceUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BuddiesService.BUDDIES_SERVICE_BROADCAST)) {
                int updateFrequency = intent.getIntExtra(BuddiesService.UPDATE_FREQUENCY_EXTRA, Integer.MIN_VALUE);
                int imagesToShowCount = intent.getIntExtra(BuddiesService.IMAGES_TO_SHOW_COUNT_EXTRA, Integer.MIN_VALUE);
                int buddiesOrderByAsInt = intent.getIntExtra(BuddiesService.BUDDIES_ORDER_BY_TYPES_EXTRA, Integer.MIN_VALUE);
                int measureUnitsAsInt = intent.getIntExtra(BuddiesService.BUDDIES_MEASURE_UNITS_EXTRA, Integer.MIN_VALUE);

                if (updateFrequency != Integer.MIN_VALUE || imagesToShowCount != Integer.MIN_VALUE ||
                        buddiesOrderByAsInt != Integer.MIN_VALUE || measureUnitsAsInt != Integer.MIN_VALUE) {
                    SettingsActivity.this.setupViews(updateFrequency, imagesToShowCount, buddiesOrderByAsInt, measureUnitsAsInt);
                }
            }
        }
    }
}