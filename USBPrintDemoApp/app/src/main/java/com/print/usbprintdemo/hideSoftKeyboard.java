package com.print.usbprintdemo;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import java.util.List;

import static android.content.Context.INPUT_METHOD_SERVICE;

/**
 * 输入法隐藏工具
 * 有EditText和交互的页面需要显示信息时，输入法会挡住部分Toast位置
 * 扁哥
 */

public class hideSoftKeyboard {

    /**
     * 隐藏软键盘(只适用于Activity，不适用于Fragment)
     */
    public static void hideSoftKeyboard(Activity activity) {
        View view = activity.getCurrentFocus();
        if (view != null) {
            InputMethodManager inputMethodManager = (InputMethodManager) activity.getSystemService(INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }


    /**
     * 隐藏软键盘(可用于Activity，Fragment)
     */
    public static void hideSoftKeyboard(Context context, List<View> viewList) {
        if (viewList == null) return;

        InputMethodManager inputMethodManager = (InputMethodManager) context.getSystemService(INPUT_METHOD_SERVICE);

        for (View v : viewList) {
            inputMethodManager.hideSoftInputFromWindow(v.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
        }
    }

    /**
     * 根据输入法的状态显示和隐藏输入法
     *
     */
    public static void autoInputmethod(Context context) {
        InputMethodManager imm = (InputMethodManager) context
                .getSystemService(INPUT_METHOD_SERVICE);
        assert imm != null;
        imm.toggleSoftInput(0, InputMethodManager.HIDE_NOT_ALWAYS);
    }
}
