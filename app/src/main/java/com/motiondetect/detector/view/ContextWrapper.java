package com.motiondetect.detector.view;

import android.content.Context;

public class ContextWrapper {
    private final Context mContext;

    public ContextWrapper(Context context) {
        mContext = context;
    }

    public Context getContext() {
        return mContext;
    }
}
