package com.motiondetect.detector.view.adapter;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;

import com.motiondetect.detector.R;
import com.motiondetect.detector.contract.DataDisplayContract.View;
import com.motiondetect.detector.view.fragment.BaseDisplayFragment;
import com.motiondetect.detector.view.fragment.ChartFragment;
import com.motiondetect.detector.view.fragment.DetailFragment;

public class DataDisplayFragmentPageAdapter extends FragmentPagerAdapter {
    private FragmentManager mManager;

    public DataDisplayFragmentPageAdapter(FragmentManager fm) {
        super(fm);
        mManager = fm;
    }

    @Override
    public Fragment getItem(int position) {
        switch (position) {
            case View.CHART_FRAGMENT_IDX:
                return ChartFragment.newInstance();
            case View.DETAIL_FRAGMENT_IDX:
                return DetailFragment.newInstance();
            default:
                throw new IllegalArgumentException("Position " + position + " is invalid.");
        }
    }

    @Override
    public int getCount() {
        return View.FRAGMENT_COUNT;
    }

    public BaseDisplayFragment getFragment(int index) {
        String tag = "android:switcher:" + R.id.data_display_viewpager + ":" + index;
        return (BaseDisplayFragment) mManager.findFragmentByTag(tag);
    }
}
