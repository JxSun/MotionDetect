package com.motiondetect.detector.base;

interface IAsyncExecutor {
    void invokeAsync(Runnable runnable);
}
