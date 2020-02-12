package com.rzyou.funtime.common.payment.wxpay;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.payment.wxpay.sdk.MyWxPayConfig;
import com.rzyou.funtime.common.payment.wxpay.sdk.WXPay;
import com.rzyou.funtime.common.payment.wxpay.sdk.WXPayUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;
@Slf4j
@Component
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
            data.put("body", "FUNTIME-RECHARGE");
            data.put("out_trade_no", orderNo);
            data.put("device_info", imei);
            data.put("fee_type", "CNY");
            data.put("total_fee", totalFee);
            data.put("spbill_create_ip", ip);
            data.put("notify_url", notifyUrl);
            data.put("trade_type", "APP");  //
            data.put("attach", orderId);
            Map<String, String> resp = wxpay.unifiedOrder(data);
            if(!"SUCCESS".equals(resp.get("return_code"))||!"SUCCESS".equals(resp.get("result_code"))){
                log.error("预支付接口:unifiedOrder失败:{}",resp);
                throw new BusinessException(ErrorMsgEnum.UNIFIELDORDER_ERROR.getValue(),ErrorMsgEnum.UNIFIELDORDER_ERROR.getDesc());
            }
            if (resp.get("prepay_id")!=null) {
                Map<String, String> signMap = new HashMap<>();

                signMap.put("prepayid", resp.get("prepay_id"));

                Map<String, String> signData = wxpay.fillRequestDataReturn(signMap);
                log.info("unifiedOrder result : {}", signData);
                return signData;
            }else{
                throw new BusinessException(ErrorMsgEnum.UNIFIELDORDER_ERROR.getValue(),ErrorMsgEnum.UNIFIELDORDER_ERROR.getDesc());
            }
        } catch (Exception e) {
            throw new BusinessException(ErrorMsgEnum.UNIFIELDORDER_ERROR.getValue(),ErrorMsgEnum.UNIFIELDORDER_ERROR.getDesc());

        }
    }

    /**
     * 支付订单查询
     * @param transaction_id
     * @param out_trade_no
     * @return
     */
    public static Map<String, String> orderQuery(String transaction_id,String out_trade_no){
        try {
            MyWxPayConfig config = new MyWxPayConfig();
            WXPay wxpay = new WXPay(config);

            Map<String, String> data = new HashMap<>();
            if (transaction_id != null) {
                data.put("transaction_id", transaction_id);
            }
            if (out_trade_no != null) {
                data.put("out_trade_no", out_trade_no);
            }
            log.debug("支付订单查询 orderQuery 入参: {}",data);
            Map<String, String> resp = wxpay.orderQuery(data);
            log.debug("支付订单查询 orderQuery 返回: {}",resp);
            return resp;
        }catch (Exception e){
            throw new BusinessException(ErrorMsgEnum.ORDERQUERY_ERROR.getValue(),ErrorMsgEnum.ORDERQUERY_ERROR.getDesc());
        }
    }

    /**
     * 关闭订单
     * @param out_trade_no
     * @return
     */
    public static Map<String, String> closeOrder(String out_trade_no){
        try {
            MyWxPayConfig config = new MyWxPayConfig();
            WXPay wxpay = new WXPay(config);

            Map<String, String> data = new HashMap<>();

            if (out_trade_no != null) {
                data.put("out_trade_no", out_trade_no);
            }
            log.debug("支付订单查询 closeOrder 入参: {}",data);
            Map<String, String> resp = wxpay.closeOrder(data);
            log.debug("支付订单查询 closeOrder 返回: {}",resp);
            return resp;
        }catch (Exception e){
            throw new BusinessException(ErrorMsgEnum.ORDERQUERY_ERROR.getValue(),ErrorMsgEnum.ORDERQUERY_ERROR.getDesc());
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
