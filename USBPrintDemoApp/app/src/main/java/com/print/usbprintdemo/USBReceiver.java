package com.print.usbprintdemo;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.util.Log;
import com.print.usbprint.util.USBUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;



/**
 * USB 设备监听广播
 *
 */

public class USBReceiver extends BroadcastReceiver {
    public static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private final static String ACTION = "android.hardware.usb.action.USB_STATE";
    private USBUtil usbUtil;
    private UsbManager mUsbManager;
    private UsbDevice mDevice;
    private HashMap<String, UsbDevice> deviceList;
    List<UsbDevice> usbDeviceList = new ArrayList<>();
    MainActivity mainActivity;

    public USBReceiver(MainActivity mainActivity) {
        this.mainActivity = mainActivity;
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        if (ACTION_USB_PERMISSION.equals(action)) {
            // 获取权限结果的广播
            synchronized (this) {
                UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                if (device != null) {
                    //call method to set up device communication
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        Log.e("USBReceiver", "获取权限成功：" + device.getDeviceName());
                        usbDeviceList.add(device);
                        Log.i("USBReceiver", String.valueOf(usbDeviceList));
                        USBUtil.getInstance().setDeviceList(usbDeviceList);
                        if (device.getVendorId() == 1155 && device.getProductId() == 22336) {
                            mainActivity.refresh();
                        }
                        else if (device.getVendorId() == 10473 && device.getProductId() == 22546) {
                            mainActivity.refresh();
                        }

                    } else {
                        Log.e("USBReceiver", "获取权限失败：" + device.getDeviceName());
                    }
                }
            }
        } else if (UsbManager.ACTION_USB_DEVICE_ATTACHED.equals(action)) {
            UsbDevice device = (UsbDevice) intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
            Log.i("检测到", "usb插入");
            getUsbPermission(device, context);
            // 有新的设备插入了，在这里一般会判断这个设备是不是我们想要的，是的话就去请求权限
        } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            // 有设备拔出了
            Log.i("USBReceiver检测到", "usb拔出");
            if (usbDeviceList != null){
                usbDeviceList.clear();
            }
            if (mainActivity != null){
                mainActivity.refresh();
            }

        } else if (ACTION.equals(action)) {
            Log.i(action, ACTION);
            //已经有usb设备连接
            try {
                getDetail(context);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void getUsbPermission(UsbDevice mUSBDevice, Context mContext) {
        PendingIntent pendingIntent = PendingIntent.getBroadcast(mContext, 0, new Intent(ACTION_USB_PERMISSION), 0);

        USBUtil.getInstance().init(mContext);
        mUsbManager = USBUtil.getInstance().getUsbManager();
        USBUtil.getInstance().setUsbManager(mUsbManager);
        mUsbManager.requestPermission(mUSBDevice, pendingIntent);
    }

    public void getDetail(Context context) throws IOException {
        UsbManager manager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        USBUtil.getInstance().setUsbManager(manager);
        deviceList = manager.getDeviceList();
        Iterator<UsbDevice> deviceIterator = deviceList.values().iterator();
        while (deviceIterator.hasNext()) {
            UsbDevice device = deviceIterator.next();
            PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 0, new Intent(ACTION_USB_PERMISSION), 0);
            manager.requestPermission(device, pendingIntent);
            String Model = device.getDeviceName();
//            int DeviceID = device.getDeviceId();
//            int Vendor = device.getVendorId();
//            int Product = device.getProductId();
//            int Class = device.getDeviceClass();
//            int Subclass = device.getDeviceSubclass();
        }
    }


}
