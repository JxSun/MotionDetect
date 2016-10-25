package com.motiondetect.detector.base;

import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;

import java.lang.ref.WeakReference;
import java.util.LinkedList;
import java.util.Queue;

public abstract class IBackgroundWorker implements IAsyncExecutor {
    private WorkerThread mWorkerThread;
    private Handler mHandler;
    private Queue<Runnable> mPendingTasks;
    private final Object mLock = new Object();

    protected IBackgroundWorker() {
        mPendingTasks = new LinkedList<>();
    }

    public void start() {
        mWorkerThread = new WorkerThread(this);
        mWorkerThread.start();
    }

    private void onAsyncOperationReady() {
        synchronized (mLock) {
            mHandler = mWorkerThread.getHandler();
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    onPreExecuteAsync();
                }
            });

            while (!mPendingTasks.isEmpty()) {
                mHandler.post(mPendingTasks.poll());
            }
        }
    }

    @Override
    public void invokeAsync(Runnable runnable) {
        if (mHandler == null) {
            synchronized (mLock) {
                if (mHandler == null) {
                    mPendingTasks.offer(runnable);
                    return;
                }
            }
        }

        mHandler.post(runnable);
    }

    protected void onPreExecuteAsync() {}

    protected void onPostExecuteAsync() {}

    public void terminate() {
        Log.d(getClass().getSimpleName(), "terminate()");

        if (mHandler != null) {
            mHandler.removeCallbacksAndMessages(null);

            invokeAsync(new Runnable() {
                @Override
                public void run() {
                    onPostExecuteAsync();
                    mWorkerThread.quit();
                    mHandler = null;
                    mWorkerThread = null;
                }
            });
        } else {
            if (mWorkerThread != null) {
                mWorkerThread.quit();
            }
            mHandler = null;
            mWorkerThread = null;
        }
    }

    private static class WorkerThread extends HandlerThread {
        private Handler mHandler;
        private WeakReference<IBackgroundWorker> mWorker;

        private String mName;

        public WorkerThread(IBackgroundWorker worker) {
            super("WorkerThread");
            mWorker = new WeakReference<>(worker);
            mName = worker.getClass().getSimpleName();
        }

        @Override
        protected void onLooperPrepared() {
            super.onLooperPrepared();
            mHandler = new Handler(getLooper());

            IBackgroundWorker worker = mWorker.get();
            if (worker != null) {
                worker.onAsyncOperationReady();
            }

        }

        @Override
        public void run() {
            final long threadId = Thread.currentThread().getId();
            Log.d(mName, "Start running worker thread: " + threadId);
            super.run();
            Log.d(mName, "Stop running worker thread: " + threadId);
        }
        public Handler getHandler() {
            return mHandler;
        }

    }
}
