package com.example.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbManager;
import android.util.Log;

public class USBReceiver extends BroadcastReceiver {
    private static final String TAG = "USBReceiver";
    private static final String ACTION_USB_PERMISSION = "com.print.usbprint.USB_PERMISSION";

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_USB_PERMISSION.equals(action)) {
            synchronized (this) {
                // 获取权限请求的设备
                android.hardware.usb.UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                // 检查是否获得权限
                if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                    if (device != null) {
                        Log.d(TAG, "Permission granted for device: " + device.getDeviceName());
                        // 权限获取后可重新尝试连接设备
                    }
                } else {
                    Log.d(TAG, "Permission denied for device: " + device.getDeviceName());
                }
            }
        }
    }
}