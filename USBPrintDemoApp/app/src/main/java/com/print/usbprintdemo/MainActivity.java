package com.print.usbprintdemo;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbDeviceConnection;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import com.print.usbprint.util.USBUtil;


import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static com.print.usbprintdemo.USBReceiver.ACTION_USB_PERMISSION;

public class MainActivity extends AppCompatActivity {

    private UsbDevice mDevice;
    private Toast toast;
    Toolbar toolbar;
    USBDeviceRecAdapter deviceRecAdapter;
    private final static String ACTION = "android.hardware.usb.action.USB_STATE";
    public static MainActivity mainActivity;
    private RecyclerView recyclerView;
    private USBReceiver usbReceiver;
    private IntentFilter filter;
    private List<UsbDevice> deviceList = new ArrayList<>();

    private String activityName = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button loaddevices = (Button) findViewById(R.id.load_devices);
        recyclerView = (RecyclerView) findViewById(R.id.usb_devices);
        toolbar = (Toolbar) findViewById(R.id.maintoolbar);
        mainActivity = this;

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null){
            activityName = bundle.getString("activity");
            bundle.clear();
        }

        toolbar.setTitle(R.string.usbConnect);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//显示toolbar的返回按钮
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        // Broadcast listen for new devices
        registerBroadcast();


        deviceRecAdapter = new USBDeviceRecAdapter(new USBDeviceRecAdapter.OnItemClick() {
            @Override
            public void onItemClick(UsbDevice device) throws IOException {
                mDevice = device;
                if (USBUtil.getInstance().IsOpen(mDevice, MainActivity.this)) {
                    goPrint(mDevice);
                }
            }
        });

        loaddevices.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refresh();
            }
        });

        refresh();
    }

    //对比list集，删除ProductId重复的数据
    public static List removelist(List<UsbDevice> list) {
        for (int i = 0; i < list.size(); i++) {
            for (int j = list.size() - 1; j > i; j--) {
                if (list.get(i).getDeviceName().equals(list.get(j).getDeviceName())) {
                    list.remove(j);
                }
            }
        }
        List<UsbDevice> usbDeviceList = new ArrayList<>();
        for (int i = 0; i< list.size(); i++) {
            if (list.get(i).getProductId() == 13624 && list.get(i).getVendorId() == 19267) {
                usbDeviceList.add(list.get(i));
            }else if (list.get(i).getProductId() == 22336 && list.get(i).getVendorId() == 1155) {
                usbDeviceList.add(list.get(i));
            }else if (list.get(i).getProductId() == 42151 && list.get(i).getVendorId() == 1317){
                usbDeviceList.add(list.get(i));
            }else if (list.get(i).getProductId() == 22546 && list.get(i).getVendorId() == 10473){
                usbDeviceList.add(list.get(i));
            }

        }
        return usbDeviceList;
    }

    public void refresh() {

        if (deviceList != null) {
            deviceList.clear();
        }

        deviceList = USBUtil.getInstance().getDeviceList(this);

        if (deviceList != null && deviceList.size() >0) {

            deviceRecAdapter.setData(deviceList);
            recyclerView.setAdapter(deviceRecAdapter);
            deviceRecAdapter.notifyDataSetChanged();

            //只有一个才自动连接，跳转页面
            if (deviceList.size()==1){
                UsbDevice usbDevice = deviceList.get(0);
                if (//不是默认的设备不自动跳转
                        !(usbDevice.getVendorId() == 1155 && usbDevice.getProductId() == 22336 )
                        || !(usbDevice.getVendorId() == 1317 && usbDevice.getProductId() == 42151 )
                        || !(usbDevice.getVendorId() == 19267 && usbDevice.getProductId() == 13624 )
                        || !(usbDevice.getProductId() == 22546 && usbDevice.getVendorId() == 10473)
                ){
                    return;
                }

                if (USBUtil.getInstance().IsOpen(usbDevice,this)){
                    if (toast != null){
                        toast.cancel();
                    }
                    String string = new String(getString(R.string.pid)+usbDevice.getProductId() + getString(R.string.vid)+usbDevice.getVendorId());
                    toast = Toast.makeText(this, string, Toast.LENGTH_LONG);
                    toast.show();
                    goPrint(usbDevice);
                }

            }
        }
        else {
            if (toast != null) {
                toast.cancel();
            }

            toast = Toast.makeText(this, R.string.deviceLIstNull, Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (usbReceiver == null){
            registerBroadcast();
        }
    }

    public void onResume() {
        super.onResume();
        if (usbReceiver == null) {
            registerBroadcast();
        }
    }

    public void registerBroadcast(){
        if (usbReceiver == null) {
            filter = new IntentFilter();
            usbReceiver = new USBReceiver(MainActivity.this);
            filter.addAction(ACTION_USB_PERMISSION);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            filter.addAction(ACTION);//usb连接状态广播
            this.registerReceiver(usbReceiver, filter);
        }
    }

    public void goPrint(UsbDevice device){
        Intent intent = null;
        if (activityName == null){
            intent = new Intent(MainActivity.this, USBESCPrintActivity.class);
        }
        else if (activityName.equals("ESC")){
            intent = new Intent(MainActivity.this, USBESCPrintActivity.class);
        }
        else if (activityName.equals("Label")){
            intent = new Intent(MainActivity.this, USBLabelActivity.class);
        }
        Bundle bundle = new Bundle();
        bundle.putString("VID", String.valueOf(device.getVendorId()));
        bundle.putString("PID", String.valueOf(device.getProductId()));
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        Intent finalIntent = intent;
        Thread myThread = new Thread() {//创建子线程
            @Override
            public void run() {
                try {
                    sleep(1000);//延迟一秒
                    startActivity(finalIntent);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        };
        myThread.start();
    }

//    @Override
//    public void onStop(){
//        super.onStop();
//        if (usbReceiver != null){
//            this.unregisterReceiver(usbReceiver);
//            usbReceiver = null;
//        }
//    }

}
