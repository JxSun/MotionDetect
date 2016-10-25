package com.motiondetect.detector.view.fragment;

import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.motiondetect.detector.R;
import com.motiondetect.detector.databinding.FragmentDetailBinding;
import com.motiondetect.detectorjni.model.MotionResult;

import java.text.DecimalFormat;

public class DetailFragment extends BaseDisplayFragment {
    public static final String TAG = DetailFragment.class.getSimpleName();

    private static final DecimalFormat DECIMAL_FORMAT = new DecimalFormat("##.000000");
    private static final String PERIOD_FORMAT = "%1$d ms";
    private static final String MOTION_FORMAT = "%1$f";

    private FragmentDetailBinding mBinding;
    private int mStepCount = 0;

    public static DetailFragment newInstance() {
        return new DetailFragment();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_detail, container, false);
        View fragmentView = mBinding.getRoot();
        showResult(new MotionResult(), 0);
        return fragmentView;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        onRefreshDisplay();
    }

    private void showResult(MotionResult result, int steps) {
        final String periodStr = String.format(PERIOD_FORMAT, result.stop - result.start);

        final float formatedMotionX = Float.parseFloat(DECIMAL_FORMAT.format(result.motionX));
        final float formatedMotionY = Float.parseFloat(DECIMAL_FORMAT.format(result.motionY));
        final float formatedMotionZ = Float.parseFloat(DECIMAL_FORMAT.format(result.motionZ));
        final String motionXStr = String.format(MOTION_FORMAT, formatedMotionX);
        final String motionYStr = String.format(MOTION_FORMAT, formatedMotionY);
        final String motionZStr = String.format(MOTION_FORMAT, formatedMotionZ);

        mBinding.setSteps(String.valueOf(steps));
        mBinding.setPeriod(periodStr);
        mBinding.setMotionX(motionXStr);
        mBinding.setMotionY(motionYStr);
        mBinding.setMotionZ(motionZStr);
    }

    @Override
    public void onUpdateData(MotionResult result) {
        mStepCount++;
        Log.d(TAG, "Step count: " + mStepCount);

        showResult(result, mStepCount);
    }

    @Override
    public void onRefreshDisplay() {
        showResult(new MotionResult(), 0);
    }
}
