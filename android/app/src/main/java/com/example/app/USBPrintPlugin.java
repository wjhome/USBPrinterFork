package com.example.app;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
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
    private UsbManager usbManager;
    private static final String ACTION_USB_PERMISSION = "com.example.app.USB_PERMISSION";
    private PluginCall pendingCall; // 保存等待权限的回调

    // 权限广播接收器
    private final BroadcastReceiver usbPermissionReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);
                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false) && device != null) {
                        // 权限已授予，继续打印
                        if (pendingCall != null) {
                            handlePrint(device, pendingCall);
                        }
                    } else {
                        // 权限被拒绝
                        if (pendingCall != null) {
                            pendingCall.reject("用户拒绝了USB权限");
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
        usbManager = (UsbManager) context.getSystemService(Context.USB_SERVICE);

        // 注册权限广播接收器
        IntentFilter filter = new IntentFilter(ACTION_USB_PERMISSION);
        context.registerReceiver(usbPermissionReceiver, filter);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        // 注销广播接收器，避免内存泄漏
        context.unregisterReceiver(usbPermissionReceiver);
    }

    /**
     * 获取USB设备列表
     */
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

    /**
     * 打印文本（处理权限和连接）
     */
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
            call.reject("未找到设备（VID: " + vid + ", PID: " + pid + "）");
            return;
        }

        // 检查权限
        if (!usbManager.hasPermission(targetDevice)) {
            pendingCall = call;
            // 请求权限
            Intent permissionIntent = new Intent(ACTION_USB_PERMISSION);
            usbManager.requestPermission(targetDevice, permissionIntent);
        } else {
            // 已有权限，直接打印
            handlePrint(targetDevice, call);
        }
    }

    /**
     * 实际执行打印逻辑
     */
    private void handlePrint(UsbDevice device, PluginCall call) {
        try {
            // 打开设备连接
            boolean isConnected = usbUtil.openDevice(device);
            if (!isConnected) {
                call.reject("设备连接失败");
                return;
            }

            // 构建打印数据（ESC/POS指令：换行+文本+切纸）
            byte[] printData = buildPrintData(call.getString("text"));
            // 发送数据
            boolean isSent = usbUtil.sendData(printData);
            if (isSent) {
                call.resolve(new JSObject().put("success", true).put("message", "打印成功"));
            } else {
                call.reject("数据发送失败");
            }

            // 关闭连接
            usbUtil.closeDevice();

        } catch (Exception e) {
            call.reject("打印失败：" + e.getMessage());
        } finally {
            pendingCall = null;
        }
    }

    /**
     * 构建符合ESC/POS标准的打印数据
     */
    private byte[] buildPrintData(String text) {
        try {
            // 文本内容（GBK编码保证中文正常显示）
            byte[] textBytes = (text + "\n\n").getBytes("GBK");
            // 切纸指令（ESC/POS标准：GS V 0）
            byte[] cutCommand = new byte[]{0x1D, 0x56, 0x00};
            // 合并数据
            byte[] allData = new byte[textBytes.length + cutCommand.length];
            System.arraycopy(textBytes, 0, allData, 0, textBytes.length);
            System.arraycopy(cutCommand, 0, allData, textBytes.length, cutCommand.length);
            return allData;
        } catch (Exception e) {
            return text.getBytes(); // 异常时使用默认编码
        }
    }

    /**
     * 根据VID和PID查找设备
     */
    private UsbDevice findDeviceByVidPid(int vid, int pid) {
        List<UsbDevice> devices = usbUtil.getDeviceList(context);
        for (UsbDevice device : devices) {
            if (device.getVendorId() == vid && device.getProductId() == pid) {
                return device;
            }
        }
        return null;
    }
}