package com.rzyou.funtime.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.OperationType;
import com.rzyou.funtime.common.PayState;
import com.rzyou.funtime.entity.FuntimeRechargeConf;
import com.rzyou.funtime.entity.FuntimeUserAccountBlueLog;
import com.rzyou.funtime.entity.FuntimeUserAccountHornLog;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.utils.StringUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class AccountServiceImpl implements AccountService {
    private static Logger log = LoggerFactory.getLogger(AccountServiceImpl.class);
    @Autowired
    FuntimeUserAccountRechargeRecordMapper userAccountRechargeRecordMapper;
    @Autowired
    FuntimeUserAccountBlueLogMapper userAccountBlueLogMapper;
    @Autowired
    FuntimeUserAccountMapper userAccountMapper;
    @Autowired
    FuntimeUserAccountHornLogMapper userAccountHornLogMapper;
    @Autowired
    FuntimeRechargeConfMapper rechargeConfMapper;

    @Override
    @Transactional
    public void recharge(FuntimeUserAccountRechargeRecord record){
        FuntimeRechargeConf rechargeConf = rechargeConfMapper.selectByPrimaryKey(record.getRechargeConfId());
        if (rechargeConf==null){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_CONF_NOT_EXISTS.getValue(),ErrorMsgEnum.RECHARGE_CONF_NOT_EXISTS.getDesc());
        }
        record.setRmb(rechargeConf.getRechargeRmb());
        record.setHornNum(rechargeConf.getHornNum());
        record.setAmount(rechargeConf.getRechargeNum());
        String orderNo = "A"+StringUtil.createOrderId();
        saveAccountRechargeRecord(record,System.currentTimeMillis(),PayState.START.getValue(), orderNo);

    }

    @Override
    @Transactional
    public void paySuccess(Long orderId) {
        FuntimeUserAccountRechargeRecord record = userAccountRechargeRecordMapper.selectByPrimaryKey(orderId);
        if(record==null){
            throw new BusinessException(ErrorMsgEnum.ORDER_NOT_EXISTS.getValue(),ErrorMsgEnum.ORDER_NOT_EXISTS.getDesc());
        }
        if (PayState.PAIED.getValue().equals(record.getState())){
            return;
        }else if (PayState.START.getValue().equals(record.getState())||PayState.FAIL.getValue().equals(record.getState())) {
            //状态变更
            updateState(orderId, PayState.PAIED.getValue());

            //增加用户账户钻石和喇叭
            userAccountMapper.updateUserAccountForPlus(orderId,null,record.getAmount(),record.getHornNum());

            //记录日志
            saveUserAccountBlueLog(record.getUserId(),record.getAmount(),record.getId()
                    , OperationType.RECHARGE.getAction(),OperationType.RECHARGE.getOperationType());
            if(record.getHornNum()!=null&&record.getHornNum()>0){
                saveUserAccountHornLog(record.getUserId(),record.getHornNum(),record.getId()
                        , OperationType.RECHARGE.getAction(),OperationType.RECHARGE.getOperationType());
            }
        }else{
            log.info("订单号: {} 的订单已失效,请重新下单",record.getOrderNo());
            throw new BusinessException(ErrorMsgEnum.ORDER_IS_INVALID.getValue(),ErrorMsgEnum.ORDER_IS_INVALID.getDesc());
        }

    }

    @Override
    public PageInfo<FuntimeUserAccountRechargeRecord> getRechargeDetailForPage(Integer startPage, Integer pageSize, String queryDate, Integer state, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        return new PageInfo<>(null);

    }

    public void saveUserAccountHornLog(Long userId, Integer amount,Long recordId,String actionType,String operationType){
        FuntimeUserAccountHornLog hornLog = new FuntimeUserAccountHornLog();
        hornLog.setUserId(userId);
        hornLog.setAmount(amount);
        hornLog.setRelationId(recordId);
        hornLog.setActionType(actionType);
        hornLog.setOperationType(operationType);
        int k = userAccountHornLogMapper.insertSelective(hornLog);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    public void saveUserAccountBlueLog(Long userId, BigDecimal amount,Long recordId,String actionType,String operationType){
        FuntimeUserAccountBlueLog blueLog = new FuntimeUserAccountBlueLog();
        blueLog.setUserId(userId);
        blueLog.setAmount(amount);
        blueLog.setRelationId(recordId);
        blueLog.setActionType(actionType);
        blueLog.setOperationType(operationType);
        int k = userAccountBlueLogMapper.insertSelective(blueLog);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }


    public void saveAccountRechargeRecord(FuntimeUserAccountRechargeRecord rechargeRecord,Long version, Integer state,String orderNo){

        rechargeRecord.setState(state);
        rechargeRecord.setVersion(version);
        rechargeRecord.setOrderNo(orderNo);

        int k = userAccountRechargeRecordMapper.insertSelective(rechargeRecord);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    public void updateState(Long id,Integer state){
        FuntimeUserAccountRechargeRecord record = new FuntimeUserAccountRechargeRecord();
        record.setId(id);
        record.setState(state);
        int k = userAccountRechargeRecordMapper.updateByPrimaryKeySelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }




}
