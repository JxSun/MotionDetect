package com.motiondetect.detector.view;

import com.motiondetect.detector.presenter.BasePresenter;

public interface BaseView<Presenter extends BasePresenter> {
    void setPresenter(Presenter presenter);
}
