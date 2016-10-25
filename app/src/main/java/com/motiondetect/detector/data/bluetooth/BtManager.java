package com.motiondetect.detector.data.bluetooth;

import android.app.Activity;
import android.app.Application;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.Context;
import android.util.Log;

import com.motiondetect.detector.MotionDetectApplication;
import com.motiondetect.detector.model.BtDeviceModel;
import com.motiondetect.detector.contract.MotionDetectAppContract;
import com.motiondetect.detector.util.BtUtil;
import com.motiondetect.detector.view.ContextWrapper;
import com.github.ivbaranov.rxbluetooth.Action;
import com.github.ivbaranov.rxbluetooth.RxBluetooth;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.schedulers.Schedulers;

public class BtManager {
    private static final String TAG = BtManager.class.getSimpleName();
    public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");

    private Application mApplication;

    private Subscription mDeviceSubscription;
    private Subscription mDiscoveryStartSubscription;
    private Subscription mDiscoveryFinishSubscription;
    private Subscription mStateSubscription;
    private Subscription mConnectSubscription;
    private OnBluetoothEventListener mOnEventListener;

    private RxBluetooth mRxBluetooth;
    private Map<BtDeviceModel, BluetoothDevice> mDeviceMap;

    private boolean mNeedRetriggerDiscovery = false;
    private boolean mIsReleased = false;

    public BtManager(MotionDetectAppContract app) {
        mApplication = (MotionDetectApplication) app;
        mRxBluetooth = new RxBluetooth(mApplication);
        mDeviceMap = new HashMap<>();
        initializeSubscriptions();
    }

    private void initializeSubscriptions() {
        mDeviceSubscription = createDeviceSubscription();
        mDiscoveryStartSubscription = createDiscoveryStartSubscription();
        mDiscoveryFinishSubscription = createDiscoveryFinishSubscription();
        mStateSubscription = createStateSubscription();
    }

    public void setOnBluetoothEventListener(OnBluetoothEventListener listener) {
        mOnEventListener = listener;
    }

    public void enableBluetoothIfNeeded(ContextWrapper wrapper, int requestCode) {
        Context context = wrapper.getContext();
        if  (!mRxBluetooth.isBluetoothAvailable() && context instanceof Activity) {
            mRxBluetooth.enableBluetooth((Activity) context, requestCode);
        }
    }

    private BtDeviceModel addBtDevice(BluetoothDevice device) {
        BtDeviceModel model = BtUtil.convertBluetoothDeviceToBtDeviceModel(device);
        if (model != null && !mDeviceMap.containsKey(model)) {
            Log.d(TAG, "addBtDevice() - add: " + model.toString());
            mDeviceMap.put(model, device);
        }
        return model;
    }

    public boolean isExist(BtDeviceModel model) {
        return mDeviceMap.containsKey(model);
    }

    public BluetoothDevice getBtDevice(BtDeviceModel model) {
        return mDeviceMap.get(model);
    }

    private void clearBtDevices() {
        mDeviceMap.clear();
    }

    public boolean isDiscovering() {
        return mRxBluetooth.isDiscovering();
    }

    public void startDiscovery() {
        Log.v(TAG, "startDiscovery() - start scanning bluetooth devices");

        if (mRxBluetooth.isDiscovering()) {
            mRxBluetooth.cancelDiscovery();
            mNeedRetriggerDiscovery = true;
        } else {
            mRxBluetooth.startDiscovery();
        }
    }

    public void cancelDiscovery() {
        if (mRxBluetooth.isDiscovering()) {
            mRxBluetooth.cancelDiscovery();
        }
    }

    public void reset() {
        Log.v(TAG, "reset()");
        mIsReleased = false;
        initializeSubscriptions();
    }

    public void release() {
        Log.v(TAG, "release() - release bluetooth resources");
        mIsReleased = true;
        cancelDiscovery();
        unsubscribeAll();
        mOnEventListener = null;
    }

    private Subscription createDeviceSubscription() {
        return mRxBluetooth.observeDevices()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .subscribe(new Action1<BluetoothDevice>() {
                    @Override
                    public void call(BluetoothDevice device) {
                        if (device == null || device.getName() == null) {
                            return;
                        }

                        addBtDevice(device);
                        mOnEventListener.onDetectDevice(BtUtil.convertBluetoothDeviceToBtDeviceModel(device));
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "getDeviceSubscription() - " + throwable.getMessage());
                    }
                });
    }

    private Subscription createDiscoveryStartSubscription() {
        return mRxBluetooth.observeDiscovery()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(Action.isEqualTo(BluetoothAdapter.ACTION_DISCOVERY_STARTED))
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        mOnEventListener.onDiscoveryStart();
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "getDiscoveryStartSubscription() - " + throwable.getMessage());
                    }
                });
    }

    private Subscription createDiscoveryFinishSubscription() {
        return mRxBluetooth.observeDiscovery()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(Action.isEqualTo(BluetoothAdapter.ACTION_DISCOVERY_FINISHED))
                .subscribe(new Action1<String>() {
                    @Override
                    public void call(String s) {
                        if (mNeedRetriggerDiscovery) {
                            mRxBluetooth.startDiscovery();
                            mNeedRetriggerDiscovery = false;
                        } else {
                            mOnEventListener.onDiscoveryEnd();
                        }
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "getDiscoveryFinishSubscription() - " + throwable.getMessage());
                    }
                });
    }

    private Subscription createStateSubscription() {
        return mRxBluetooth.observeBluetoothState()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.computation())
                .filter(Action.isEqualTo(
                        BluetoothAdapter.STATE_ON,
                        BluetoothAdapter.STATE_OFF,
                        BluetoothAdapter.STATE_TURNING_OFF,
                        BluetoothAdapter.STATE_TURNING_ON))
                .subscribe(new Action1<Integer>() {
                    @Override
                    public void call(Integer state) {
                        if (state == BluetoothAdapter.STATE_TURNING_OFF || state == BluetoothAdapter.STATE_OFF) {
                            clearBtDevices();
                        }
                        mOnEventListener.onStateChange(state);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "getStateSubscription() - " + throwable.getMessage());
                    }
                });
    }

    public void connect(BtDeviceModel target) {
        mConnectSubscription = mRxBluetooth.observeConnectDevice(getBtDevice(target), MY_UUID)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(new Action1<BluetoothSocket>() {
                    @Override
                    public void call(BluetoothSocket socket) {
                        mOnEventListener.onConnect(socket);
                    }
                }, new Action1<Throwable>() {
                    @Override
                    public void call(Throwable throwable) {
                        Log.e(TAG, "connect() - " + throwable.getMessage());
                    }
                });
    }

    public void unsubscribeAll() {
        unsubscribe(mDeviceSubscription);
        unsubscribe(mDiscoveryStartSubscription);
        unsubscribe(mDiscoveryFinishSubscription);
        unsubscribe(mConnectSubscription);
        unsubscribe(mStateSubscription);
    }

    public void unsubscribe(Subscription subscription) {
        if (subscription != null && !subscription.isUnsubscribed()) {
            subscription.unsubscribe();
        }
    }

    public boolean isReleased() {
        return mIsReleased;
    }

    public interface OnBluetoothEventListener {
        void onDetectDevice(BtDeviceModel device);
        void onDiscoveryStart();
        void onDiscoveryEnd();
        void onConnect(BluetoothSocket socket);
        void onStateChange(int state);
    }
}
