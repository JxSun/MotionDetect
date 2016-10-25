package com.motiondetect.detector.presenter;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.motiondetect.detector.base.ILifeCycle;
import com.motiondetect.detector.contract.DeviceListContract;
import com.motiondetect.detector.data.bluetooth.BtManager;
import com.motiondetect.detector.data.dropbox.DropboxServiceManager;
import com.motiondetect.detector.model.BtDeviceModel;
import com.motiondetect.detector.view.ContextWrapper;

public class DeviceListPresenter implements DeviceListContract.Presenter, ILifeCycle {
    private static final String TAG = DeviceListPresenter.class.getSimpleName();

    private final DeviceListContract.View mView;
    private final DropboxServiceManager mDbxManager;

    private BtManager mBtManager;

    private final BtManager.OnBluetoothEventListener mOnBluetoothEventListener
            = new BtManager.OnBluetoothEventListener() {
        @Override
        public void onDetectDevice(BtDeviceModel device) {
            Log.v(TAG, "onDetectDevice() - Detected " + device.name);
            mView.addDeviceItem(device);
        }

        @Override
        public void onDiscoveryStart() {
            Log.d(TAG, "onDiscoveryStart()");
            mView.showDiscovering();
        }

        @Override
        public void onDiscoveryEnd() {
            Log.d(TAG, "onDiscoveryEnd()");
            mView.dismissDiscovering();
        }

        @Override
        public void onConnect(BluetoothSocket socket) {

        }

        @Override
        public void onStateChange(int state) {
            Log.d(TAG, "onStateChange() - Bluetooth state changed to " + state);
            if (state == BluetoothAdapter.STATE_TURNING_OFF || state == BluetoothAdapter.STATE_OFF) {
                mView.clearDeviceItems();
            }
        }
    };

    public DeviceListPresenter(DeviceListContract.View view, BtManager btManager, DropboxServiceManager dbxManager) {
        mView = view;
        mBtManager = btManager;
        mDbxManager = dbxManager;
        mView.setPresenter(this);
    }

    @Override
    public void onCreate() {
        mView.initializeUI();
        mView.requestPermissions();
    }

    @Override
    public void onStart() {
        // The BtManager instance might have been released by others, so re-acquire an instance here.
        if (mBtManager.isReleased()) {
            mBtManager.reset();
        }
        mBtManager.setOnBluetoothEventListener(mOnBluetoothEventListener);
    }

    @Override
    public void onResume() {
        mBtManager.startDiscovery();
        mDbxManager.loadAccessToken();
    }

    @Override
    public void onPause() {
        mBtManager.cancelDiscovery();
        mView.clearDeviceItems();
    }

    @Override
    public void onStop() {}

    @Override
    public void onDestroy() {
        mBtManager.release();
    }

    @Override
    public void onClickDiscoveryButton() {
        mBtManager.startDiscovery();
    }

    @Override
    public void onClickDropboxLoginButton() {
        mDbxManager.requestAuthentication();
    }

    @Override
    public void onStartDataDisplayActivity(BtDeviceModel target) {
        mBtManager.cancelDiscovery();
        mView.startDataDisplayActivity(target);
    }

    public void requestBluetooth(ContextWrapper wrapper, int code) {
        mBtManager.enableBluetoothIfNeeded(wrapper, code);
    }
}
