package com.motiondetect.detector.contract;

import com.motiondetect.detector.model.BtDeviceModel;
import com.motiondetect.detector.presenter.BasePresenter;
import com.motiondetect.detector.view.BaseView;

public interface DeviceListContract {
    interface View extends BaseView<Presenter> {
        void initializeUI();
        void requestPermissions();
        void addDeviceItem(BtDeviceModel device);
        void clearDeviceItems();
        void showDiscovering();
        void dismissDiscovering();
        void startDataDisplayActivity(BtDeviceModel target);
    }

    interface Presenter extends BasePresenter {
        void onClickDiscoveryButton();
        void onClickDropboxLoginButton();
        void onStartDataDisplayActivity(BtDeviceModel target);
    }
}
