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
    private UsbEndpoint outputEndpoint; // 用于发送数据的批量输出端点

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

    // 修正方法名（Java规范：方法名小写开头）
    public boolean isOpen(UsbDevice device, Context context) {
        if (usbManager == null) init(context);

        // 1. 检查是否有设备权限
        if (!usbManager.hasPermission(device)) {
            Log.e(TAG, "没有设备操作权限，请先申请权限");
            return false;
        }

        // 2. 打开设备连接
        connection = usbManager.openDevice(device);
        if (connection == null) {
            Log.e(TAG, "无法打开设备连接");
            return false;
        }

        // 3. 查找批量输出端点（打印机通常使用这种端点发送数据）
        outputEndpoint = findBulkOutputEndpoint(device);
        return outputEndpoint != null;
    }

    // 查找批量输出端点（核心逻辑）
    private UsbEndpoint findBulkOutputEndpoint(UsbDevice device) {
        // 遍历设备的所有接口
        for (int i = 0; i < device.getInterfaceCount(); i++) {
            UsbInterface usbInterface = device.getInterface(i);
            if (usbInterface == null) continue;

            // 申请占用该接口（必须操作，否则无法使用接口下的端点）
            if (!connection.claimInterface(usbInterface, true)) {
                Log.e(TAG, "无法占用接口 " + i);
                continue;
            }

            // 遍历接口的所有端点，寻找批量输出类型
            for (int j = 0; j < usbInterface.getEndpointCount(); j++) {
                UsbEndpoint endpoint = usbInterface.getEndpoint(j);
                if (endpoint != null
                        && endpoint.getType() == UsbEndpoint.TYPE_BULK  // 批量传输类型
                        && endpoint.getDirection() == UsbEndpoint.DIRECTION_OUT) {  // 输出方向（发送数据到设备）
                    Log.d(TAG, "找到批量输出端点：接口" + i + ", 端点" + j);
                    return endpoint;
                }
            }
        }
        Log.e(TAG, "未找到批量输出端点，设备可能不支持打印");
        return null;
    }

    // 发送数据到USB设备
    public void sendData(byte[] data) {
        if (connection == null || outputEndpoint == null || data == null) {
            Log.e(TAG, "连接未建立或端点无效，无法发送数据");
            return;
        }

        // 通过批量传输发送数据（超时时间1秒）
        int bytesWritten = connection.bulkTransfer(outputEndpoint, data, data.length, 1000);
        if (bytesWritten <= 0) {
            Log.e(TAG, "数据发送失败，写入字节数：" + bytesWritten);
        } else {
            Log.d(TAG, "成功发送 " + bytesWritten + " 字节数据");
        }
    }

    // 关闭设备连接，释放资源
    public void close() {
        if (connection != null) {
            connection.close();
            connection = null;
        }
        outputEndpoint = null;
    }
}