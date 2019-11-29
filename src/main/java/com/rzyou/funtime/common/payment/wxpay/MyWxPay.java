package com.rzyou.funtime.common.payment.wxpay;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.payment.wxpay.sdk.MyWxPayConfig;
import com.rzyou.funtime.common.payment.wxpay.sdk.WXPay;

import java.util.HashMap;
import java.util.Map;

public class MyWxPay {



    /**
     * 统一下单
     *
     * @return
     */
    public static Map<String, String> unifiedOrder(String totalFee,String ip,String orderNo,String imei,String notifyUrl,String orderId) {
        try {
            MyWxPayConfig config = new MyWxPayConfig();
            WXPay wxpay = new WXPay(config);

            Map<String, String> data = new HashMap<>();
            data.put("body", "FUNTIME语音直播-充值");
            data.put("out_trade_no", orderNo);
            data.put("device_info", imei);
            data.put("fee_type", "CNY");
            data.put("total_fee", totalFee);
            data.put("spbill_create_ip", ip);
            data.put("notify_url", notifyUrl);
            data.put("trade_type", "APP");  //
            data.put("attach", orderId);


            Map<String, String> resp = wxpay.unifiedOrder(data);
            return resp;
        } catch (Exception e) {
            throw new BusinessException(ErrorMsgEnum.UNIFIELDORDER_ERROR.getValue(),ErrorMsgEnum.UNIFIELDORDER_ERROR.getDesc());

        }
    }

    public static boolean isPayResultNotifySignatureValid(Map<String,String> map){
        try {
            MyWxPayConfig config = new MyWxPayConfig();
            WXPay wxpay = new WXPay(config);
            return wxpay.isPayResultNotifySignatureValid(map);
        }catch (Exception e){
            throw new BusinessException(ErrorMsgEnum.VALID_SIGN_ERROR.getValue(),ErrorMsgEnum.VALID_SIGN_ERROR.getDesc());
        }

    }


}
