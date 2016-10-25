package com.motiondetect.detector.data.bluetooth;

import android.bluetooth.BluetoothSocket;
import android.util.Log;

import com.motiondetect.detector.model.MotionModel;
import com.motiondetect.detector.util.ArduinoDataParser;

import java.io.IOException;
import java.io.InputStream;
import java.security.InvalidParameterException;
import java.util.ArrayList;
import java.util.List;

public class ArduinoReceiver extends Thread {
    private static final String TAG = ArduinoReceiver.class.getSimpleName();

    private BluetoothSocket mSocket;
    private InputStream mInputStream;
    private boolean mConnected = false;
    private List<OnReceiveDataListener> mRxListeners = new ArrayList<>();

    public ArduinoReceiver(BluetoothSocket socket) throws Exception {
        if (socket == null) {
            throw new InvalidParameterException("Bluetooth socket can't be null");
        }

        mSocket = socket;

        try {
            mInputStream = socket.getInputStream();
            mConnected = true;
        } catch (IOException e) {
            throw new Exception("Can't get stream from bluetooth socket");
        } finally {
            if (!mConnected) {
                closeConnection();
            }
        }
    }

    public ArduinoReceiver addOnReceiveDataListener(OnReceiveDataListener listener) {
        Log.d(TAG, "addOnReceiveDataListener() - add " + listener.toString());
        mRxListeners.add(listener);
        return this;
    }

    @Override
    public void run() {
        Log.d(TAG, "ArduinoReceiver starts");
        try {
            ArduinoDataParser parser = new ArduinoDataParser(mInputStream);
            while (mConnected && mSocket.isConnected()) {
                List<MotionModel> motions = parser.parseJsonStream();

                for (int i = 0; mConnected && i < mRxListeners.size(); i++) {
                    mRxListeners.get(i).onReceive(motions);
                }
            }
        } catch (IOException e) {
            Log.e(TAG, e.getMessage());
        } finally {
            if (!mConnected) {
                closeConnection();
            }
        }
        Log.d(TAG, "ArduinoReceiver ends");
    }

    private void closeConnection() {
        Log.d(TAG, "closeConnection()");

        mConnected = false;

        if (!mRxListeners.isEmpty()) {
            mRxListeners.clear();
        }

        if (mInputStream != null) {
            try {
                mInputStream.close();
            } catch(IOException ignored) {
                Log.e(TAG, "closeConnection() - close input stream failed, got " + ignored.getMessage());
            }
            mInputStream = null;
        }

        if (mSocket != null) {
            try {
                mSocket.close();
            } catch (IOException ignored) {
                Log.e(TAG, "closeConnection() - close socket failed, got " + ignored.getMessage());
            }
            mSocket = null;
        }
    }

    public void close() {
        Log.d(TAG, "close()");
        // Force close connection
        closeConnection();
    }

    public interface OnReceiveDataListener {
        void onReceive(List<MotionModel> data);
    }
}
