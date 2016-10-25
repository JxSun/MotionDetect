package com.motiondetect.detector.data.storage;

import com.motiondetect.detector.base.IReleasable;

public interface IStorage extends IReleasable {
    boolean isAvailable();
    void open();
    void close();
    <T> void save(OnSaveFileCallback callback, T... params);

    enum STATE {
        OPEN, CLOSE
    }

    interface OnSaveFileCallback {
        void onSaved();
        void onError(String msg);
    }
}
