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

import com.gercho.findmybuddies.helpers.NavigationDrawerHelper;

public class MainActivity extends FragmentActivity implements ListView.OnItemClickListener {

    public static final String EXTRA_COURSE_LIB = "course lib";

    private static final int COURSE_LIB_NOT_SET = -1;

//    private CoursePagerAdapter mCoursePagerAdapter;
    private ViewPager mViewPager;
    private NavigationDrawerHelper mNavigationDrawerHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_main);

//        this.mCoursePagerAdapter = new CoursePagerAdapter(
//                this.getSupportFragmentManager(), this);

//        this.mViewPager = (ViewPager) findViewById(R.id.pager);
//        this.mViewPager.setAdapter(this.mCoursePagerAdapter);

        this.mNavigationDrawerHelper = new NavigationDrawerHelper();
        this.mNavigationDrawerHelper.init(this, this);

        Intent startupIntent = this.getIntent();
        int courseLib = startupIntent.getIntExtra(EXTRA_COURSE_LIB, COURSE_LIB_NOT_SET);
        if (courseLib != COURSE_LIB_NOT_SET) {
//            this.mCoursePagerAdapter.setCourseLib(courseLib);
            this.mNavigationDrawerHelper.setSelection(courseLib);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        this.getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View view, int optionsLib, long l) {
//        this.mCoursePagerAdapter.setCourseLib(optionsLib);
        this.mNavigationDrawerHelper.handleSelect(optionsLib);
    }

    @Override
    protected void onPostCreate(Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        this.mNavigationDrawerHelper.syncState();
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        this.mNavigationDrawerHelper.handleOnPrepareOptionsMenu(menu);
        return super.onPrepareOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        this.mNavigationDrawerHelper.handleOnOptionsItemSelected(item);
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        this.mNavigationDrawerHelper.syncState();
    }
}
