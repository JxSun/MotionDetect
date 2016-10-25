package com.motiondetect.detector.presenter;

import android.util.Log;

import com.motiondetect.detector.contract.DataDisplayContract;
import com.motiondetect.detector.data.bluetooth.BtManager;
import com.motiondetect.detector.data.storage.StorageFactory;
import com.motiondetect.detector.model.BtDeviceModel;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.PowerMockRunner;

import static org.mockito.Mockito.*;

@RunWith(PowerMockRunner.class)
@PrepareForTest({Log.class})
public class DataDisplayPresenterTest {
    private DataDisplayPresenter mPresenter;
    private DataDisplayContract.View mView;
    private BtDeviceModel mDevice;
    private BtManager mBtManager;
    private StorageFactory mFactory;

    @Before
    public void setUp() throws Exception {
        mView = mock(DataDisplayContract.View.class);
        mDevice = mock(BtDeviceModel.class);
        mBtManager = mock(BtManager.class);
        mFactory = mock(StorageFactory.class);
        mPresenter = new DataDisplayPresenter(mView, mDevice, mBtManager, mFactory);
    }

    @Test
    public void testOnClickRefreshButton() throws Exception {
        PowerMockito.mockStatic(Log.class);
        mPresenter.onClickRefreshButton();

        verify(mView).refreshDataDisplay(DataDisplayContract.View.CHART_FRAGMENT_IDX);
    }
}