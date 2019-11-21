package com.rzyou.funtime.service;

public interface SmsService {

    void sendSms(String phone, String resend,String ip,int smsType);

    Long validateSms(int type, String phone, String code);

    void updateSmsInfoById(Long smsId, int isUsed);
}
