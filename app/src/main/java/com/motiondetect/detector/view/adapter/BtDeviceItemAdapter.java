package com.motiondetect.detector.view.adapter;

import android.databinding.DataBindingUtil;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.motiondetect.detector.R;
import com.motiondetect.detector.databinding.ItemDeviceBinding;
import com.motiondetect.detector.model.BtDeviceModel;
import com.motiondetect.detector.view.IConnectionDialog;
import com.motiondetect.detector.viewModel.DeviceViewModel;

import java.util.ArrayList;
import java.util.List;

public class BtDeviceItemAdapter extends RecyclerView.Adapter<BtDeviceItemAdapter.BindingHolder> {
    private static final String TAG = BtDeviceItemAdapter.class.getSimpleName();

    private List<BtDeviceModel> mDeviceList;
    private IConnectionDialog mDialog;

    public BtDeviceItemAdapter(IConnectionDialog dialog) {
        mDeviceList = new ArrayList<>();
        mDialog = dialog;
    }

    @Override
    public BindingHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        ItemDeviceBinding binding = DataBindingUtil.inflate(
                LayoutInflater.from(parent.getContext()),
                R.layout.item_device,
                parent,
                false);
        return new BindingHolder(binding);
    }

    @Override
    public void onBindViewHolder(BindingHolder holder, int position) {
        holder.binding.setViewModel(new DeviceViewModel(mDeviceList.get(position), mDialog));
    }

    @Override
    public int getItemCount() {
        return mDeviceList.size();
    }

    public void addItem(BtDeviceModel device) {
        if (device != null && !mDeviceList.contains(device)) {
            mDeviceList.add(device);
            Log.v(TAG, "addItem() - added " + device.name + " into device list");
            notifyItemInserted(mDeviceList.size());
        }
    }

    public List<BtDeviceModel> getItems() {
        return mDeviceList;
    }

    public void clearItems() {
        int size = mDeviceList.size();
        mDeviceList.clear();
        notifyItemRangeRemoved(0, size);
    }

    public static class BindingHolder extends RecyclerView.ViewHolder {
        private ItemDeviceBinding binding;

        public BindingHolder(ItemDeviceBinding binding) {
            super(binding.deviceItemCardView);
            this.binding = binding;
        }
    }
}
