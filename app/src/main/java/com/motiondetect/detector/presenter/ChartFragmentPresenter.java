package com.motiondetect.detector.presenter;

import android.os.SystemClock;
import android.util.Log;

import com.motiondetect.detector.contract.ChartFragmentContract;
import com.motiondetect.detector.model.ChartPointValue;
import com.motiondetect.detectorjni.model.MotionResult;

import java.util.ArrayList;
import java.util.List;

import static com.motiondetect.detector.contract.ChartFragmentContract.View;

public class ChartFragmentPresenter implements ChartFragmentContract.Presenter {
    private static final String TAG = ChartFragmentPresenter.class.getSimpleName();

    private ChartPointValue[] mMeanMotionsData;
    private List<MotionResult> mChartData;

    private long mBaseTimeMillis;
    private int mStepCount = 0;

    private final ChartFragmentContract.View mView;

    private Runnable mUpdateChartsRunnable = new Runnable() {
        @Override
        public void run() {
            if (isNeedUpdateCharts()) {
                ChartPointValue[] values = computeMeanMotionsPerInterval();
                mView.updateCharts(values);
                mView.scheduleNextUpdate(this, false);
            }
        }
    };

    public ChartFragmentPresenter(ChartFragmentContract.View view) {
        mView = view;
    }

    private ChartPointValue[] computeMeanMotionsPerInterval() {
        final float elapsedTime = SystemClock.uptimeMillis() - mBaseTimeMillis;
        final int newXAxisValue =
                (int) ((elapsedTime / View.CHART_TIME_LIMIT_MILLIS) * View.CHART_DATA_SAMPLING_PRECISION);

        float avgX = 0f;
        float avgY = 0f;
        float avgZ = 0f;
        for (MotionResult tmp : mChartData) {
            avgX += tmp.motionX;
            avgY += tmp.motionY;
            avgZ += tmp.motionZ;
        }
        final int size = mChartData.size();
        if (size == 0) {
            avgX = avgY = avgZ = 0f;
        } else {
            avgX /= size;
            avgY /= size;
            avgZ /= size;
        }
        mChartData.clear();

        mMeanMotionsData[View.MOTION_X_CHART_IDX].x = newXAxisValue;
        mMeanMotionsData[View.MOTION_X_CHART_IDX].y = avgX;
        mMeanMotionsData[View.MOTION_Y_CHART_IDX].x = newXAxisValue;
        mMeanMotionsData[View.MOTION_Y_CHART_IDX].y = avgY;
        mMeanMotionsData[View.MOTION_Z_CHART_IDX].x = newXAxisValue;
        mMeanMotionsData[View.MOTION_Z_CHART_IDX].y = avgZ;

        return mMeanMotionsData;
    }

    @Override
    public void initialize() {
        mChartData = new ArrayList<>();
        mMeanMotionsData = new ChartPointValue[View.CHART_COUNT];
        for (int i = 0; i < View.CHART_COUNT; i++) {
            mMeanMotionsData[i] = new ChartPointValue(0, 0f);
        }

        mView.initializeCharts();
    }

    private boolean isNeedUpdateCharts() {
        return SystemClock.uptimeMillis() - mBaseTimeMillis < View.CHART_TIME_LIMIT_MILLIS;
    }

    @Override
    public void updateData(MotionResult result) {
        mStepCount++;

        if (isNeedUpdateCharts()) {
            Log.d(TAG, "updateData() - steps: " + mStepCount
                    + ", x/y/z: " + result.motionX + "/" + result.motionY + "/" + result.motionZ);
            mChartData.add(result);
            mView.updateBinding(result, mStepCount);
        }
    }

    @Override
    public void cancelUpdate() {
        mView.cancelSchedule(mUpdateChartsRunnable);
    }

    @Override
    public void refreshDisplay() {
        mStepCount = 0;
        mView.refreshCharts();

        refreshData();

        mView.scheduleNextUpdate(mUpdateChartsRunnable, true);
        mView.updateBinding(new MotionResult(), 0);
    }

    private void refreshData() {
        mChartData.clear();
        for (int i = 0; i < View.CHART_COUNT; i++) {
            mMeanMotionsData[i].reset();
        }

        mBaseTimeMillis = SystemClock.uptimeMillis();
    }
}
