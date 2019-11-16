package com.rzyou.funtime.service;

import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.entity.*;

import java.math.BigDecimal;
import java.util.Map;

public interface AccountService {
    void recharge(FuntimeUserAccountRechargeRecord record);

    void paySuccess(Long orderId);

    PageInfo<FuntimeUserAccountRechargeRecord> getRechargeDetailForPage(Integer startPage, Integer pageSize, String queryDate, Integer state, Long userId);

    void createRedpacket(FuntimeUserRedpacket redpacket);

    Map<String,Object> grabRedpacket(Long userId, Long redpacketId);

    PageInfo<FuntimeUserRedpacket> getRedpacketOfSendForPage(Integer startPage, Integer pageSize, String queryDate, Long userId);

    PageInfo<FuntimeUserAccountRedpacketRecord> getRedpacketOfRecieveForPage(Integer startPage, Integer pageSize, String queryDate, Long userId);

    Long giftTrans(Long userId, Long toUserId, Integer giftId, Integer giftNum,String operationDesc,Integer giveChannelId);

    void updateStateForInvalid();

    void diamondConvert(Long userId, String from, String to, BigDecimal amount,Integer convertType);

    PageInfo<FuntimeUserConvertRecord> getUserConvertRecordForPage(Integer startPage, Integer pageSize, Long userId, String queryDate,Integer convertType);

    PageInfo<FuntimeUserAccountGifttransRecord> getGiftOfSendForPage(Integer startPage, Integer pageSize, String queryDate, Long userId);

    PageInfo<FuntimeUserAccountGifttransRecord> getGiftOfRecieveForPage(Integer startPage, Integer pageSize, String queryDate, Long userId);

    void applyWithdrawal(Long userId,Integer withdrawalType, BigDecimal blackAmount);

    PageInfo<FuntimeUserAccountWithdrawalRecord> getWithdrawalForPage(Integer startPage, Integer pageSize, String queryDate, Integer state, Long userId);

}
