package com.rzyou.funtime.common.sms.tencent;
import com.alibaba.fastjson.JSONObject;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.Constant;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.SmsType;
import com.tencentcloudapi.common.Credential;
import com.tencentcloudapi.common.profile.ClientProfile;
import com.tencentcloudapi.common.profile.HttpProfile;
import com.tencentcloudapi.common.exception.TencentCloudSDKException;

import com.tencentcloudapi.sms.v20190711.SmsClient;
import com.tencentcloudapi.sms.v20190711.models.*;
import lombok.extern.slf4j.Slf4j;

@Slf4j
public class TencentUtil {


    public static void sendSMS(String phone, int smsType,String code) {
        SendSmsResponse resp;
        try {
            Credential cred = new Credential(Constant.TENCENT_YUN_SECRETID, Constant.TENCENT_YUN_SECRETKEY);

            HttpProfile httpProfile = new HttpProfile();
            httpProfile.setEndpoint(Constant.TENCENT_SMS_URL);

            ClientProfile clientProfile = new ClientProfile();
            clientProfile.setHttpProfile(httpProfile);

            SmsClient client = new SmsClient(cred, "", clientProfile);

            SendSmsRequest req = new SendSmsRequest();
            String[] phoneNumberSet = {phone};
            req.setPhoneNumberSet(phoneNumberSet);


            String[] templateParamSet = {code};
            req.setTemplateParamSet(templateParamSet);

            req.setTemplateID(TencentSmsType.getDescByValue(smsType));
            req.setSmsSdkAppid(Constant.TENCENT_SMS_SDKID);
            req.setSign("触娱语音APP");
            resp = client.SendSms(req);
        }catch (TencentCloudSDKException e) {
            throw new BusinessException(ErrorMsgEnum.USER_SMS_FAIL);
        }
        if (resp == null||!"Ok".equals(resp.getSendStatusSet()[0].getCode())){
            log.error("短信发送失败=========>{}", JSONObject.toJSONString(resp));
        }
    }
    enum TencentSmsType{
        REGISTER(SmsType.REGISTER_LOGIN.getValue(),"781043"),
        UPDATE_PHONENUMBER(SmsType.UPDATE_PHONENUMBER.getValue(),"781046"),
        REAL_VALID(SmsType.REAL_VALID.getValue(),"781044"),
        BIND_PHONENUMBER(SmsType.BIND_PHONENUMBER.getValue(),"781047"),
        USERCANCELATION(SmsType.USERCANCELATION.getValue(),"781048");
        int value;
        String templateId;

        TencentSmsType(int value, String templateId) {
            this.value = value;
            this.templateId = templateId;
        }
        public static String getDescByValue(int val){
            for (TencentSmsType template:TencentSmsType.values()){
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

    public static void main(String[] args) {
        sendSMS("+8613246769301",1,"123456");
    }
}
