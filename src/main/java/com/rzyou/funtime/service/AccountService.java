package com.rzyou.funtime.service;

import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AccountService {

    /**
     * 查询充值记录
     * @param id
     * @return
     */
    FuntimeUserAccountRechargeRecord getRechargeRecordById(Long id);

    /**
     * 生成充值记录
     * @param record
     * @return
     */
    Map<String,Object> createRecharge(FuntimeUserAccountRechargeRecord record);

    /**
     * 订单回调
     * @param orderId
     * @return
     */
    Map<String,String> paySuccess(Long orderId);

    /**
     * 充值记录列表
     * @param startPage
     * @param pageSize
     * @param queryDate
     * @param state
     * @param userId
     * @return
     */
    PageInfo<FuntimeUserAccountRechargeRecord> getRechargeDetailForPage(Integer startPage, Integer pageSize, String queryDate, Integer state, Long userId);

    /**
     * 创建红包
     * @param redpacket
     */
    void createRedpacket(FuntimeUserRedpacket redpacket);

    /**
     * 抢红包
     * @param userId
     * @param redpacketId
     * @return
     */
    ResultMsg<Object> grabRedpacket(Long userId, Long redpacketId);

    /**
     * 发出的红包明细
     * @param startPage
     * @param pageSize
     * @param queryDate
     * @param userId
     * @return
     */
    PageInfo<FuntimeUserRedpacket> getRedpacketOfSendForPage(Integer startPage, Integer pageSize, String queryDate, Long userId);

    /**
     * 收到的红包明细
     * @param startPage
     * @param pageSize
     * @param queryDate
     * @param userId
     * @return
     */
    PageInfo<FuntimeUserAccountRedpacketRecord> getRedpacketOfRecieveForPage(Integer startPage, Integer pageSize, String queryDate, Long userId);

    /**
     * 查询发出的红包被抢数
     * @param userId
     * @param queryDate
     * @return
     */
    BigDecimal querySnedSumAmountByGrab(Long userId, String queryDate);

    /**
     * 查询抢到的红包
     * @param userId
     * @param queryDate
     * @return
     */
    BigDecimal getSumGrabAmountById(Long userId, String queryDate);

    /**
     * 查询收到的红包的标签
     * @param userId
     * @param queryDate
     * @return
     */
    List<Map<String,Object>> getSumGrabTagsById(Long userId, String queryDate);


    /**
     * 送礼物
     * @param userId
     * @param toUserIds
     * @param giftId
     * @param giftNum
     * @param operationDesc
     * @param giveChannelId
     * @param roomId
     * @return
     */
    ResultMsg<Object> createGiftTrans(Long userId, String toUserIds, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannelId, Long roomId);

    /**
     * 红包失效更改
     */
    void updateStateForInvalid();

    /**
     * 钻石兑换
     * @param userId
     * @param from
     * @param to
     * @param amount
     * @param convertType
     */
    void diamondConvert(Long userId, String from, String to, BigDecimal amount,Integer convertType);

    /**
     * 兑换列表
     * @param startPage
     * @param pageSize
     * @param userId
     * @param queryDate
     * @param convertType
     * @return
     */
    PageInfo<FuntimeUserConvertRecord> getUserConvertRecordForPage(Integer startPage, Integer pageSize, Long userId, String queryDate,Integer convertType);

    /**
     * 礼物发送记录
     * @param startPage
     * @param pageSize
     * @param queryDate
     * @param userId
     * @return
     */
    PageInfo<FuntimeUserAccountGifttransRecord> getGiftOfSendForPage(Integer startPage, Integer pageSize, String queryDate, Long userId);

    /**
     * 礼物接受记录
     * @param startPage
     * @param pageSize
     * @param queryDate
     * @param userId
     * @return
     */
    PageInfo<FuntimeUserAccountGifttransRecord> getGiftOfRecieveForPage(Integer startPage, Integer pageSize, String queryDate, Long userId);

    /**
     * 生成领赏记录
     * @param userId
     * @param withdrawalType
     * @param blackAmount
     */
    void applyWithdrawal(Long userId,Integer withdrawalType, BigDecimal blackAmount);

    /**
     * 领赏记录列表
     * @param startPage
     * @param pageSize
     * @param queryDate
     * @param state
     * @param userId
     * @return
     */
    PageInfo<FuntimeUserAccountWithdrawalRecord> getWithdrawalForPage(Integer startPage, Integer pageSize, String queryDate, Integer state, Long userId);

    /**
     * 房间待抢红包列表
     * @param startPage
     * @param pageSize
     * @param roomId
     * @return
     */
    PageInfo<FuntimeUserRedpacket> getRedpacketListByRoomId(Integer startPage, Integer pageSize,Long roomId,Long userId);

    /**
     * 查询红包已抢记录
     * @param redpacketId
     * @return
     */
    List<FuntimeUserAccountRedpacketRecord> getRecordListByRedId(Long redpacketId);

    /**
     * 全房送礼物
     * @param userId
     * @param giftId
     * @param giftNum
     * @param operationDesc
     * @param giveChannel
     * @param roomId
     */
    ResultMsg<Object> createGiftTrans(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId);

    /**
     * 获取充值配置
     * @return
     */
    List<FuntimeRechargeConf> getRechargeConf();
}
