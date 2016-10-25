package com.motiondetect.detector.viewModel;

import android.databinding.BaseObservable;
import android.util.Log;
import android.view.View;

import com.motiondetect.detector.model.BtDeviceModel;
import com.motiondetect.detector.view.IConnectionDialog;

public class DeviceViewModel extends BaseObservable {
    private static final String TAG = DeviceViewModel.class.getSimpleName();
    private BtDeviceModel mDevice;
    private IConnectionDialog mDialog;

    public DeviceViewModel(BtDeviceModel device, IConnectionDialog dialog) {
        mDevice = device;
        mDialog = dialog;
    }

    public String getDeviceName() {
        return mDevice.name;
    }

    public View.OnClickListener onClick() {
        return new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.v(TAG, "On click " + getDeviceName());
                mDialog.showDialog(mDevice);
            }
        };
    }
}
