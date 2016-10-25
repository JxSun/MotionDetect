package com.motiondetect.detector.data.dropbox;

import android.support.annotation.NonNull;
import android.util.Log;

import com.dropbox.core.DbxException;
import com.dropbox.core.v2.DbxClientV2;
import com.dropbox.core.v2.files.WriteMode;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class DropboxClient {
    public static final DropboxClient NULL = new DropboxClient();
    private static final String TAG = DropboxClient.class.getSimpleName();

    private boolean mIsAvailable = false;
    private DbxClientV2 mClient = null;

    private DropboxClient() {
    }

    public DropboxClient(@NonNull DbxClientV2 client) {
        mClient = client;
        mIsAvailable = true;
    }

    public boolean isAvailable() {
        return mIsAvailable;
    }

    public DbxClientV2 get() {
        return mClient;
    }

    public boolean uploadAndFinish(File file, String targetName) {
        if (!isAvailable()) {
            Log.e(TAG, "Dropbox client not available yet.");
            return false;
        }

        try (InputStream inputStream = new FileInputStream(file)) {
            mClient.files().uploadBuilder("/" + targetName)
                    .withMode(WriteMode.OVERWRITE)
                    .uploadAndFinish(inputStream);
        } catch (DbxException | IOException e) {
            Log.e(TAG, e.getMessage());
            return false;
        }
        return true;
    }
}
