package com.rzyou.funtime.service;

import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.ResultMsg;
import com.rzyou.funtime.entity.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public interface AccountService {

    /**
     * 同步腾讯头像框
     * @param userId
     * @param levelUrl
     */
    void portraitSetLevelUrl(Long userId,String levelUrl);

    /**
     * 签到
     * @param userId
     */
    ResultMsg<Object> doSign(Long userId);

    /**
     * 是否首次充值
     * @param userId
     * @return
     */
    boolean checkWithdrawalRecordIsFirst(Long userId);

    /**
     * 查询充值记录
     * @param id
     * @return
     */
    FuntimeUserAccountRechargeRecord getRechargeRecordById(Long id);

    /**
     * 生成充值记录(微信)
     * @param record
     * @param ip
     * @param trade_type
     * @return
     */
    Map<String,String> createRecharge(FuntimeUserAccountRechargeRecord record, String ip, String trade_type);

    /**
     * 支付宝充值
     * @param record
     * @return
     */
    Map<String,Object> createRecharge(FuntimeUserAccountRechargeRecord record);

    /**
     * 支付宝H5
     * @param record
     * @return
     */
    Map<String, Object>  createRechargeAlipayH5(FuntimeUserAccountRechargeRecord record);

    /**
     * 订单回调
     * @param orderId
     * @param transaction_id
     * @param total_fee
     * @return
     */
    Map<String,String> paySuccess(Long orderId, String transaction_id, String total_fee);

    /**
     * 订单回调
     * @param orderId
     * @param transaction_id
     * @return
     */
    void payFail(Long orderId, String transaction_id);

    /**
     * alipay支付回调
     * @param outTradeNo
     * @param tradeStatus
     * @param totalAmount
     * @param tradeNo
     */
    void aliPayOrderCallBack(String outTradeNo,String tradeStatus,BigDecimal totalAmount,String tradeNo);
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
    Long createRedpacket(FuntimeUserRedpacket redpacket);

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
    ResultMsg<Object> createGiftTrans(Long userId, String toUserIds, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannelId, Long roomId,Integer unlock);

    /**
     * 送宝箱
     * @param userId
     * @param toUserIds
     * @param giftId
     * @param giftNum
     * @param operationDesc
     * @param giveChannelId
     * @param roomId
     * @return
     */
    ResultMsg<Object> sendGiftForBox(Long userId, String toUserIds, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannelId, Long roomId);

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
     * @param blackAmount
     * @param preRmbAmount
     * @param preChannelAmount
     * @param amount
     */
    boolean applyWithdrawal(Long userId, BigDecimal blackAmount, BigDecimal preRmbAmount, BigDecimal preChannelAmount, BigDecimal amount, String ip);

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
    ResultMsg<Object> sendGiftForRoom(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId);

    /**
     * 活动赠送礼物
     * @param userId
     * @param toUserId
     * @param giftId
     * @param giftNum
     */
    void createGiftTrans(Long userId, Long toUserId, Integer giftId, Integer giftNum);
    /**
     * 全房宝箱
     * @param userId
     * @param giftId
     * @param giftNum
     * @param operationDesc
     * @param giveChannel
     * @param roomId
     * @return
     */
    ResultMsg<Object> sendGiftForRoomBox3(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId);
    /**
     * 全房送礼物
     * @param userId
     * @param giftId
     * @param giftNum
     * @param operationDesc
     * @param giveChannel
     * @param roomId
     */
    ResultMsg<Object> sendGiftForRoom2(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId);

    /**
     * 全麦送礼物
     * @param userId
     * @param giftId
     * @param giftNum
     * @param operationDesc
     * @param giveChannel
     * @param roomId
     * @return
     */
    ResultMsg<Object> sendGiftForMic(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId);

    /**
     * 麦上宝箱
     * @param userId
     * @param giftId
     * @param giftNum
     * @param operationDesc
     * @param giveChannel
     * @param roomId
     * @return
     */
    ResultMsg<Object> sendGiftForMicBox3(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId);

    /**
     * 全麦送礼物
     * @param userId
     * @param giftId
     * @param giftNum
     * @param operationDesc
     * @param giveChannel
     * @param roomId
     * @return
     */
    ResultMsg<Object> sendGiftForMic2(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId);

    /**
     * 获取充值配置
     * @return
     * @param platform
     */
    List<FuntimeRechargeConf> getRechargeConf(Integer platform);

    /**
     * 获取红包信息
     * @param id
     * @param userId
     * @return
     */
    FuntimeUserRedpacket getRedpacketInfoById(Long id, Long userId);

    /**
     * 红钻兑换蓝钻试算
     * @param userId
     * @param from
     * @param to
     * @param amount
     * @param value
     * @return
     */
    Integer diamondConvertTrial(Long userId, String from, String to, BigDecimal amount, int value);

    /**
     * 获取兑换比例
     * @param userId
     * @param from
     * @param to
     * @return
     */
    BigDecimal getRatio(Long userId, String from, String to);

    /**
     * 获取用户收到的礼物统计
     * @param startPage
     * @param pageSize
     * @param userId
     * @return
     */
    PageInfo<Map<String,Object>> getGiftsByUserId(Integer startPage, Integer pageSize, Long userId);

    /**
     * 用户账户信息
     * @param userId
     * @return
     */
    FuntimeUserAccount getUserAccountByUserId(Long userId);
    /**
     * 修改订单状态
     * @param id
     * @param state
     */
    void updateRechargeRecordState(Long id,Integer state);

    /**
     * 订单查询定时任务
     */
    void orderQueryTask();

    /**
     * 苹果内购
     * @param userId
     * @param transactionId
     * @param payload
     * @param productId
     */
    void iosRecharge(Long userId, String transactionId, String payload, String productId);

    /**
     * 蓝钻日志
     * @param userId
     * @param amount
     * @param recordId
     * @param actionType
     * @param operationType
     * @param roomId
     */
    void saveUserAccountBlueLog(Long userId, BigDecimal amount, Long recordId, String actionType, String operationType, Long roomId);

    /**
     * 红钻日志
     * @param userId
     * @param amount
     * @param recordId
     * @param actionType
     * @param operationType
     */
    void saveUserAccountBlackLog(Long userId, BigDecimal amount,Long recordId,String actionType,String operationType);

    /**
     * 喇叭日志
     * @param userId
     * @param amount
     * @param recordId
     * @param actionType
     * @param operationType
     */
    void saveUserAccountHornLog(Long userId, Integer amount,Long recordId,String actionType,String operationType);

    /**
     * 金币日志
     * @param userId
     * @param amount
     * @param recordId
     * @param actionType
     * @param operationType
     */
    void saveUserAccountGoldLog(Long userId, BigDecimal amount,Long recordId,String actionType,String operationType);

    /**
     * 等级值财富值记录
     * @param userId
     * @param levelVal
     * @param wealthVal
     * @param recordId
     * @param type
     */
    Long saveUserAccountLevelWealthRecord(Long userId, Integer levelVal,Integer wealthVal,Long recordId,Integer type);
    /**
     * 等级值财富值日志
     * @param userId
     * @param levelVal
     * @param wealthVal
     * @param recordId
     * @param actionType
     * @param operationType
     */
    void saveUserAccountLevelWealthLog(Long userId, Integer levelVal,Integer wealthVal,Long recordId,String actionType,String operationType);

    /**
     * 捕鱼子弹
     * @param userId
     * @param roomId
     * @return
     */
    Map<String, Object> getBulletOfFish(Long userId, Long roomId);

    /**
     * 保存得分
     * @param userId
     * @param score
     * @param bullet
     */
    void saveScoreOfFish(Long userId, Integer score, Integer bullet);

    /**
     * 保存购买记录
     * @param userId
     * @param bullet
     * @param parseInt
     * @param roomId
     * @param type
     * @return
     */
    Long insertFishAccountRecord(Long userId, Integer bullet, int parseInt, Long roomId, Integer type);

    /**
     * 捕鱼排行榜
     * @param endCount
     * @param type
     * @return
     */
    List<Map<String, Object>> getFishRanklist(int endCount, Integer type);

    /**
     * 增加子弹数
     * @param userId
     * @param bullet
     */
    void updateBulletForPlus(Long userId, int bullet);

    /**
     * 保存背包
     */
    void saveUserKnapsack(Long userId, int type, Integer drawId, int num);

    /**
     * 礼物日志
     * @param userId
     * @param type
     * @param itemId
     * @param itemNum
     * @param actionType
     * @param operationType
     */
    void saveKnapsackLog( Long userId,int type,Integer itemId, int itemNum,String actionType, String operationType);

    /**
     * 个人背包信息
     * @param userId
     * @return
     */
    List<Map<String, Object>> getUserKnapsackByUserId(Long userId);

    /**
     * 获取礼物
     * @param id
     * @return
     */
    FuntimeGift getGiftById(Integer id);

    /**
     * 获取座驾
     * @param id
     * @return
     */
    Map<String,Object> getCarInfoById(Integer id);

    /**
     * 获取用户座驾
     * @param carId
     * @return
     */
    Map<String,Object> getCarInfoByCarId(Integer carId);

    /**
     * 用户座驾
     * @param userId
     * @return
     */
    Integer getShowCountsById(Long userId);

    /**
     * 新增炫耀座驾记录
     * @param userId
     * @param carId
     */
    void insertShowcarRecord(Long userId,Integer carId);

    /**
     * 中奖保存座驾

     */
    void drawCar(Map<String,Object> map);


    /**
     * 背包送礼
     * @param userId
     * @param toUserIds
     * @param giftId
     * @param giftNum
     * @param desc
     * @param giveChannel
     * @param roomId
     * @param unlock
     * @return
     */
    ResultMsg<Object> sendGiftForKnapsack(Long userId, String toUserIds, Integer giftId, Integer giftNum, String desc, Integer giveChannel, Long roomId, Integer unlock);

    /**
     * 定时设置过期座驾
     */
    void setCarTask();

    /**
     * 用户座驾
     * @param userId
     * @return
     */
    List<Map<String, Object>> getUserCarByUserId(Long userId);

    /**
     * 座驾列表
     * @param userId
     * @return
     */
    List<Map<String, Object>> getCarList(Long userId);

    /**
     * 购买坐骑
     * @param userId
     * @param id
     * @return
     */
    ResultMsg<Object> buyCar(Long userId, Integer id);

    /**
     * 设置坐骑
     * @param userId
     * @param carId
     */
    void setCar(Long userId, Integer carId);

    /**
     * 等级配置
     * @param userId
     * @return
     */
    List<Map<String, Object>> getLevelConf(Long userId);

    /**
     * 金币兑换配置
     * @param userId
     * @return
     */
    Map<String,Object> getGoldConvertConf(Long userId);

    /**
     * 金币兑换
     * @param userId
     * @param id
     */
    void goldConvert(Long userId, Integer id);

    /**
     * 宝箱
     * @return
     */
    List<Map<String,Object>> getBoxList();

    /**
     * 取消座驾
     * @param userId
     * @param carId
     */
    void cancelCar(Long userId, Integer carId);
}
