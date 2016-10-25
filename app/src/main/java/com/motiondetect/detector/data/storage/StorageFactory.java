package com.motiondetect.detector.data.storage;

import android.content.Context;

import com.motiondetect.detector.MotionDetectApplication;

public class StorageFactory {
    public static final int STORAGE_LOCAL = 0;
    public static final int STORAGE_DROPBOX = 1;
    private final Context mContext;

    public StorageFactory(Context context) {
        mContext = context;
    }

    public IStorage create(final int storageId) {
        if (storageId != STORAGE_LOCAL && storageId != STORAGE_DROPBOX) {
            throw new IllegalArgumentException("Invalid storage ID: " + storageId);
        }

        BaseStorage storage = null;
        switch (storageId) {
            case STORAGE_LOCAL:
                storage = new LocalFileStorage();
                break;
            case STORAGE_DROPBOX:
                storage =  new DropboxStorage(
                        ((MotionDetectApplication) mContext.getApplicationContext()).getDropboxServiceManager().getClient());
                break;
        }

        storage.start();

        return storage;
    }
}
