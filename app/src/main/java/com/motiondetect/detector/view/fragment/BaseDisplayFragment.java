package com.motiondetect.detector.view.fragment;

import android.support.v4.app.Fragment;

import com.motiondetect.detector.data.analysis.IDataUpdater;

public abstract class BaseDisplayFragment extends Fragment implements IDataUpdater {
    public abstract void onRefreshDisplay();
}
