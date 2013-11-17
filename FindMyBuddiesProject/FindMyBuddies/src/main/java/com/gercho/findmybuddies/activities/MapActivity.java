package com.gercho.findmybuddies.activities;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Editable;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.devices.LocationInfo;
import com.gercho.findmybuddies.helpers.LogoutAssistant;
import com.gercho.findmybuddies.helpers.NavigationDrawer;
import com.gercho.findmybuddies.helpers.ServiceActions;
import com.gercho.findmybuddies.helpers.ToastNotifier;
import com.gercho.findmybuddies.models.BuddieModel;
import com.gercho.findmybuddies.models.CoordinatesModel;
import com.gercho.findmybuddies.services.BuddiesService;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.gson.Gson;

/**
 * Created by Gercho on 11/16/13.
 */
public class MapActivity extends Activity implements ListView.OnItemClickListener {

    public static final String BUDDIE_NOT_FOUND = "No buddie with that name found in your list";

    private NavigationDrawer mNavigationDrawer;
    private BuddiesServiceUpdateReceiver mBuddiesServiceUpdateReceiver;
    private GoogleMap mMap;
    private LocationInfo mLocationInfo;
    private BuddieModel[] mBuddies;
    private boolean mIsCameraAtMyLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_map);

        this.mNavigationDrawer = new NavigationDrawer();
        this.mNavigationDrawer.init(this, this);
        this.mNavigationDrawer.setSelection(NavigationDrawer.DRAWER_OPTION_MAP);
        this.mLocationInfo = new LocationInfo(this);
    }

    @Override
    protected void onResume() {
        super.onResume();

        boolean isVersionValid = this.validateVersion();
        if (!isVersionValid) {
            this.finish();
        }

        if (this.mBuddiesServiceUpdateReceiver == null) {
            this.mBuddiesServiceUpdateReceiver = new BuddiesServiceUpdateReceiver();
            IntentFilter intentFilter = new IntentFilter(BuddiesService.BUDDIES_SERVICE_BROADCAST);
            this.registerReceiver(this.mBuddiesServiceUpdateReceiver, intentFilter);
        }

        this.loadMap();

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

    private void loadMap() {
        mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map)).getMap();
        mMap.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
        this.setupViews();
    }

    private void setupViews() {
        Button findButton = (Button) this.findViewById(R.id.button_find);
        findButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MapActivity.this.handleFindBuddie();
            }
        });
    }

    private void handleFindBuddie() {
        EditText findBuddieEditText = (EditText) this.findViewById(R.id.editText_nicknameMap);
        Editable nicknameEditable = findBuddieEditText.getText();
        if (nicknameEditable != null) {
            String nickname = nicknameEditable.toString().trim();
            for(BuddieModel buddie : this.mBuddies) {
                if (buddie.getNickname().equalsIgnoreCase(nickname)) {
                    LatLng buddiePosition = new LatLng(buddie.getLatitude(), buddie.getLongitude());
                    this.moveCamera(buddiePosition);
                    this.mIsCameraAtMyLocation = false;
                    return;
                }
            }
        }

        ToastNotifier.makeToast(this, BUDDIE_NOT_FOUND);
    }

    private void handleBuddiesUpdated(BuddieModel[] buddies) {
        this.mMap.clear();
        this.mBuddies = buddies;
        this.setMyLocation();
        this.setBuddiesLocation();
    }

    private void setMyLocation() {
        CoordinatesModel coordinates = this.mLocationInfo.getLastKnownLocation();
        LatLng myPosition = new LatLng(coordinates.getLatitude(), coordinates.getLongitude());
        this.mMap.addMarker(new MarkerOptions()
                .position(myPosition)
                .title("It's Me")
                .icon(BitmapDescriptorFactory.fromResource(R.drawable.ic_launcher)));

        if (!this.mIsCameraAtMyLocation) {
            this.mIsCameraAtMyLocation = true;
            this.moveCamera(myPosition);
        }
    }

    private void setBuddiesLocation() {
        for(BuddieModel buddie : this.mBuddies) {
            LatLng buddiePosition = new LatLng(buddie.getLatitude(), buddie.getLongitude());
            this.mMap.addMarker(new MarkerOptions()
                    .position(buddiePosition)
                    .title(buddie.getNickname())
                    .snippet(buddie.getCoordinatesTimestampDifference()));
        }
    }

    private void moveCamera(LatLng position) {
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(position, 15));
    }

    private boolean validateVersion() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode == ConnectionResult.SUCCESS) {
            return true;
        }

        if (resultCode == ConnectionResult.SERVICE_DISABLED) {
            GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_DISABLED, this, 0);
        } else if (resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, this, 0);
        } else if (resultCode == ConnectionResult.SERVICE_MISSING) {
            GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_MISSING, this, 0);
        }

        return false;
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
                String buddieModelsAsJson = intent.getStringExtra(BuddiesService.BUDDIES_INFO_UPDATE_EXTRA);
                if (buddieModelsAsJson != null) {
                    BuddieModel[] buddies = this.mGson.fromJson(buddieModelsAsJson, BuddieModel[].class);
                    if (buddies != null && buddies.length > 0) {
                        MapActivity.this.handleBuddiesUpdated(buddies);
                    }
                }
            }
        }
    }
}