package com.rzyou.funtime.common.sms.ouyi;


import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.OuyiSmsTemplate;
import com.rzyou.funtime.common.sms.ouyi.utils.HttpClient;
import com.rzyou.funtime.utils.StringUtil;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;
@Slf4j
public class OuyiSmsUtil {

    //用户名  实际使用时应该从session中获取
    static String username = "610581";
    //密码 实际使用时应该从session中得到的用户名中获取
    static String password = "5811114";
    //原地址(扩展码 可不填 前端获取 测试时为方便定义)
    //static String source_address = "107";
    //外部编码 (有需求是可获取)
    long external_id = 0x1L;
    //发送短信方法
    static String mt = "mt";

    //单一内容下发&单一内容群发
    public static void sengSindleSMS(String phone, int smsType,String code) {
        //以下参数均由前端页面传来
        String result = "";
        String content = OuyiSmsTemplate.getDescByValue(smsType).replace("#",code);
        //因做测试 故原地址与外部编码不参与测试 实际情况有需要从前端定义参数获取
        try {
            //做URLEncoder - UTF-8编码
            String sm = URLEncoder.encode(content, "utf8");
            //将参数进行封装
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("un", username);
            paramMap.put("pw", password);
            //paramMap.put("sa",source_address);

            //单一内容时群发  将手机号用;隔开
            paramMap.put("da", phone);
            paramMap.put("sm", sm);
            //发送POST请求
            result = HttpClient.SendPOST(mt,paramMap);
            log.debug("ouyi sms result : {}",result);
            JSONObject resultObj = JSONObject.parseObject(result);
            if(!resultObj.getBoolean("success")){
                throw new BusinessException(ErrorMsgEnum.USER_SMS_FAIL.getValue(),ErrorMsgEnum.USER_SMS_FAIL.getDesc());
            }
        } catch (UnsupportedEncodingException e) {
            throw new BusinessException(ErrorMsgEnum.USER_SMS_FAIL.getValue(),ErrorMsgEnum.USER_SMS_FAIL.getDesc());
        }

    }

    public static void sengSindleSMS(String phone, String content) {
        //以下参数均由前端页面传来
        String result = "";
       // String content = OuyiSmsTemplate.getDescByValue(smsType).replace("#",code);
        //因做测试 故原地址与外部编码不参与测试 实际情况有需要从前端定义参数获取
        try {
            //做URLEncoder - UTF-8编码
            String sm = URLEncoder.encode(content, "utf8");
            //将参数进行封装
            Map<String, Object> paramMap = new HashMap<>();
            paramMap.put("un", username);
            paramMap.put("pw", password);
            //paramMap.put("sa",source_address);

            //单一内容时群发  将手机号用;隔开
            paramMap.put("da", phone);
            paramMap.put("sm", sm);
            //发送POST请求
            result = HttpClient.SendPOST(mt,paramMap);
            log.debug("ouyi sms result : {}",result);
            JSONObject resultObj = JSONObject.parseObject(result);
            if(!resultObj.getBoolean("success")){
                throw new BusinessException(ErrorMsgEnum.USER_SMS_FAIL.getValue(),ErrorMsgEnum.USER_SMS_FAIL.getDesc());
            }
        } catch (UnsupportedEncodingException e) {
            throw new BusinessException(ErrorMsgEnum.USER_SMS_FAIL.getValue(),ErrorMsgEnum.USER_SMS_FAIL.getDesc());
        }

    }
}
