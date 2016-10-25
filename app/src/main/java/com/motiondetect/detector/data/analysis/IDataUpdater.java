package com.motiondetect.detector.data.analysis;

import com.motiondetect.detectorjni.model.MotionResult;

public interface IDataUpdater {
    void onUpdateData(MotionResult result);
}
