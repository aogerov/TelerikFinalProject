package com.gercho.findmybuddies.http;

import org.apache.http.client.HttpClient;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.BasicResponseHandler;
import org.apache.http.impl.client.DefaultHttpClient;

/**
 * Created by Gercho on 11/10/13.
 */
public class HttpRequester {

    private static final String BASE_URI = "http://wherearemybuddiesapi.apphb.com/api/";
    private static final String BASE_URI_LOCAL = "http://localhost:34585/api/";

    public HttpResponse get(String uriEnd, String sessionKey) {
        String uri = BASE_URI + uriEnd + sessionKey;

        String content = null;
        String error = null;

        try {
            HttpClient client = new DefaultHttpClient();
            HttpGet httpGet = new HttpGet(uri);
            httpGet.setHeader("Content-type", "application/json");
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            content = client.execute(httpGet, responseHandler);
        } catch (Exception e) {
            error = e.getMessage();
        }

        if (error != null) {
            return new HttpResponse(false, error);
        } else {
            return new HttpResponse(true, content);
        }
    }

    public HttpResponse post(String uriEnd, String data, String sessionKey) {
        String uri = BASE_URI + uriEnd;
        if (sessionKey != null) {
            uri += sessionKey;
        }

        String content = null;
        String error = null;

        try {
            HttpClient client = new DefaultHttpClient();
            HttpPost httpPost = new HttpPost(uri);
            httpPost.setEntity(new StringEntity(data));
            httpPost.setHeader("Content-type", "application/json");
            ResponseHandler<String> responseHandler = new BasicResponseHandler();
            content = client.execute(httpPost, responseHandler);
        } catch (Exception e) {
            error = e.getMessage();
        }

        if (error != null) {
            return new HttpResponse(false, error);
        } else {
            return new HttpResponse(true, content);
        }
    }
}
