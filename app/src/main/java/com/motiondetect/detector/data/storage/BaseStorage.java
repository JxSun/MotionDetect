package com.motiondetect.detector.data.storage;

import android.os.Environment;

import com.motiondetect.detector.base.IBackgroundWorker;

import java.io.File;

public abstract class BaseStorage extends IBackgroundWorker implements IStorage {
    protected static final String BASE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath();
    protected static final String FILE_NAME_ANALYZED = "motion_analyzed.csv";
    protected static final String FILE_NAME_RAW = "motion_raw.csv";
    protected static final String FILE_PATH_ANALYZED = BASE_DIRECTORY + File.separator + FILE_NAME_ANALYZED;
    protected static final String FILE_PATH_RAW = BASE_DIRECTORY + File.separator + FILE_NAME_RAW;

    protected STATE mState = STATE.CLOSE;
}
