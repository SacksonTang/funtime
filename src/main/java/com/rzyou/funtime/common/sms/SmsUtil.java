package com.rzyou.funtime.common.sms;

import com.alibaba.fastjson.JSON;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.sms.chuanglan.model.request.SmsSendRequest;
import com.rzyou.funtime.common.sms.chuanglan.model.response.SmsSendResponse;
import com.rzyou.funtime.common.sms.chuanglan.util.ChuangLanSmsUtil;
import com.rzyou.funtime.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SmsUtil {

    private static Logger log = LoggerFactory.getLogger(SmsUtil.class);

    public static final String charset = "utf-8";
    // 用户平台API账号(非登录账号,示例:N1234567)
    public static String account = "YZM7027355";
    // 用户平台API密码(非登录密码)
    public static String password = "DWfSRz3CV55108";

    public final static String smsSingleRequestServerUrl = "http://smssh1.253.com/msg/send/json";

    public static void sendSms(String phone){

        //状态报告
        String report= "true";

        String msg = "【触娱】您好，您的验证码是: "+ StringUtil.createRandom(true, 6);

        SmsSendRequest smsSingleRequest = new SmsSendRequest(account, password, msg, phone,report);

        String requestJson = JSON.toJSONString(smsSingleRequest);

        log.info("before request string is: {}" , requestJson);

        String response = ChuangLanSmsUtil.sendSmsByPost(smsSingleRequestServerUrl, requestJson);

        log.info("response after request result is : {}" , response);

        SmsSendResponse smsSingleResponse = JSON.parseObject(response, SmsSendResponse.class);

        if(!"0".equals(smsSingleResponse.getCode())){
            throw new BusinessException(smsSingleResponse.getCode(),smsSingleResponse.getErrorMsg());
        }


    }

}
