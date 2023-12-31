package com.rzyou.funtime.controller;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;
import com.alipay.api.internal.util.AlipaySignature;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.common.payment.alipay.config.MyAliPayConfig;
import com.rzyou.funtime.common.payment.wxpay.MyWxPay;
import com.rzyou.funtime.common.payment.wxpay.sdk.WXPayUtil;
import com.rzyou.funtime.common.request.HttpHelper;
import com.rzyou.funtime.component.StaticData;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.entity.dto.SdkParam;
import com.rzyou.funtime.entity.dto.UserStateOfflineParam;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.AdvertisService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.UsersigUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.math.BigDecimal;
import java.util.*;

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
        MyAliPayConfig aliPayConfig = new MyAliPayConfig();
        Boolean verifyNotify = AlipaySignature.rsaCertCheckV1(params,aliPayConfig.getAlipayCertPath(),"utf-8",aliPayConfig.getSignType());
        if (!verifyNotify){
            log.error("支付宝支付回调签名不正确");
            return "sign error";
        }
        //商户订单号
        String outTradeNo = request.getParameter("out_trade_no");
        //交易状态
        String tradeStatus = request.getParameter("trade_status");
        //资金总额
        String totalAmount = request.getParameter("total_amount");
        //支付宝账户流水
        String tradeNo = request.getParameter("trade_no");
        //买家支付宝账户
        String buyerLogonId = request.getParameter("buyer_logon_id");
        //签名
        String sign = request.getParameter("sign");

        if (StringUtils.isBlank(outTradeNo)||StringUtils.isBlank(tradeStatus)||StringUtils.isBlank(totalAmount)){
            return "success";
        }

        accountService.aliPayOrderCallBack(outTradeNo,tradeStatus,new BigDecimal(totalAmount),tradeNo);

        return "success";

    }

    /**
     * 支付宝回调
     * @param request
     * @return
     * @throws Exception
     */
    @RequestMapping(value = "notifyIosReturnPay", produces = MediaType.APPLICATION_JSON_VALUE)
    public String notifyIosReturnPay(HttpServletRequest request) throws Exception {
        //获取苹果退款POST过来反馈信息
        String str = HttpHelper.getBodyString(request);
        if (StringUtils.isBlank(str)){
            return "error";
        }
        JSONObject obj = JSONObject.parseObject(str);
        if(obj==null){
            return "error";
        }
        JSONObject data = obj.getJSONObject("unified_receipt");
        if (data == null){
            return "error";
        }
        JSONArray unified_receipt = data.getJSONArray("latest_receipt_info");
        if (unified_receipt == null||unified_receipt.size() == 0){
            return "error";
        }
        FuntimeAppleRefund funtimeAppleRefund;
        List<FuntimeAppleRefund> list = new ArrayList<>();
        for (int i = 0;i<unified_receipt.size();i++){
            JSONObject o = unified_receipt.getJSONObject(i);
            funtimeAppleRefund = JSONObject.toJavaObject(o,FuntimeAppleRefund.class);
            list.add(funtimeAppleRefund);
        }
        accountService.operateAppleRefund(list);
        return "success";

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
        log.info("reviewImage=======>{}",bodyString);
        if (StringUtils.isBlank(bodyString)){
            return "200";
        }
        try {
            Integer forbidden_status = 0;
            String url = null;
            JSONObject obj = JSONObject.parseObject(bodyString);
            Integer code = obj.getInteger("code");
            if (code != null && code == 0) {
                JSONObject data = obj.getJSONObject("data");
                if (data != null && !data.isEmpty()) {
                    forbidden_status = data.getInteger("forbidden_status");
                    String trace_id = data.getString("trace_id");
                    url = data.getString("url");
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

            if (forbidden_status==1&&StringUtils.isNotBlank(url)){
                String[] array = url.split("/");
                String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_SYSTEMUSER);
                List<String> userIds = new ArrayList<>();
                userIds.add(array[4]);
                FuntimeUser user = userService.getUserInfoByShowId("10000");
                if (user!=null&&!userIds.isEmpty()){
                    TencentUtil.batchsendmsg(userSig,userIds,Constant.PIC_ERROR_INFO,user.getId().toString());
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        return "200";
    }

    /**
     * 广点通
     * @param params
     * @return
     */
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

    /**
     * 广点通监测
     * @param params
     * @return
     */
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

    /**
     * 快手监测链接
     * @param params
     * @return
     */
    @GetMapping(value = "ksMonitor")
    public JSONObject ksMonitor(@RequestParam(required = false) Map<String, Object> params) {

        JSONObject result = new JSONObject();
        result.put("ret",0);
        result.put("msg","sucess");
        JSONObject obj = new JSONObject(params);
        FuntimeKuaishouAdMonitor ad = JSONObject.toJavaObject(obj,FuntimeKuaishouAdMonitor.class);

        if(StringUtils.isNotBlank(ad.getOs())) {
            advertisService.saveKuaishouAdMonitor(ad);
        }

        return result;

    }

    /**
     * 头条监测链接
     * @param params
     * @return
     */
    @GetMapping(value = "ttMonitor")
    public JSONObject ttMonitor(@RequestParam(required = false) Map<String, Object> params) {

        JSONObject result = new JSONObject();
        result.put("status",0);
        JSONObject obj = new JSONObject(params);
        FuntimeToutiaoAdMonitor ad = JSONObject.toJavaObject(obj,FuntimeToutiaoAdMonitor.class);

        advertisService.saveToutiaoAdMonitor(ad);


        return result;

    }

    /**
     * WIFI监测链接
     * @param params
     * @return
     */
    @GetMapping(value = "wifiMonitor")
    public JSONObject wifiMonitor(@RequestParam(required = false) Map<String, Object> params,HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("status",0);
        JSONObject obj = new JSONObject(params);
        FuntimeWifiAdMonitor ad = JSONObject.toJavaObject(obj,FuntimeWifiAdMonitor.class);

        ad.setIp(HttpHelper.getClientIpAddr(request));
        advertisService.saveWifiAdMonitor(ad);
        return result;

    }
    /**
     * 知乎监测链接
     * @param params
     * @return
     */
    @GetMapping(value = "zhihuMonitor")
    public JSONObject zhihuMonitor(@RequestParam(required = false) Map<String, Object> params,HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("status",0);
        JSONObject obj = new JSONObject(params);
        FuntimeZhihuAdMonitor ad = JSONObject.toJavaObject(obj,FuntimeZhihuAdMonitor.class);
        if (StringUtils.isBlank(ad.getIp())) {
            ad.setIp(HttpHelper.getClientIpAddr(request));
        }
        advertisService.saveZhihuAdMonitor(ad);
        return result;

    }
    /**
     * sohu监测链接
     * @param params
     * @return
     */
    @GetMapping(value = "sohuMonitor")
    public JSONObject sohuMonitor(@RequestParam(required = false) Map<String, Object> params,HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("status",true);
        result.put("desc","成功");
        JSONObject obj = new JSONObject(params);
        FuntimeSohuAdMonitor ad = JSONObject.toJavaObject(obj,FuntimeSohuAdMonitor.class);
        if (StringUtils.isBlank(ad.getIp())) {
            ad.setIp(HttpHelper.getClientIpAddr(request));
        }
        advertisService.saveSohuAdMonitor(ad);
        return result;

    }

    /**
     * 美拍监测链接
     * @param params
     * @return
     */
    @GetMapping(value = "meipaiMonitor")
    public JSONObject meipaiMonitor(@RequestParam(required = false) Map<String, Object> params,HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("status",200);
        JSONObject obj = new JSONObject(params);
        FuntimeMeipaiAdMonitor ad = JSONObject.toJavaObject(obj,FuntimeMeipaiAdMonitor.class);
        if (StringUtils.isBlank(ad.getIp())) {
            ad.setIp(HttpHelper.getClientIpAddr(request));
        }
        advertisService.saveMeipaiAdMonitor(ad);
        return result;

    }

    /**
     * B站监测链接
     * @param params
     * @return
     */
    @GetMapping(value = "bstationMonitor")
    public JSONObject bstationMonitor(@RequestParam(required = false) Map<String, Object> params,HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("status",0);
        JSONObject obj = new JSONObject(params);
        FuntimeBstationAdMonitor ad = JSONObject.toJavaObject(obj,FuntimeBstationAdMonitor.class);
        if (StringUtils.isBlank(ad.getIp())) {
            ad.setIp(HttpHelper.getClientIpAddr(request));
        }
        advertisService.saveBstationAdMonitor(ad);
        return result;

    }

    /**
     * 触宝监测链接
     * @param params
     * @return
     */
    @GetMapping(value = "chubaoMonitor")
    public JSONObject chubaoMonitor(@RequestParam(required = false) Map<String, Object> params,HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("status",0);
        JSONObject obj = new JSONObject(params);
        FuntimeChubaoAdMonitor ad = JSONObject.toJavaObject(obj,FuntimeChubaoAdMonitor.class);
        if (StringUtils.isBlank(ad.getIp())) {
            ad.setIp(HttpHelper.getClientIpAddr(request));
        }
        String url = Constant.CHUBAO_CALLBACKURL+"clickid="+ad.getClickid()
                +"&event_type=0"+"&androidid="+ad.getAndroidid()+"&imei="+ad.getImei()
                +"&oaid="+ad.getOaid()+"&pkg="+ad.getPkg()+"&idfa="+ad.getIdfa()+"&ip="+ad.getIp();

        ad.setCallbackUrl(url);
        advertisService.saveChubaoAdMonitor(ad);
        return result;

    }

    /**
     * 最右监测链接
     * @param params
     * @return
     */
    @GetMapping(value = "zuiyouMonitor")
    public JSONObject zuiyouMonitor(@RequestParam(required = false) Map<String, Object> params,HttpServletRequest request) {

        JSONObject result = new JSONObject();
        result.put("status",0);
        JSONObject obj = new JSONObject(params);
        FuntimeZuiyouAdMonitor ad = JSONObject.toJavaObject(obj,FuntimeZuiyouAdMonitor.class);
        if (StringUtils.isBlank(ad.getIp())) {
            ad.setIp(HttpHelper.getClientIpAddr(request));
        }
        advertisService.saveZuiyouAdMonitor(ad);
        return result;

    }

}
