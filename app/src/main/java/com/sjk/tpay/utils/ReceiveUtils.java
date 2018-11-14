package com.sjk.tpay.utils;

import android.content.IntentFilter;

import com.sjk.tpay.HKApplication;
import com.sjk.tpay.ReceiverMain;

import static com.sjk.tpay.HookMain.RECEIVE_BILL_ALIPAY;
import static com.sjk.tpay.HookMain.RECEIVE_BILL_ALIPAY2;
import static com.sjk.tpay.HookMain.RECEIVE_BILL_WECHAT;
import static com.sjk.tpay.HookMain.RECEIVE_QR_ALIPAY;
import static com.sjk.tpay.HookMain.RECEIVE_QR_WECHAT;

/**
 * @ Created by Dpc
 * @ <p>TiTle:  ReceiveUtils</p>
 * @ <p>Description:</p>
 * @ date:  2018/10/14 12:02
 * @ QQ:    315096953
 */
public class ReceiveUtils {

    public static void startReceive() {
        if (!ReceiverMain.mIsInit) {
            IntentFilter filter = new IntentFilter(RECEIVE_QR_WECHAT);
            filter.addAction(RECEIVE_QR_ALIPAY);
            filter.addAction(RECEIVE_BILL_WECHAT);
            filter.addAction(RECEIVE_BILL_ALIPAY);
            filter.addAction(RECEIVE_BILL_ALIPAY2);
            HKApplication.app.registerReceiver(new ReceiverMain(), filter);
        }
    }

}
