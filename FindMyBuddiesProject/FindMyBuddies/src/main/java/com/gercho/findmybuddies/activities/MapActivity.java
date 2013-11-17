package com.gercho.findmybuddies.activities;

import android.app.Activity;
import android.os.Bundle;
import android.view.Menu;

import com.gercho.findmybuddies.R;
import com.gercho.findmybuddies.helpers.ToastNotifier;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

/**
 * Created by Gercho on 11/16/13.
 */
public class MapActivity extends Activity {

    static final LatLng HAMBURG = new LatLng(53.558, 9.927);
    static final LatLng KIEL = new LatLng(53.551, 9.993);
    private GoogleMap map;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.setContentView(R.layout.activity_map);
    }

    @Override
    protected void onResume() {
        super.onResume();
        boolean isVersionValid = this.validateVersion();
        if (isVersionValid) {
            this.activateMap();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.logout, menu);
        return true;
    }

    private void activateMap() {
        map = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                .getMap();
        Marker hamburg = map.addMarker(new MarkerOptions().position(HAMBURG)
                .title("Hamburg"));
        Marker kiel = map.addMarker(new MarkerOptions()
                .position(KIEL)
                .title("Kiel")
                .snippet("Kiel is cool")
                .icon(BitmapDescriptorFactory
                        .fromResource(R.drawable.ic_launcher)));

//        Move the camera instantly to hamburg with a zoom of 15.
        map.moveCamera(CameraUpdateFactory.newLatLngZoom(HAMBURG, 15));

        // Zoom in, animating the camera.
        map.animateCamera(CameraUpdateFactory.zoomTo(10), 2000, null);
    }

    private boolean validateVersion() {
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);

        if (resultCode == ConnectionResult.SUCCESS) {
            ToastNotifier.makeToast(this, "All OK");
            return true;
        } else  if (resultCode == ConnectionResult.SERVICE_DISABLED) {
            GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_DISABLED, this, 0);
        } else if (resultCode == ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED) {
            GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED, this, 0);
        } else if (resultCode == ConnectionResult.SERVICE_MISSING) {
            GooglePlayServicesUtil.getErrorDialog(ConnectionResult.SERVICE_MISSING, this, 0);
        }

        return false;
    }
}