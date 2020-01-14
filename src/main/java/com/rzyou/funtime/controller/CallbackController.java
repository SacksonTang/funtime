package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.payment.wxpay.MyWxPay;
import com.rzyou.funtime.common.payment.wxpay.sdk.WXPayUtil;
import com.rzyou.funtime.entity.dto.SdkParam;
import com.rzyou.funtime.entity.dto.UserStateOfflineParam;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("callback")
public class CallbackController {

    @Autowired
    AccountService accountService;
    @Autowired
    UserService userService;

    /**
     * 腾讯在线状态回调
     * @param params
     * @param sdkParam
     * @return
     */
    @RequestMapping(value = "userStateOffline")
    public JSONObject userStateOffline(@RequestBody UserStateOfflineParam params, SdkParam sdkParam){
        JSONObject result = new JSONObject();

        if (StringUtils.isBlank(sdkParam.getCallbackCommand())||StringUtils.isBlank(sdkParam.getSdkAppid())
                ||!sdkParam.getSdkAppid().equals(String.valueOf(Constant.TENCENT_YUN_SDK_APPID))){
            log.error("**************用户：{} 已下线参数有问题******************",params.getInfo().getTo_Account());
            result.put("ActionStatus","FAIL");
            result.put("ErrorCode",-1);
            result.put("ErrorInfo","error");
            return result;
        }
        if (sdkParam.getCallbackCommand().equals("State.StateChange")){
            if (params.getInfo().getAction().equals("Logout")
                    ||params.getInfo().getAction().equals("TimeOut")
                    ||params.getInfo().getAction().equals("Disconnect")){
                log.warn("**************用户：{} 已下线******************REASON:{}",params.getInfo().getTo_Account(),params.getInfo().getReason());
                userService.updateOnlineState(Long.parseLong(params.getInfo().getTo_Account()),2);
                result.put("ActionStatus","OK");
                result.put("ErrorCode",0);
                result.put("ErrorInfo","");
                return result;
            }else{
                log.warn("**************用户：{} 已上线******************REASON:{}",params.getInfo().getTo_Account(),params.getInfo().getReason());
                userService.updateOnlineState(Long.parseLong(params.getInfo().getTo_Account()),1);
                result.put("ActionStatus","OK");
                result.put("ErrorCode",0);
                result.put("ErrorInfo","");
                return result;
            }
        }
        result.put("ActionStatus","OK");
        result.put("ErrorCode",0);
        result.put("ErrorInfo","");
        return result;

    }



    /**
     * 微信支付回调
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "notifyWxPay", produces = MediaType.APPLICATION_JSON_VALUE)
    public String notifyWxPay(HttpServletRequest request) throws Exception {
        log.info("微信支付回调");
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        String resultxml = new String(outSteam.toByteArray(), "utf-8");
        Map<String, String> params = WXPayUtil.xmlToMap(resultxml);
        outSteam.close();
        inStream.close();


        Map<String,String> return_data = new HashMap<>();
        if (!MyWxPay.isPayResultNotifySignatureValid(params)) {
            // 支付失败
            return_data.put("return_code", "FAIL");
            return_data.put("return_msg", "签名验证失败");
            return WXPayUtil.mapToXml(return_data);
        } else {
            log.info("===============签名验证成功==============");
            // ------------------------------
            // 处理业务开始
            // ------------------------------
            // 此处处理订单状态，结合自己的订单数据完成订单状态的更新
            // ------------------------------

            try {

                String attach = params.get("attach");
                //微信支付订单号
                String transaction_id = params.get("transaction_id");
                Long orderId = Long.parseLong(attach);

                return_data = accountService.paySuccess(orderId,transaction_id);
                return WXPayUtil.mapToXml(return_data);
            }catch (BusinessException e1){
                e1.printStackTrace();
                return_data.put("return_code", "FAIL");
                return_data.put("return_msg", e1.getMsg());
                return WXPayUtil.mapToXml(return_data);
            }catch (Exception e2){
                e2.printStackTrace();
                return_data.put("return_code", "FAIL");
                return_data.put("return_msg", "SYSTEM ERROR");
                return WXPayUtil.mapToXml(return_data);
            }
        }
    }

}
