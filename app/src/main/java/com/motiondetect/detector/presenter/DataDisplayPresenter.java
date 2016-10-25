package com.motiondetect.detector.presenter;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.motiondetect.detector.base.ILifeCycle;
import com.motiondetect.detector.contract.DataDisplayContract;
import com.motiondetect.detector.data.analysis.MotionAnalyst;
import com.motiondetect.detector.data.bluetooth.ArduinoReceiver;
import com.motiondetect.detector.data.bluetooth.BtManager;
import com.motiondetect.detector.data.storage.IStorage;
import com.motiondetect.detector.data.storage.StorageFactory;
import com.motiondetect.detector.model.BtDeviceModel;
import com.motiondetect.detector.contract.DataDisplayContract.View;
import com.motiondetect.detectorjni.model.MotionResult;

public class DataDisplayPresenter implements DataDisplayContract.Presenter, ILifeCycle {
    private static final String TAG = DataDisplayPresenter.class.getSimpleName();

    private final View mView;
    private final StorageFactory mStorageFactory;
    private final BtDeviceModel mTargetDevice;

    private boolean mIsBackKeyPressed = false;
    private boolean mIsHomeKeyOrMultiTaskKeyPressed = false;

    private IStorage mDropboxStorage;
    private IStorage mLocalStorage;
    private ArduinoReceiver mReceiverThread;
    private MotionAnalyst mAnalyst;

    private BtManager mBtManager;
    private final BtManager.OnBluetoothEventListener mOnBluetoothEventListener
            = new BtManager.OnBluetoothEventListener() {
        @Override
        public void onDetectDevice(BtDeviceModel device) {
            if (device.equals(mTargetDevice)) {
                mBtManager.cancelDiscovery();
                mBtManager.connect(mTargetDevice);
            }
        }

        @Override
        public void onDiscoveryStart() {

        }

        @Override
        public void onDiscoveryEnd() {
            if (!mBtManager.isExist(mTargetDevice)) {
                Log.d(TAG, mTargetDevice.name + " still not found, keep searching");
                mBtManager.startDiscovery();
            }
        }

        @Override
        public void onConnect(BluetoothSocket socket) {
            Log.v(TAG, "onConnect() - Get connected to " + mTargetDevice.name);
            onBluetoothConnected(socket);
            mView.dismissRecoveryDialog();

            for (int i = 0; i < View.FRAGMENT_COUNT; i++) {
                mView.refreshDataDisplay(i);
            }
        }

        @Override
        public void onStateChange(int state) {

        }
    };

    private final MotionAnalyst.Callback mOnAnalysisCallback = new MotionAnalyst.Callback() {
        @Override
        public void onAnalysisResult(final MotionResult result) {
            mView.updateDataDisplay(result);
        }
    };

    private IStorage.OnSaveFileCallback mOnUploadCallback = new IStorage.OnSaveFileCallback() {
        @Override
        public void onSaved() {
            mDropboxStorage.close();

            mView.dismissUploadDataDialog();

            Log.v(TAG, "Recover all resources after uploading to Dropbox");
            reacquireResourcesAfterUpload();
        }

        @Override
        public void onError(String msg) {
            mDropboxStorage.close();

            mView.dismissUploadDataDialog();
            mView.showUploadFailedToast(msg);

            Log.v(TAG, "Recover all resources after failed uploading");
            reacquireResourcesAfterUpload();
        }
    };

    public DataDisplayPresenter(View view, BtDeviceModel device, BtManager btManager, StorageFactory factory) {
        mView = view;
        mView.setPresenter(this);
        mTargetDevice = device;
        mBtManager = btManager;
        mStorageFactory = factory;
    }

    @Override
    public void onClickRefreshButton() {
        Log.v(TAG, "On click refresh button");
        mView.refreshDataDisplay(View.CHART_FRAGMENT_IDX);
    }

    @Override
    public void onClickUploadButton() {
        if (!mDropboxStorage.isAvailable()) {
            Log.w(TAG, "Need to login to Dropbox first");
            mView.showDropboxNotAvailableToast();
            return;
        }

        mView.showUploadDataDialog();
    }

    @Override
    public void releaseResourcesBeforeUpload() {
        if (mReceiverThread != null) {
            mReceiverThread.close();
        }

        mLocalStorage.close();
        mBtManager.release();
    }

    @Override
    public void reacquireResourcesAfterUpload() {
        mLocalStorage.open();
        createBluetoothConnection(true);
        mIsHomeKeyOrMultiTaskKeyPressed = false;
    }

    @Override
    public void onCreate() {
        mView.initializeUI();
        mLocalStorage = mStorageFactory.create(StorageFactory.STORAGE_LOCAL);
        mDropboxStorage = mStorageFactory.create(StorageFactory.STORAGE_DROPBOX);
        createBluetoothConnection(false);
    }

    @Override
    public void onStart() {

    }

    @Override
    public void onResume() {
        if (mIsHomeKeyOrMultiTaskKeyPressed) {
            Log.d(TAG, "onResume() - handle resources recovery");
            mLocalStorage = mStorageFactory.create(StorageFactory.STORAGE_LOCAL);
            mDropboxStorage = mStorageFactory.create(StorageFactory.STORAGE_DROPBOX);
            createBluetoothConnection(true);
            mIsHomeKeyOrMultiTaskKeyPressed = false;
        }
    }

    @Override
    public void onPause() {
        mView.dismissRecoveryDialog();

        if (!mIsBackKeyPressed) {
            mIsHomeKeyOrMultiTaskKeyPressed = true;
        }
        mIsBackKeyPressed = false;

        Log.d(TAG, "onPause() - stop arduino receiver");
        if (mReceiverThread != null) {
            mReceiverThread.close();
        }

        Log.d(TAG, "onPause() - stop analyst");
        if (mAnalyst != null) {
            mAnalyst.release();
        }

        Log.d(TAG, "onPause() - stop all storages");
        mLocalStorage.release();
        mDropboxStorage.release();

        Log.d(TAG, "onPause() - release bt manager");
        mBtManager.release();
    }

    @Override
    public void onStop() {

    }

    @Override
    public void onDestroy() {

    }

    private void createBluetoothConnection(boolean rediscovery) {
        Log.d(TAG, "createBluetoothConnection() - start connecting to " + mTargetDevice.name);

        if (rediscovery) {
            mBtManager.reset();
            mBtManager.setOnBluetoothEventListener(mOnBluetoothEventListener);

            mView.showRecoveryDialog();

            mBtManager.startDiscovery();
        } else {
            mBtManager.setOnBluetoothEventListener(mOnBluetoothEventListener);
            mBtManager.connect(mTargetDevice);
        }
    }

    private void onBluetoothConnected(BluetoothSocket socket) {
        try {
            mLocalStorage.open();

            Log.d(TAG, "Start MotionAnalyst");
            mAnalyst = new MotionAnalyst();
            mAnalyst.addCallback(mOnAnalysisCallback)
                    .addCallback((MotionAnalyst.Callback) mLocalStorage);
            mAnalyst.start();

            Log.d(TAG, "Start ArduinoReceiver");
            mReceiverThread = new ArduinoReceiver(socket)
                    .addOnReceiveDataListener(mAnalyst)
                    .addOnReceiveDataListener((ArduinoReceiver.OnReceiveDataListener) mLocalStorage);
            mReceiverThread.start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void upload(String analyzedName, String rawName) {
        Log.v(TAG, "Stop all resources before uploading to Dropbox");
        releaseResourcesBeforeUpload();

        mDropboxStorage.open();
        mDropboxStorage.save(mOnUploadCallback, analyzedName, rawName);
    }

    public void onBackPressed() {
        mIsBackKeyPressed = true;
    }
}
