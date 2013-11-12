package com.gercho.findmybuddies;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.ViewPager;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;

import com.gercho.findmybuddies.helpers.NavigationDrawer;
import com.gercho.findmybuddies.services.UserService;

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_find_my_buddies);

        this.startServices();

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

    private void startServices() {

    }

    private void logout() {
        Intent userServiceIntent = new Intent();
        userServiceIntent.setAction(UserService.LOGOUT_USER_SERVICE);
        this.startService(userServiceIntent);

        Intent mainActivityIntent = new Intent(this, MainActivity.class);
        this.startActivity(mainActivityIntent);
    }
}