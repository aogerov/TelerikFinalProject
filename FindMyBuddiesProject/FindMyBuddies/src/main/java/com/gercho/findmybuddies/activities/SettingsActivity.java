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
        buddiesServiceIntent.setAction(ServiceActions.GET_CURRENT_SETTINGS);
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

    private void setupViews(int updateFrequency, int measureUnitsAsInt,
                            int buddiesOrderByAsInt, int imagesToShowCount) {
        this.setupUpdateFrequency(updateFrequency);
        this.setupMeasureUnits(measureUnitsAsInt);
        this.setupOrderBy(buddiesOrderByAsInt);
        this.setupImagesCount(imagesToShowCount);

        this.setupSaveButton();
    }

    private void setupUpdateFrequency(int updateFrequency) {
        Spinner spinnerUpdateFrequency = (Spinner) findViewById(R.id.spinner_updateFrequency);
        ArrayAdapter<CharSequence> adapterUpdateFrequency = ArrayAdapter.createFromResource(this,
                R.array.update_frequency_array, android.R.layout.simple_spinner_item);
        adapterUpdateFrequency.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerUpdateFrequency.setAdapter(adapterUpdateFrequency);

        if (updateFrequency != Integer.MIN_VALUE) {
            int updateFrequencyInMinutes = updateFrequency / 1000 / 60;
            int index = updateFrequencyInMinutes - 1;
            spinnerUpdateFrequency.setSelection(index);
        }
    }

    private void setupMeasureUnits(int measureUnitsAsInt) {
        Spinner spinnerMeasureUnits = (Spinner) findViewById(R.id.spinner_measureUnits);
        ArrayAdapter<CharSequence> adapterMeasureUnits = ArrayAdapter.createFromResource(this,
                R.array.measure_units_array, android.R.layout.simple_spinner_item);
        adapterMeasureUnits.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerMeasureUnits.setAdapter(adapterMeasureUnits);

        if (measureUnitsAsInt != Integer.MIN_VALUE) {
            spinnerMeasureUnits.setSelection(measureUnitsAsInt);
        }
    }

    private void setupOrderBy(int buddiesOrderByAsInt) {
        Spinner spinnerOrderBy = (Spinner) findViewById(R.id.spinner_orderBy);
        ArrayAdapter<CharSequence> adapterOrderBy = ArrayAdapter.createFromResource(this,
                R.array.order_by_array, android.R.layout.simple_spinner_item);
        adapterOrderBy.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerOrderBy.setAdapter(adapterOrderBy);

        if (buddiesOrderByAsInt != Integer.MIN_VALUE) {
            spinnerOrderBy.setSelection(buddiesOrderByAsInt);
        }
    }

    private void setupImagesCount(int imagesToShowCount) {
        Spinner spinnerImagesCount = (Spinner) findViewById(R.id.spinner_imagesCount);
        ArrayAdapter<CharSequence> adapterImagesCount = ArrayAdapter.createFromResource(this,
                R.array.images_count_array, android.R.layout.simple_spinner_item);
        adapterImagesCount.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinnerImagesCount.setAdapter(adapterImagesCount);

        if (imagesToShowCount != Integer.MIN_VALUE) {
            int index = imagesToShowCount - 1;
            spinnerImagesCount.setSelection(index);
        }
    }

    private void setupSaveButton() {
        this.findViewById(R.id.button_save).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                SettingsActivity.this.handleSaveSettings();
            }
        });
    }

    private void handleSaveSettings() {
        this.saveUpdateFrequency();
        this.saveMeasureUnits();
        this.saveOrderBy();
        this.saveImagesCount();

        this.finish();
    }

    private void saveUpdateFrequency() {
        Spinner spinnerUpdateFrequency = (Spinner) findViewById(R.id.spinner_updateFrequency);
        long updateFrequencyIndex = spinnerUpdateFrequency.getSelectedItemId();
        int updateFrequency = (int) ((updateFrequencyIndex + 1) * 1000 * 60);

        Intent intent = new Intent();
        intent.setAction(ServiceActions.SET_UPDATE_FREQUENCY);
        intent.putExtra(BuddiesService.UPDATE_FREQUENCY_EXTRA, updateFrequency);
        this.startService(intent);
    }

    private void saveMeasureUnits() {
        Spinner spinnerMeasureUnits = (Spinner) findViewById(R.id.spinner_measureUnits);
        long measureUnitsIndex = spinnerMeasureUnits.getSelectedItemId();
        int measureUnitsAsInt = (int) measureUnitsIndex;

        Intent intent = new Intent();
        intent.setAction(ServiceActions.SET_MEASURE_UNITS);
        intent.putExtra(BuddiesService.BUDDIES_MEASURE_UNITS_EXTRA, measureUnitsAsInt);
        this.startService(intent);
    }

    private void saveOrderBy() {
        Spinner spinnerOrderBy = (Spinner) findViewById(R.id.spinner_orderBy);
        long buddiesOrderByIndex = spinnerOrderBy.getSelectedItemId();
        int buddiesOrderByAsInt = (int) buddiesOrderByIndex;

        Intent intent = new Intent();
        intent.setAction(ServiceActions.SET_BUDDIES_ORDER_BY);
        intent.putExtra(BuddiesService.BUDDIES_ORDER_BY_TYPES_EXTRA, buddiesOrderByAsInt);
        this.startService(intent);
    }

    private void saveImagesCount() {
        Spinner spinnerImagesCount = (Spinner) findViewById(R.id.spinner_imagesCount);
        long imagesToShowCountIndex = spinnerImagesCount.getSelectedItemId();
        int imagesToShowCount = (int) (imagesToShowCountIndex + 1);

        Intent intent = new Intent();
        intent.setAction(ServiceActions.SET_IMAGES_TO_SHOW_COUNT);
        intent.putExtra(BuddiesService.IMAGES_TO_SHOW_COUNT_EXTRA, imagesToShowCount);
        this.startService(intent);
    }

    private class BuddiesServiceUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BuddiesService.BUDDIES_SERVICE_BROADCAST)) {
                int updateFrequency = intent.getIntExtra(BuddiesService.UPDATE_FREQUENCY_EXTRA, Integer.MIN_VALUE);
                int measureUnitsAsInt = intent.getIntExtra(BuddiesService.BUDDIES_MEASURE_UNITS_EXTRA, Integer.MIN_VALUE);
                int buddiesOrderByAsInt = intent.getIntExtra(BuddiesService.BUDDIES_ORDER_BY_TYPES_EXTRA, Integer.MIN_VALUE);
                int imagesToShowCount = intent.getIntExtra(BuddiesService.IMAGES_TO_SHOW_COUNT_EXTRA, Integer.MIN_VALUE);

                if (updateFrequency != Integer.MIN_VALUE || imagesToShowCount != Integer.MIN_VALUE ||
                        buddiesOrderByAsInt != Integer.MIN_VALUE || measureUnitsAsInt != Integer.MIN_VALUE) {
                    SettingsActivity.this.setupViews(updateFrequency, measureUnitsAsInt, buddiesOrderByAsInt, imagesToShowCount);
                }
            }
        }
    }
}