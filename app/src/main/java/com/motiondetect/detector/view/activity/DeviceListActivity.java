package com.motiondetect.detector.view.activity;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.os.Bundle;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.motiondetect.detector.MotionDetectApplication;
import com.motiondetect.detector.R;
import com.motiondetect.detector.data.bluetooth.BtManager;
import com.motiondetect.detector.data.dropbox.DropboxServiceManager;
import com.motiondetect.detector.model.BtDeviceModel;
import com.motiondetect.detector.contract.DeviceListContract;
import com.motiondetect.detector.presenter.DeviceListPresenter;
import com.motiondetect.detector.util.PermissionUtil;
import com.motiondetect.detector.view.ContextWrapper;
import com.motiondetect.detector.view.IConnectionDialog;
import com.motiondetect.detector.view.adapter.BtDeviceItemAdapter;
import com.motiondetect.detector.view.fragment.ConnectionDialogFragment;

import butterknife.BindView;
import butterknife.OnClick;

public class DeviceListActivity extends BaseActivity implements DeviceListContract.View {
    private static final String TAG = DeviceListActivity.class.getSimpleName();

    // Permission request code
    public static final int REQUEST_ENABLE_BT = 0;
    public static final int REQUEST_ACCESS_COARSE_LOCATION_PERMISSION = 1;
    public static final int REQUEST_STORAGE_PERMISSION = 2;

    private static final String[] PERMISSONS_STORAGE = new String[] {
            Manifest.permission.READ_EXTERNAL_STORAGE,
            Manifest.permission.WRITE_EXTERNAL_STORAGE
    };

    // UI
    @BindView(R.id.device_list_recycler) RecyclerView mRecycler;
    @BindView(R.id.device_scan_indicator) ProgressBar mProgressBar;
    @BindView(R.id.device_discovery_btn) ImageButton mDiscoverBtn;
    @BindView(R.id.dropbox_loging_btn) ImageButton mLoginBtn;
    private BtDeviceItemAdapter mDeviceAdapter;

    private DeviceListPresenter mPresenter;

    private final IConnectionDialog mConnectionDialog = new IConnectionDialog() {
        @Override
        public void showDialog(BtDeviceModel device) {
            Log.d(TAG, "showDialog() - device: " + device.name);
            ConnectionDialogFragment dialog = ConnectionDialogFragment.newInstance(device);
            dialog.setOnSelectTargetListener(new ConnectionDialogFragment.OnSelectTargetListener() {
                @Override
                public void onSelectTarget(BtDeviceModel target) {
                    Log.d(TAG, "onSelectTarget() - selected " + target.name);
                    mPresenter.onStartDataDisplayActivity(target);
                }
            });
            dialog.show(getSupportFragmentManager(), "ConnectionDialog");
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.d(TAG, "onCreate() - start");
        super.onCreate(savedInstanceState);

        BtManager btManager = ((MotionDetectApplication) getApplication()).getBtManager();
        DropboxServiceManager dbxManager = ((MotionDetectApplication) getApplication()).getDropboxServiceManager();
        mPresenter = new DeviceListPresenter(this, btManager, dbxManager);
        mPresenter.onCreate();

        Log.d(TAG, "onCreate() - end");
    }

    @Override
    public void startDataDisplayActivity(BtDeviceModel target) {
        Log.d(TAG, "Switch to DataDisplayActivity");
        Intent intent = new Intent(DeviceListActivity.this, DataDisplayActivity.class);
        Bundle bundle = new Bundle();
        bundle.putSerializable("device", target);
        intent.putExtras(bundle);
        startActivity(intent);
    }

    @Override
    public void initializeUI() {
        updateDiscoverButton(false);
        setupRecyclerView();
    }

    @Override
    public void requestPermissions() {
        mPresenter.requestBluetooth(new ContextWrapper(this), REQUEST_ENABLE_BT);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestLocationPermission();
            requestStoragePermission();
        }
    }

    private void requestLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    new String[] { Manifest.permission.ACCESS_COARSE_LOCATION },
                    REQUEST_ACCESS_COARSE_LOCATION_PERMISSION);
        }
    }

    private void requestStoragePermission() {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ActivityCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                    this,
                    PERMISSONS_STORAGE,
                    REQUEST_STORAGE_PERMISSION);
        }
    }

    @Override
    protected int getLayoutId() {
        return R.layout.activity_device_list;
    }

    @Override
    public void addDeviceItem(BtDeviceModel device) {
        mDeviceAdapter.addItem(device);
    }

    @Override
    public void showDiscovering() {
        mProgressBar.setVisibility(View.VISIBLE);
        updateDiscoverButton(true);
    }

    @Override
    public void dismissDiscovering() {
        mProgressBar.setVisibility(View.INVISIBLE);
        updateDiscoverButton(false);
    }

    @Override
    public void clearDeviceItems() {
        mDeviceAdapter.clearItems();
    }

    @OnClick(R.id.device_discovery_btn)
    public void onClickDiscoveryButton() {
        Log.d(TAG, "Discovery button gets clicked");
        mPresenter.onClickDiscoveryButton();
    }

    @OnClick(R.id.dropbox_loging_btn)
    public void onClickDropboxLoginButton() {
        Log.d(TAG, "Perform logging to Dropbox");
        mPresenter.onClickDropboxLoginButton();
    }

    private void setupRecyclerView() {
        mDeviceAdapter = new BtDeviceItemAdapter(mConnectionDialog);
        mRecycler.setLayoutManager(new LinearLayoutManager(this));
        mRecycler.setAdapter(mDeviceAdapter);
    }

    private void updateDiscoverButton(boolean discovering) {
        if (mDiscoverBtn == null) {
            Log.w(TAG, "Discover button is null");
            return;
        }

        Drawable drawable = mDiscoverBtn.getDrawable();
        drawable.setColorFilter(discovering ? Color.BLACK : Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        mDiscoverBtn.setClickable(!discovering);
        mDiscoverBtn.setImageDrawable(drawable);
    }

    @Override
    protected void onStart() {
        Log.d(TAG, "onStart()");
        super.onStart();

        mPresenter.onStart();
    }

    @Override
    protected void onResume() {
        Log.d(TAG, "onResume()");
        super.onResume();

        mPresenter.onResume();
    }

    @Override
    protected void onPause() {
        Log.d(TAG, "onPause()");
        super.onPause();

        mPresenter.onPause();
    }

    @Override
    protected void onStop() {
        Log.d(TAG, "onStop()");
        super.onStop();

        mPresenter.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.d(TAG, "onDestroy()");
        super.onDestroy();

        mPresenter.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_ENABLE_BT) {
            if (resultCode != RESULT_OK) {
                Log.w(TAG, "Bluetooth not turned on");

                // Notify the user that bluetooth must be turned on.
                Toast.makeText(this, "Bluetooth must be turned on", Toast.LENGTH_SHORT).show();
                mPresenter.requestBluetooth(new ContextWrapper(this), REQUEST_ENABLE_BT);
            } else {
                Log.d(TAG, "Bluetooth has been turned on");
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_STORAGE_PERMISSION:
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    Log.d(TAG, "REQUEST_STORAGE_PERMISSION granted");
                } else {
                    requestStoragePermission();
                }
                break;
            case REQUEST_ACCESS_COARSE_LOCATION_PERMISSION:
                if (PermissionUtil.verifyPermissions(grantResults)) {
                    Log.d(TAG, "REQUEST_ACCESS_COARSE_LOCATION_PERMISSION granted");
                } else {
                    requestLocationPermission();
                }
                break;
        }

        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Override
    public void setPresenter(DeviceListContract.Presenter presenter) {
        mPresenter = (DeviceListPresenter) presenter;
    }
}
