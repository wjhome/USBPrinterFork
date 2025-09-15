package com.print.usbprintdemo;

import android.Manifest;
import android.annotation.TargetApi;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.print.usbprint.command.Esc;
import com.print.usbprint.command.Label;
import com.print.usbprint.util.USBUtil;
import com.print.usbprintdemo.PrinterStateReceiver;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Vector;

public class USBLabelActivity extends AppCompatActivity {

    public static final int CHOOSE_PHOTO = 1;

    LinearLayout root;
    Toolbar toolbar;
    EditText editText;

    Button checkpaper;
    TextView paper;
    TextView device;
    Button ESC;
    Button Label;
    Button USBLV;
    TextView type;

    Button PageStart;
    ImageView PageStartHelp;
    EditText PageX;
    EditText PageY;
    EditText PageWidth;
    EditText PageHeight;
    EditText PageRotate;

    Button PrintText;
    ImageView TextHelp;
    EditText TextX;
    EditText TextY;
    EditText TextHeight;
    EditText TextBold;
    EditText TextUnderLine;
    EditText TextInclude;
    EditText TextWidthType;
    EditText TextHeightType;
    EditText TextRotate;
    EditText TextDeleteLine;

    Button BarCode;
    ImageView BarCodeHelp;
    EditText BarCodeX;
    EditText BarCodeY;
    EditText BarCodeType;
    EditText BarCodeHight;
    EditText BarCodeWidth;
    EditText BarCodeRotate;

    Button Bitmap;
    ImageView BitmapHelp;
    EditText BitmapX;
    EditText BitmapY;
    EditText BitmapInclude;
    EditText BitmapWidth;
    EditText BitmapHeight;
    EditText BitmapRotate;
    EditText BitmapWidthType;
    EditText BitmapHeightType;

    Button QRCode;
    ImageView QRCodeHelp;
    EditText QRCodeX;
    EditText QRCodeY;
    EditText QRCodeWidth;
    EditText QRCodeVersion;
    EditText QRCodeECC;
    EditText QRCodeRotate;

    Button TextBitmap;
    ImageView TextBitmapHelp;
    EditText TextBitmapX;
    EditText TextBitmapY;
    EditText TextBitmapInclude;
    EditText TextBitmapWidth;
    EditText TextBitmapHeight;
    EditText TextBitmapRotate;
    EditText TextBitmapWidthType;
    EditText TextBitmapHeightType;

    Button Print;
    ImageView PrintHelp;
    EditText PrintNumber;

    Button cutall;
    Button cuthalf;
    Button checkPaper;
    Button Quit;

    Toast toast;
    Label label;

    private String VID = "";
    private String PID = "";

    private PrinterStateReceiver printerStateReceiver;

    private  int print = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.label_usb);
        root = (LinearLayout) findViewById(R.id.ULroot);
        root.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                hideSoftKeyboard.autoInputmethod(USBLabelActivity.this);//关闭输入法，防止输入法影响toast和跳转效果
            }
        });
        toolbar = (Toolbar) findViewById(R.id.USBLabelPrint);
        editText = (EditText) findViewById(R.id.ULet);


        checkpaper = (Button) findViewById(R.id.ULCheckPaper);
        paper = (TextView) findViewById(R.id.ULPaper);
        paper.setText(getString(R.string.hasPaper));
        ESC = (Button) findViewById(R.id.ULESC);
        Label = (Button) findViewById(R.id.ULLabel);
        USBLV = (Button) findViewById(R.id.USBLV);
        label = new Label();

        Intent intent = getIntent();
        Bundle bundle = intent.getExtras();
        if (bundle != null) {
            device = (TextView) findViewById(R.id.ULdevice);
            PID = bundle.getString("PID");
            VID = bundle.getString("VID");
            if (PID != "" && VID != "") {
                device.setText(getString(R.string.connected) + "VID：" + VID + "     PID：" + PID);
            }
            bundle.clear();
        }

        ESC.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ULESC();
            }
        });

        Label.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                ULLabel();
            }
        });

        USBLV.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    USBLV();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        register();

        init();


        PageX = (EditText) findViewById(R.id.ULabelPageX);
        PageY = (EditText) findViewById(R.id.ULabelPageY);
        PageStart = (Button) findViewById(R.id.ULabelPageStart);
        PageWidth = (EditText) findViewById(R.id.ULabelPageWidth);
        PageHeight = (EditText) findViewById(R.id.ULabelPageHeight);
        PageRotate = (EditText) findViewById(R.id.ULabelPageRotate);
        PageStartHelp = (ImageView) findViewById(R.id.ULabelPageStarthelp);
        PageStartHelp.setImageDrawable(getResources().getDrawable(R.mipmap.help));
        pageStart();

        PrintText = (Button) findViewById(R.id.ULprinttext);
        TextHelp = (ImageView) findViewById(R.id.ULabeltexthelp);
        TextHelp.setImageDrawable(getResources().getDrawable(R.mipmap.help));
        TextX = (EditText) findViewById(R.id.ULabelTextX);
        TextY = (EditText) findViewById(R.id.ULabelTextY);
        TextBold = (EditText) findViewById(R.id.ULabelTextBold);
        TextDeleteLine = (EditText) findViewById(R.id.ULabelTextDeleteLine);
        TextHeight = (EditText) findViewById(R.id.ULabelTextHeight);
        TextInclude = (EditText) findViewById(R.id.ULabelTextInclude);
        TextUnderLine = (EditText) findViewById(R.id.ULabelTextUnderline);
        TextHeightType = (EditText) findViewById(R.id.ULabelTextHeightType);
        TextWidthType = (EditText) findViewById(R.id.ULabelTextWidthType);
        TextRotate = (EditText) findViewById(R.id.ULabelTextRotate);
        CreateText();

        BarCode = (Button) findViewById(R.id.ULprintBarcode);
        BarCodeHelp = (ImageView) findViewById(R.id.ULabelbarcodehelp);
        BarCodeHelp.setImageDrawable(getResources().getDrawable(R.mipmap.help));
        BarCodeX = (EditText) findViewById(R.id.ULabelBarcodeX);
        BarCodeY = (EditText) findViewById(R.id.ULabelBarcodeY);
        BarCodeHight = (EditText) findViewById(R.id.ULabelBarcodeHeight);
        BarCodeWidth = (EditText) findViewById(R.id.ULabelBarcodeWidth);
        BarCodeType = (EditText) findViewById(R.id.ULabelBarcodeType);
        BarCodeRotate = (EditText) findViewById(R.id.ULabelBarcodeRotate);
        CreateBarCode();

        Bitmap = (Button) findViewById(R.id.ULabelprintbitmap);
        BitmapHelp = (ImageView) findViewById(R.id.ULabelbitmaphelp);
        BitmapHelp.setImageDrawable(getResources().getDrawable(R.mipmap.help));
        BitmapX = (EditText) findViewById(R.id.ULabelBitmapX);
        BitmapY = (EditText) findViewById(R.id.ULabelBitmapY);
        BitmapHeight = (EditText) findViewById(R.id.ULabelBitmapHeight);
        BitmapInclude = (EditText) findViewById(R.id.ULabelBitmapInclude);
        BitmapWidth = (EditText) findViewById(R.id.ULabelBitmapWidth);
        BitmapHeightType = (EditText) findViewById(R.id.ULabelBitmapHeightType);
        BitmapWidthType = (EditText) findViewById(R.id.ULabelBitmapWidthType);
        BitmapRotate = (EditText) findViewById(R.id.ULabelBitmapRotate);
        CreateBitmap();

        QRCode = (Button) findViewById(R.id.ULabelprintQRCode);
        QRCodeHelp = (ImageView) findViewById(R.id.ULabelQRCodehelp);
        QRCodeHelp.setImageDrawable(getResources().getDrawable(R.mipmap.help));
        QRCodeECC = (EditText) findViewById(R.id.ULabelQRCodECC);
        QRCodeVersion = (EditText) findViewById(R.id.ULabelQRCodVersion);
        QRCodeX = (EditText) findViewById(R.id.ULabelQRCodX);
        QRCodeY = (EditText) findViewById(R.id.ULabelQRCodY);
        QRCodeWidth = (EditText) findViewById(R.id.ULabelQRCodWidth);
        QRCodeRotate = (EditText) findViewById(R.id.ULabelQRCodRotate);
        CreateQRCode();

        TextBitmap = (Button) findViewById(R.id.ULtextBitmap);
        TextBitmapHelp = (ImageView) findViewById(R.id.ULtextBitmapHelp);
        TextBitmapHelp.setImageDrawable(getResources().getDrawable(R.mipmap.help));
        TextBitmapX = (EditText) findViewById(R.id.ULtextBitmapX);
        TextBitmapY = (EditText) findViewById(R.id.ULtextBitmapY);
        TextBitmapHeight = (EditText) findViewById(R.id.ULtextBitmapHeight);
        TextBitmapInclude = (EditText) findViewById(R.id.ULtextBitmapInclude);
        TextBitmapWidth = (EditText) findViewById(R.id.ULtextBitmapWidth);
        TextBitmapHeightType = (EditText) findViewById(R.id.ULTextBitmapHeightType);
        TextBitmapWidthType = (EditText) findViewById(R.id.ULTextBitmapWidthType);
        TextBitmapRotate = (EditText) findViewById(R.id.ULtextBitmapRotate);
        CreateTextBitmap();

        Print = (Button) findViewById(R.id.ULabelPrintPage);
        PrintHelp = (ImageView) findViewById(R.id.ULabelPrinthelp);
        PrintHelp.setImageDrawable(getResources().getDrawable(R.mipmap.help));
        PrintNumber = (EditText) findViewById(R.id.ULPrintNumber);
        GoPrint();

        cutall = (Button) findViewById(R.id.ULcutAll);
        cutall.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Label label = new Label();
                label.reset();
                label.cutAll();
                label.reset();
                try {
                    USBUtil.getInstance().CommandLabel(label);;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        cuthalf = (Button) findViewById(R.id.ULcutHalf);
        cuthalf.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Label label = new Label();
                label.reset();
                label.cutHalf();
                label.reset();
                try {
                    USBUtil.getInstance().CommandLabel(label);;
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        checkPaper = (Button) findViewById(R.id.ULpaperWillRunOut);
        checkPaper.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Label label = new Label();
                label.reset();
                label.paperWillRunOut();
                label.reset();
                try {
                    USBUtil.getInstance().CommandLabel(label);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        });

        Quit = (Button) findViewById(R.id.ULabelQuit);
        QuitOut();
    }

    private void showToast(String string) {
        if (toast != null) {
            toast.cancel();
        }
        toast = Toast.makeText(USBLabelActivity.this, string, Toast.LENGTH_SHORT);
        toast.show();
    }

    private int StrToInt(String s) {
        return Integer.valueOf(s);
    }

    public void ULESC() {
        showToast(getString(R.string.ESCModel));
        Intent intent = new Intent(USBLabelActivity.this, USBESCPrintActivity.class);
        Bundle bundle = new Bundle();
        bundle.putString("mode",getString(R.string.multimode));
        if (VID != null && PID!= null && !VID.equals("") && !PID.equals("")){
            bundle.putString("VID",VID);
            bundle.putString("PID",PID);
        }
        intent.putExtras(bundle);
        intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
        startActivity(intent);
        finish();
    }

    public void ULLabel() {
        showToast(getString(R.string.LabelModel));
    }

    private void init() {
        toolbar.setTitle(R.string.labelModel);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);//显示toolbar返回按钮
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(USBLabelActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });
    }

    private void pageStart() {
        PageStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                print = 1;
                String x = PageX.getText().toString();
                if (x.equals("")) {
                    x = "0";
                }
                String y = PageY.getText().toString();
                if (y.equals("")) {
                    y = "0";
                }
                String width = PageWidth.getText().toString();
                if (width.equals("")) {
                    width = "384";
                }
                String height = PageHeight.getText().toString();
                if (height.equals("")) {
                    height = "320";
                }
                String Rotate = PageRotate.getText().toString();
                if (Rotate.equals("")) {
                    Rotate = "0";
                }
                label.reset();
//                label.customPageStart(Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(Rotate));
                label.customPageStartWithXY( Integer.valueOf(x), Integer.valueOf(y), Integer.valueOf(width), Integer.valueOf(height), Integer.valueOf(Rotate));

            }
        });
        PageStartHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PageStartHelp();
            }
        });
    }
    private void PageStartHelp() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(USBLabelActivity.this);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage(R.string.pageStartParm);
        dialog.show();
    }

    private void CreateText() {
        PrintText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if (text.equals("")) {
                    showToast(getString(R.string.pleaseInput));
                    return;
                }
                String Textx = TextX.getText().toString();
                if (Textx.equals("")) {
                    Textx = "0";
                }
                String Texty = TextY.getText().toString();
                if (Texty.equals("")) {
                    Texty = "0";
                }
                String Textheight = TextHeight.getText().toString();
                if (Textheight.equals("")) {
                    Textheight = "16";
                }
                String Textbold = TextBold.getText().toString();
                if (Textbold.equals("")) {
                    Textbold = "0";
                }
                String Textunderline = TextUnderLine.getText().toString();
                if (Textunderline.equals("")) {
                    Textunderline = "0";
                }
                String Textinclude = TextInclude.getText().toString();
                if (Textinclude.equals("")) {
                    Textinclude = "0";
                }
                String Textwidthtype = TextWidthType.getText().toString();
                if (Textwidthtype.equals("")) {
                    Textwidthtype = "0";
                }
                String Textheighttype = TextHeightType.getText().toString();
                if (Textheighttype.equals("")) {
                    Textheighttype = "0";
                }
                String Textrotate = TextRotate.getText().toString();
                if (Textrotate.equals("")) {
                    Textrotate = "0";
                }
                String Textdeleteline = TextDeleteLine.getText().toString();
                if (Textdeleteline.equals("")) {
                    Textdeleteline = "0";
                }
                int x = StrToInt(Textx);
                int y = StrToInt(Texty);
                int height = StrToInt(Textheight);
                int bold = StrToInt(Textbold);
                int underline = StrToInt(Textunderline);
                int include = StrToInt(Textinclude);
                int deleteline = StrToInt(Textdeleteline);
                int rotate = StrToInt(Textrotate);
                int widthtype = StrToInt(Textwidthtype);
                int heighttype = StrToInt(Textheighttype);
                label.customPrintText(text, x, y, height, bold, underline, include, deleteline, rotate, widthtype, heighttype);

            }
        });
        TextHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintTextHelp();
            }
        });
    }

    private void PrintTextHelp() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(USBLabelActivity.this);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage(R.string.labelTextParm);
        dialog.show();
    }

    private void CreateBarCode() {
        BarCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if (text.equals("")) {
                    showToast(getString(R.string.pleaseInput));
                }
                String barcodex = BarCodeX.getText().toString();
                if (barcodex.equals("")) {
                    barcodex = "0";
                }
                String barcodey = BarCodeY.getText().toString();
                if (barcodey.equals("")) {
                    barcodey = "0";
                }
                String barcodetype = BarCodeType.getText().toString();
                if (barcodetype.equals("")) {
                    barcodetype = "0";
                }
                String barcodewidth = BarCodeWidth.getText().toString();
                if (barcodewidth.equals("")) {
                    barcodewidth = "2";
                }
                String barcodeheight = BarCodeHight.getText().toString();
                if (barcodeheight.equals("")) {
                    barcodeheight = "85";
                }
                String barcoderotate = BarCodeRotate.getText().toString();
                if (barcoderotate.equals("")) {
                    barcoderotate = "0";
                }
                int x = StrToInt(barcodex);
                int y = StrToInt(barcodey);
                int type = StrToInt(barcodetype);
                int height = StrToInt(barcodeheight);
                int width = StrToInt(barcodewidth);
                int rotate = StrToInt(barcoderotate);
                label.printBarcode(text, x, y, type, height, width, rotate);
            }
        });
        BarCodeHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BarCodeHelp();
            }
        });
    }

    private void BarCodeHelp() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(USBLabelActivity.this);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage(R.string.labelBarcodeParm);
        dialog.show();
    }

    private void CreateBitmap() {
        Bitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CHOOSE_PHOTO();
            }
        });
        BitmapHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                BitmapHelp();
            }
        });
    }

    private void BitmapHelp() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(USBLabelActivity.this);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage(R.string.labelBitmapParm);
        dialog.show();
    }

    private void CreateQRCode() {
        QRCode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if (text.equals("")) {
                    showToast(getString(R.string.pleaseInput));
                    return;
                }
                String QRx = QRCodeX.getText().toString();
                if (QRx.equals("")) {
                    QRx = "0";
                }
                String QRy = QRCodeY.getText().toString();
                if (QRy.equals("")) {
                    QRy = "0";
                }
                String QRwidth = QRCodeWidth.getText().toString();
                if (QRwidth.equals("")) {
                    QRwidth = "4";
                }
                String QRversion = QRCodeVersion.getText().toString();
                if (QRversion.equals("")) {
                    QRversion = "3";
                }
                String QRECC = QRCodeECC.getText().toString();
                if (QRECC.equals("")) {
                    QRECC = "3";
                }
                String QRrotate = QRCodeRotate.getText().toString();
                if (QRrotate.equals("")) {
                    QRrotate = "0";
                }
                int version = StrToInt(QRversion);
                int ECC = StrToInt(QRECC);
                int x = StrToInt(QRx);
                int y = StrToInt(QRy);
                int width = StrToInt(QRwidth);
                int Rotate = StrToInt(QRrotate);
                label.printQRcode(text, version, ECC, x, y, width, Rotate);
            }
        });
        QRCodeHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                QRHelp();
            }
        });
    }

    private void CreateTextBitmap() {
        TextBitmap.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String text = editText.getText().toString();
                if (text.equals("")) {
                    showToast(getString(R.string.pleaseInput));
                    return;
                }
                String textbitmapwidth = TextBitmapWidth.getText().toString();
                if (textbitmapwidth.equals("")) {
                    textbitmapwidth = "24";
                }
                String textbitmapheight = TextBitmapHeight.getText().toString();
                if (textbitmapheight.equals("")) {
                    textbitmapheight = "24";
                }
                String textbitmapx = TextBitmapX.getText().toString();
                if (textbitmapx.equals("")) {
                    textbitmapx = "0";
                }
                String textbitmapy = TextBitmapY.getText().toString();
                if (textbitmapy.equals("")) {
                    textbitmapy = "0";
                }
                String textbitmapinclude = TextBitmapInclude.getText().toString();
                if (textbitmapinclude.equals("")) {
                    textbitmapinclude = "0";
                }
                String Rotate = TextRotate.getText().toString();
                if (Rotate.equals("")) {
                    Rotate = "0";
                }
                String textbitmapwidthtype = TextBitmapWidthType.getText().toString();
                if (textbitmapwidthtype.equals("")) {
                    textbitmapwidthtype = "0";
                }
                String textbitmapheighttype = TextBitmapHeightType.getText().toString();
                if (textbitmapheighttype.equals("")) {
                    textbitmapheighttype = "0";
                }
                int width = StrToInt(textbitmapwidth);
                int height = StrToInt(textbitmapheight);
                int x = StrToInt(textbitmapx);
                int y = StrToInt(textbitmapy);
                int include = StrToInt(textbitmapinclude);
                int rotate = StrToInt(Rotate);
                int widthtype = StrToInt(textbitmapwidthtype);
                int heighttype = StrToInt(textbitmapheighttype);
                label.customPrintTextBitmap(USBLabelActivity.this, width, height, text, x, y, include, rotate, widthtype, heighttype);
            }
        });
        TextBitmapHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextBitmapHelp();
            }
        });
    }

    private void TextBitmapHelp() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(USBLabelActivity.this);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage(R.string.labelTextBitmapParm);
        dialog.show();
    }

    private void GoPrint() {
        Print.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (print == 1) {
                    String string = PrintNumber.getText().toString();
                    if (string.equals("")) {
                        string = "1";
                    }
                    int number = StrToInt(string);
                    label.pageEnd();
                    label.customPrintPage(number);
                    try {
                        USBUtil.getInstance().CommandLabel(label);
                        label.clear();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    print = 0;
                }
                else {
                    showToast(getString(R.string.pleaseSetupPageStart));
                    label.clear();
                }
            }
        });
        PrintHelp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PrintHelp();
            }
        });
    }

    private void PrintHelp() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(USBLabelActivity.this);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage(R.string.printParm);
        dialog.show();
    }

    public void QRHelp() {
        AlertDialog.Builder dialog = new AlertDialog.Builder(USBLabelActivity.this);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.setMessage(R.string.labelQRParm);
        dialog.show();
    }

    private void QuitOut() {
        Quit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(USBLabelActivity.this, MainActivity.class);
                intent.addFlags(Intent.FLAG_ACTIVITY_REORDER_TO_FRONT);
                startActivity(intent);
                finish();
            }
        });
    }

    private void displayImage(String imagepath) throws IOException {
        if (imagepath == null) {
            showToast(getString(R.string.picNull));
            return;
        }
        android.graphics.Bitmap bitmap = BitmapFactory.decodeFile(imagepath);//根据图片路径获取图片
        String bitmapx = BitmapX.getText().toString();
        if (bitmapx.equals("")) {
            bitmapx = "0";
        }
        String bitmapy = BitmapY.getText().toString();
        if (bitmapy.equals("")) {
            bitmapy = "0";
        }
        String bitmapinclude = BitmapInclude.getText().toString();
        if (bitmapinclude.equals("")) {
            bitmapinclude = "0";
        }
        String bitmapwidth = BitmapWidth.getText().toString();
        if (bitmapwidth.equals("")) {
            bitmapwidth = "160";
        }
        String bitmapheight = BitmapHeight.getText().toString();
        if (bitmapheight.equals("")) {
            bitmapheight = "160";
        }
        String bitmapwidthtype = BitmapWidthType.getText().toString();
        if (bitmapwidthtype.equals("")) {
            bitmapwidthtype = "0";
        }
        String bitmapheighttype = BitmapHeightType.getText().toString();
        if (bitmapheighttype.equals("")) {
            bitmapheighttype = "0";
        }
        String bitmaprotate = BitmapRotate.getText().toString();
        if (bitmaprotate.equals("")) {
            bitmaprotate = "0";
        }
        int x = StrToInt(bitmapx);
        int y = StrToInt(bitmapy);
        int width = StrToInt(bitmapwidth);
        int height = StrToInt(bitmapheight);
        int include = StrToInt(bitmapinclude);
        int rotate = StrToInt(bitmaprotate);
        int widthtype = StrToInt(bitmapwidthtype);
        int heighttype = StrToInt(bitmapheighttype);
        label.customPrintBitmap(bitmap, x, y, width, height, include, rotate, widthtype, heighttype);
    }

    private void CHOOSE_PHOTO() {
        {
            //如果没有权限则申请权限
            if (ContextCompat.checkSelfPermission(USBLabelActivity.this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(USBLabelActivity.this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1);
            }
            //调用打开相册
            openAlbum();
        }
    }

    private void openAlbum() {
        Intent intent = new Intent("android.intent.action.GET_CONTENT");
        intent.setType("image/*");
        startActivityForResult(intent, CHOOSE_PHOTO); // 打开相册
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        switch (requestCode) {
            case 1:
                if (data != null) {
                    // 判断手机系统版本号
                    if (Build.VERSION.SDK_INT >= 19) {
                        // 4.4及以上系统使用这个方法处理图片
                        try {
                            handleImageOnKitKat(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } else {
                        // 4.4以下系统使用这个方法处理图片
                        try {
                            handleImageBeforeKitKat(data);
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    }
                } else {//防止用户未从媒体库选择图片返回
                    String textView = getString(R.string.picNull);
                    showToast(textView);
                }
                break;
        }
    }

    @TargetApi(19)
    private void handleImageOnKitKat(Intent data) throws IOException {
        String imagePath = null;
        Uri uri = data.getData();
        uri = getUri(uri);//区分uri
//        Uri uri = Uri.parse(data.toString());
        Log.d("TAG", "handleImageOnKitKat: uri is " + uri);
        if (DocumentsContract.isDocumentUri(this, uri)) {
            // 如果是document类型的Uri，则通过document id处理
            String docId = DocumentsContract.getDocumentId(uri);
            if ("com.android.providers.media.documents".equals(uri.getAuthority())) {
                String id = docId.split(":")[1]; // 解析出数字格式的id
                String selection = MediaStore.Images.Media._ID + "=" + id;
                imagePath = getImagePath(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, selection);
//                imagePath = getImagePath2(this,MediaStore.Images.Media.EXTERNAL_CONTENT_URI);

            } else if ("com.android.providers.downloads.documents".equals(uri.getAuthority())) {
                Uri contentUri = ContentUris.withAppendedId(Uri.parse("content://downloads/public_downloads"), Long.valueOf(docId));
                imagePath = getImagePath(contentUri, null);
//                imagePath = getImagePath2(this,contentUri);
            }
        } else if ("content".equalsIgnoreCase(uri.getScheme())) {
            // 如果是content类型的Uri，则使用普通方式处理
//            imagePath = getImagePath(uri, null);
            imagePath = getFilePathForN(this, uri);
        } else if ("file".equalsIgnoreCase(uri.getScheme())) {
            // 如果是file类型的Uri，直接获取图片路径即可
            imagePath = uri.getPath();
        }
        if (imagePath != null) {
            displayImage(imagePath); // 根据图片路径打印图片
        } else {
            String textView = "无法获取所选文件path，请确认该文件为图片类型文件";
            showToast(textView);
        }
    }

    private void handleImageBeforeKitKat(Intent data) throws IOException {
        Uri uri = data.getData();
        String imagePath = getImagePath(uri, null);
        displayImage(imagePath);// 根据图片路径打印图片
    }

    private Uri getUri(Uri uri) {
        String path = uri.getEncodedPath();
        if (path != null) {
            path = Uri.decode(path);
            ContentResolver cr = this.getContentResolver();
            StringBuffer buff = new StringBuffer();
            buff.append("(")
                    .append(MediaStore.Images.ImageColumns.DATA)
                    .append("=")
                    .append("'" + path + "'")
                    .append(")");
            Cursor cur = cr.query(
                    MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
                    new String[]{MediaStore.Images.ImageColumns._ID},
                    buff.toString(), null, null);
            int index = 0;
            for (cur.moveToFirst(); !cur.isAfterLast(); cur
                    .moveToNext()) {
                index = cur.getColumnIndex(MediaStore.Images.ImageColumns._ID);
                index = cur.getInt(index);
            }
            if (index == 0) {
            } else {
                Uri uri_temp = Uri
                        .parse("content://media/external/images/media/"
                                + index);
                if (uri_temp != null) {
                    uri = uri_temp;
                }
            }
        }
        return uri;
    }


    private String getImagePath(Uri uri, String selection) {
        String path = null;
        // 通过Uri和selection来获取真实的图片路径
        Cursor cursor = getContentResolver().query(uri, null, selection, null, null);
        if (cursor != null) {
            if (cursor.moveToFirst()) {
                Log.i("media", MediaStore.Images.Media.DATA);
                path = cursor.getString(cursor.getColumnIndex(MediaStore.Images.Media.DATA));
            }
            cursor.close();
        }
        return path;
    }

    private static String getFilePathForN(Context context, Uri uri) {
        try {
            Cursor returnCursor = context.getContentResolver().query(uri, null, null, null, null);
            int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
            returnCursor.moveToFirst();
            String name = (returnCursor.getString(nameIndex));
            File file = new File(context.getFilesDir(), name);
            InputStream inputStream = context.getContentResolver().openInputStream(uri);
            FileOutputStream outputStream = new FileOutputStream(file);
            int read = 0;
            int maxBufferSize = 1 * 1024 * 1024;
            int bytesAvailable = inputStream.available();
            int bufferSize = Math.min(bytesAvailable, maxBufferSize);
            final byte[] buffers = new byte[bufferSize];
            while ((read = inputStream.read(buffers)) != -1) {
                outputStream.write(buffers, 0, read);
            }
            returnCursor.close();
            inputStream.close();
            outputStream.close();
            return file.getPath();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public void ULone(View view) throws IOException {
//        Esc esc = new Esc();
        Label label = new Label();
        byte[] bytes = new byte[]{0x1B, 0x40,
                0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x01, (byte) 0xF0, 0x00, 0x00, 0x1A, 0x54, 0x01, 0x05, 0x00, 0x05, 0x00, 0x18, 0x00, 0x00, 0x22, (byte) 0xD5, (byte) 0xC5, (byte) 0xB6, (byte) 0xAB, (byte) 0xB7, (byte) 0xBD, 0x00,
                0x1A, 0x54, 0x01, (byte) 0xA0, 0x00, 0x05, 0x00, 0x18, 0x00, 0x00, 0x00, (byte) 0xC4, (byte) 0xD0, 0x20, 0x32, 0x30, (byte) 0xCB, (byte) 0xEA, 0x20, (byte) 0xC6, (byte) 0xD5, (byte) 0xC4, (byte) 0xDA, (byte) 0xBF, (byte) 0xC6, (byte) 0xC3, (byte) 0xC5, (byte) 0xD5, (byte) 0xEF, 0x00,
                0x1A, 0x54, 0x01, (byte) 0xA0, 0x00, 0x1E, 0x00, 0x18, 0x00, 0x00, 0x00, 0x31, 0x30, 0x31, 0x35, 0x31, 0x31, 0x34, 0x30, 0x31, 0x00,
                0x1A, 0x30, 0x00, 0x46, 0x00, 0x37, 0x00, 0x08, 0x4B, 0x02, 0x00, 0x31, 0x32, 0x33, 0x34, 0x35, 0x37, 0x31, 0x31, 0x00,
                0x1A, 0x54, 0x00, 0x40, 0x01, 0x41, 0x00, (byte) 0xC7, (byte) 0xE0, (byte) 0xB9, (byte) 0xDC, 0x00, 0x1A, 0x54, 0x00, 0x18, 0x01, 0x23, 0x00, 0x31, 0x30, 0x30, 0x32, 0x31, 0x00,
                0x1A, 0x54, 0x00, 0x40, 0x01, 0x64, 0x00, (byte) 0xD1, (byte) 0xAA, (byte) 0xC7, (byte) 0xE5, 0x00, 0x1A, 0x54, 0x01, 0x0A, 0x00, (byte) 0xA1, 0x00, 0x18, 0x00, 0x00, 0x00, 0x46, 0x50, 0x47, 0x2B, (byte) 0xB5, (byte) 0xE7, 0x2B, (byte) 0xB8, (byte) 0xCE, 0x2B, (byte) 0xC9, (byte) 0xF6, 0x2B, (byte) 0xD6, (byte) 0xAC, 0x00,
                0x1A, 0x54, 0x01, 0x05, 0x00, (byte) 0xD2, 0x00, 0x18, 0x00, 0x00, 0x00, 0x32, 0x30, 0x31, 0x39, 0x2D, 0x30, 0x37, 0x2D, 0x32, 0x37, 0x20, 0x31, 0x36, 0x3A, 0x33, 0x34, 0x3A, 0x34, 0x35, 0x00,
                0x1A, 0x5D, 0x00, 0x1A, 0x4F, 0x00};
//        esc.addArrayToCommand(bytes);
//        esc.cutHalf();
//        USBUtil.getInstance().CommandEsc(esc);

        label.addArrayToCommand(bytes);
        label.cutHalf();
        USBUtil.getInstance().CommandLabel(label);

        //测试打印线条
//        byte[] bytes1 = new byte[]{0x1B, 0x40,
//                0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, (byte)0x80,  0x01, 0x40, 0x01, 0x00,
//                0x1A, 0x5C, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x01, 0x00, 0x00, 0x30, 0x00, 0x01,
//                0x1A, 0x4F, 0x00};
//        label.reset();
//        label.customPageStart(384,320,0);
//        label.customPrintLine(56,50,500,50,1,1);
//        label.customPrintLine(16,80,300,80,2,1);
//        label.customPrintLine(20,96,240,96,5,1);
//        label.customPrintLine(40,124,384,124,7,1);
////        label.defaultPrintLine(80,80,240,80);
//        label.pageEnd();
//        label.defaultPrintPage();
////        label.addArrayToCommand(bytes1);
//        label.cutHalf();
//        USBUtil.getInstance().CommandLabel(label);
//
//        /* TODO: test */
//        byte[] bytesTemp = new byte[(label.getCommand().size())];
//        int i=0;
//        for(Byte e:label.getCommand())
//        {
//            bytesTemp[i++] = e;
//        }
//        String string = printerStateReceiver.bytesToHexString(bytesTemp);
//
//        byte[] byteL = new byte[]{0x00};
//        byte[] byteH = new byte[]{0x30};
//        int low = byteL[0] & 0xFF;
//        int high = byteH[0] & 0xFF;
//        int result = (high << 8) | low;

    }

    public void ULtwo(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40, 0x1a, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x01, (byte) 0xF0, 0x00, 0x00,
                0x1A, 0x31, 0x00, 0x03, 0x03, 0x00, 0x00, 0x20, 0x00, 0x04, 0x00, 0x68, 0x74, 0x74, 0x70, 0x73, 0x3A, 0x2F, 0x2F, 0x75, 0x2E, 0x77, 0x65, 0x63, 0x68, 0x61, 0x74, 0x2E, 0x63, 0x6F, 0x6D, 0x2F, 0x4D, 0x46, 0x77, 0x41, 0x75, 0x6D, 0x75, 0x63, 0x2D, 0x41, 0x6F, 0x52, 0x5F, 0x4C, 0x64, 0x59, 0x49, 0x48, 0x64, 0x38, 0x43, 0x5F, 0x45, 0x00,
                0x1A, 0x54, 0x00, (byte) 0xB0, 0x00, 0x20, 0x00, (byte) 0xD0, (byte) 0xA3, (byte) 0xD4, (byte) 0xB0, (byte) 0xB9, (byte) 0xB2, (byte) 0xCF, (byte) 0xED, 0x00,
                0x1A, 0x54, 0x00, (byte) 0xB0, 0x00, 0x50, 0x00, (byte) 0xCA, (byte) 0xA6, (byte) 0xD0, (byte) 0xD6, (byte) 0xB4, (byte) 0xAB, (byte) 0xCA, (byte) 0xE9, 0x00,
                0x1A, 0x54, 0x00, (byte) 0x90, 0x00, (byte) 0x80, 0x00, (byte) 0xBB, (byte) 0xB4, (byte) 0xD2, (byte) 0xF5, (byte) 0xB9, (byte) 0xA4, (byte) 0xD1, (byte) 0xA7, (byte) 0xD4, (byte) 0xBA, (byte) 0xB4, (byte) 0xB4, (byte) 0xBF, (byte) 0xCD, (byte) 0xCA, (byte) 0xB5, (byte) 0xD1, (byte) 0xE9, (byte) 0xCA, (byte) 0xD2, 0x00,
                0x1A, 0x54, 0x00, 0x10, 0x00, (byte) 0xB0, 0x00, (byte) 0xC1, (byte) 0xF4, (byte) 0xD1, (byte) 0xD4, 0x3A, (byte) 0xD5, (byte) 0xE2, (byte) 0xCA, (byte) 0xC7, (byte) 0xD2, (byte) 0xBB, (byte) 0xB1, (byte) 0xBE, (byte) 0xBA, (byte) 0xC3, (byte) 0xCA, (byte) 0xE9, (byte) 0xA3, (byte) 0xAC, (byte) 0xD6, (byte) 0xB5, (byte) 0xB5, (byte) 0xC3, (byte) 0xD2, (byte) 0xBB, (byte) 0xB6, (byte) 0xC1, 0x00,
                0x1a, 0x5d, 0x00,
                0x1a, 0x4f, 0x00};
        esc.addArrayToCommand(bytes);
        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULthree(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x01, (byte) 0xFA, 0x00, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, 0x64, 0x00, 0x0A, 0x00, 0x04, 0x00, 0x68, 0x74, 0x74, 0x70, 0x73, 0x3A, 0x2F, 0x2F, 0x75, 0x2E, 0x77, 0x65, 0x63, 0x68, 0x61, 0x74, 0x2E, 0x63, 0x6F, 0x6D, 0x2F, 0x4D, 0x46, 0x77, 0x41, 0x75, 0x6D, 0x75, 0x63, 0x2D, 0x41, 0x6F, 0x52, 0x5F, 0x4C, 0x64, 0x59, 0x49, 0x48, 0x64, 0x38, 0x43, 0x5F, 0x45, 0x00,
                0x1A, 0x54, 0x01, 0x67, 0x00, (byte) 0xA5, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB1, (byte) 0xE0, (byte) 0xBA, (byte) 0xC5, (byte) 0xA3, (byte) 0xBA, 0x31, 0x30, 0x30, 0x35, 0x35, 0x36, 0x00,
                0x1A, 0x54, 0x01, 0x43, 0x00, (byte) 0xC3, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xC0, (byte) 0xAC, (byte) 0xBB, (byte) 0xF8, (byte) 0xB7, (byte) 0xD6, (byte) 0xC0, (byte) 0xE0, (byte) 0xA3, (byte) 0xBA, (byte) 0xC8, (byte) 0xCB, (byte) 0xC8, (byte) 0xCB, (byte) 0xD3, (byte) 0xD0, (byte) 0xD4, (byte) 0xF0, 0x00,
                0x1A, 0x5D, 0x00, 0x1A, 0x4F, 0x00};
        esc.addArrayToCommand(bytes);
        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULfour(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40,
                0x1a, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x01, (byte) 0xF0, 0x00, 0x00,
                0x1A, 0x54, 0x00, 0x10, 0x00, 0x10, 0x00, (byte) 0xC9, (byte) 0xEE, (byte) 0xDB, (byte) 0xDA, (byte) 0xB8, (byte) 0xE8, (byte) 0xD2, (byte) 0xED, (byte) 0xBF, (byte) 0xC6, (byte) 0xBC, (byte) 0xBC, (byte) 0xD3, (byte) 0xD0, (byte) 0xCF, (byte) 0xDE, (byte) 0xB9, (byte) 0xAB, (byte) 0xCB, (byte) 0xBE, 0x00,
                0x1A, 0x54, 0x00, 0x10, 0x00, 0x30, 0x00, (byte) 0xB2, (byte) 0xFA, (byte) 0xC6, (byte) 0xB7, (byte) 0xC3, (byte) 0xFB, (byte) 0xB3, (byte) 0xC6, (byte) 0xA3, (byte) 0xBA, (byte) 0xC7, (byte) 0xB6, (byte) 0xC8, (byte) 0xEB, (byte) 0xCA, (byte) 0xBD, (byte) 0xB1, (byte) 0xEA, (byte) 0xC7, (byte) 0xA9, (byte) 0xB4, (byte) 0xF2, (byte) 0xD3, (byte) 0xA1, (byte) 0xBB, (byte) 0xFA, 0x00,
                0x1A, 0x54, 0x00, 0x10, 0x00, 0x50, 0x00, (byte) 0xB2, (byte) 0xFA, (byte) 0xC6, (byte) 0xB7, (byte) 0xD0, (byte) 0xCD, (byte) 0xBA, (byte) 0xC5, (byte) 0xA3, (byte) 0xBA, 0x47, 0x59, 0x2D, 0x42, 0x51, 0x30, 0x32, 0x30, 0x31, 0x00,
                0x1A, 0x54, 0x00, 0x10, 0x00, 0x70, 0x00, (byte) 0xCD, (byte) 0xA8, (byte) 0xD1, (byte) 0xB6, (byte) 0xBD, (byte) 0xD3, (byte) 0xBF, (byte) 0xDA, (byte) 0xA3, (byte) 0xBA, 0x52, 0x53, 0x32, 0x33, 0x32, 0x2B, 0x55, 0x53, 0x42, (byte) 0xBD, (byte) 0xD3, (byte) 0xBF, (byte) 0xDA, 0x00,
                0x1A, 0x54, 0x00, 0x10, 0x00, (byte) 0x90, 0x00, (byte) 0xB5, (byte) 0xD8, (byte) 0xD6, (byte) 0xB7, (byte) 0xA3, (byte) 0xBA, (byte) 0xC9, (byte) 0xEE, (byte) 0xDB, (byte) 0xDA, (byte) 0xCA, (byte) 0xD0, (byte) 0xB1, (byte) 0xA6, (byte) 0xB0, (byte) 0xB2, (byte) 0xC7, (byte) 0xF8, (byte) 0xC9, (byte) 0xB3, (byte) 0xBE, (byte) 0xAE, (byte) 0xBD, (byte) 0xD6, (byte) 0xB5, (byte) 0xC0, (byte) 0xC2, (byte) 0xED, (byte) 0xB0, (byte) 0xB2, 0x00,
                0x1A, 0x54, 0x00, 0x10, 0x00, (byte) 0xB0, 0x00, (byte) 0xC9, (byte) 0xBD, (byte) 0xB0, (byte) 0xB0, (byte) 0xCA, (byte) 0xA4, (byte) 0xC2, (byte) 0xB7, 0x32, 0x35, (byte) 0xBA, (byte) 0xC5, 0x42, (byte) 0xB6, (byte) 0xB0, 0x34, (byte) 0xC2, (byte) 0xA5, 0x00,
                0x1A, 0x54, 0x00, 0x10, 0x00, (byte) 0xD0, 0x00, (byte) 0xB5, (byte) 0xE7, (byte) 0xBB, (byte) 0xB0, (byte) 0xA3, (byte) 0xBA, 0x34, 0x30, 0x30, 0x2D, 0x30, 0x30, 0x38, 0x2D, 0x38, 0x39, 0x33, 0x30, 0x00,
                0x1a, 0x5d, 0x00,
                0x1a, 0x4f, 0x00};
        esc.addArrayToCommand(bytes);
        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULfive(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x01, (byte) 0xFA, 0x00, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, 0x00, 0x00, 0x15, 0x00, 0x05, 0x00, 0x68, 0x74, 0x74, 0x70, 0x73, 0x3A, 0x2F, 0x2F, 0x75, 0x2E, 0x77, 0x65, 0x63, 0x68, 0x61, 0x74, 0x2E, 0x63, 0x6F, 0x6D, 0x2F, 0x4D, 0x46, 0x77, 0x41, 0x75, 0x6D, 0x75, 0x63, 0x2D, 0x41, 0x6F, 0x52, 0x5F, 0x4C, 0x64, 0x59, 0x49, 0x48, 0x64, 0x38, 0x43, 0x5F, 0x45, 0x00,
                0x1A, 0x54, 0x01, (byte) 0xc5, 0x00, 0x15, 0x00, 0x00, 0x60, 0x00, 0x11, 0x4D, 0x43, 0x20, 0x31, 0x30, 0x30, 0x31, 0x00,
                0x1A, 0x54, 0x01, (byte) 0xc5, 0x00, 0x2F, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB5, (byte) 0xE7, 0x20, (byte) 0xB3, (byte) 0xD8, 0x00,
                0x1A, 0x54, 0x01, (byte) 0xc5, 0x00, 0x4B, 0x00, 0x00, 0x60, 0x00, 0x11, 0x32, 0x30, 0x31, 0x38, 0x2D, 0x30, 0x37, 0x2D, 0x31, 0x30, 0x00,
                0x1A, 0x54, 0x01, (byte) 0xc5, 0x00, 0x68, 0x00, 0x00, 0x60, 0x00, 0x11, 0x31, 0x37, 0x3A, 0x33, 0x34, 0x3A, 0x33, 0x30, 0x00,
                0x1A, 0x54, 0x01, (byte) 0xc5, 0x00, (byte) 0x83, 0x00, 0x00, 0x60, 0x00, 0x11, 0x53, 0x54, 0x43, 0x50, 0x30, 0x30, 0x30, 0x30, 0x30, 0x31, 0x33, 0x36, 0x00,
                0x1A, 0x54, 0x01, (byte) 0xc3, 0x00, (byte) 0x9C, 0x00, 0x00, 0x60, 0x00, 0x11, 0x32, 0x30, 0x31, 0x38, 0x2D, 0x30, 0x37, 0x2D, 0x32, 0x30, (byte) 0xC7, (byte) 0xB0, (byte) 0xD3, (byte) 0xD0, (byte) 0xD0, (byte) 0xA7, 0x00,
                0x1A, 0x54, 0x01, (byte) 0xc5, 0x00, (byte) 0xB3, 0x00, 0x00, 0x60, 0x00, 0x11, 0x4C, 0x53, 0x20, 0x33, 0x32, 0x30, 0x36, 0x30, 0x00,
                0x1A, 0x5D, 0x00, 0x1A, 0x4F, 0x00};
        esc.addArrayToCommand(bytes);
        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULsix(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40,
                0x1a, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x01, (byte) 0xF0, 0x00, 0x00,
                0x1a, 0x26, 0x01, 0x00, 0x00, 0x05, 0x00, (byte) 0x80, 0x01, (byte) 0xe0, 0x00, 0x05, 0x00, 0x01,
                0x1A, 0x5C, 0x01, 0x00, 0x00, 0x50, 0x00, (byte) 0x80, 0x01, 0x50, 0x00, 0x05, 0x00, 0x01,
                0x1A, 0x5C, 0x01, 0x00, 0x00, (byte) 0x98, 0x00, (byte) 0x80, 0x01, (byte) 0x98, 0x00, 0x05, 0x00, 0x01,
                0x1A, 0x5C, 0x01, (byte) 0x80, 0x00, 0x00, 0x00, (byte) 0x80, 0x00, (byte) 0xe0, 0x00, 0x05, 0x00, 0x01,
                0x1A, 0x54, 0x00, 0x20, 0x00, 0x20, 0x00, (byte) 0xC6, (byte) 0xB7, 0x20, (byte) 0xC3, (byte) 0xFB, 0x00,
                0x1A, 0x54, 0x00, 0x20, 0x00, 0x68, 0x00, (byte) 0xD0, (byte) 0xCD, 0x20, (byte) 0xBA, (byte) 0xC5, 0x00,
                0x1A, 0x54, 0x00, 0x20, 0x00, (byte) 0xb0, 0x00, (byte) 0xB9, (byte) 0xE6, 0x20, (byte) 0xB8, (byte) 0xF1, 0x00,
                0x1a, 0x4f, 0x00};
        esc.addArrayToCommand(bytes);
        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULseven(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40, 0x1a, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x01, (byte) 0xF0, 0x00, 0x00,
                0x1a, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, (byte) 0x80, 0x01, 0x00, 0x01, 0x00,
                0x1a, 0x30, 0x00, 0x00, 0x00, 0x20, 0x00, 0x08, 0x59, 0x03, 0x00, 0x31, 0x30, 0x31, 0x30, 0x30, 0x00,
                0x1a, 0x5d, 0x00,
                0x1A, 0x54, 0x00, (byte) 0xf2, 0x00, 0x20, 0x00, (byte) 0xC7, (byte) 0xE0, (byte) 0xB5, (byte) 0xBA, (byte) 0xBB, (byte) 0xAA, (byte) 0xC5, (byte) 0xB5, (byte) 0xD2, (byte) 0xBD, (byte) 0xD1, (byte) 0xA7, 0x00,//青岛华诺医学
                0x1A, 0x54, 0x00, (byte) 0xf2, 0x00, 0x50, 0x00, (byte) 0xBF, (byte) 0xC6, (byte) 0xBC, (byte) 0xBC, (byte) 0xD3, (byte) 0xD0, (byte) 0xCF, (byte) 0xDE, (byte) 0xB9, (byte) 0xAB, (byte) 0xCB, (byte) 0xBE, 0x00,//科技有限公司
                0x1A, 0x54, 0x00, (byte) 0xa4, 0x00, (byte) 0xA0, 0x00, (byte) 0xD6, (byte) 0xC7, (byte) 0xC4, (byte) 0xDC, (byte) 0xCC, (byte) 0xF5, (byte) 0xC2, (byte) 0xEB, (byte) 0xD0, (byte) 0xC5, (byte) 0xCF, (byte) 0xA2, (byte) 0xB4, (byte) 0xF2, (byte) 0xD3, (byte) 0xA1, (byte) 0xBB, (byte) 0xFA, 0x00,//智能条码信息打印机
                0x1A, 0x54, 0x00, 0x04, 0x00, (byte) 0xc8, 0x00, (byte) 0xD4, (byte) 0xE6, (byte) 0xD7, (byte) 0xAF, (byte) 0xCA, (byte) 0xD0, (byte) 0xC1, (byte) 0xA2, (byte) 0xD2, (byte) 0xBD, (byte) 0xD4, (byte) 0xBA, (byte) 0xCD, (byte) 0xCB, 0x20, 0x20, (byte) 0xB2, (byte) 0xE2, (byte) 0xCA, (byte) 0xD4, (byte) 0xCE, (byte) 0xDE, (byte) 0xD2, (byte) 0xEC, (byte) 0xB3, (byte) 0xA3, 0x00,//枣庄市立医院退  测试无异常
                0x1a, 0x5d, 0x00,
                0x1a, 0x4f, 0x00};
        esc.addArrayToCommand(bytes);
        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULeight(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40, 0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, 0x40, 0x02, 0x68, 0x01, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, (byte) 0x96, 0x01, 0x78, 0x00, 0x03, 0x00, 0x31, 0x38, 0x36, 0x38, 0x37, 0x30, 0x34, 0x30, 0x34, 0x38, 0x30, 0x33, 0x33, 0x33, 0x33, 0x34, 0x31, 0x39, 0x30, 0x31, 0x32, 0x31, 0x31, 0x32, 0x33, 0x36, 0x31, 0x30, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, 0x20, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xD2, (byte) 0xBD, (byte) 0xD4, (byte) 0xBA, (byte) 0xA3, (byte) 0xBA, (byte) 0xD4, (byte) 0xBD, (byte) 0xB3, (byte) 0xC7, (byte) 0xC7, (byte) 0xF8, (byte) 0xD6, (byte) 0xD0, (byte) 0xD2, (byte) 0xBD, (byte) 0xD2, (byte) 0xBD, (byte) 0xD4, (byte) 0xBA, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, 0x48, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xBF, (byte) 0xC6, (byte) 0xCA, (byte) 0xD2, (byte) 0xA3, (byte) 0xBA, (byte) 0xD4, (byte) 0xBD, (byte) 0xB3, (byte) 0xC7, (byte) 0xC7, (byte) 0xF8, (byte) 0xBF, (byte) 0xC6, (byte) 0xCA, (byte) 0xD2, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, 0x70, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xD6, (byte) 0xD8, (byte) 0xC1, (byte) 0xBF, (byte) 0xA3, (byte) 0xBA, 0x30, 0x2E, 0x33, 0x35, 0x4B, 0x47, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, (byte) 0x98, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xD6, (byte) 0xD6, (byte) 0xC0, (byte) 0xE0, (byte) 0xA3, (byte) 0xBA, (byte) 0xB8, (byte) 0xD0, (byte) 0xC8, (byte) 0xBE, (byte) 0xD0, (byte) 0xD4, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, (byte) 0xC0, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xCA, (byte) 0xD5, (byte) 0xBC, (byte) 0xAF, (byte) 0xC8, (byte) 0xCB, (byte) 0xA3, (byte) 0xBA, (byte) 0xD4, (byte) 0xBD, (byte) 0xB3, (byte) 0xC7, (byte) 0xC7, (byte) 0xF8, (byte) 0xB2, (byte) 0xE2, (byte) 0xCA, (byte) 0xD4, (byte) 0xC8, (byte) 0xCB, (byte) 0xD4, (byte) 0xB1, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, (byte) 0xE8, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xC8, (byte) 0xB7, (byte) 0xC8, (byte) 0xCF, (byte) 0xC8, (byte) 0xCB, (byte) 0xA3, (byte) 0xBA, (byte) 0xD4, (byte) 0xBD, (byte) 0xB3, (byte) 0xC7, (byte) 0xC7, (byte) 0xF8, (byte) 0xD2, (byte) 0xBD, (byte) 0xBB, (byte) 0xA4, (byte) 0xC8, (byte) 0xCB, (byte) 0xD4, (byte) 0xB1, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, 0x10, 0x01, 0x00, 0x60, 0x00, 0x11, (byte) 0xC8, (byte) 0xD5, (byte) 0xC6, (byte) 0xDA, (byte) 0xA3, (byte) 0xBA, 0x32, 0x30, 0x31, 0x39, 0x2D, 0x30, 0x31, 0x2D, 0x32, 0x31, 0x20, 0x31, 0x32, 0x3A, 0x33, 0x36,
                0x1a, 0x5d, 0x00,
                0x1a, 0x4f, 0x00};
        esc.addArrayToCommand(bytes);
        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULnine(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40, 0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, 0x40, 0x02, 0x68, 0x01, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, 0x40, 0x00, 0x65, 0x00, 0x04, 0x00, 0x31, 0x39, 0x30, 0x37, 0x32, 0x36, 0x31, 0x32, 0x30, 0x38, 0x30, 0x39, 0x32, 0x36, 0x37, 0x30, 0x30, 0x31, 0x00,
                0x1A, 0x54, 0x01, 0x12, 0x01, 0x15, 0x00, 0x00, 0x60, 0x00, 0x20, (byte) 0xB0, (byte) 0xB2, (byte) 0xCC, (byte) 0xA4, (byte) 0xCF, (byte) 0xC3, (byte) 0xC3, (byte) 0xC5, (byte) 0xD3, (byte) 0xAA, (byte) 0xD4, (byte) 0xCB, (byte) 0xD6, (byte) 0xD0, (byte) 0xD0, (byte) 0xC4, 0x00,
                0x1A, 0x54, 0x01, 0x12, 0x01, 0x48, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xC0, (byte) 0xB4, (byte) 0xB7, (byte) 0xC3, (byte) 0xBF, (byte) 0xCD, (byte) 0xC8, (byte) 0xCB, (byte) 0xA3, (byte) 0xBA, (byte) 0xB2, (byte) 0xE2, (byte) 0xCA, (byte) 0xD4, 0x32, 0x36, 0x00,
                0x1A, 0x54, 0x01, 0x12, 0x01, 0x70, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB5, (byte) 0xC7, (byte) 0xBC, (byte) 0xC7, (byte) 0xCA, (byte) 0xB1, (byte) 0xBC, (byte) 0xE4, (byte) 0xA3, (byte) 0xBA, 0x37, (byte) 0xD4, (byte) 0xC2, 0x32, 0x36, (byte) 0xC8, (byte) 0xD5, 0x31, 0x32, 0x3A, 0x30, 0x38, 0x00,
                0x1A, 0x54, 0x01, 0x12, 0x01, (byte) 0x98, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xCA, (byte) 0xD6, (byte) 0xBB, (byte) 0xFA, (byte) 0xBA, (byte) 0xC5, (byte) 0xC2, (byte) 0xEB, (byte) 0xA3, (byte) 0xBA, 0x31, 0x38, 0x38, 0x32, 0x33, 0x33, 0x30, 0x31, 0x37, 0x35, 0x31, 0x00,
                0x1A, 0x54, 0x01, 0x12, 0x01, (byte) 0xC0, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB5, (byte) 0xA5, (byte) 0xCE, (byte) 0xBB, (byte) 0xC3, (byte) 0xFB, (byte) 0xB3, (byte) 0xC6, (byte) 0xA3, (byte) 0xBA, (byte) 0xB8, (byte) 0xE8, (byte) 0xD2, (byte) 0xED, (byte) 0xBF, (byte) 0xC6, (byte) 0xBC, (byte) 0xBC, 0x00,
                0x1A, 0x54, 0x01, 0x12, 0x01, (byte) 0xE8, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB3, (byte) 0xB5, (byte) 0xC5, (byte) 0xC6, (byte) 0xBA, (byte) 0xC5, (byte) 0xC2, (byte) 0xEB, (byte) 0xA3, (byte) 0xBA, (byte) 0xD4, (byte) 0xC1, 0x42, 0x20, 0x4B, 0x39, 0x35, 0x30, 0x45, 0x00,
                0x1A, 0x54, 0x01, 0x12, 0x01, 0x10, 0x01, 0x00, 0x60, 0x00, 0x11, (byte) 0xBD, (byte) 0xD3, (byte) 0xB4, (byte) 0xFD, (byte) 0xB2, (byte) 0xBF, (byte) 0xC3, (byte) 0xC5, (byte) 0xA3, (byte) 0xBA, (byte) 0xCF, (byte) 0xFA, (byte) 0xCA, (byte) 0xDB, (byte) 0xB2, (byte) 0xBF, (byte) 0xA3, (byte) 0xFC, (byte) 0xD3, (byte) 0xE0, (byte) 0xCF, (byte) 0xC8,
                0x1a, 0x5d, 0x00,
                0x1a, 0x4f, 0x00};
        esc.addArrayToCommand(bytes);
        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULten(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40, 0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, 0x40, 0x02, (byte) 0x80, 0x01, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, 0x29, 0x01, (byte) 0xe7, 0x00, 0x03, 0x00, (byte) 0xC9, (byte) 0xEE, (byte) 0xDB, (byte) 0xDA, (byte) 0xB8, (byte) 0xE8, (byte) 0xD2, (byte) 0xED, (byte) 0xBF, (byte) 0xC6, (byte) 0xBC, (byte) 0xBC, (byte) 0xD3, (byte) 0xD0, (byte) 0xCF, (byte) 0xDE, (byte) 0xB9, (byte) 0xAB, (byte) 0xCB, (byte) 0xBE, 0x77, 0x77, 0x77, 0x2E, 0x67, 0x79, 0x2D, 0x70, 0x72, 0x69, 0x6E, 0x74, 0x65, 0x72, 0x2E, 0x63, 0x6F, 0x6D, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, (byte) 0xc9, 0x01, (byte) 0xe7, 0x00, 0x03, 0x00, 0x68, 0x74, 0x74, 0x70, 0x73, 0x3A, 0x2F, 0x2F, 0x75, 0x2E, 0x77, 0x65, 0x63, 0x68, 0x61, 0x74, 0x2E, 0x63, 0x6F, 0x6D, 0x2F, 0x4D, 0x46, 0x77, 0x41, 0x75, 0x6D, 0x75, 0x63, 0x2D, 0x41, 0x6F, 0x52, 0x5F, 0x4C, 0x64, 0x59, 0x49, 0x48, 0x64, 0x38, 0x43, 0x5F, 0x45, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, 0x15, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB2, (byte) 0xFA, (byte) 0xC6, (byte) 0xB7, (byte) 0xC3, (byte) 0xFB, (byte) 0xB3, (byte) 0xC6, (byte) 0xA3, (byte) 0xBA, (byte) 0xC7, (byte) 0xB6, (byte) 0xC8, (byte) 0xEB, (byte) 0xCA, (byte) 0xBD, (byte) 0xB1, (byte) 0xEA, (byte) 0xC7, (byte) 0xA9, (byte) 0xB4, (byte) 0xF2, (byte) 0xD3, (byte) 0xA1, (byte) 0xBB, (byte) 0xFA, 0x47, 0x59, 0x2D, 0x42, 0x51, 0x30, 0x38, 0x30, 0x31, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, 0x3f, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xCD, (byte) 0xA8, (byte) 0xD1, (byte) 0xB6, (byte) 0xBD, (byte) 0xD3, (byte) 0xBF, (byte) 0xDA, (byte) 0xA3, (byte) 0xBA, 0x52, 0x53, 0x32, 0x33, 0x32, 0x2B, 0x54, 0x54, 0x4C, 0x2B, 0x55, 0x53, 0x42, (byte) 0xBD, (byte) 0xD3, (byte) 0xBF, (byte) 0xDA, 0x20, (byte) 0xB9, (byte) 0xA9, (byte) 0xB5, (byte) 0xE7, (byte) 0xB5, (byte) 0xE7, (byte) 0xD1, (byte) 0xB9, (byte) 0xA3, (byte) 0xBA, 0x31, 0x32, 0x56, 0x33, 0x41, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, 0x69, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xC4, (byte) 0xAC, (byte) 0xC8, (byte) 0xCF, (byte) 0xD3, (byte) 0xEF, (byte) 0xD1, (byte) 0xD4, (byte) 0xA3, (byte) 0xBA, (byte) 0xBC, (byte) 0xF2, (byte) 0xCC, (byte) 0xE5, (byte) 0xD6, (byte) 0xD0, (byte) 0xCE, (byte) 0xC4, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, (byte) 0xB2, (byte) 0xA8, 0x20, (byte) 0xCC, (byte) 0xD8, 0x20, (byte) 0xC2, (byte) 0xCA, (byte) 0xA3, (byte) 0xBA, 0x31, 0x31, 0x35, 0x32, 0x30, 0x30, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, (byte) 0x93, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xD3, (byte) 0xA6, (byte) 0xD3, (byte) 0xC3, (byte) 0xC1, (byte) 0xEC, (byte) 0xD3, (byte) 0xF2, (byte) 0xA3, (byte) 0xBA, (byte) 0xD2, (byte) 0xBD, (byte) 0xC1, (byte) 0xC6, (byte) 0xB7, (byte) 0xCF, (byte) 0xCE, (byte) 0xEF, (byte) 0xC0, (byte) 0xAC, (byte) 0xBB, (byte) 0xF8, (byte) 0xCD, (byte) 0xB0, (byte) 0xA1, (byte) 0xA2, (byte) 0xD2, (byte) 0xBD, (byte) 0xB7, (byte) 0xCF, (byte) 0xC0, (byte) 0xAC, (byte) 0xBB, (byte) 0xF8, (byte) 0xB3, (byte) 0xB5, (byte) 0xB5, (byte) 0xC8, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, (byte) 0xbd, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB5, (byte) 0xD8, (byte) 0xD6, (byte) 0xB7, (byte) 0xA3, (byte) 0xBA, (byte) 0xB9, (byte) 0xE3, (byte) 0xB6, (byte) 0xAB, (byte) 0xCA, (byte) 0xA1, (byte) 0xC9, (byte) 0xEE, (byte) 0xDB, (byte) 0xDA, (byte) 0xCA, (byte) 0xD0, (byte) 0xB1, (byte) 0xA6, (byte) 0xB0, (byte) 0xB2, (byte) 0xC7, (byte) 0xF8, (byte) 0xC9, (byte) 0xB3, (byte) 0xBE, (byte) 0xAE, (byte) 0xBD, (byte) 0xD6, (byte) 0xB5, (byte) 0xC0, (byte) 0xC2, (byte) 0xED, (byte) 0xB0, (byte) 0xB0, (byte) 0xC9, (byte) 0xBD, (byte) 0xB0, (byte) 0xB2, (byte) 0xCA, (byte) 0xA4, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, (byte) 0xe7, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xC2, (byte) 0xB7, 0x32, 0x35, (byte) 0xBA, (byte) 0xC5, 0x42, (byte) 0xB6, (byte) 0xB0, 0x34, (byte) 0xC2, (byte) 0xA5, 0x00,
                0x1A, 0x5D, 0x00,
                0x1A, 0x4F, 0x00,
                0x1b, 0x6d,};
        esc.addArrayToCommand(bytes);
//        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULeleven(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40, 0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, 0x40, 0x02, (byte) 0x80, 0x01, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, (byte) 0x99, 0x01, 0x15, 0x00, 0x04, 0x00, (byte) 0xC9, (byte) 0xEE, (byte) 0xDB, (byte) 0xDA, (byte) 0xB8, (byte) 0xE8, (byte) 0xD2, (byte) 0xED, (byte) 0xBF, (byte) 0xC6, (byte) 0xBC, (byte) 0xBC, (byte) 0xD3, (byte) 0xD0, (byte) 0xCF, (byte) 0xDE, (byte) 0xB9, (byte) 0xAB, (byte) 0xCB, (byte) 0xBE, 0x77, 0x77, 0x77, 0x2E, 0x67, 0x79, 0x2D, 0x70, 0x72, 0x69, 0x6E, 0x74, 0x65, 0x72, 0x2E, 0x63, 0x6F, 0x6D, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, (byte) 0x99, 0x01, (byte) 0xc5, 0x00, 0x04, 0x00, 0x68, 0x74, 0x74, 0x70, 0x73, 0x3A, 0x2F, 0x2F, 0x75, 0x2E, 0x77, 0x65, 0x63, 0x68, 0x61, 0x74, 0x2E, 0x63, 0x6F, 0x6D, 0x2F, 0x4D, 0x46, 0x77, 0x41, 0x75, 0x6D, 0x75, 0x63, 0x2D, 0x41, 0x6F, 0x52, 0x5F, 0x4C, 0x64, 0x59, 0x49, 0x48, 0x64, 0x38, 0x43, 0x5F, 0x45, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, 0x15, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB2, (byte) 0xFA, (byte) 0xC6, (byte) 0xB7, (byte) 0xC3, (byte) 0xFB, (byte) 0xB3, (byte) 0xC6, (byte) 0xA3, (byte) 0xBA, (byte) 0xC7, (byte) 0xB6, (byte) 0xC8, (byte) 0xEB, (byte) 0xCA, (byte) 0xBD, (byte) 0xB1, (byte) 0xEA, (byte) 0xC7, (byte) 0xA9, (byte) 0xB4, (byte) 0xF2, (byte) 0xD3, (byte) 0xA1, (byte) 0xBB, (byte) 0xFA, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, 0x3F, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB2, (byte) 0xFA, (byte) 0xC6, (byte) 0xB7, (byte) 0xD0, (byte) 0xCD, (byte) 0xBA, (byte) 0xC5, (byte) 0xA3, (byte) 0xBA, 0x47, 0x59, 0x2D, 0x42, 0x51, 0x30, 0x38, 0x30, 0x31, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, 0x69, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xCD, (byte) 0xA8, (byte) 0xD1, (byte) 0xB6, (byte) 0xBD, (byte) 0xD3, (byte) 0xBF, (byte) 0xDA, (byte) 0xA3, (byte) 0xBA, 0x52, 0x53, 0x32, 0x33, 0x32, 0x2B, 0x54, 0x54, 0x4C, 0x2B, 0x55, 0x53, 0x42, (byte) 0xBD, (byte) 0xD3, (byte) 0xBF, (byte) 0xDA, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, (byte) 0x93, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB9, (byte) 0xA9, (byte) 0xB5, (byte) 0xE7, (byte) 0xB5, (byte) 0xE7, (byte) 0xD1, (byte) 0xB9, (byte) 0xA3, (byte) 0xBA, 0x31, 0x32, 0x56, 0x33, 0x41, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, (byte) 0xbd, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xC4, (byte) 0xAC, (byte) 0xC8, (byte) 0xCF, (byte) 0xD3, (byte) 0xEF, (byte) 0xD1, (byte) 0xD4, (byte) 0xA3, (byte) 0xBA, (byte) 0xBC, (byte) 0xF2, (byte) 0xCC, (byte) 0xE5, (byte) 0xD6, (byte) 0xD0, (byte) 0xCE, (byte) 0xC4, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, (byte) 0xe7, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xB2, (byte) 0xA8, 0x20, (byte) 0xCC, (byte) 0xD8, 0x20, (byte) 0xC2, (byte) 0xCA, (byte) 0xA3, (byte) 0xBA, 0x31, 0x31, 0x35, 0x32, 0x30, 0x30, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, 0x11, 0x01, 0x00, 0x60, 0x00, 0x11, (byte) 0xB5, (byte) 0xD8, (byte) 0xD6, (byte) 0xB7, (byte) 0xA3, (byte) 0xBA, (byte) 0xB9, (byte) 0xE3, (byte) 0xB6, (byte) 0xAB, (byte) 0xCA, (byte) 0xA1, (byte) 0xC9, (byte) 0xEE, (byte) 0xDB, (byte) 0xDA, (byte) 0xCA, (byte) 0xD0, (byte) 0xB1, (byte) 0xA6, (byte) 0xB0, (byte) 0xB2, (byte) 0xC7, (byte) 0xF8, (byte) 0xC9, (byte) 0xB3, (byte) 0xBE, (byte) 0xAE, (byte) 0xBD, (byte) 0xD6, 0x00,
                0x1A, 0x54, 0x01, 0x15, 0x00, 0x30, 0x01, 0x00, 0x60, 0x00, 0x11, (byte) 0xB5, (byte) 0xC0, (byte) 0xC2, (byte) 0xED, (byte) 0xB0, (byte) 0xB0, (byte) 0xC9, (byte) 0xBD, (byte) 0xB0, (byte) 0xB2, (byte) 0xCA, (byte) 0xA4, (byte) 0xC2, (byte) 0xB7, 0x32, 0x35, (byte) 0xBA, (byte) 0xC5, 0x42, (byte) 0xB6, (byte) 0xB0, 0x34, (byte) 0xC2, (byte) 0xA5, 0x00,
                0x1A, 0x5D, 0x00,
                0x1A, 0x4F, 0x00,
                0x1b, 0x6d};
        esc.addArrayToCommand(bytes);
//        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULtwelve(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40, 0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, 0x40, 0x02, 0x68, 0x01, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, 0x00, 0x00, 0x22, 0x00, 0x08, 0x00, 0x68, 0x74, 0x74, 0x70, 0x73, 0x3A, 0x2F, 0x2F, 0x75, 0x2E, 0x77, 0x65, 0x63, 0x68, 0x61, 0x74, 0x2E, 0x63, 0x6F, 0x6D, 0x2F, 0x4D, 0x46, 0x77, 0x41, 0x75, 0x6D, 0x75, 0x63, 0x2D, 0x41, 0x6F, 0x52, 0x5F, 0x4C, 0x64, 0x59, 0x49, 0x48, 0x64, 0x38, 0x43, 0x5F, 0x45, 0x00,
                0x1A, 0x54, 0x01, 0x4A, 0x01, 0x15, 0x00, 0x00, 0x60, 0x00, 0x20, 0x4D, 0x43, 0x20, 0x31, 0x30, 0x30, 0x31, 0x00,
                0x1A, 0x54, 0x01, 0x4A, 0x01, 0x44, 0x00, 0x00, 0x60, 0x00, 0x20, (byte) 0xB5, (byte) 0xE7, 0x20, (byte) 0xB3, (byte) 0xD8, 0x00,
                0x1A, 0x54, 0x01, 0x4A, 0x01, 0x73, 0x00, 0x00, 0x60, 0x00, 0x20, 0x32, 0x30, 0x31, 0x38, 0x2D, 0x30, 0x37, 0x2D, 0x31, 0x30, 0x00,
                0x1A, 0x54, 0x01, 0x4A, 0x01, (byte) 0xA2, 0x00, 0x00, 0x60, 0x00, 0x20, 0x31, 0x37, 0x3A, 0x33, 0x34, 0x3A, 0x33, 0x30, 0x00,
                0x1A, 0x54, 0x01, 0x4A, 0x01, (byte) 0xD1, 0x00, 0x00, 0x60, 0x00, 0x20, 0x53, 0x54, 0x43, 0x50, 0x30, 0x30, 0x30, 0x30, 0x30, 0x31, 0x33, 0x36, 0x00,
                0x1A, 0x54, 0x01, 0x4A, 0x01, 0x00, 0x01, 0x00, 0x60, 0x00, 0x20, 0x32, 0x30, 0x31, 0x38, 0x2D, 0x30, 0x37, 0x2D, 0x32, 0x30, (byte) 0xC7, (byte) 0xB0, (byte) 0xD3, (byte) 0xD0, (byte) 0xD0, (byte) 0xA7, 0x00,
                0x1A, 0x54, 0x01, 0x4A, 0x01, 0x2E, 0x01, 0x00, 0x60, 0x00, 0x20, 0x4C, 0x53, 0x20, 0x33, 0x32, 0x30, 0x36, 0x30, 0x00,
                0x1A, 0x5D, 0x00, 0x1A, 0x4F, 0x00,
                0x1B, 0x6d};
        esc.addArrayToCommand(bytes);
//        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);

    }

    public void ULthirteen(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40, 0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, 0x40, 0x01, (byte) 0xef, 0x00, 0x00,
                0x1A, 0x30, 0x00, 0x20, 0x00, 0x05, 0x00, 0x0c, 0x45, 0x02, 0x00, 0x31, 0x38, 0x30, 0x31, 0x30, 0x36, 0x30, 0x30, 0x30, 0x30, 0x32, 0x00,
                0x1A, 0x54, 0x01, 0x1b, 0x00, 0x55, 0x00, 0x00, 0x60, 0x00, 0x11, 0x31, 0x20, 0x38, 0x20, 0x30, 0x20, 0x31, 0x20, 0x30, 0x20, 0x36, 0x20, 0x30, 0x20, 0x30, 0x20, 0x30, 0x20, 0x30, 0x20, 0x32, 0x00,
                0x1A, 0x54, 0x01, 0x25, 0x00, 0x75, 0x00, 0x00, 0x60, 0x00, 0x20, (byte) 0xD5, (byte) 0xC5, (byte) 0xC8, (byte) 0xFD, (byte) 0xB7, (byte) 0xE1, 0x20, 0x20, (byte) 0xC4, (byte) 0xD0, 0x20, 0x20, 0x32, 0x39, 0x00,
                0x1A, 0x54, 0x01, 0x25, 0x00, (byte) 0xb5, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xCC, (byte) 0xC7, (byte) 0xC0, (byte) 0xE0, (byte) 0xBF, (byte) 0xB9, (byte) 0xD4, (byte) 0xAD, 0x35, 0x30, 0x2B, (byte) 0xCC, (byte) 0xC7, (byte) 0xC0, (byte) 0xE0, (byte) 0xBF, (byte) 0xB9, (byte) 0xD4, (byte) 0xAD, 0x00,
                0x1A, 0x5D, 0x00, 0x1A, 0x4F, 0x00,
                0x1B, 0x6d};
        esc.addArrayToCommand(bytes);
//        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public void ULfourteen(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40, 0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, 0x40, 0x02, 0x68, 0x01, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, 0x00, 0x00, 0x22, 0x00, 0x08, 0x00, 0x68, 0x74, 0x74, 0x70, 0x73, 0x3A, 0x2F, 0x2F, 0x75, 0x2E, 0x77, 0x65, 0x63, 0x68, 0x61, 0x74, 0x2E, 0x63, 0x6F, 0x6D, 0x2F, 0x4D, 0x46, 0x77, 0x41, 0x75, 0x6D, 0x75, 0x63, 0x2D, 0x41, 0x6F, 0x52, 0x5F, 0x4C, 0x64, 0x59, 0x49, 0x48, 0x64, 0x38, 0x43, 0x5F, 0x45, 0x00,
                0x1A, 0x54, 0x01, 0x4A, 0x01, 0x73, 0x00, 0x00, 0x60, 0x00, 0x20, (byte) 0xC9, (byte) 0xA8, (byte) 0xD2, (byte) 0xBB, (byte) 0xC9, (byte) 0xA8, (byte) 0xBC, (byte) 0xD3, (byte) 0xCE, (byte) 0xD2, (byte) 0xCE, (byte) 0xA2, (byte) 0xD0, (byte) 0xC5, 0x00,
                0x1A, 0x54, 0x01, 0x4A, 0x01, (byte) 0xD1, 0x00, 0x00, 0x60, 0x00, 0x20, (byte) 0xD3, (byte) 0xD1, (byte) 0xD2, (byte) 0xEA, (byte) 0xB4, (byte) 0xD3, (byte) 0xB4, (byte) 0xCB, (byte) 0xBF, (byte) 0xCC, (byte) 0xBF, (byte) 0xAA, (byte) 0xCA, (byte) 0xBC, 0x00,
                0x1A, 0x5D, 0x00, 0x1A, 0x4F, 0x00,
                0x1B, 0x6d};
        esc.addArrayToCommand(bytes);
//        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }
    public void ULfiftteen(View view) throws IOException {
        Esc esc = new Esc();
        byte[] bytes = new byte[]{0x1B, 0x40,0x1B, 0x4C,
        };

        byte[] byte2 = new byte[]{0x1A, 0x5B, 0x01, 0x00, 0x00, 0x00, 0x00, 0x40, 0x02, 0x68, 0x01, 0x00,
                0x1A, 0x31, 0x00, 0x05, 0x02, (byte) 0x96, 0x01, 0x78, 0x00, 0x03, 0x00, 0x31, 0x38, 0x36, 0x38, 0x37, 0x30, 0x34, 0x30, 0x34, 0x38, 0x30, 0x33, 0x33, 0x33, 0x33, 0x34, 0x31, 0x39, 0x30, 0x31, 0x32, 0x31, 0x31, 0x32, 0x33, 0x36, 0x31, 0x30, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, 0x20, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xD2, (byte) 0xBD, (byte) 0xD4, (byte) 0xBA, (byte) 0xA3, (byte) 0xBA, (byte) 0xD4, (byte) 0xBD, (byte) 0xB3, (byte) 0xC7, (byte) 0xC7, (byte) 0xF8, (byte) 0xD6, (byte) 0xD0, (byte) 0xD2, (byte) 0xBD, (byte) 0xD2, (byte) 0xBD, (byte) 0xD4, (byte) 0xBA, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, 0x48, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xBF, (byte) 0xC6, (byte) 0xCA, (byte) 0xD2, (byte) 0xA3, (byte) 0xBA, (byte) 0xD4, (byte) 0xBD, (byte) 0xB3, (byte) 0xC7, (byte) 0xC7, (byte) 0xF8, (byte) 0xBF, (byte) 0xC6, (byte) 0xCA, (byte) 0xD2, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, 0x70, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xD6, (byte) 0xD8, (byte) 0xC1, (byte) 0xBF, (byte) 0xA3, (byte) 0xBA, 0x30, 0x2E, 0x33, 0x35, 0x4B, 0x47, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, (byte) 0x98, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xD6, (byte) 0xD6, (byte) 0xC0, (byte) 0xE0, (byte) 0xA3, (byte) 0xBA, (byte) 0xB8, (byte) 0xD0, (byte) 0xC8, (byte) 0xBE, (byte) 0xD0, (byte) 0xD4, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, (byte) 0xC0, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xCA, (byte) 0xD5, (byte) 0xBC, (byte) 0xAF, (byte) 0xC8, (byte) 0xCB, (byte) 0xA3, (byte) 0xBA, (byte) 0xD4, (byte) 0xBD, (byte) 0xB3, (byte) 0xC7, (byte) 0xC7, (byte) 0xF8, (byte) 0xB2, (byte) 0xE2, (byte) 0xCA, (byte) 0xD4, (byte) 0xC8, (byte) 0xCB, (byte) 0xD4, (byte) 0xB1, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, (byte) 0xE8, 0x00, 0x00, 0x60, 0x00, 0x11, (byte) 0xC8, (byte) 0xB7, (byte) 0xC8, (byte) 0xCF, (byte) 0xC8, (byte) 0xCB, (byte) 0xA3, (byte) 0xBA, (byte) 0xD4, (byte) 0xBD, (byte) 0xB3, (byte) 0xC7, (byte) 0xC7, (byte) 0xF8, (byte) 0xD2, (byte) 0xBD, (byte) 0xBB, (byte) 0xA4, (byte) 0xC8, (byte) 0xCB, (byte) 0xD4, (byte) 0xB1, 0x00,
                0x1A, 0x54, 0x01, (byte) 0x80, 0x00, 0x10, 0x01, 0x00, 0x60, 0x00, 0x11, (byte) 0xC8, (byte) 0xD5, (byte) 0xC6, (byte) 0xDA, (byte) 0xA3, (byte) 0xBA, 0x32, 0x30, 0x31, 0x39, 0x2D, 0x30, 0x31, 0x2D, 0x32, 0x31, 0x20, 0x31, 0x32, 0x3A, 0x33, 0x36,
                0x1a, 0x5d, 0x00,
                0x1a, 0x4f, 0x00};

        for (int i=0;i<20;i++){
            bytes = byteMerger(bytes,byte2);
        }
        esc.addArrayToCommand(bytes);
        esc.cutHalf();
        USBUtil.getInstance().CommandEsc(esc);
    }

    public byte[] byteMerger(byte[] byte1, byte[] byte2) {
        byte[] byteResult = new byte[byte1.length + byte2.length];
        System.arraycopy(byte1, 0, byteResult, 0, byte1.length);
        System.arraycopy(byte2, 0, byteResult, byte1.length, byte2.length);
        return byteResult;
    }

    public void USBLV() throws IOException {
        label.verify();
        USBUtil.getInstance().CommandLabel(label);
        label.clear();
    }


//    public void setULESC(View view) throws IOException {
//        Esc esc = new Esc();
//        esc.switchESC();
//        USBUtil.getInstance().CommandEsc(esc);
//    }

    public void setLabel(View view) throws IOException {

        AlertDialog.Builder dialog = new AlertDialog.Builder(this);
        dialog.setMessage(R.string.setupLabelModelTips);
        dialog.setPositiveButton(R.string.OK, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                try {
                    label.switchLabel();
                    USBUtil.getInstance().CommandLabel(label);
                    label.clear();
                    dialog.dismiss();
                }catch (Exception e){
                    e.printStackTrace();
                }
            }
        });
        dialog.setNeutralButton("取消", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });
        dialog.show();
    }

    @Override
    public void onStop() {
        super.onStop();
        if (printerStateReceiver != null) {
            this.unregisterReceiver(printerStateReceiver);
            printerStateReceiver = null;
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        register();
    }

    public void register() {

        if (printerStateReceiver == null) {
            IntentFilter filter = new IntentFilter();
            printerStateReceiver = new PrinterStateReceiver(USBLabelActivity.this, paper);
            filter.addAction("PrinterState");
            filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
            this.registerReceiver(printerStateReceiver, filter);
        }
    }
}
