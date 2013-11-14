package com.gercho.findmybuddies;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gercho.findmybuddies.enums.MeasureUnits;
import com.gercho.findmybuddies.helpers.LogHelper;
import com.gercho.findmybuddies.helpers.NavigationDrawer;
import com.gercho.findmybuddies.models.BuddieModel;
import com.gercho.findmybuddies.services.BuddiesService;
import com.gercho.findmybuddies.services.UserService;
import com.google.gson.Gson;

/**
 * Created by Gercho on 11/8/13.
 */
public class FindMyBuddiesActivity extends FragmentActivity implements ListView.OnItemClickListener {

    public static final String EXTRA_COURSE_LIB = "course lib";
    private static final int COURSE_LIB_NOT_SET = -1;

    //    private CoursePagerAdapter mCoursePagerAdapter;
    private ViewPager mViewPager;
    private NavigationDrawer mNavigationDrawer;
    private boolean mIsLoggingInProgress;

    private BuddiesServiceUpdateReceiver mBuddiesServiceUpdateReceiver;
    private Gson mGson;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_find_my_buddies);

        this.mGson = new Gson();

//        this.mCoursePagerAdapter = new CoursePagerAdapter(
//                this.getSupportFragmentManager(), this);

//        this.mViewPager = (ViewPager) findViewById(R.id.pager);
//        this.mViewPager.setAdapter(this.mCoursePagerAdapter);

        this.mNavigationDrawer = new NavigationDrawer();
        this.mNavigationDrawer.init(this, this);

        Intent startupIntent = this.getIntent();
        int courseLib = startupIntent.getIntExtra(EXTRA_COURSE_LIB, COURSE_LIB_NOT_SET);
        if (courseLib != COURSE_LIB_NOT_SET) {
//            this.mCoursePagerAdapter.setCourseLib(courseLib);
            this.mNavigationDrawer.setSelection(courseLib);
        }
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
        buddiesServiceIntent.setAction(BuddiesService.RESUME_BUDDIES_SERVICE);
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
        buddiesServiceIntent.setAction(BuddiesService.PAUSE_BUDDIES_SERVICE);
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
//        this.mCoursePagerAdapter.setCourseLib(optionsLib);
        this.mNavigationDrawer.handleSelect(optionsLib);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mNavigationDrawer.syncState();
    }

    private void forceUpdate() {
        Intent userServiceIntent = new Intent();
        userServiceIntent.setAction(BuddiesService.FORCE_UPDATING_BUDDIES_SERVICE);
        this.startService(userServiceIntent);
    }

    private void logout() {
        // TODO send "popup" to the user, saying that "logging out will dismiss your current session and the session if you logged from other devices", on yes logout, on no nothing happens
        Intent buddiesServiceIntent = new Intent();
        buddiesServiceIntent.setAction(BuddiesService.STOP_BUDDIES_SERVICE);
        this.startService(buddiesServiceIntent);

        Intent userServiceIntent = new Intent();
        userServiceIntent.setAction(UserService.LOGOUT);
        this.startService(userServiceIntent);

        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        this.startActivity(mainActivityIntent);
    }

    private class BuddiesServiceUpdateReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (action != null && action.equals(BuddiesService.BUDDIES_SERVICE_BROADCAST)) {
                String buddieModelsAsJson = intent.getStringExtra(BuddiesService.BUDDIES_INFO_UPDATE_EXTRA);
                int measureUnitsAsInt = intent.getIntExtra(BuddiesService.BUDDIES_MEASURE_UNITS_EXTRA, Integer.MIN_VALUE);

                if (buddieModelsAsJson != null && measureUnitsAsInt != Integer.MIN_VALUE) {
                    this.handleBuddiesUpdated(buddieModelsAsJson, measureUnitsAsInt);
                }
            }
        }

        private void handleBuddiesUpdated(String buddieModelsAsJson, int measureUnitsAsInt) {
            try {
                BuddieModel[] buddies = FindMyBuddiesActivity.this.mGson.fromJson(buddieModelsAsJson, BuddieModel[].class);
                MeasureUnits measureUnits = MeasureUnits.values()[measureUnitsAsInt];
                if (buddies.length > 0) {
//                    ToastNotifier.makeToast(FindMyBuddiesActivity.this, "buddies count - " + buddies.length + " measure units - " + measureUnits);
                }
            } catch (Exception ex) {
                LogHelper.logThreadId("updateBuddiesInfo fromJson() parse error");
            }
        }
    }
}