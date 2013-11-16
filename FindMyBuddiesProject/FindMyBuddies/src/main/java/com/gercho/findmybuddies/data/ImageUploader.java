package com.gercho.findmybuddies.data;

import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.MediaStore;

import com.gercho.findmybuddies.helpers.LogHelper;

import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.InputStreamEntity;
import org.apache.http.impl.client.DefaultHttpClient;

import java.io.File;
import java.io.FileInputStream;

//import android.net.Uri;

/**
 * Created by Gercho on 11/16/13.
 */
public class ImageUploader {

    public void sendImage(Context context, Uri imageUri) {
        String url = "https://api.imgur.com/3/image";
        String filePath = getRealPathFromURI(context, imageUri);

        File file = new File(filePath);

        try {
            FileInputStream fileInputStream = new FileInputStream(file);
            InputStreamEntity inputStreamEntity = new InputStreamEntity(fileInputStream, -1);
            inputStreamEntity.setContentType("binary/octet-stream");
            inputStreamEntity.setChunked(true);

            HttpClient httpclient = new DefaultHttpClient();
            HttpPost httppost = new HttpPost(url);
            httppost.setEntity(inputStreamEntity);

            HttpResponse response = httpclient.execute(httppost);

        } catch (Exception ex) {
            LogHelper.logThreadId(ex.getMessage());
        }
    }

    private String getRealPathFromURI(Context context, Uri imageUri) {
        Cursor cursor = null;
        try {
            String[] projection = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(imageUri, projection, null, null, null);
            int columnIndex = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(columnIndex);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }
}