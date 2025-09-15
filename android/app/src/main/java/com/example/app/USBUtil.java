package com.print.usbprint.util;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbEndpoint;
import android.hardware.usb.UsbInterface;
import android.hardware.usb.UsbManager;
import android.util.Log;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class USBUtil {
    private static final String TAG = "USBUtil";
    private static USBUtil instance;
    private UsbManager usbManager;
    private UsbDeviceConnection connection;
    private UsbEndpoint outputEndpoint; // 输出端点（用于发送数据）

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

    public List<UsbDevice> getDeviceList(Context context) {
        if (usbManager == null) init(context);
        HashMap<String, UsbDevice> deviceMap = usbManager.getDeviceList();
        return new ArrayList<>(deviceMap.values());
    }

    public boolean IsOpen(UsbDevice device, Context context) {
        if (usbManager == null) init(context);
        if (device == null) return false;

        // 检查权限
        if (!usbManager.hasPermission(device)) {
            Log.e(TAG, "No USB permission for device");
            return false;
        }

        // 打开设备连接（若未连接）
        if (connection == null) {
            connection = usbManager.openDevice(device);
            if (connection == null) {
                Log.e(TAG, "Failed to open device connection");
                return false;
            }
            // 查找批量输出端点
            outputEndpoint = findOutputEndpoint(device);
            if (outputEndpoint == null) {
                Log.e(TAG, "No bulk output endpoint found");
                connection.close();
                connection = null;
                return false;
            }
        }
        return true;
    }

    public void sendData(byte[] data) {
        if (connection == null || outputEndpoint == null) {
            Log.e(TAG, "Connection or endpoint is null");
            return;
        }
        // 通过批量传输发送数据（超时1秒）
        int bytesWritten = connection.bulkTransfer(outputEndpoint, data, data.length, 1000);
        if (bytesWritten < 0) {
            Log.e(TAG, "Failed to send data, error code: " + bytesWritten);
        } else {
            Log.d(TAG, "Sent " + bytesWritten + " bytes");
        }
    }

    /**
     * 查找设备的批量输出端点（关键修正：使用常量值替代可能未识别的符号）
     */
    private UsbEndpoint findOutputEndpoint(UsbDevice device) {
        if (device == null || connection == null) {
            Log.e(TAG, "Device or connection is null");
            return null;
        }

        // 遍历设备的所有接口
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface usbInterface = device.getInterface(i);
            if (usbInterface == null) continue;

            // 申请占用接口（必须操作，否则无法使用端点）
            if (!connection.claimInterface(usbInterface, true)) {
                Log.e(TAG, "Failed to claim interface " + i);
                continue;
            }

            // 遍历接口的所有端点，查找批量输出端点
            for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(j);
                if (endpoint == null) continue;

                // 关键修正：使用常量值 0x02 代替 UsbEndpoint.TYPE_BULK（批量传输）
                // 0x00 代替 UsbEndpoint.DIRECTION_OUT（输出方向）
                if (endpoint.getType() == 0x02 && endpoint.getDirection() == 0x00) {
                    Log.d(TAG, "Found bulk output endpoint at interface " + i + ", endpoint " + j);
                    return endpoint;
                }
            }
        }
        return null;
    }

    // 关闭连接
    public void close() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
        outputEndpoint = null;
    }
}