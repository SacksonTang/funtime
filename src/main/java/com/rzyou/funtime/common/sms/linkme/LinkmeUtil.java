package com.rzyou.funtime.common.sms.linkme;

import com.alibaba.fastjson.JSON;

import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.SmsType;
import lombok.extern.slf4j.Slf4j;

import org.apache.commons.lang3.StringUtils;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;
import java.util.TreeMap;

@Slf4j
public class LinkmeUtil {

    public static String appKey = "270008c663a081d1e8c860281c4a6a55";

    public static String sign_name = "触娱";

    public static String status_callback_url = "www.baidu.com";

    public static String extend = "id";

    public static String RSA_Private_key = "MIICdgIBADANBgkqhkiG9w0BAQEFAASCAmAwggJcAgEAAoGBANpDxsS6qzc5F6ER" +
            "xpnU7qW7YGk6qLL7LnVF7zvPkBXHyMeH/Sd8WDC4yafmXgMBumK3DCCLmF0vseJZ" +
            "T013ZkNvP0flkjLEzlJyLvZMbibA5e2B5ZvbGt/41gXPhpSb5sFykJ45i+io4d++" +
            "pESkQZ0fU1g2XSdeCBDDzIo7avUhAgMBAAECgYBw4y0jAwka/sRh4je9yIvF3Cv5" +
            "QQWPzKoyrYEWhjwXh8UorgUZLw7N5EUoOdXV6EbmV5ZGHu0nBUwTre1+O4r/0SVz" +
            "Jm1Q8hNkvPahrOcnByBWF+svXn5ryn7Egh4azke+XPF/gf3TeSd5NLoPYUx7+MP9" +
            "Kf8UuxEod0Hoec1oAQJBAPMFZ+ecG3nMtAAKHlRM1+fzBNx/8yJ4tbpW/eHx5TGb" +
            "sUkDD2wETNrhz0+II8d66KyCwTyNLrgp0DqJHNChERECQQDl6+aMbarId1OFsdBj" +
            "2lhYrojB596p6Fsd+IsX6mJYsRPfepzauLBa8Cfn3XETL350JPOs7Qr/2YM8ofyl" +
            "K6MRAkBswpSX0QNy5SwBgIXGUIWn5tjcHd8gJEmgVWJWBj3+j6Et/dKfEuWaZ8ix" +
            "3Um18snCutnkUYMBJKVuLQLaU9shAkEAzp6bVhFry7EoVto/yqw6fp+CeLc163zK" +
            "/XkRDpHshYXEtS2L7ibRHTf6tKzU5AfnXNqkPP/cxaWkAYYU9B3t8QJAQee0Dznt" +
            "yj2NziM3zzLIHu6ssCwB0vH7n0aTpiEUAzC9XCMDIJlmqKHJUAKmi95YB4KHLK9f" +
            "bK5CSZ5QsvYkiA==";
    public static String public_key = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQDaQ8bEuqs3ORehEcaZ1O6lu2Bp" +
            "Oqiy+y51Re87z5AVx8jHh/0nfFgwuMmn5l4DAbpitwwgi5hdL7HiWU9Nd2ZDbz9H" +
            "5ZIyxM5Sci72TG4mwOXtgeWb2xrf+NYFz4aUm+bBcpCeOYvoqOHfvqREpEGdH1NY" +
            "Nl0nXggQw8yKO2r1IQIDAQAB";

    public final static String smsSingleRequestServerUrl = "https://account.linkedme.cc/sms/text/send";
    public final static String getPhoneRequestServerUrl = "https://account.linkedme.cc/phone/info";


    public static String getPhone(String token,Integer channel,Integer platform,String code){
        String sign;
        GetPhoneRequest request;
        if (code==null){
            sign = getSign(channel,platform,token);
            request = new GetPhoneRequest(appKey,channel.toString(),platform.toString(),token,sign);
        }else {
            sign = getSign(channel,platform,token,code);
            request = new GetPhoneRequest(appKey,channel.toString(),platform.toString(),token,code,sign);
        }

        String requestJson = JSONObject.toJSONString(request);

        log.info("before request string is: {}" , requestJson);
        String response = sendSmsByPost(getPhoneRequestServerUrl, requestJson);
        log.info("response after request result is : {}" , response);

        JSONObject resultObj = JSONObject.parseObject(response);
        if (resultObj==null||resultObj.getJSONObject("header")==null||resultObj.getJSONObject("header").getInteger("code")!=200){
            throw new BusinessException(ErrorMsgEnum.USER_LOGIN_ONEKEY_ERROR.getValue(),ErrorMsgEnum.USER_LOGIN_ONEKEY_ERROR.getDesc());
        }
        String phoneStr = resultObj.getString("body");
        return RsaUtils.decryptHexData(phoneStr,RSA_Private_key);

    }

    private static String getSign(Integer channel, Integer platform, String token) {
        Map<String, String> paramsTreeMap = new TreeMap<>();
        paramsTreeMap.put("app_key",appKey);
        paramsTreeMap.put("channel",channel.toString());
        paramsTreeMap.put("platform",platform.toString());
        paramsTreeMap.put("token",token);

        return RsaUtils.getHexSign(paramsTreeMap,RSA_Private_key);
    }

    private static String getSign(Integer channel, Integer platform, String token,String code) {
        Map<String, String> paramsTreeMap = new TreeMap<>();
        paramsTreeMap.put("app_key",appKey);
        paramsTreeMap.put("auth_code",code);
        paramsTreeMap.put("channel",channel.toString());
        paramsTreeMap.put("platform",platform.toString());
        paramsTreeMap.put("token",token);
        String sign = RsaUtils.getHexSign(paramsTreeMap, RSA_Private_key);
        return sign;
    }

    private static boolean verifyHexSign(Map<String, String> paramsTreeMap,String sign){
        boolean b = RsaUtils.verifyHexSign(paramsTreeMap, public_key, sign);
        return b;
    }

    public static void sendSms(String phone,String code,int smsType){

        String template_id = LinkmeSmsType.getDescByValue(smsType);
        String[] template_params = {code};

        String sign = getSign(phone,template_id,template_params);


        SmsSendRequest smsSingleRequest = new SmsSendRequest(appKey,phone,sign_name,template_id,template_params,status_callback_url,sign,extend);

        String requestJson = JSON.toJSONString(smsSingleRequest);

        log.debug("before request string is: {}" , requestJson);
        String response = sendSmsByPost(smsSingleRequestServerUrl, requestJson);

        log.debug("response after request result is : {}" , response);



    }

    public static String sendSmsByPost(String path, String postContent) {
        URL url = null;
        try {
            url = new URL(path);
            HttpURLConnection httpURLConnection = (HttpURLConnection) url.openConnection();
            httpURLConnection.setRequestMethod("POST");// 提交模式
            httpURLConnection.setConnectTimeout(10000);//连接超时 单位毫秒
            httpURLConnection.setReadTimeout(10000);//读取超时 单位毫秒
            // 发送POST请求必须设置如下两行
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setRequestProperty("Charset", "UTF-8");
            httpURLConnection.setRequestProperty("Content-Type", "application/json");


            httpURLConnection.connect();
            OutputStream os=httpURLConnection.getOutputStream();
            os.write(postContent.getBytes("UTF-8"));
            os.flush();

            StringBuilder sb = new StringBuilder();
            int httpRspCode = httpURLConnection.getResponseCode();
            if (httpRspCode == HttpURLConnection.HTTP_OK) {
                // 开始获取数据
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(httpURLConnection.getInputStream(), "utf-8"));
                String line = null;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();
                return sb.toString();

            }

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String getSign(String recipient,String template_id,String[] template_params){

        Map<String, String> paramsTreeMap = new TreeMap<>();
        paramsTreeMap.put("app_key",appKey);
        paramsTreeMap.put("extend",extend);
        paramsTreeMap.put("recipient",recipient);
        paramsTreeMap.put("sign_name",sign_name);
        paramsTreeMap.put("status_callback_url",status_callback_url);
        paramsTreeMap.put("template_id",template_id);
        paramsTreeMap.put("template_params", StringUtils.join(template_params,","));

        return RsaUtils.getHexSign(paramsTreeMap,RSA_Private_key);
    }


    enum LinkmeSmsType{
        REGISTER(SmsType.REGISTER_LOGIN.getValue(),"110240"),
        UPDATE_PHONENUMBER(SmsType.UPDATE_PHONENUMBER.getValue(),"110240"),
        WITHDRAWAL(SmsType.WITHDRAWAL.getValue(),"110240");
        int value;
        String templateId;

        LinkmeSmsType(int value, String templateId) {
            this.value = value;
            this.templateId = templateId;
        }
        public static String getDescByValue(int val){
            for (LinkmeSmsType template:LinkmeSmsType.values()){
                if (template.value==val){
                    return template.templateId;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }

        public void setValue(int value) {
            this.value = value;
        }

        public String getTemplateId() {
            return templateId;
        }

        public void setTemplateId(String templateId) {
            this.templateId = templateId;
        }
    }

}
