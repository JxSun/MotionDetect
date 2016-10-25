package com.motiondetect.detector.data.dropbox;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.dropbox.core.DbxRequestConfig;
import com.dropbox.core.android.Auth;
import com.dropbox.core.http.OkHttp3Requestor;
import com.dropbox.core.v2.DbxClientV2;
import com.motiondetect.detector.R;

public class DropboxServiceManager {
    private static final String TAG = DropboxServiceManager.class.getSimpleName();
    private static final String PROJECT_NAME = "motiondetect";
    private static final String VERSION = "1.0";
    private static final String FULL_NAME = PROJECT_NAME + "/" + VERSION;

    private final Context mContext;
    private String mAccessToken = "";
    private DropboxClient mDbxClient = DropboxClient.NULL;

    public DropboxServiceManager(Context context) {
        mContext = context;
    }

    public void requestAuthentication() {
        Auth.startOAuth2Authentication(mContext, mContext.getString(R.string.dropbox_test_app_key));
    }

    public String loadAccessToken() {
        if (mAccessToken.isEmpty()) {
            SharedPreferences prefs = mContext.getSharedPreferences("motiondetect", Context.MODE_PRIVATE);

            String accessToken = prefs.getString("access-token", null);
            if (accessToken == null) {
                accessToken = Auth.getOAuth2Token();
                if (accessToken != null) {
                    Log.d(TAG, "getAccessToken() - update access token");
                    prefs.edit().putString("access-token", accessToken).apply();
                    mAccessToken = accessToken;
                } else {
                    Log.w(TAG, "getAccessToken() - still has no access token");
                    mAccessToken = "";
                }
            } else {
                mAccessToken = accessToken;
            }
        }

        return mAccessToken;
    }

    public DropboxClient getClient() {
        if (mAccessToken.isEmpty()) {
            Log.w(TAG, "getClient() - no access token.");
            return DropboxClient.NULL;
        }

        if (!mDbxClient.isAvailable()) {
            DbxRequestConfig requestConfig = DbxRequestConfig.newBuilder(FULL_NAME)
                    .withHttpRequestor(OkHttp3Requestor.INSTANCE)
                    .build();
            mDbxClient = new DropboxClient(new DbxClientV2(requestConfig, mAccessToken));
        }
        return mDbxClient;
    }
}
