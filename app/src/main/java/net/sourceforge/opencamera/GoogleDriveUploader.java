package net.sourceforge.opencamera;

import android.content.Context;

import android.util.Log;

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.HttpHeaderParser;
import com.android.volley.toolbox.StringRequest;

import net.gotev.uploadservice.BinaryUploadRequest;
import net.gotev.uploadservice.UploadNotificationConfig;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by evansu on 3/12/18.
 * GoogleDriveUploader is a singleton class responsible for communication with Google Drive API.
 * It performs resumable upload with the android-upload-service library (https://github.com/gotev/android-upload-service).
 * Please see https://developers.google.com/drive/v3/web/resumable-upload for the protocal of resumable upload.
 * An upload process consists of three steps.
 * 1. Get access token of google drive api
 * 2. Init resumable (upload meta data of the file and get a file ID)
 * 3. Upload resumable (upload binary part of the file)
 *
 * Todo: using OAuth to get token is insecure. Maybe API Key.
 */

public class GoogleDriveUploader {
    private static final String TAG = "GoogleDirveUploader";
    private static GoogleDriveUploader mInstance;
    private static Context mCtx;
    private static final String url = "https://www.googleapis.com/oauth2/v4/token";
    private static final String initurl = "https://www.googleapis.com/upload/drive/v3/files?uploadType=resumable";

    private GoogleDriveUploader(Context context) {
        mCtx = context.getApplicationContext();
    }

    public static synchronized GoogleDriveUploader getInstance(Context context) {
        if (mInstance == null) {
            mInstance = new GoogleDriveUploader(context);
        }
        return mInstance;
    }
    public static synchronized void initialize(Context context){
        if (mInstance == null) {
            mInstance = new GoogleDriveUploader(context);
        }
    }
    // can get Instance without context but may throw if instance not initialized
    public static GoogleDriveUploader getInstance() throws NullPointerException{
        if(mInstance == null) {
            throw new NullPointerException("Google drive instance is not initialized. ");
        }
        return mInstance;
    }
    public interface VolleyCallback{
        void onSuccess(String result);
    }
    private void getToken(final VolleyCallback callback){
        StringRequest tokenRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        try {
                            JSONObject jsonResponse = new JSONObject(response);
                            String access_token = jsonResponse.getString("access_token");
                            Log.d(TAG, "Access token is: " + access_token);
                            callback.onSuccess(access_token);

                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                }, new Response.ErrorListener() {
            @Override
            public void onErrorResponse(VolleyError error) {
                if(error.networkResponse != null){
                    int  statusCode = error.networkResponse.statusCode;
                    NetworkResponse response = error.networkResponse;
                    Log.d("testerror",""+statusCode+" "+response.data);
                } else {
                    Log.d(TAG, "onErrorResponse: networkResponse is null");
                }
            }
        }){
            @Override
            protected Map<String, String> getParams()
            {
                Map<String, String>  params = new HashMap<String, String>();
                params.put("grant_type", "refresh_token");
                params.put("client_id", "27478463154-fiqbn4p677ffb0gtu4ucfl4s1ftovns8.apps.googleusercontent.com");
                params.put("client_secret", "BRvPhAMICFmHa4h7uf9bRLrW");
                params.put("refresh_token", "1/USjQN9w9pkZ460x9eAq-jGHcpt7p4hFOl4NSbnnbkkU");

                return params;
            }

            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String>  headers = new HashMap<String, String>();
                headers.put("Content-Type", "application/x-www-form-urlencoded");
                return headers;
            }
        };
        RequestQueue queue = VolleyQueue.getInstance(mCtx).getRequestQueue();
        queue.add(tokenRequest);
    }
    private void initResumable(final String token, final String metaData, final VolleyCallback callback){
        StringRequest initialRequest = new StringRequest(Request.Method.POST, initurl,
                new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        // Display the first 500 characters of the response string.
                        Log.d(TAG, "onResponse initResumable: " + response);
                        callback.onSuccess(response);
                    }
                }, new Response.ErrorListener() {
            @Override

            public void onErrorResponse(VolleyError error) {
                String body;
                try {
                    body = new String(error.networkResponse.data,"UTF-8");
                    Log.d(TAG, "That didn't work!\n" + body);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }

            }
        }){
            @Override
            public byte[] getBody(){
                Log.d(TAG, "getBody: Call getBody");
                return metaData.getBytes();
            }
            @Override
            public String getBodyContentType()
            {
                return "application/json";
            }
            @Override
            public Map<String, String> getHeaders()
            {
                Map<String, String>  headers = new HashMap<String, String>();
                headers.put("Authorization", "Bearer " + token);
                return headers;
            }
            @Override
            protected Response<String> parseNetworkResponse(NetworkResponse response) {
                Log.d(TAG, "parseNetworkResponse: ");
                return Response.success(response.headers.get("Location"),
                        HttpHeaderParser.parseCacheHeaders(response));

            }
        };
        RequestQueue queue = VolleyQueue.getInstance(mCtx).getRequestQueue();
        queue.add(initialRequest);
    }
    private void initResumable(final String token, final Map<String, String> metaData, final VolleyCallback callback){
        initResumable(token, new JSONObject(metaData).toString(), callback);
    }
    private String uploadResumable(String accessToken, String gdLocation, String filePath) throws IOException {
        UploadNotificationConfig notificationConfig = new UploadNotificationConfig();
        notificationConfig.getCompleted().autoClear = true;
		notificationConfig.setRingToneEnabled(false);
        return new BinaryUploadRequest(mCtx, gdLocation)
                .addHeader("Authorization", String.format("Bearer %s", accessToken))
                .setNotificationConfig(notificationConfig)
                .setFileToUpload(filePath)
                .setAutoDeleteFilesAfterSuccessfulUpload(true)
                .setUsesFixedLengthStreamingMode(true)
                .setMaxRetries(1)
                .startUpload();
    }

    public void upload(final String filePath, final Map<String, String> metaData){
        upload(filePath, new JSONObject(metaData).toString());
    }
    public void upload(final String filePath, final String metaData){
        getToken(new VolleyCallback() {
            @Override
            public void onSuccess(final String token) {
                initResumable(token, metaData, new VolleyCallback() {
                    @Override
                    public void onSuccess(String location) {
                        Log.d(TAG, "onSuccess:" + location);
                        try {
                            uploadResumable(token, location, filePath);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                });
            }
        });
    }

}
