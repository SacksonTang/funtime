package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.entity.FuntimeSms;
import com.rzyou.funtime.mapper.FuntimeSmsMapper;
import com.rzyou.funtime.service.SmsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    FuntimeSmsMapper funtimeSmsMapper;

    @Override
    public void sendSms(String phone) {

    }

    @Override
    public void validateSms(String phone, String code) {
        FuntimeSms sms = funtimeSmsMapper.querySmsByMobile(phone,code);
        if(sms==null){
            throw new BusinessException(ErrorMsgEnum.SMS_NOT_EXISTS.getValue(),ErrorMsgEnum.SMS_NOT_EXISTS.getDesc());
        }
        if(sms.getIsUsed().intValue()==1){
            throw new BusinessException(ErrorMsgEnum.SMS_IS_USED.getValue(),ErrorMsgEnum.SMS_IS_USED.getDesc());
        }

        if(new Date().after(sms.getExpireTime())){
            throw new BusinessException(ErrorMsgEnum.SMS_IS_EXPIRE.getValue(),ErrorMsgEnum.SMS_IS_EXPIRE.getDesc());
        }
    }
}
