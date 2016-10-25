package com.motiondetect.detector.data.storage;

import android.util.Log;

import com.motiondetect.detector.data.analysis.MotionAnalyst;
import com.motiondetect.detector.data.bluetooth.ArduinoReceiver;
import com.motiondetect.detector.model.MotionModel;
import com.motiondetect.detectorjni.model.MotionResult;
import com.opencsv.CSVWriter;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.List;

/**
 * Save to local CVS files.
 */
public class LocalFileStorage extends BaseStorage
        implements MotionAnalyst.Callback, ArduinoReceiver.OnReceiveDataListener {
    private static final String TAG = LocalFileStorage.class.getSimpleName();
    private static final SimpleDateFormat WORLD_TIME_SDF = new SimpleDateFormat("yyyyMMdd_HH:mm:ss.SSS");

    static {
        WORLD_TIME_SDF.setTimeZone(Calendar.getInstance().getTimeZone());
    }

    private File mAnalyzedFile;
    private File mRawFile;
    private CSVWriter mAnalyzedWriter;
    private CSVWriter mRawWriter;

    public LocalFileStorage() {
        super();
        Log.d(TAG, "Create LocalFileStorage - analyzed path: " + FILE_PATH_ANALYZED + ", raw path: " + FILE_PATH_RAW);
        mAnalyzedFile = new File(FILE_PATH_ANALYZED);
        mRawFile = new File(FILE_PATH_RAW);
    }

    @Override
    public boolean isAvailable() {
        return mAnalyzedFile.canWrite() && mRawFile.canWrite();
    }

    @Override
    public void open() {
        invokeAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "open() - open analyzed data file");
                    if (mAnalyzedFile.exists() && !mAnalyzedFile.isDirectory()) {
                        mAnalyzedWriter = new CSVWriter(new FileWriter(FILE_PATH_ANALYZED, true));
                    } else {
                        mAnalyzedWriter = new CSVWriter(new FileWriter(FILE_PATH_ANALYZED));
                    }

                    Log.d(TAG, "open() - open raw data file");
                    if (mRawFile.exists() && !mRawFile.isDirectory()) {
                        mRawWriter = new CSVWriter(new FileWriter(FILE_PATH_RAW, true));
                    } else {
                        mRawWriter = new CSVWriter(new FileWriter(FILE_PATH_RAW));
                    }

                    mState = STATE.OPEN;
                } catch (IOException e) {
                    Log.e(TAG, "Failed to create CSVWriter: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    @SuppressWarnings("unchecked")
    @SafeVarargs
    @Override
    public final <T> void save(OnSaveFileCallback callback, final T... params) {
        invokeAsync(new Runnable() {
            @Override
            public void run() {
                if (mState != STATE.OPEN) {
                    return;
                }

                final List<MotionModel> data = (List<MotionModel>) params[0];
                final long currentTimeMillis = System.currentTimeMillis();
                final long baseArduinoTimeMillis = data.get(0).timestamp;

                for (MotionModel motion : data) {
                    final long systemTimestamp = currentTimeMillis + (motion.timestamp - baseArduinoTimeMillis);

                    String[] line = {
                            WORLD_TIME_SDF.format(new Date(systemTimestamp)), // timestamp of Android
                            String.valueOf(motion.timestamp), // timestamp of Arduino
                            String.valueOf(motion.accX), // acc x
                            String.valueOf(motion.accY), // acc y
                            String.valueOf(motion.accZ), // acc z
                            String.valueOf(motion.gyroX), // gyro x
                            String.valueOf(motion.gyroY), // gyro y
                            String.valueOf(motion.gyroZ)  // gyro z
                    };
                    mRawWriter.writeNext(line);
                }
            }
        });
    }

    // Close when leave the fragment or stop recording
    @Override
    public void close() {
        invokeAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    Log.d(TAG, "close() - close all files");
                    closeFiles();
                } catch (IOException e) {
                    Log.e(TAG, "close() - Failed to close CSVWriter: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }

    private void closeFiles() throws IOException {
        if (mAnalyzedWriter != null) {
            mAnalyzedWriter.close();
        }
        if (mRawWriter != null) {
            mRawWriter.close();
        }
        mState = STATE.CLOSE;
    }

    @Override
    public void onAnalysisResult(final MotionResult result) {
        // Save analyzed data
        invokeAsync(new Runnable() {
            @Override
            public void run() {
                if (mState != STATE.OPEN) {
                    return;
                }

                String[] line = {
                        String.valueOf(result.start), // Start timestamp
                        String.valueOf(result.stop),  // Stop timestamp
                        String.valueOf(result.motionX), // Motion x
                        String.valueOf(result.motionY), // Motion Y
                        String.valueOf(result.motionZ)  // Motion Z
                };
                mAnalyzedWriter.writeNext(line);
            }
        });
    }

    @SuppressWarnings("unchecked")
    @Override
    public void onReceive(List<MotionModel> data) {
        if (data.size() > 0) {
            save(null, data);
        }
    }

    @Override
    public void release() {
        invokeAsync(new Runnable() {
            @Override
            public void run() {
                try {
                    if (mState != STATE.CLOSE) {
                        Log.d(TAG, "release() - close all files");
                        closeFiles();
                    }

                    terminate();
                } catch (IOException e) {
                    Log.e(TAG, "release() - Failed to close CSVWriter: " + e.getMessage());
                    e.printStackTrace();
                }
            }
        });
    }
}
