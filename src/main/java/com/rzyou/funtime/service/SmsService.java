package com.rzyou.funtime.service;

public interface SmsService {

    /**
     * 发短信
     * @param phone
     * @param resend
     * @param ip
     * @param smsType
     */
    void sendSms(String phone, String resend,String ip,int smsType);

    /**
     * 校验短信
     * @param type
     * @param phone
     * @param code
     * @return
     */
    Long validateSms(int type, String phone, String code);

    /**
     * 修改短信状态
     * @param smsId
     * @param isUsed
     */
    void updateSmsInfoById(Long smsId, int isUsed);
}
