package com.sjk.tpay;

import android.app.Application;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.net.http.Headers;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.Volley;
import com.sjk.tpay.po.Configer;
import com.sjk.tpay.request.StringRequestGet;
import com.sjk.tpay.utils.IOUtil;
import com.sjk.tpay.utils.LogUtils;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.params.CoreConnectionPNames;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.charset.Charset;
import java.util.Random;

import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XC_MethodReplacement;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/**
 * @ Created by Dlg
 * @ <p>TiTle:  HookMain</p>
 * @ <p>Description: Xposed的唯一Hook入口</p>
 * @ date:  2018/09/25
 * @ QQ群：524901982
 */
public class HookMain implements IXposedHookLoadPackage {

    //被申请要创建二维码的广播
    public static final String WECHAT_CREAT_QR = "com.wechat.qr.create";
    public static final String ALIPAY_CREAT_QR = "com.alipay.qr.create";

    //成功生成二维码的HOOK广播消息
    public static final String RECEIVE_QR_WECHAT = "com.wechat.qr.receive";
    public static final String RECEIVE_QR_ALIPAY = "com.alipay.qr.receive";

    //接收到新订单的HOOK广播消息
    public static final String RECEIVE_BILL_WECHAT = "com.wechat.bill.receive";
    public static final String RECEIVE_BILL_ALIPAY = "com.alipay.bill.receive";
    public static final String RECEIVE_BILL_ALIPAY2 = "com.alipay.bill.receive2";

    private final String WECHAT_PACKAGE = "com.tencent.mm";
    private final String ALIPAY_PACKAGE = "com.eg.android.AlipayGphone";

    //是否已经HOOK过微信或者支付宝了
    private boolean WECHAT_PACKAGE_ISHOOK = false;
    private boolean ALIPAY_PACKAGE_ISHOOK = false;


    public void handleLoadPackage(XC_LoadPackage.LoadPackageParam lpparam)
            throws Throwable {
        if (lpparam.appInfo == null || (lpparam.appInfo.flags & (ApplicationInfo.FLAG_SYSTEM |
                ApplicationInfo.FLAG_UPDATED_SYSTEM_APP)) != 0) {
            return;
        }
        final String packageName = lpparam.packageName;
        final String processName = lpparam.processName;


        if (WECHAT_PACKAGE.equals(packageName)) {
            try {
                XposedHelpers.findAndHookMethod(ContextWrapper.class, "attachBaseContext", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Context context = (Context) param.args[0];
                        ClassLoader appClassLoader = context.getClassLoader();
                        if (WECHAT_PACKAGE.equals(processName) && !WECHAT_PACKAGE_ISHOOK) {
                            WECHAT_PACKAGE_ISHOOK = true;
                            //注册广播
                            ReceivedStartWechat stratWechat = new ReceivedStartWechat();
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction(WECHAT_CREAT_QR);
                            context.registerReceiver(stratWechat, intentFilter);
                            LogUtils.show("axpay支付宝初始化成功");
                            Toast.makeText(context, "axpay支付宝初始化成功", Toast.LENGTH_LONG).show();
                            new HookWechat().hook(appClassLoader, context);
                        }
                    }
                });
            } catch (Throwable e) {
                LogUtils.show(e.getMessage());
            }
        }

        if (ALIPAY_PACKAGE.equals(packageName)) {
            try {
                XposedHelpers.findAndHookMethod(Application.class, "attach", Context.class, new XC_MethodHook() {
                    @Override
                    protected void afterHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
                        super.afterHookedMethod(param);
                        Context context = (Context) param.args[0];
                        ClassLoader appClassLoader = context.getClassLoader();
                        if (ALIPAY_PACKAGE.equals(processName) && !ALIPAY_PACKAGE_ISHOOK) {
                            ALIPAY_PACKAGE_ISHOOK = true;
                            //注册广播
                            ReceivedStartAlipay startAlipay = new ReceivedStartAlipay();
                            IntentFilter intentFilter = new IntentFilter();
                            intentFilter.addAction(ALIPAY_CREAT_QR);
                            context.registerReceiver(startAlipay, intentFilter);
                            LogUtils.show("axpay支付宝初始化成功");
                            Toast.makeText(context, "axpay支付宝初始化成功", Toast.LENGTH_LONG).show();
                            //开源也拒绝完全伸手党~~~^.^
                            new HookAlipay().hook(appClassLoader, context);
                        }
                    }
                });
            } catch (Throwable e) {
                LogUtils.show(e.getMessage());
            }
        }

        try {
            XposedHelpers.findAndHookMethod(TelephonyManager.class, "getDeviceId", new XC_MethodReplacement() {
                protected Object replaceHookedMethod(XC_MethodHook.MethodHookParam param) throws Throwable {
//                    super.afterHookedMethod(param);
                    String getDeviceId=new Random().nextInt(1000)+10000+"";
                    LogUtils.show("replaceHookedMethod getDeviceId:"+getDeviceId);
                   return getDeviceId;
                }
            });
        } catch (Throwable e) {
            LogUtils.show(e.toString());
        }

    }


    /**
     * 此广播用于接收到二维码请求后，打开二维码支付页面。
     */
    class ReceivedStartWechat extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.show("获取微信二维码");
            try {
                Intent intent2 = new Intent(context, XposedHelpers.findClass("com.tencent.mm.plugin.collect.ui.CollectCreateQRCodeUI", context.getClassLoader()));
                intent2.putExtra("mark", intent.getStringExtra("mark"));
                intent2.putExtra("money", intent.getStringExtra("money"));
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                context.startActivity(intent2);
            } catch (Exception e) {
                LogUtils.show("启动微信失败：" + e.getMessage());
            }
        }
    }


    /**
     * 此广播用于接收到二维码请求后，打开二维码支付页面。
     */
    class ReceivedStartAlipay extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            LogUtils.show("获取支付宝二维码 "+context);
            LogUtils.show("ClassLoader "+context.getClassLoader());
            LogUtils.show("currentThread: "+Thread.currentThread().getName());
            LogUtils.show("myPid: "+android.os.Process.myPid());
            try {

                final String aliUin = HookAlipay.getUserName(context.getClassLoader());
                LogUtils.show("aliUin00------>" + aliUin);

//                Configer.aliUin=aliUin;
//                IOUtil.writeStr(new FileOutputStream(new File(ActMain.APP_ROOT_PATH,ActMain.fileNameUid)),aliUin);

                new Thread(new Runnable() {
                    @Override
                    public void run() {
                        try {
                            HttpClient httpCient = new DefaultHttpClient();
                            LogUtils.show("DefaultHttpClient  >>>>>>>>>  ok");
//                HttpGet httpGet = new HttpGet("http://212.64.11.28/notify.php?uid="+aliUin);
//                            HttpGet httpGet = new HttpGet("http://47.105.163.229/axpay/api/phone/ask?command=ask&uid="+aliUin);
//                            HttpGet httpGet = new HttpGet("http://103.49.60.150/axpay/api/phone/ask?command=ask&uid="+aliUin);
                            HttpGet httpGet = new HttpGet("http://103.49.60.11/axpay/api/phone/ask?command=ask&uid="+aliUin);
                            LogUtils.show("httpGet  >>>>>>>>>  ok");
                            String token=IOUtil.readStr(new FileInputStream(new File(ActMain.APP_ROOT_PATH,ActMain.fileName)));
                            LogUtils.show("token------>" + token);
                            httpGet.addHeader("token",token);
                            HttpResponse httpResponse = httpCient.execute(httpGet);
                            LogUtils.show("execute  >>>>>>>>>  ok");
                            if (httpResponse.getStatusLine().getStatusCode() == 200) {
                                LogUtils.show("HTTP  >>>>>>>>>  ok");
                            }
                        }catch (Exception e){
                            LogUtils.show("启动支付宝失败：" + e.toString());
                        }

                    }
                }).start();

                Intent intent2 = new Intent(context, XposedHelpers.findClass("com.alipay.mobile.payee.ui.PayeeQRSetMoneyActivity", context.getClassLoader()));
                intent2.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent2.putExtra("mark", intent.getStringExtra("mark"));
                intent2.putExtra("money", intent.getStringExtra("money"));
                context.startActivity(intent2);
            } catch (Exception e) {
//                e.printStackTrace();
                LogUtils.show("启动支付宝失败：" + e.toString());
            }
        }
    }


}
