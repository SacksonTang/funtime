package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONObject;
import com.alipay.easysdk.factory.Factory;
import com.alipay.easysdk.kernel.util.AntCertificationUtil;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.payment.wxpay.MyWxPay;
import com.rzyou.funtime.common.payment.wxpay.sdk.WXPayUtil;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.component.StaticData;
import com.rzyou.funtime.entity.FuntimeImgeCallback;
import com.rzyou.funtime.entity.FuntimeTencentAd;
import com.rzyou.funtime.entity.FuntimeTencentAdMonitor;
import com.rzyou.funtime.entity.dto.SdkParam;
import com.rzyou.funtime.entity.dto.UserStateOfflineParam;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.AdvertisService;
import com.rzyou.funtime.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.jacoco.agent.rt.internal_035b120.core.internal.flow.IFrame;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("callback")
public class CallbackController {

    @Autowired
    AccountService accountService;
    @Autowired
    UserService userService;
    @Autowired
    AdvertisService advertisService;

    /**
     * 腾讯在线状态回调
     * @param params
     * @param sdkParam
     * @return
     */
    @RequestMapping(value = "userStateOffline")
    public JSONObject userStateOffline(@RequestBody UserStateOfflineParam params, SdkParam sdkParam){
        JSONObject result = new JSONObject();

        try {

            if (StringUtils.isBlank(sdkParam.getCallbackCommand()) || StringUtils.isBlank(sdkParam.getSdkAppid())
                    || !sdkParam.getSdkAppid().equals(String.valueOf(StaticData.TENCENT_YUN_SDK_APPID))) {
                log.error("**************用户：{} 已下线参数有问题******************", params.getInfo().getTo_Account());
                result.put("ActionStatus", "FAIL");
                result.put("ErrorCode", -1);
                result.put("ErrorInfo", "error");
                return result;
            }
            if (sdkParam.getCallbackCommand().equals("State.StateChange")) {
                if (params.getInfo().getAction().equals("Logout")
                        || params.getInfo().getAction().equals("TimeOut")
                        || params.getInfo().getAction().equals("Disconnect")) {
                    log.warn("**************用户：{} 已下线******************REASON:{}", params.getInfo().getTo_Account(), params.getInfo().getReason());
                    userService.saveImHeart(Long.parseLong(params.getInfo().getTo_Account()), 2, params.getInfo().getAction(), params.getInfo().getReason());
                    result.put("ActionStatus", "OK");
                    result.put("ErrorCode", 0);
                    result.put("ErrorInfo", "");
                    return result;
                } else {
                    log.warn("**************用户：{} 已上线******************REASON:{}", params.getInfo().getTo_Account(), params.getInfo().getReason());
                    userService.saveImHeart(Long.parseLong(params.getInfo().getTo_Account()), 1, params.getInfo().getAction(), params.getInfo().getReason());
                    result.put("ActionStatus", "OK");
                    result.put("ErrorCode", 0);
                    result.put("ErrorInfo", "");
                    return result;
                }
            }
            result.put("ActionStatus", "OK");
            result.put("ErrorCode", 0);
            result.put("ErrorInfo", "");
            return result;
        }catch (Exception e){
            e.printStackTrace();
            result.put("ActionStatus", "FAIL");
            result.put("ErrorCode", -1);
            result.put("ErrorInfo", "error");
            return result;
        }

    }



    /**
     * 微信支付回调
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "notifyWxPay", produces = MediaType.APPLICATION_JSON_VALUE)
    public String notifyWxPay(HttpServletRequest request) throws Exception {
        InputStream inStream = request.getInputStream();
        ByteArrayOutputStream outSteam = new ByteArrayOutputStream();
        byte[] buffer = new byte[1024];
        int len = 0;
        while ((len = inStream.read(buffer)) != -1) {
            outSteam.write(buffer, 0, len);
        }
        String resultxml = new String(outSteam.toByteArray(), "utf-8");
        log.info("微信支付回调参数: {}",resultxml);
        boolean bool = WXPayUtil.isSignatureValid(resultxml, Constant.WX_PAY_APPSECRET);
        log.info("resultXml bool :{}",bool);
        Map<String, String> params = WXPayUtil.xmlToMap(resultxml);
        outSteam.close();
        inStream.close();
        log.info("微信支付回调参数: {}",params);

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
            if (!"SUCCESS".equals(params.get("return_code"))){
                return_data.put("return_code", "FAIL");
                return_data.put("return_msg", "支付失败");
                return WXPayUtil.mapToXml(return_data);
            }
            try {
                String attach = params.get("attach");
                String total_fee = params.get("total_fee");
                //微信支付订单号
                String transaction_id = params.get("transaction_id");
                Long orderId = Long.parseLong(attach);
                if (!"SUCCESS".equals(params.get("result_code"))){

                    accountService.payFail(orderId,transaction_id);
                    return_data.put("return_code", "SUCCESS");
                    return_data.put("return_msg", "SUCCESS");
                    return WXPayUtil.mapToXml(return_data);
                }
                return_data = accountService.paySuccess(orderId,transaction_id,total_fee);
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

    /**
     * 支付宝回调
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "notifyAliPay", produces = MediaType.APPLICATION_JSON_VALUE)
    public String notifyAliPay(HttpServletRequest request) throws Exception {
        //获取支付宝POST过来反馈信息
        Map<String,String> params = new HashMap<>();
        Map<String, String[]> requestParams = request.getParameterMap();
        log.info("支付宝回调参数:{}",requestParams);
        for (Iterator iter = requestParams.keySet().iterator(); iter.hasNext();) {
            String name = (String) iter.next();
            String[] values = requestParams.get(name);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            //乱码解决，这段代码在出现乱码时使用。
            //valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            params.put(name, valueStr);
        }
        log.info("支付宝回调参数:{}",params);
        Boolean verifyNotify = Factory.Payment.Common().verifyNotify(params);
        if (!verifyNotify){
            log.error("支付宝支付回调签名不正确");
            throw new BusinessException(ErrorMsgEnum.ALIPAY_ERROR.getValue(),ErrorMsgEnum.ALIPAY_ERROR.getDesc());
        }
        //商户订单号
        String outTradeNo = request.getParameter("out_trade_no");
        //交易状态
        String tradeStatus = request.getParameter("trade_status");
        //资金总额
        String totalAmount = request.getParameter("total_amount");
        //卖家支付宝账户
        String sellerId = request.getParameter("seller_id");
        //买家支付宝账户
        String buyerLogonId = request.getParameter("buyer_logon_id");
        //签名
        String sign = request.getParameter("sign");

        if (StringUtils.isBlank(outTradeNo)||StringUtils.isBlank(tradeStatus)||StringUtils.isBlank(totalAmount)){
            return "0";
        }

        accountService.aliPayOrderCallBack(outTradeNo,tradeStatus,new BigDecimal(totalAmount),buyerLogonId);

        return "0";

    }

    /**
     * 内容审核回调
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "reviewImage", produces = MediaType.APPLICATION_JSON_VALUE)
    public String reviewImage(HttpServletRequest request) {

        String bodyString = HttpHelper.getBodyString(request);
        if (StringUtils.isBlank(bodyString)){
            return "200";
        }
        try {
            JSONObject obj = JSONObject.parseObject(bodyString);
            Integer code = obj.getInteger("code");
            if (code != null && code == 0) {
                JSONObject data = obj.getJSONObject("data");
                if (data != null && !data.isEmpty()) {
                    Integer forbidden_status = data.getInteger("forbidden_status");
                    String trace_id = data.getString("trace_id");
                    String url = data.getString("url");
                    Integer result = data.getInteger("result");
                    JSONObject porn_info = data.getJSONObject("porn_info");
                    JSONObject terrorist_info = data.getJSONObject("terrorist_info");
                    JSONObject politics_info = data.getJSONObject("politics_info");
                    JSONObject ads_info = data.getJSONObject("ads_info");
                    FuntimeImgeCallback imgeCallback = new FuntimeImgeCallback();
                    if (porn_info!=null) {
                        Integer porn_info_hit_flag = porn_info.getInteger("porn_info_hit_flag");
                        Integer porn_info_score = porn_info.getInteger("porn_info_score");
                        Integer porn_info_count = porn_info.getInteger("porn_info_count");
                        String porn_info_label = porn_info.getString("porn_info_label");
                        imgeCallback.setPornInfoCount(porn_info_count);
                        imgeCallback.setPornInfoHitFlag(porn_info_hit_flag);
                        imgeCallback.setPornInfoLabel(porn_info_label);
                        imgeCallback.setPornInfoScore(porn_info_score);
                    }
                    if (terrorist_info!=null) {
                        Integer terrorist_info_hit_flag = terrorist_info.getInteger("terrorist_info_hit_flag");
                        Integer terrorist_info_score = terrorist_info.getInteger("terrorist_info_score");
                        Integer terrorist_info_count = terrorist_info.getInteger("terrorist_info_count");
                        String terrorist_info_label = terrorist_info.getString("terrorist_info_label");
                        imgeCallback.setTerroristInfoCount(terrorist_info_count);
                        imgeCallback.setTerroristInfoHitFlag(terrorist_info_hit_flag);
                        imgeCallback.setTerroristInfoLabel(terrorist_info_label);
                        imgeCallback.setTerroristInfoScore(terrorist_info_score);
                    }
                    if (politics_info!=null) {
                        Integer politics_info_hit_flag = politics_info.getInteger("politics_info_hit_flag");
                        Integer politics_info_score = politics_info.getInteger("politics_info_score");
                        Integer politics_info_count = politics_info.getInteger("politics_info_count");
                        String politics_info_label = politics_info.getString("politics_info_label");
                        imgeCallback.setPoliticsInfoCount(politics_info_count);
                        imgeCallback.setPoliticsInfoHitFlag(politics_info_hit_flag);
                        imgeCallback.setPoliticsInfoLabel(politics_info_label);
                        imgeCallback.setPoliticsInfoScore(politics_info_score);
                    }
                    if (ads_info!=null) {
                        Integer ads_info_hit_flag = ads_info.getInteger("ads_info_hit_flag");
                        Integer ads_info_score = ads_info.getInteger("ads_info_score");
                        Integer ads_info_count = ads_info.getInteger("ads_info_count");
                        String ads_info_label = ads_info.getString("ads_info_label");
                        imgeCallback.setAdsInfoCount(ads_info_count);
                        imgeCallback.setAdsInfoHitFlag(ads_info_hit_flag);
                        imgeCallback.setAdsInfoLabel(ads_info_label);
                        imgeCallback.setAdsInfoScore(ads_info_score);
                    }

                    imgeCallback.setForbiddenStatus(forbidden_status);
                    imgeCallback.setResult(result);
                    imgeCallback.setTraceId(trace_id);
                    imgeCallback.setUrl(url);
                    userService.insertFuntimeImgeCallback(imgeCallback);
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "200";
    }

    @GetMapping(value = "gdt")
    public JSONObject gdt(@RequestParam(required = false) Map<String, Object> params) {

        JSONObject result = new JSONObject();
        result.put("ret",0);
        result.put("msg","sucess");
        JSONObject obj = new JSONObject(params);
        FuntimeTencentAd tencentAd = JSONObject.toJavaObject(obj,FuntimeTencentAd.class);

        advertisService.saveTencentAd(tencentAd);

        return result;

    }
    @GetMapping(value = "gdtMonitor")
    public JSONObject gdtMonitor(@RequestParam(required = false) Map<String, Object> params) {

        JSONObject result = new JSONObject();
        result.put("ret",0);
        result.put("msg","sucess");
        JSONObject obj = new JSONObject(params);
        FuntimeTencentAdMonitor tencentAd = JSONObject.toJavaObject(obj,FuntimeTencentAdMonitor.class);

        advertisService.saveTencentAdMonitor(tencentAd);

        return result;

    }

}
