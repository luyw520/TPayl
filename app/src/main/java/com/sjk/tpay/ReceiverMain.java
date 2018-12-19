package com.sjk.tpay;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.alibaba.fastjson.JSON;
import com.sjk.tpay.bll.ApiBll;
import com.sjk.tpay.po.QrBean;
import com.sjk.tpay.utils.LogUtils;
import com.sjk.tpay.utils.PayUtils;

import java.util.logging.Handler;

import static com.sjk.tpay.HookMain.RECEIVE_BILL_ALIPAY;
import static com.sjk.tpay.HookMain.RECEIVE_BILL_ALIPAY2;
import static com.sjk.tpay.HookMain.RECEIVE_BILL_WECHAT;
import static com.sjk.tpay.HookMain.RECEIVE_QR_ALIPAY;
import static com.sjk.tpay.HookMain.RECEIVE_QR_WECHAT;


/**
 * @ Created by Dlg
 * @ <p>TiTle:  ReceiverMain</p>
 * @ <p>Description: 当HOOK之后的处理结果，只能用此广播来接受，不然很多数据不方便共享的</p>
 * @ date:  2018/09/22
 * @ QQ群：524901982
 */
public class ReceiverMain extends BroadcastReceiver {
    private ApiBll mApiBll;
    public static boolean mIsInit = false;
    private static String lastMsg = "";//防止重启接收广播，一定要用static
    private static long mLastSucc = 0;
    private static String cook = "";


    public ReceiverMain() {
        super();
        mIsInit = true;
        LogUtils.show("Receiver创建成功！");
        mApiBll = new ApiBll();
    }

    @Override
    public void onReceive(Context context, Intent intent) {


        LogUtils.show("Receiver >>>>>>>>>>>\t"+intent.getAction());
        try {
            String data = intent.getStringExtra("data");
            if (lastMsg.contentEquals(data)) {//暂时不管
                return;
            }
            LogUtils.show("Receiver  Data>>>>>>>>>>>\n"+data);

//            if (!intent.getAction().contentEquals(RECEIVE_BILL_ALIPAY)
//                    && lastMsg.contentEquals(data)) {
//                return;
//            }
            lastMsg = intent.getStringExtra("data");
            QrBean qrBean = JSON.parseObject(data, QrBean.class);

            if (intent.getAction().equals("com.alipay.bill.receive2")){
                qrBean = JSON.parseObject(data, QrBean.class);
                new ApiBll().payQR(qrBean);
            }

            switch (intent.getAction()) {
                case RECEIVE_QR_WECHAT:
//                    QrBean qrBean = JSON.parseObject(data, QrBean.class);
                    mApiBll.sendQR(qrBean.getUrl(), qrBean.getMark_sell());
                    break;
                case RECEIVE_BILL_WECHAT:
                    qrBean = JSON.parseObject(data, QrBean.class);
                    mApiBll.payQR(qrBean);
                case RECEIVE_QR_ALIPAY:
                    qrBean = JSON.parseObject(data, QrBean.class);
                    mApiBll.sendQR(qrBean.getUrl(), qrBean.getMark_sell());
                    break;
                case RECEIVE_BILL_ALIPAY2:
//                    PayUtils.dealAlipayWebTrade(context, data);
                    qrBean = JSON.parseObject(data, QrBean.class);
//                    LogUtils.show("");
                    new ApiBll().payQR(qrBean);
//                    mApiBll.payQR(qrBean);
                    break;
                case RECEIVE_BILL_ALIPAY:
                    cook = data;
                    mLastSucc = System.currentTimeMillis();
                    PayUtils.dealAlipayWebTrade(context, data);
                    break;
            }

            switch (intent.getAction()) {
                case RECEIVE_BILL_WECHAT:
                case RECEIVE_BILL_ALIPAY:
                    new android.os.Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mApiBll.dealTaskList();
                        }
                    }, 5000);
            }
        } catch (Exception e) {
            LogUtils.show(e.getMessage());
        }
    }

    public static String getCook() {
        return cook == null ? "" : cook;
    }

    public static void setCook(String cook) {
        ReceiverMain.cook = cook;
    }

    public static long getmLastSucc() {
        return mLastSucc;
    }

    public static void setmLastSucc(long mLastSucc) {
        ReceiverMain.mLastSucc = mLastSucc;
    }

}
