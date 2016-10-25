package com.motiondetect.detector.presenter;

import com.motiondetect.detector.contract.DeviceListContract;
import com.motiondetect.detector.data.bluetooth.BtManager;
import com.motiondetect.detector.data.dropbox.DropboxServiceManager;
import com.motiondetect.detector.model.BtDeviceModel;

import org.junit.Before;
import org.junit.Test;

import static org.mockito.Mockito.*;

public class DeviceListPresenterTest {

    private DeviceListPresenter mPresenter;
    private DeviceListContract.View mView;
    private DropboxServiceManager mDbxManager;
    private BtManager mBtManager;

    @Before
    public void setUp() throws Exception {
        mView = mock(DeviceListContract.View.class);
        mBtManager = mock(BtManager.class);
        mDbxManager = mock(DropboxServiceManager.class);
        mPresenter = new DeviceListPresenter(mView, mBtManager, /*mAuthManager*/ mDbxManager);
    }

    @Test
    public void testOnCreate() throws Exception {
        mPresenter.onCreate();

        verify(mView).initializeUI();
        verify(mView).requestPermissions();
    }

    @Test
    public void testOnStartWhenBluetoothHasReleased() throws Exception {
        when(mBtManager.isReleased()).thenReturn(true);
        mPresenter.onStart();

        verify(mBtManager).reset();
        verify(mBtManager).setOnBluetoothEventListener((BtManager.OnBluetoothEventListener) anyObject());
    }

    @Test
    public void testOnStartWhenBluetoothNotReleased() throws Exception {
        when(mBtManager.isReleased()).thenReturn(false);
        mPresenter.onStart();

        verify(mBtManager, never()).reset();
        verify(mBtManager).setOnBluetoothEventListener((BtManager.OnBluetoothEventListener) anyObject());
    }

    @Test
    public void testOnResume() throws Exception {
        mPresenter.onResume();

        verify(mBtManager).startDiscovery();
        verify(mDbxManager).loadAccessToken();
    }

    @Test
    public void testOnPause() throws Exception {
        mPresenter.onPause();

        verify(mBtManager).cancelDiscovery();
        verify(mView).clearDeviceItems();
    }

    @Test
    public void testOnDestroy() throws Exception {
        mPresenter.onDestroy();

        verify(mBtManager).release();
    }

    @Test
    public void testOnClickDiscoveryButton() throws Exception {
        mPresenter.onClickDiscoveryButton();

        verify(mBtManager).startDiscovery();
    }

    @Test
    public void testOnClickDropboxLoginButton() throws Exception {
        mPresenter.onClickDropboxLoginButton();

        verify(mDbxManager).requestAuthentication();
    }

    @Test
    public void testOnStartDataDisplayActivity() throws Exception {
        BtDeviceModel target = mock(BtDeviceModel.class);
        mPresenter.onStartDataDisplayActivity(target);

        verify(mBtManager).cancelDiscovery();
        verify(mView).startDataDisplayActivity(target);
    }

    @Test
    public void testRequestBluetooth() throws Exception {

    }
}