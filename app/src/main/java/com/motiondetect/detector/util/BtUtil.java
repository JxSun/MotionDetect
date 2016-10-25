package com.motiondetect.detector.util;

import android.bluetooth.BluetoothDevice;

import com.motiondetect.detector.model.BtDeviceModel;

public abstract class BtUtil {
    public static BtDeviceModel convertBluetoothDeviceToBtDeviceModel(BluetoothDevice device) {
        return (device == null || device.getName() == null)
                ? null : new BtDeviceModel(device.getName(), device.getAddress());
    }
}
