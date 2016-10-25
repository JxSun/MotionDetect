package com.motiondetect.detector.contract;

import com.motiondetect.detector.data.bluetooth.BtManager;
import com.motiondetect.detector.data.dropbox.DropboxServiceManager;

public interface MotionDetectAppContract {
    BtManager getBtManager();
    DropboxServiceManager getDropboxServiceManager();
}
