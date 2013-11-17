package com.gercho.findmybuddies.helpers;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.activities.FindNewBuddiesActivity;
import com.gercho.findmybuddies.activities.MapActivity;
import com.gercho.findmybuddies.activities.MyBuddiesActivity;
import com.gercho.findmybuddies.activities.RespondRequestsActivity;
import com.gercho.findmybuddies.activities.SettingsActivity;
import com.gercho.findmybuddies.activities.TakePictureActivity;

/**
 * Created by Gercho on 11/7/13.
 */
public class NavigationDrawer {

    public static final int DRAWER_OPTION_MY_BUDDIES = 0;
    public static final int DRAWER_OPTION_FIND_NEW_BUDDIE = 1;
    public static final int DRAWER_OPTION_RESPOND_REQUESTS = 2;
    public static final int DRAWER_OPTION_MAP = 3;
    public static final int DRAWER_OPTION_TAKE_PICTURE = 4;
    public static final int DRAWER_OPTION_SETTING = 5;

    private Activity mActivity;
    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private ActionBarDrawerToggle mDrawerToggle;
    private int mCurrentSelection;

    public void init(Activity activity, ListView.OnItemClickListener listener) {
        this.mActivity = activity;
        this.mDrawerLayout = (DrawerLayout) activity.findViewById(R.id.drawer_layout);
        this.mDrawerListView = (ListView) activity.findViewById(R.id.left_drawer);
        this.mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        String[] navigationDrawerOptions =
                activity.getResources().getStringArray(R.array.navigation_drawer_options);

        ArrayAdapter<String> navigationDrawerAdapter =
                new ArrayAdapter<String>(activity, R.layout.drawer_option_item, navigationDrawerOptions);

        this.mDrawerListView.setAdapter(navigationDrawerAdapter);
        this.mDrawerListView.setOnItemClickListener(listener);

        this.setupActionBar(activity);
    }

    public void setSelection(int option) {
        this.mDrawerListView.setItemChecked(option, true);
        this.mCurrentSelection = option;
    }

    public void syncState() {
        this.mDrawerToggle.syncState();
    }

    public void handleOnPrepareOptionsMenu(Menu menu) {
        boolean isItemVisible = !this.mDrawerLayout.isDrawerOpen(this.mDrawerListView);

        for (int index = 0; index < menu.size(); index++) {
            MenuItem item = menu.getItem(index);
            item.setEnabled(isItemVisible);
        }
    }

    public void handleOnOptionsItemSelected(MenuItem item) {
        this.mDrawerToggle.onOptionsItemSelected(item);
    }

    public void handleSelect(int option) {
        boolean isSelectValid = true;
        if (this.mCurrentSelection == option) {
            isSelectValid = false;
        }

        this.mDrawerListView.setItemChecked(option, true);
        this.mDrawerLayout.closeDrawer(this.mDrawerListView);

        if (isSelectValid) {
            switch (option) {
                case DRAWER_OPTION_MY_BUDDIES:
                    this.switchToMyBuddies();
                    break;
                case DRAWER_OPTION_FIND_NEW_BUDDIE:
                    this.switchToFindNewBuddie();
                    break;
                case DRAWER_OPTION_RESPOND_REQUESTS:
                    this.switchToRespondRequests();
                    break;
                case DRAWER_OPTION_MAP:
                    this.switchToMap();
                    break;
                case DRAWER_OPTION_TAKE_PICTURE:
                    this.switchToTakePicture();
                    break;
                case DRAWER_OPTION_SETTING:
                    this.switchToSettings();
                    break;
            }
        }
    }

    private void switchToMyBuddies() {
        Intent intent = new Intent(this.mActivity, MyBuddiesActivity.class);
        this.mActivity.startActivity(intent);
    }

    private void switchToFindNewBuddie() {
        Intent intent = new Intent(this.mActivity, FindNewBuddiesActivity.class);
        this.mActivity.startActivity(intent);
    }

    private void switchToRespondRequests() {
        Intent intent = new Intent(this.mActivity, RespondRequestsActivity.class);
        this.mActivity.startActivity(intent);
    }

    private void switchToMap() {
        Intent intent = new Intent(this.mActivity, MapActivity.class);
        this.mActivity.startActivity(intent);
    }

    private void switchToTakePicture() {
        Intent intent = new Intent(this.mActivity, TakePictureActivity.class);
        this.mActivity.startActivity(intent);
    }

    private void switchToSettings() {
        Intent intent = new Intent(this.mActivity, SettingsActivity.class);
        this.mActivity.startActivity(intent);
    }

    private void setupActionBar(Activity theActivity) {
        final Activity activity = theActivity;

        // this works on API 11 and above
        ActionBar actionBar = theActivity.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        this.mDrawerToggle = new ActionBarDrawerToggle(theActivity, this.mDrawerLayout,
                R.drawable.ic_drawer, R.string.open_drawer_message, R.string.close_drawer_message) {
            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
                activity.invalidateOptionsMenu();
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
                activity.invalidateOptionsMenu();
            }
        };
    }
}
