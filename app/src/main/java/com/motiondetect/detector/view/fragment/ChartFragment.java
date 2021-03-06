package com.motiondetect.detector.view.fragment;

import android.databinding.DataBindingUtil;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.motiondetect.detector.R;
import com.motiondetect.detector.databinding.FragmentChartBinding;
import com.motiondetect.detector.model.ChartPointValue;
import com.motiondetect.detector.contract.ChartFragmentContract;
import com.motiondetect.detector.presenter.ChartFragmentPresenter;
import com.motiondetect.detector.view.activity.DataDisplayActivity;
import com.motiondetect.detectorjni.model.MotionResult;

import java.util.ArrayList;
import java.util.List;

import butterknife.BindViews;
import butterknife.ButterKnife;
import lecho.lib.hellocharts.model.Axis;
import lecho.lib.hellocharts.model.AxisValue;
import lecho.lib.hellocharts.model.Column;
import lecho.lib.hellocharts.model.ColumnChartData;
import lecho.lib.hellocharts.model.SubcolumnValue;
import lecho.lib.hellocharts.model.Viewport;
import lecho.lib.hellocharts.util.ChartUtils;
import lecho.lib.hellocharts.view.ColumnChartView;

public class ChartFragment extends BaseDisplayFragment implements ChartFragmentContract.View {
    public static final String TAG = ChartFragment.class.getSimpleName();

    // Co-play with MOTION_X_CHART_IDX, MOTION_Y_CHART_IDX and MOTION_Z_CHART_IDX.
    @BindViews({ R.id.motion_x_chart, R.id.motion_y_chart, R.id.motion_z_chart })
    List<ColumnChartView> mCharts;

    private FragmentChartBinding mBinding;
    private DataDisplayActivity mActivity;
    private ChartFragmentPresenter mPresenter;

    public static ChartFragment newInstance() {
        return new ChartFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_chart, container, false);
        View fragmentView = mBinding.getRoot();
        ButterKnife.bind(this, fragmentView);

        mActivity = (DataDisplayActivity) getActivity();
        mPresenter = new ChartFragmentPresenter(this);
        mPresenter.initialize();

        return fragmentView;
    }

    @Override
    public void initializeCharts() {
        for (int i = 0; i < CHART_COUNT; i++) {
            ColumnChartView chart = mCharts.get(i);

            List<Column> columns = new ArrayList<>();
            for (int j = 0; j < CHART_DATA_SAMPLING_PRECISION; j++) {
                List<SubcolumnValue> values = new ArrayList<>();
                values.add(new SubcolumnValue(0));
                Column column = new Column(values);
                columns.add(column);
            }

            ColumnChartData data = new ColumnChartData(columns);

            List<AxisValue> values = new ArrayList<>();
            for (int j = 0; j < CHART_DATA_SAMPLING_PRECISION + 1; j++) {
                AxisValue value = new AxisValue(j);
                boolean needLabel = (j * CHART_UPDATE_TIME_MILLIS % 2000 == 0);
                String label = needLabel ? String.valueOf(j * CHART_UPDATE_TIME_MILLIS / 1000) : "";
                value.setLabel(label);
                values.add(value);
            }
            Axis axisX = new Axis(values)
                    .setName(CHART_DESCRIPTIONS[i] + " (seconds)")
                    .setTextColor(Color.BLACK)
                    .setLineColor(Color.BLACK)
                    .setAutoGenerated(false)
                    .setMaxLabelChars(2)
                    .setTextSize(10);
            Axis axisY = Axis.generateAxisFromRange(-2, 2, 0.01f)
                    .setTextColor(Color.BLACK)
                    .setLineColor(Color.BLACK)
                    .setHasLines(true)
                    .setAutoGenerated(true)
                    .setTextSize(10);
            data.setAxisXBottom(axisX);
            data.setAxisYLeft(axisY);

            chart.setColumnChartData(data);

            // Adjust viewport to make the charts look better.
            final Viewport viewport = new Viewport(chart.getMaximumViewport());
            viewport.left = 0;
            viewport.right = CHART_DATA_SAMPLING_PRECISION + 10;
            chart.setMaximumViewport(viewport);
            chart.setCurrentViewport(viewport);
            chart.setValueSelectionEnabled(false);
        }
    }

    @Override
    public void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();
        mPresenter.cancelUpdate();
    }

    @Override
    public void onUpdateData(MotionResult result) {
        mPresenter.updateData(result);
    }

    @Override
    public void onRefreshDisplay() {
        mPresenter.refreshDisplay();
    }

    @Override
    public void updateBinding(MotionResult result, int steps) {
        mBinding.setSteps(String.valueOf(steps));
        mBinding.setPeriod(String.valueOf(result.stop - result.start));
        mBinding.setMotionX(String.valueOf(Float.parseFloat(DECIMAL_FORMAT.format(result.motionX))));
        mBinding.setMotionY(String.valueOf(Float.parseFloat(DECIMAL_FORMAT.format(result.motionY))));
        mBinding.setMotionZ(String.valueOf(Float.parseFloat(DECIMAL_FORMAT.format(result.motionZ))));
    }

    @Override
    public void updateCharts(ChartPointValue[] meanMotions) {
        for (int i = 0; i < CHART_COUNT; i++) {
            final int x = meanMotions[i].x;
            final float y = meanMotions[i].y;

            ColumnChartData data = mCharts.get(i).getColumnChartData();
            data.getColumns().get(x).getValues().get(0).setValue(y).setColor(ChartUtils.COLOR_BLUE);
            mCharts.get(i).setColumnChartData(data);
        }
    }

    @Override
    public void refreshCharts() {
        for (int i = 0; i < CHART_COUNT; i++) {
            ColumnChartData data = mCharts.get(i).getColumnChartData();
            for (int j = 0; j < CHART_DATA_SAMPLING_PRECISION; j++) {
                List<SubcolumnValue> values = data.getColumns().get(j).getValues();
                values.get(0).setValue(0).setColor(ChartUtils.DEFAULT_COLOR);
                data.getColumns().get(j).setValues(values);
            }
            mCharts.get(i).setColumnChartData(data);
        }
    }

    @Override
    public void setPresenter(ChartFragmentContract.Presenter presenter) {
        mPresenter = (ChartFragmentPresenter) presenter;
    }

    @Override
    public void scheduleNextUpdate(Runnable updateTask, boolean reschedule) {
        if (reschedule) {
            cancelSchedule(updateTask);
        }
        mActivity.getHandler().postDelayed(updateTask, CHART_UPDATE_TIME_MILLIS);
    }

    @Override
    public void cancelSchedule(Runnable updateTask) {
        mActivity.getHandler().removeCallbacks(updateTask);
    }
}
