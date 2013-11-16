package com.gercho.findmybuddies.activities;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;

import com.gercho.findmybuddies.helpers.ServiceActions;
import com.gercho.findmybuddies.services.BuddiesService;

/**
 * Created by Gercho on 11/16/13.
 */
public class TakePictureActivity extends Activity {

    private static final int TAKE_PICTURE_REQUEST_CODE = 1000;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onResume() {
        super.onResume();
        this.takePicture();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        if (resultCode == RESULT_OK && requestCode == TAKE_PICTURE_REQUEST_CODE) {
            this.handleTakePictureResult(resultIntent);
        }

        this.finish();
    }

    private void takePicture() {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        this.startActivityForResult(intent, TAKE_PICTURE_REQUEST_CODE);
    }

    private void handleTakePictureResult(Intent resultIntent) {
        Uri photoPath = resultIntent.getData();
        Intent intent = new Intent();
        intent.setAction(ServiceActions.SEND_NEW_IMAGE);
        intent.putExtra(BuddiesService.NEW_IMAGE_URI_EXTRA, photoPath);
        this.startService(intent);
    }
}