package com.motiondetect.detector.view.fragment;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

import com.motiondetect.detector.model.BtDeviceModel;

public class ConnectionDialogFragment extends DialogFragment {
    public interface OnSelectTargetListener {
        void onSelectTarget(BtDeviceModel target);
    }

    private OnSelectTargetListener mListener;

    public static ConnectionDialogFragment newInstance(BtDeviceModel device) {
        ConnectionDialogFragment fragment = new ConnectionDialogFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable("device", device);
        fragment.setArguments(bundle);
        return fragment;
    }

    public void setOnSelectTargetListener(OnSelectTargetListener listener) {
        mListener = listener;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final BtDeviceModel device = (BtDeviceModel) getArguments().getSerializable("device");

        final AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        builder.setTitle("Connect to " + device.name + "?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (mListener != null) {
                            mListener.onSelectTarget(device);
                        }
                    }
                })
                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dismiss();
                    }
                });

        return builder.create();
    }
}
