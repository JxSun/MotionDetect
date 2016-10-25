package com.motiondetect.detector;

import android.app.Application;

import com.motiondetect.detector.contract.MotionDetectAppContract;
import com.motiondetect.detector.data.bluetooth.BtManager;
import com.motiondetect.detector.data.dropbox.DropboxServiceManager;


public class MotionDetectApplication extends Application implements MotionDetectAppContract {
    private BtManager mBtManager;
    private DropboxServiceManager mDropboxServiceManager;

    @Override
    public BtManager getBtManager() {
        if (mBtManager == null) {
            mBtManager = new BtManager(this);
        }
        return mBtManager;
    }

    @Override
    public DropboxServiceManager getDropboxServiceManager() {
        if (mDropboxServiceManager == null) {
            mDropboxServiceManager = new DropboxServiceManager(this);
        }
        return mDropboxServiceManager;
    }
}
