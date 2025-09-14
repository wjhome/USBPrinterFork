package com.example.app;

import android.content.Context;
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
            call.reject("Device not found");
            return;
        }

        try {
            if (usbUtil.IsOpen(targetDevice, context)) {
                // 发送打印命令（这里简化为文本打印，实际需根据打印机指令集调整）
                byte[] printData = (text + "\n").getBytes("GBK");
                usbUtil.sendData(printData); // 假设USBUtil有此方法
                call.resolve(new JSObject().put("success", true));
            } else {
                call.reject("Failed to open device");
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
}