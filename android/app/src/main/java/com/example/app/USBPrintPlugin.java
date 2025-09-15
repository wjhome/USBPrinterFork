package com.example.app;

import android.content.Context;
import android.hardware.usb.UsbDevice;
import com.getcapacitor.JSObject;
import com.getcapacitor.Plugin;
import com.getcapacitor.PluginCall;
import com.getcapacitor.PluginMethod;
import com.getcapacitor.annotation.CapacitorPlugin;
import com.print.usbprint.util.USBUtil;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.List;

@CapacitorPlugin(name = "USBPrintPlugin")
public class USBPrintPlugin extends Plugin {
    private Context context;
    private USBUtil usbUtil;

    @Override
    public void load() {
        super.load();
        context = getContext();
        usbUtil = USBUtil.getInstance();
        usbUtil.init(context);
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
            obj.put("productName", device.getProductName()); // 补充产品名称
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
            call.reject("Text cannot be empty");
            return;
        }

        UsbDevice targetDevice = findDeviceByVidPid(vid, pid);
        if (targetDevice == null) {
            call.reject("Device not found (VID: " + vid + ", PID: " + pid + ")");
            return;
        }

        try {
            if (usbUtil.IsOpen(targetDevice, context)) {
                // 处理编码异常（GBK编码可能不被所有设备支持，可根据打印机调整）
                byte[] printData;
                try {
                    printData = (text + "\n").getBytes("GBK");
                } catch (UnsupportedEncodingException e) {
                    //  fallback to UTF-8
                    printData = (text + "\n").getBytes("UTF-8");
                }
                usbUtil.sendData(printData);
                call.resolve(new JSObject().put("success", true));
            } else {
                call.reject("Failed to open device (check permission)");
            }
        } catch (Exception e) {
            call.reject("Print failed: " + e.getMessage());
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
    protected void handleOnDestroy() {
        super.handleOnDestroy();
        // 释放资源
        usbUtil.close();
    }
}