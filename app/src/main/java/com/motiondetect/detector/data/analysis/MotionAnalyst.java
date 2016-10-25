package com.motiondetect.detector.data.analysis;

import android.util.Log;

import com.motiondetect.detector.base.IBackgroundWorker;
import com.motiondetect.detector.base.IReleasable;
import com.motiondetect.detector.data.bluetooth.ArduinoReceiver;
import com.motiondetect.detector.model.MotionModel;
import com.motiondetect.detector.util.MotionConverter;
import com.motiondetect.detectorjni.NativeMotionAnalyst;
import com.motiondetect.detectorjni.model.MotionResult;

import java.util.ArrayList;
import java.util.List;

public class MotionAnalyst extends IBackgroundWorker
        implements IReleasable, ArduinoReceiver.OnReceiveDataListener {
    private static final String TAG = MotionAnalyst.class.getSimpleName();

    private volatile List<Callback> mCallbacks;
    private NativeMotionAnalyst mNativeAnalyst;

    private final NativeMotionAnalyst.NativeCallback mNativeCallback = new NativeMotionAnalyst.NativeCallback() {
        @Override
        public void onAnalysisResult(MotionResult result) {
            for (Callback callback : mCallbacks) {
                callback.onAnalysisResult(result);
            }
        }
    };

    public MotionAnalyst() {
        super();
        mCallbacks = new ArrayList<>();
        mNativeAnalyst = new NativeMotionAnalyst();
    }

    @Override
    protected void onPreExecuteAsync() {
        Log.v(TAG, "onPreExecuteAsync() - register callback");
        mNativeAnalyst.registerCallback(mNativeCallback);
    }

    @Override
    protected void onPostExecuteAsync() {
        Log.v(TAG, "onPostExecuteAsync() - unregister callback");
        mNativeAnalyst.unregisterCallback();
    }

    public MotionAnalyst addCallback(Callback callback) {
        if (callback == null) {
            throw new IllegalArgumentException("Callback cannot be null");
        }
        Log.v(TAG, "addCallback(" + callback.toString() + ")");
        mCallbacks.add(callback);
        return this;
    }

    @Override
    public void onReceive(final List<MotionModel> data) {
        invokeAsync(new Runnable() {
            @Override
            public void run() {
                mNativeAnalyst.analysisStepsAsync(MotionConverter.convert(data));
            }
        });
    }

    @Override
    public void release() {
        mCallbacks.clear();
        terminate();
    }

    public interface Callback {
        void onAnalysisResult(MotionResult result);
    }
}
