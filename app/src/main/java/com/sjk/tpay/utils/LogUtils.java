package com.sjk.tpay.utils;

import android.util.Log;

import com.sjk.tpay.ActMain;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.robv.android.xposed.XposedBridge;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  LogUtils</p>
 * @ <p>Description: 懒得去判断这个日志是哪个进程发送的了，统一下日志接口吧。</p>
 * @ date:  2018/9/22
 * @ QQ群：524901982
 */
public class LogUtils {
    public static final SimpleDateFormat format=new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
    public static void show(String tips) {
        try {
            XposedBridge.log(tips);
        } catch (NoClassDefFoundError ignore) {

        }
        Log.e("LogUtils", tips);
        try {
            IOUtil.writeStr(new FileOutputStream(new File(ActMain.APP_ROOT_PATH,"log.txt"),true),"["+format.format(new Date())+"]"+tips);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
