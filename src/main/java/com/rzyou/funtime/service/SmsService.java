package com.rzyou.funtime.service;

import com.rzyou.funtime.entity.FuntimeSms;

public interface SmsService {

    void sendSms(String phone);

    void validateSms(String phone,String code);

}
