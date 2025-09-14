package com.print.usbprint.util;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class USBUtil {
    private static USBUtil instance;
    private UsbManager usbManager;
    private UsbDeviceConnection connection;

    private USBUtil() {}

    public static synchronized USBUtil getInstance() {
        if (instance == null) {
            instance = new USBUtil();
        }
        return instance;
    }

    public void init(Context context) {
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
    }

    public List<UsbDevice> getDeviceList(Context context) {
        if (usbManager == null) init(context);
        HashMap<String, UsbDevice> deviceMap = usbManager.getDeviceList();
        return new ArrayList<>(deviceMap.values());
    }

    public boolean IsOpen(UsbDevice device, Context context) {
        if (usbManager == null) init(context);
        // 检查权限并打开设备（简化版）
        if (!usbManager.hasPermission(device)) {
            // 这里需要请求权限的逻辑，参考原代码的USBReceiver
            return false;
        }
        // 实际项目中需要实现设备连接逻辑
        return true;
    }

    public void sendData(byte[] data) {
        // 实现向USB设备发送数据的逻辑
        if (connection != null) {
            // 具体发送逻辑根据打印机型号调整
        }
    }
}