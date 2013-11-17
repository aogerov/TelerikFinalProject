package com.gercho.findmybuddies.data;

        import android.content.Context;
        import android.database.Cursor;
        import android.net.Uri;
        import android.provider.MediaStore;
        import android.util.Log;

        import java.io.DataOutputStream;
        import java.io.File;
        import java.io.FileInputStream;
        import java.io.IOException;
        import java.io.InputStream;
        import java.net.HttpURLConnection;
        import java.net.URL;

/**
 * Created by Gercho on 11/16/13.
 */
public class ImageUploader {

    private static final String BASE_URI = "http://uploads.im/api?upload";

    public static HttpResponse sendImage(Context context, Uri imageUri) {
        String fileName = getRealPathFromURI(context, imageUri);
        HttpURLConnection urlConnection;
        DataOutputStream dos;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;

        File sourceFile = new File(fileName);
        if (!sourceFile.isFile())
        {
            Log.e("uploadFile", "Source File Does not exist");
        }

        try {
            FileInputStream fileInputStream = new FileInputStream(sourceFile);
            URL url = new URL(BASE_URI);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setDoInput(true);
            urlConnection.setDoOutput(true);
            urlConnection.setUseCaches(false);
            urlConnection.setRequestMethod("POST");
            urlConnection.setRequestProperty("Connection", "Keep-Alive");
            urlConnection.setRequestProperty("ENCTYPE", "multipart/form-data");
            urlConnection.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
            urlConnection.setRequestProperty("uploaded_file", fileName);

            dos = new DataOutputStream(urlConnection.getOutputStream());
            dos.writeBytes(twoHyphens + boundary + lineEnd);
            dos.writeBytes("Content-Disposition: form-data; name=\"uploaded_file\";filename=\""+ fileName + "\"" + lineEnd);
            dos.writeBytes(lineEnd);

            bytesAvailable = fileInputStream.available();
            bufferSize = Math.min(bytesAvailable, maxBufferSize);
            buffer = new byte[bufferSize];
            bytesRead = fileInputStream.read(buffer, 0, bufferSize);

            while (bytesRead > 0) {
                dos.write(buffer, 0, bufferSize);
                bytesAvailable = fileInputStream.available();
                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);
            }

            dos.writeBytes(lineEnd);
            dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

            fileInputStream.close();
            dos.flush();
            dos.close();

            boolean isStatusOk  = (urlConnection.getResponseCode() == 200);
            String responseMessage = receiveResponse(urlConnection);
            return new HttpResponse(isStatusOk, responseMessage);

        } catch (Exception ex) {
            return new HttpResponse(false, ex.getMessage());
        }
    }

    private static String getRealPathFromURI(Context context, Uri imageUri) {
        Cursor cursor = null;
        try {
            String[] projection = {MediaStore.Images.Media.DATA};
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

    private static String receiveResponse(HttpURLConnection urlConnection)
            throws IOException {
        urlConnection.setConnectTimeout(10000);
        urlConnection.setReadTimeout(10000);
        InputStream inputStream = null;
        try {
            inputStream = urlConnection.getInputStream();
            int ch;
            StringBuilder stringBuilder = new StringBuilder();
            while ((ch = inputStream.read()) != -1) {
                stringBuilder.append((char) ch);
            }

            return stringBuilder.toString();
        } catch (IOException e) {
            throw e;
        } finally {
            if (inputStream != null) {
                inputStream.close();
            }
        }
    }
}