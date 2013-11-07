package com.gercho.findmybuddies.helpers;

import android.app.ActionBar;
import android.app.Activity;
import android.support.v4.app.ActionBarDrawerToggle;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.services.UserService;

/**
 * Created by Gercho on 11/7/13.
 */
public class NavigationDrawer {

    private DrawerLayout mDrawerLayout;
    private ListView mDrawerListView;
    private ActionBarDrawerToggle mDrawerToggle;

    public void init(Activity theActivity, ListView.OnItemClickListener listener) {
        this.mDrawerLayout = (DrawerLayout) theActivity.findViewById(R.id.drawer_layout);
        this.mDrawerListView = (ListView) theActivity.findViewById(R.id.left_drawer);

        this.mDrawerLayout.setDrawerShadow(R.drawable.drawer_shadow, GravityCompat.START);

        String[] navigationDrawerOptions =
                theActivity.getResources().getStringArray(R.array.navigation_drawer_options);
        ArrayAdapter<String> navigationDrawerAdapter =
                new ArrayAdapter<String>(theActivity, R.layout.drawer_option_item, navigationDrawerOptions);

        this.mDrawerListView.setAdapter(navigationDrawerAdapter);
        this.mDrawerListView.setOnItemClickListener(listener);
//        this.mDrawerListView.setItemChecked(CoursePagerAdapter.COURSE_LIB_ANDROID, true);

        this.setupActionBar(theActivity);
        this.setupDrawerVisibility();
    }

    public void handleSelect(int option) {
        this.mDrawerListView.setItemChecked(option, true);
        this.mDrawerLayout.closeDrawer(this.mDrawerListView);
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

    public void syncState() {
        this.mDrawerToggle.syncState();
    }

    public void setSelection(int option) {
        this.mDrawerListView.setItemChecked(option, true);
    }

    public void setupDrawerVisibility() {
        UserService userService = new UserService();
        if (userService.getIsUserLoggedIn()) {
            this.mDrawerLayout.setVisibility(DrawerLayout.VISIBLE);

        } else {
            this.mDrawerLayout.setVisibility(DrawerLayout.INVISIBLE);
        }
    }

    private void setupActionBar(Activity theActivity) {
        final Activity activity = theActivity;

        // this works on API 11 and above
        ActionBar actionBar = theActivity.getActionBar();
        actionBar.setDisplayHomeAsUpEnabled(true);

        this.mDrawerToggle = new ActionBarDrawerToggle(
                theActivity,
                this.mDrawerLayout,
                R.drawable.ic_drawer,
                R.string.open_drawer_message,
                R.string.close_drawer_message
        ) {
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
