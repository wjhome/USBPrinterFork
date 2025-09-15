package com.example.app;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.print.usbprint.util.USBUtil;
import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(name = "USBPrintPlugin")
public class USBPrintPlugin extends Plugin {
    private Context context;
    private USBUtil usbUtil;
    private static final String ACTION_USB_PERMISSION = "com.example.app.USB_PERMISSION";
    private PluginCall pendingCall; // 保存等待权限的调用

    // 权限请求广播接收器
    private final android.content.BroadcastReceiver usbPermissionReceiver = new android.content.BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null) {
                        // 权限授予后执行打印
                        if (pendingCall != null) {
                            executePrint(pendingCall, device);
                        }
                    } else {
                        // 权限被拒绝
                        if (pendingCall != null) {
                            pendingCall.reject("用户拒绝USB权限");
                            pendingCall = null;
                        }
                    }
                }
            }
        }
    };

    @Override
    public void load() {
        super.load();
        context = getContext();
        usbUtil = USBUtil.getInstance();
        usbUtil.init(context);
        // 注册权限广播接收器
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(usbPermissionReceiver, filter);
    }

    // 获取USB设备列表
    @PluginMethod
    public void getDevices(PluginCall call) {
        List<UsbDevice> deviceList = usbUtil.getDeviceList(context);
        List<JSObject> devices = new ArrayList<>();
        
        for (UsbDevice device : deviceList) {
            JSObject obj = new JSObject();
            obj.put("vid", device.getVendorId());
            obj.put("pid", device.getProductId());
            obj.put("name", device.getDeviceName());
            devices.add(obj);
        }
        
        JSObject result = new JSObject();
        result.put("devices", devices);
        call.resolve(result);
    }

    // 连接设备并打印文本
    @PluginMethod
    public void printText(PluginCall call) {
        int vid = call.getInt("vid");
        int pid = call.getInt("pid");
        String text = call.getString("text");
        
        if (text == null || text.isEmpty()) {
            call.reject("打印内容不能为空");
            return;
        }

        UsbDevice targetDevice = findDeviceByVidPid(vid, pid);
        if (targetDevice == null) {
            call.reject("未找到设备（VID:" + vid + ", PID:" + pid + "）");
            return;
        }

        // 检查权限，若无则请求
        if (!usbUtil.hasPermission(targetDevice)) {
            pendingCall = call; // 保存当前调用
            usbUtil.requestPermission(targetDevice, context, ACTION_USB_PERMISSION);
        } else {
            // 已有权限，直接打印
            executePrint(call, targetDevice);
        }
    }

    // 执行打印逻辑
    private void executePrint(PluginCall call, UsbDevice device) {
        try {
            if (usbUtil.openDevice(device)) {
                // 拼接打印指令（示例：添加换行和切纸指令）
                String printContent = text + "\n\n"; // 换行
                byte[] printData = printContent.getBytes("GBK");
                // 添加切纸指令（根据打印机型号调整）
                byte[] cutCommand = {0x1D, 0x56, 0x01}; // 部分打印机切纸指令
                byte[] allData = new byte[printData.length + cutCommand.length];
                System.arraycopy(printData, 0, allData, 0, printData.length);
                System.arraycopy(cutCommand, 0, allData, printData.length, cutCommand.length);
                
                usbUtil.sendData(allData);
                usbUtil.closeDevice(); // 打印完成后关闭连接
                call.resolve(new JSObject().put("success", true));
            } else {
                call.reject("无法打开设备");
            }
        } catch (Exception e) {
            call.reject("打印失败: " + e.getMessage());
        } finally {
            pendingCall = null; // 清空等待调用
        }
    }

    // 根据VID和PID查找设备
    private UsbDevice findDeviceByVidPid(int vid, int pid) {
        List<UsbDevice> devices = usbUtil.getDeviceList(context);
        for (UsbDevice device : devices) {
            if (device.getVendorId() == vid && device.getProductId() == pid) {
                return device;
            }
        }
        return null;
    }

    @Override
    public void destroy() {
        super.destroy();
        // 销毁时注销广播接收器
        context.unregisterReceiver(usbPermissionReceiver);
    }
}