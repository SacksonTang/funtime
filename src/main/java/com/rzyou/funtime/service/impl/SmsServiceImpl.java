package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.sms.linkme.LinkmeUtil;
import com.rzyou.funtime.common.sms.ouyi.OuyiSmsUtil;
import com.rzyou.funtime.entity.FuntimeSms;
import com.rzyou.funtime.mapper.FuntimeSmsMapper;
import com.rzyou.funtime.service.SmsService;
import com.rzyou.funtime.utils.StringUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;

@Service
public class SmsServiceImpl implements SmsService {

    @Autowired
    FuntimeSmsMapper funtimeSmsMapper;

    @Override
    public void sendSms(String phone, String resend,String ip,int smsType) {

        String code = StringUtil.createRandom(true,6);
        if ("1".equals(resend)){
            OuyiSmsUtil.sengSindleSMS(phone,smsType,code);
        }else{
            LinkmeUtil.sendSms(phone,code,smsType);
        }

        saveSms(phone,code,null,ip,smsType);

    }

    public void saveSms(String phone,String code,String msg,String ip,int smsType){
        FuntimeSms sms = new FuntimeSms();
        sms.setIsUsed(2);
        sms.setCreateTime(new Date());
        sms.setExpireTime(DateUtils.addMinutes(new Date(),5));
        sms.setIsSended(2);
        sms.setIp(ip);
        sms.setMobileNumber(phone);
        sms.setSmsType(smsType);
        sms.setSms(msg);
        sms.setValidateCode(code);
        int k = funtimeSmsMapper.insertSelective(sms);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
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
