package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.PayState;
import com.rzyou.funtime.common.payment.wxpay.MyWxPay;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.PayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
public class PayServiceImpl implements PayService {
    @Value("app.pay.notifyUrl")
    private String notifyUrl ;
    @Autowired
    AccountService accountService;

    @Override
    public Map<String, String> unifiedOrder(String ip,String imei, String orderId) {
        FuntimeUserAccountRechargeRecord record = accountService.getRechargeRecordById(Long.parseLong(orderId));
        if (record==null){
            throw new BusinessException(ErrorMsgEnum.ORDER_NOT_EXISTS.getValue(),ErrorMsgEnum.ORDER_NOT_EXISTS.getDesc());
        }

        Map<String, String> resultMap = MyWxPay.unifiedOrder(record.getRmb().toString(), ip, record.getOrderNo(), imei, notifyUrl, orderId, "",1);
        if("SUCCESS".equals(resultMap.get("return_code"))){
            accountService.updateRechargeRecordState(record.getId(), PayState.PAYING.getValue());
        }else{
            accountService.updateRechargeRecordState(record.getId(), PayState.FAIL.getValue());
        }
        return resultMap;
    }
}
