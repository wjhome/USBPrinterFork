package com.print.usbprintdemo;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.AssetManager;
import android.widget.TextView;
import android.hardware.usb.UsbManager;
import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.appcompat.app.AlertDialog;

import com.print.usbprint.command.Esc;
import com.print.usbprint.command.Label;
import com.print.usbprint.util.USBUtil;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.ByteBuffer;
import java.nio.CharBuffer;
import java.nio.charset.Charset;
import java.text.DecimalFormat;

public class PrinterStateReceiver extends BroadcastReceiver{
    public static final String PrinterState = "PrinterState";

    private TextView textView;
    private Activity activity;
    public Esc esc = new Esc();

    public PrinterStateReceiver(Activity activity,TextView textView){
        this.textView = textView;
        this.activity = activity;
    }

    public void setEsc(Esc esc) {
        this.esc = esc;
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();

        byte[] byteResult = intent.getByteArrayExtra(PrinterState);
        String stringState = bytesToHexString(byteResult);
        if (stringState == null){
            stringState = "";
        }

        if (stringState.indexOf("FC4F4B") != -1) { //打印完成（FC 4F 4B）
//            textView.setText("打印完成");
            Toast toast = Toast.makeText(context, R.string.printComplete, Toast.LENGTH_SHORT);
            toast.show();
        }
        else if(stringState.indexOf("FC6E6F") != -1){ // 打印未完成（FC 6E 6F）
//            textView.setText("打印未完成");
            Toast toast = Toast.makeText(context, R.string.printIncomplete, Toast.LENGTH_SHORT);
            toast.show();
        }
        else if (stringState.indexOf("EF231A") != -1) { //缺纸 （EF 23 1A）
            textView.setText(R.string.noPaper);
        }
        else if (stringState.indexOf("FE2312") != -1 ){ //有纸（FE 23 12）
            textView.setText(R.string.hasPaper);
        }
        else if (stringState.indexOf("FE2410") != -1) { //纸将尽（FE 24 10）
            textView.setText(R.string.thePaperWillRunOut);
        }
        else if(stringState.indexOf("FE2411") != -1){ // 纸还有（FE 24 11）
            textView.setText(R.string.paperSufficient);
        }
        else if (stringState.indexOf("FE2510") != -1) { //温度正常（FE 25 10）
            textView.setText(R.string.normalTemperature);
        }
        else if(stringState.indexOf("FE2511") != -1){ // 温度不正常（FE 25 11）
            textView.setText(R.string.abnormalTemperature);
        }
        else if (stringState.indexOf("FE2610") != -1) { //切刀复位（FE 26 10）
            textView.setText(R.string.cutterReset);
        }
        else if(stringState.indexOf("FE2611") != -1){ // 切刀未复位（FE 26 11）
            textView.setText(R.string.cutterNotReset);
        }
        else if (stringState.indexOf("FE2710") != -1) { //胶辊合上（FE 27 10）
            textView.setText(R.string.cotsClosed);
        }
        else if(stringState.indexOf("FE2711") != -1){ // 胶辊开着（FE 27 11）
            textView.setText(R.string.cotsOpened);
        }
        else if (stringState.indexOf("FE2810") != -1) { //纸张正常（FE 28 10）
            textView.setText(R.string.paperOK);
        }
        else if(stringState.indexOf("FE2811") != -1){ // 卡纸（FE 28 11）
            textView.setText(R.string.paperJam);
        }
        else if (stringState.indexOf("FE2B10") != -1) { //电压正常（FE 2B 10）
            textView.setText(R.string.normalVoltage);
        }
        else if(stringState.indexOf("FE2B11") != -1){ // 电压异常（FE 2B 11）
            textView.setText(R.string.abnormalVoltage);
        }
        else if (stringState.indexOf("FE2B10") != -1) { //蓝牙连接（FE 2C 10）
            textView.setText(R.string.bleConnected);
        }
        else if(stringState.indexOf("FE2B11") != -1){ // 蓝牙已断开（FE 2C 11）
            textView.setText(R.string.bleDisconnection);
        }
        else if (stringState.indexOf("FE29") != -1) { //温度值：FE 29 xx xx (xx xx 温度值放大 100 倍 如返回25.2度 FE 29 D8 09  xx xx 低位在前高位在后)

            if (byteResult.length >= 4){
                int low = byteResult[2] & 0xFF;
                int high = byteResult[3] & 0xFF;
                int result = (high << 8) | low;//先将高位左移，在与低位相与
                double value = result/100.00;
                DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                textView.setText(R.string.temperature+ decimalFormat.format(value) +"°C");
            }
            else {
                textView.setText(R.string.temperatureUnknown);
            }

        }
        else if (stringState.indexOf("FE2A") != -1) { //电压值：FE 2A xx xx (xx xx 电压值放大 100 倍 如返回24.5V  FE 2A 92 09  xx xx 低位在前高位在后)

            if (byteResult.length >= 4){
                int low = byteResult[2] & 0xFF;
                int high = byteResult[3] & 0xFF;
                int result = (high << 8) | low;//先将高位左移，在与低位相与
                double value = result/100.00;
                DecimalFormat decimalFormat = new DecimalFormat(".00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                textView.setText(R.string.voltage+ decimalFormat.format(value) +"v");
            }
            else {
                textView.setText(R.string.voltageUnknown);
            }
        }
        else if (stringState.indexOf("FEAA") != -1) { //纸张数：FE AA 01 00 (十六进制，低位在前，高位在后)
            //            标签模式下连续打印，自动返回打印纸张的第几张数值
            if (byteResult.length >= 4){
                int low = byteResult[2] & 0xFF;
                int high = byteResult[3] & 0xFF;
                int result = (high << 8) | low;//先将高位左移，在与低位相与

                Toast toast = Toast.makeText(context, R.string.theNumberOfCopies+ result, Toast.LENGTH_SHORT);
                toast.show();

                //返回了，打印完成
                if (byteResult.length == 7) {
                    Intent intentResult = new Intent();
                    byte[] bytes = new byte[3];
                    System.arraycopy(byteResult,4,bytes,0,bytes.length);
                    intentResult.setAction(PrinterState);
                    intentResult.putExtra(PrinterState,bytes);
                    this.onReceive(context,intentResult);
                }
            }
            else {
                Toast toast = Toast.makeText(context, R.string.theNumberOfCopies, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
            // 有设备拔出了
            Log.i("PrinterStateReceiver检测到", "usb拔出");
            String string = activity.getLocalClassName();
            if (string.startsWith("com.print.usbprintdemo.USBESCPrintActivity")) {
                USBUtil.getInstance().closeport(0);
                Intent intent1 = new Intent(activity, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                Bundle bundle = new Bundle();
                bundle.putString("activity", "ESC");
                intent1.putExtras(bundle);
                activity.startActivity(intent1);
                activity.finish();
            }
            else if (string.startsWith("com.print.usbprintdemo.USBLabelActivity")){
                USBUtil.getInstance().closeport(0);
                Intent intent1 = new Intent(activity, MainActivity.class);
                intent1.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                Bundle bundle = new Bundle();
                bundle.putString("activity", "Label");
                intent1.putExtras(bundle);
                activity.startActivity(intent1);
                activity.finish();
            }
        }

        //更新固件判断
        if (byteResult == null){
            byteResult = new byte[0];
        }
        String updateResult = new String(byteResult);
        Log.e("updateResult ->", updateResult);

        if (updateResult.indexOf("$AgreeUpdate$") != -1){

            String str = new String("$ErasureFlash$");
            Esc ee = new Esc();
            ee.addArrayToCommand(str.getBytes());
            try{
                Toast toast = Toast.makeText(context, R.string.updating, Toast.LENGTH_LONG);
                toast.show();
                Thread.sleep(500);
                USBUtil.getInstance().CommandEsc(ee);
            }
            catch (Exception e){
                e.printStackTrace();
                Toast toast = Toast.makeText(context, R.string.updateFailed, Toast.LENGTH_SHORT);
                toast.show();
            }
        }
        else if (updateResult.indexOf("$ErasureSuccess$") != -1) {
            try{
                Toast toast = Toast.makeText(context, R.string.updating, Toast.LENGTH_SHORT);
                toast.show();

                Thread.sleep(1000);
                USBUtil.getInstance().CommandEsc(esc);
            }
            catch (Exception e){
                e.printStackTrace();
            }
        }
        else if (updateResult.indexOf("$WriteSuccess$") != -1) {
//            Toast toast = Toast.makeText(context, "更新成功，请重新启动打印机", Toast.LENGTH_LONG);
//            toast.show();

            AlertDialog.Builder dialog = new AlertDialog.Builder(context);
            dialog.setTitle(R.string.firmwareWrittenSuccessfully);
            dialog.setMessage(R.string.updateTips);
            dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                    Esc esc1 = new Esc();
                    byte[] bytes = new byte[]{0x1B, 0x40,
                            0x1F, 0x2F, (byte)0xFE, 0x00, 0x04, (byte)0xAB, (byte)0xAB, (byte)0xAB, (byte)0xAB, (byte)0xAC};
                    esc.addArrayToCommand(bytes);

                    try{
                        Thread.sleep(1000);
                        USBUtil.getInstance().CommandEsc(esc);
                    }
                    catch (Exception e){
                        e.printStackTrace();
                    }

                    Toast toast = Toast.makeText(context, R.string.wating, Toast.LENGTH_LONG);
                    toast.show();

                }
            });

            dialog.setNeutralButton(R.string.cancel, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.dismiss();
                }
            });

            dialog.show();

        }
        else if (updateResult.indexOf("$WriteFail$ ") != -1) {
            Toast toast = Toast.makeText(context, "写入数据失败", Toast.LENGTH_SHORT);
            toast.show();
        }
    }

    /**
     * Convert byte[] to hex string.将byte转换成int，然后利用Integer.toHexString(int)来转换成16进制字符串。
     * @param src byte[] data
     * @return hex string （大写）
     * */
    public static String bytesToHexString(byte[] src){
        StringBuilder stringBuilder = new StringBuilder("");
        if (src == null || src.length <= 0) {
            return null;
        }
        for (int i = 0; i < src.length; i++) {
            int v = src[i] & 0xFF;
            String hv = Integer.toHexString(v).toUpperCase();
            if (hv.length() < 2) {
                stringBuilder.append(0);
            }
            stringBuilder.append(hv);
        }
        return stringBuilder.toString();
    }
    /**
     * Convert char to byte
     * @param c char
     * @return byte
     */
    private static byte charToByte(char c) {
        return (byte) "0123456789ABCDEF".indexOf(c);
    }

    /**
     * Convert hex string to byte[]
     * @param hexString the hex string
     * @return byte[]
     */
    public static byte[] hexStringToBytes(String hexString) {
        if (hexString == null || hexString.equals("")) {
            return null;
        }
        hexString = hexString.toUpperCase();
        int length = hexString.length() / 2;
        char[] hexChars = hexString.toCharArray();
        byte[] d = new byte[length];
        for (int i = 0; i < length; i++) {
            int pos = i * 2;
            d[i] = (byte) (charToByte(hexChars[pos]) << 4 | charToByte(hexChars[pos + 1]));
        }
        return d;
    }
}
