package com.motiondetect.detector.view.activity;

import android.app.ProgressDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.motiondetect.detector.MotionDetectApplication;
import com.motiondetect.detector.R;
import com.motiondetect.detector.data.bluetooth.BtManager;
import com.motiondetect.detector.data.storage.StorageFactory;
import com.motiondetect.detector.model.BtDeviceModel;
import com.motiondetect.detector.contract.DataDisplayContract;
import com.motiondetect.detector.presenter.DataDisplayPresenter;
import com.motiondetect.detector.view.adapter.DataDisplayFragmentPageAdapter;
import com.motiondetect.detector.view.fragment.ChartFragment;
import com.motiondetect.detector.view.fragment.DetailFragment;
import com.motiondetect.detector.view.fragment.UploadLogDialogFragment;
import com.motiondetect.detectorjni.model.MotionResult;

import butterknife.BindView;
import butterknife.OnClick;

public class DataDisplayActivity extends BaseActivity
        implements DataDisplayContract.View, ViewPager.OnPageChangeListener {
    private static final String TAG = DataDisplayActivity.class.getSimpleName();

    // UI
    @BindView(R.id.data_display_viewpager) ViewPager mPager;
    @BindView(R.id.page_indicator) RadioGroup mIndicatorGroup;
    @BindView(R.id.upload_log_btn) ImageButton mUploadLogBtn;
    @BindView(R.id.display_refresh_btn) ImageButton mRefreshBtn;
    private ProgressDialog mRecoveryDialog;
    private ProgressDialog mUploadDialog;
    private DataDisplayFragmentPageAdapter mPageAdapter;

    private DataDisplayPresenter mPresenter;

    private BtDeviceModel mTargetDevice;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() - start");
        super.onCreate(savedInstanceState);

        Bundle extras = getIntent().getExtras();
        mTargetDevice = (BtDeviceModel) extras.getSerializable("device");
        BtManager btManager = ((MotionDetectApplication) getApplication()).getBtManager();
        StorageFactory storageFactory = new StorageFactory(getApplicationContext());
        mPresenter = new DataDisplayPresenter(this, mTargetDevice, btManager, storageFactory);
        mPresenter.onCreate();

        Log.d(TAG, "onCreate() - end");
    }

    @Override
    public void initializeUI() {
        mPageAdapter = new DataDisplayFragmentPageAdapter(getSupportFragmentManager());
        mPager.setAdapter(mPageAdapter);
        mPager.addOnPageChangeListener(this);
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_data_display;
    }

    @OnClick(R.id.upload_log_btn)
    void onClickUploadButton() {
        mPresenter.onClickUploadButton();
    }

    @OnClick(R.id.display_refresh_btn)
    void onClickRefreshButton() {
        mPresenter.onClickRefreshButton();
    }

    private ProgressDialog createUploadDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Uploading to Dropbox...");
        dialog.setMessage("Upload analyzed and raw logs");
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    private ProgressDialog createRecoveryDialog() {
        ProgressDialog dialog = new ProgressDialog(this);
        dialog.setTitle("Recovering...");
        dialog.setMessage("Reconnect to " + mTargetDevice.name);
        dialog.setIndeterminate(true);
        dialog.setCancelable(false);
        return dialog;
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume");
        super.onResume();

        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause");
        super.onPause();

        mPresenter.onPause();
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        switch (position) {
            case 0:
                Log.d(TAG, ChartFragment.TAG + " is selected");
                mIndicatorGroup.check(R.id.chart_indicator);
                break;
            case 1:
                Log.d(TAG, DetailFragment.TAG + " is selected");
                mIndicatorGroup.check(R.id.detail_indicator);
                break;
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();

        mPresenter.onBackPressed();
    }

    @Override
    public void updateDataDisplay(final MotionResult result) {
        runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    for (int i = 0; i < FRAGMENT_COUNT; i++) {
                        mPageAdapter.getFragment(i).onUpdateData(result);
                    }
                }
            });
    }

    @Override
    public void refreshDataDisplay(final int fragmentIdx) {
        mPageAdapter.getFragment(fragmentIdx).onRefreshDisplay();
    }

    @Override
    public void showUploadDataDialog() {
        UploadLogDialogFragment fragment = UploadLogDialogFragment.newInstance(DataDisplayActivity.this);
        fragment.setOnSetFileNameListener(new UploadLogDialogFragment.OnSetFileNameListener() {
            @Override
            public void onSetFileNames(String analyzedName, String rawName) {
                Log.v(TAG, "Uploading...");
                mUploadDialog = createUploadDialog();
                mUploadDialog.show();

                mPresenter.upload(analyzedName, rawName);
            }
        });
        fragment.show(getSupportFragmentManager(), "UploadFileNameDialog");
    }

    @Override
    public void dismissUploadDataDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mUploadDialog.dismiss();
            }
        });
    }

    @Override
    public void showUploadFailedToast(final String msg) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(DataDisplayActivity.this, msg, Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    public void showRecoveryDialog() {
        mRecoveryDialog = createRecoveryDialog();
        mRecoveryDialog.show();
    }

    @Override
    public void dismissRecoveryDialog() {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                if (mRecoveryDialog != null) {
                    mRecoveryDialog.dismiss();
                    mRecoveryDialog = null;
                }
            }
        });
    }

    @Override
    public void showDropboxNotAvailableToast() {
        Toast.makeText(this, "Please login to Dropbox first", Toast.LENGTH_SHORT).show();
    }

    @Override
    public void setPresenter(DataDisplayContract.Presenter presenter) {
        mPresenter = (DataDisplayPresenter) presenter;
    }
}
