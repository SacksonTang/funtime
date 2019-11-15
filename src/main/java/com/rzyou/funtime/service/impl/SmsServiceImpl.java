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
    public Long validateSms(int type, String phone, String code) {
        FuntimeSms sms = funtimeSmsMapper.querySmsByMobile(type,phone,code);
        if(sms==null){
            throw new BusinessException(ErrorMsgEnum.SMS_NOT_EXISTS.getValue(),ErrorMsgEnum.SMS_NOT_EXISTS.getDesc());
        }
        if(sms.getIsUsed().intValue()==1){
            throw new BusinessException(ErrorMsgEnum.SMS_IS_USED.getValue(),ErrorMsgEnum.SMS_IS_USED.getDesc());
        }

        if(new Date().after(sms.getExpireTime())){
            throw new BusinessException(ErrorMsgEnum.SMS_IS_EXPIRE.getValue(),ErrorMsgEnum.SMS_IS_EXPIRE.getDesc());
        }
        return sms.getId();
    }

    @Override
    public void updateSmsInfoById(Long smsId, int isUsed) {
        FuntimeSms sms = new FuntimeSms();
        sms.setId(smsId);
        sms.setIsUsed(isUsed);
        int k = funtimeSmsMapper.updateByPrimaryKeySelective(sms);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

    }
}
