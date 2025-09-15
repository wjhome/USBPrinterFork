package com.print.usbprintdemo;

import android.annotation.SuppressLint;
import android.hardware.usb.UsbDevice;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.recyclerview.widget.RecyclerView;

import com.print.usbprint.util.USBUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * usb适配器
 */

public class USBDeviceRecAdapter extends RecyclerView.Adapter<USBDeviceRecAdapter.MyHolder> {
    private List<UsbDevice> data;
    private OnItemClick onItemClick;
    private Toast toast;

    public USBDeviceRecAdapter(OnItemClick onItemClick) {
        this.onItemClick = onItemClick;
        data = new ArrayList<>();
    }

    @NonNull
    @Override
    public MyHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.usb_item, parent, false);
        return new MyHolder(view);
    }

    @SuppressLint("SetTextI18n")
    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onBindViewHolder(@NonNull MyHolder holder, int position) {
        final UsbDevice usbDevice = data.get(position);
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    onItemClick.onItemClick(usbDevice);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("USBDeviceRecAdapter",usbDevice.getDeviceName());
                USBUtil.getInstance().IsOpen(usbDevice,v.getContext());
            }
        });
        holder.deviceName.setText(R.string.device + usbDevice.getDeviceName()+"VID："+usbDevice.getVendorId()+"PID:"+usbDevice.getProductId());
        holder.goprint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                UsbDevice mDevice = usbDevice;
                try {
                    onItemClick.onItemClick(usbDevice);
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Log.i("USBDeviceRecAdapter",usbDevice.getDeviceName());
                USBUtil.getInstance().IsOpen(usbDevice,v.getContext());
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public void setData(List<UsbDevice> data) {
        if (data != null) {
            this.data = data;
        } else {
            this.data.clear();
        }
        notifyDataSetChanged();
    }

    class MyHolder extends RecyclerView.ViewHolder {
        TextView deviceName;
        TextView goprint;

        public MyHolder(View itemView) {
            super(itemView);
            deviceName = itemView.findViewById(R.id.device_name);
            goprint = itemView.findViewById(R.id.goprint);
        }
    }

    public interface OnItemClick {
        void onItemClick(UsbDevice device) throws IOException;
    }
}
