package com.motiondetect.detector.data.storage;

import android.util.Log;

import com.motiondetect.detector.data.dropbox.DropboxClient;

import java.io.File;

/**
 * Backup to Dropbox.
 */
public class DropboxStorage extends BaseStorage {
    private static final String TAG = DropboxStorage.class.getSimpleName();

    private DropboxClient mDropboxClient;

    public DropboxStorage(DropboxClient client) {
        super();
        Log.d(TAG, "Create DropboxStorage");
        mDropboxClient = client;
    }

    @Override
    public boolean isAvailable() {
        return mDropboxClient.isAvailable();
    }

    @Override
    public void open() {
        mState = STATE.OPEN;
    }

    @Override
    public <T> void save(final OnSaveFileCallback callback, final T... params) {
        if (!mDropboxClient.isAvailable()) {
            return;
        }

        invokeAsync(new Runnable() {
            @Override
            public void run() {
                // Delete the local files if they have been uploaded successfully.
                File analyzedFile = new File(FILE_PATH_ANALYZED);
                if (analyzedFile.exists()) {
                    uploadFile(callback, analyzedFile, (String) params[0]);
                    analyzedFile.delete();
                }

                File rawFile = new File(FILE_PATH_RAW);
                if (rawFile.exists()) {
                    uploadFile(callback, rawFile, (String) params[1]);
                    rawFile.delete();
                }

                callback.onSaved();
            }
        });
    }

    private void uploadFile(OnSaveFileCallback callback, File file, String targetName) {
        Log.d(TAG, "uploadFile() - Uploading " + targetName);
        boolean success = mDropboxClient.uploadAndFinish(file, targetName);
        if (!success) {
            callback.onError("Failed to upload " + targetName);
        }
    }

    @Override
    public void close() {
        mState = STATE.CLOSE;
    }

    @Override
    public void release() {
        mState = STATE.CLOSE;
        terminate();
    }
}
