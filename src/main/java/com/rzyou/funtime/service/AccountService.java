package com.rzyou.funtime.service;

import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.entity.FuntimeUserAccountRechargeRecord;
import com.rzyou.funtime.entity.FuntimeUserAccountRedpacketRecord;
import com.rzyou.funtime.entity.FuntimeUserRedpacket;
import org.apache.ibatis.annotations.Param;

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

}
