package com.motiondetect.detector.contract;

import com.motiondetect.detector.presenter.BasePresenter;
import com.motiondetect.detector.view.BaseView;
import com.motiondetect.detectorjni.model.MotionResult;

public interface DataDisplayContract {
    interface View extends BaseView<Presenter> {
        int CHART_FRAGMENT_IDX = 0;
        int DETAIL_FRAGMENT_IDX = 1;
        int FRAGMENT_COUNT = 2;

        void initializeUI();
        void updateDataDisplay(final MotionResult result);
        void refreshDataDisplay(final int fragmentIdx);
        void showUploadDataDialog();
        void dismissUploadDataDialog();
        void showUploadFailedToast(final String msg);
        void showRecoveryDialog();
        void dismissRecoveryDialog();
        void showDropboxNotAvailableToast();
    }

    interface Presenter extends BasePresenter {
        void onClickRefreshButton();
        void onClickUploadButton();
        void releaseResourcesBeforeUpload();
        void reacquireResourcesAfterUpload();
    }
}
