package com.print.usbprint.util;

import android.content.Context;
import android.content.Intent;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.hardware.usb.UsbEndpoint;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class USBUtil {
    private static USBUtil instance;
    private UsbManager usbManager;
    private UsbDeviceConnection connection;
    private UsbInterface usbInterface;
    private UsbEndpoint outEndpoint; // 输出端点（发送数据到设备）

    private USBUtil() {}

    public static synchronized USBUtil getInstance() {
        if (instance == null) {
            instance = new USBUtil();
        }
        return instance;
    }

    public void init(Context context) {
        if (usbManager == null) {
            usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);
        }
    }

    // 获取设备列表
    public List<UsbDevice> getDeviceList(Context context) {
        init(context);
        HashMap<String, UsbDevice> deviceMap = usbManager.getDeviceList();
        return new ArrayList<>(deviceMap.values());
    }

    // 检查是否有设备权限
    public boolean hasPermission(UsbDevice device) {
        return usbManager.hasPermission(device);
    }

    // 请求设备权限
    public void requestPermission(UsbDevice device, Context context, String action) {
        PendingIntent permissionIntent = PendingIntent.getBroadcast(
            context, 0, new Intent(action), PendingIntent.FLAG_IMMUTABLE
        );
        usbManager.requestPermission(device, permissionIntent);
    }

    // 打开设备连接
    public boolean openDevice(UsbDevice device) {
        if (device == null) return false;

        // 查找第一个可用的接口和输出端点
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface intf = device.getInterface(i);
            // 查找输出端点（方向为OUT）
            for (int j = 0; j < intf.getEndpointCount(); j++) {
                UsbEndpoint endpoint = intf.getEndpoint(j);
                if (endpoint.getDirection() == UsbEndpoint.DIRECTION_OUT) {
                    usbInterface = intf;
                    outEndpoint = endpoint;
                    break;
                }
            }
            if (usbInterface != null) break;
        }

        if (usbInterface == null || outEndpoint == null) {
            return false; // 未找到可用端点
        }

        // 打开设备连接
        connection = usbManager.openDevice(device);
        if (connection == null) {
            return false;
        }

        //  claim接口（获取接口使用权）
        return connection.claimInterface(usbInterface, true);
    }

    // 发送数据到设备
    public int sendData(byte[] data) {
        if (connection == null || outEndpoint == null || data == null) {
            return -1;
        }
        // 通过输出端点发送数据
        return connection.bulkTransfer(outEndpoint, data, data.length, 1000); // 超时1秒
    }

    // 关闭设备连接
    public void closeDevice() {
        if (connection != null) {
            connection.releaseInterface(usbInterface);
            connection.close();
            connection = null;
        }
        usbInterface = null;
        outEndpoint = null;
    }
}