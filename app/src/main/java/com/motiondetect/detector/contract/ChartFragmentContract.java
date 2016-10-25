package com.motiondetect.detector.contract;

import com.motiondetect.detector.model.ChartPointValue;
import com.motiondetect.detector.presenter.BasePresenter;
import com.motiondetect.detector.view.BaseView;
import com.motiondetect.detectorjni.model.MotionResult;

import java.text.DecimalFormat;

public interface ChartFragmentContract {
    interface View extends BaseView<Presenter> {
        int CHART_TIME_LIMIT_MILLIS = 20000; // 20 seconds
        int CHART_UPDATE_TIME_MILLIS = 200; // 0.2 second
        int CHART_DATA_SAMPLING_PRECISION = CHART_TIME_LIMIT_MILLIS / CHART_UPDATE_TIME_MILLIS;
        int CHART_COUNT = 3;
        int MOTION_X_CHART_IDX = 0;
        int MOTION_Y_CHART_IDX = 1;
        int MOTION_Z_CHART_IDX = 2;
        String[] CHART_DESCRIPTIONS = {
                "Motion X", "Motion Y", "Motion Z"
        };

        DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.00");

        void initializeCharts();
        void updateCharts(ChartPointValue[] meanTorques);
        void refreshCharts();
        void updateBinding(MotionResult result, int mStepCount);
        void scheduleNextUpdate(Runnable updateTask, boolean reschedule);
        void cancelSchedule(Runnable updateTask);
    }

    interface Presenter extends BasePresenter {
        void initialize();
        void refreshDisplay();
        void updateData(MotionResult result);
        void cancelUpdate();
    }
}
