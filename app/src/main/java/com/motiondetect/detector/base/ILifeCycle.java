package com.motiondetect.detector.base;

public interface ILifeCycle {
    void onCreate();
    void onStart();
    void onResume();
    void onPause();
    void onStop();
    void onDestroy();
}
