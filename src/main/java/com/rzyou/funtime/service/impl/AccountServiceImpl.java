package com.rzyou.funtime.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.alipay.easysdk.payment.common.models.AlipayTradeQueryResponse;
import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.common.im.TencentUtil;
import com.rzyou.funtime.common.payment.alipay.MyAlipay;
import com.rzyou.funtime.common.payment.iospay.IosPayUtil;
import com.rzyou.funtime.common.payment.wxpay.MyWxPay;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.*;
import com.rzyou.funtime.utils.*;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.javassist.bytecode.stackmap.BasicBlock;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;

@Slf4j
@Service
public class AccountServiceImpl implements AccountService {


    @Value("${app.pay.notifyUrl}")
    public String notifyUrl ;

    @Autowired
    UserService userService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    RoomService roomService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    SmsService smsService;
    @Autowired
    HeadwearService headwearService;

    @Autowired
    FuntimeCarMapper carMapper;
    @Autowired
    FuntimeUserAccountWithdrawalRecordMapper userAccountWithdrawalRecordMapper;
    @Autowired
    FuntimeUserAccountRechargeRecordMapper userAccountRechargeRecordMapper;
    @Autowired
    FuntimeUserAccountGifttransRecordMapper userAccountGifttransRecordMapper;
    @Autowired
    FuntimeUserAccountBlueLogMapper userAccountBlueLogMapper;
    @Autowired
    FuntimeUserAccountGoldLogMapper userAccountGoldLogMapper;
    @Autowired
    FuntimeUserAccountMapper userAccountMapper;
    @Autowired
    FuntimeUserAccountHornLogMapper userAccountHornLogMapper;
    @Autowired
    FuntimeUserAccountBlackLogMapper userAccountBlackLogMapper;
    @Autowired
    FuntimeUserAccountLevelWealthLogMapper userAccountLevelWealthLogMapper;
    @Autowired
    FuntimeRechargeConfMapper rechargeConfMapper;
    @Autowired
    FuntimeUserRedpacketMapper userRedpacketMapper;
    @Autowired
    FuntimeUserRedpacketDetailMapper userRedpacketDetailMapper;
    @Autowired
    FuntimeUserAccountRedpacketRecordMapper userAccountRedpacketRecordMapper;
    @Autowired
    FuntimeUserConvertRecordMapper userConvertRecordMapper;
    @Autowired
    FuntimeTagMapper tagMapper;
    @Autowired
    FuntimeGiftMapper giftMapper;
    @Autowired
    FuntimeWithdrawalConfMapper withdrawalConfMapper;
    @Autowired
    FuntimeSignMapper signMapper;
    @Autowired
    FuntimeBoxMapper boxMapper;


    @Override
    public FuntimeUserAccountRechargeRecord getRechargeRecordById(Long id) {
        return userAccountRechargeRecordMapper.selectByPrimaryKey(id);
    }

    public boolean isFirstRecharge(Long userId){
        Integer count = userAccountRechargeRecordMapper.getRechargeRecordByUserId(userId);
        if (count>0){
            return false;
        }else{
            return true;
        }
    }

    public void orderQueryTask(){
        List<FuntimeUserAccountRechargeRecord> records = userAccountRechargeRecordMapper.getRechargeRecordByTask();
        List<Map<String, Object>> tags = userService.queryTagsByType("recharge_channel", null);
        if (tags == null || tags.isEmpty()){
            return ;
        }

        if (records!=null&&!records.isEmpty()){
            Map<String, String> resultMap;
            for (FuntimeUserAccountRechargeRecord record : records){
                Integer rechargeChannelId = record.getRechargeChannelId();
                String channel = null;
                for (Map<String, Object> map : tags){
                    if (rechargeChannelId.toString().equals(map.get("id").toString())){
                        channel = map.get("tagName").toString();
                        break;
                    }
                }
                if (channel == null){
                    continue;
                }
                if (channel.equals(RechargeChannel.WX.name())) {
                    if (record.getPollTimes() > 10) {
                        closeOrder(record.getId(), record.getOrderNo(), record.getPayType());
                        continue;
                    }
                    String out_trade_no = record.getOrderNo();
                    resultMap = MyWxPay.orderQuery(null, out_trade_no, record.getPayType());
                    if (resultMap != null && "SUCCESS".equals(resultMap.get("return_code"))
                            && "SUCCESS".equals(resultMap.get("result_code"))) {
                        String trade_state = resultMap.get("trade_state");
                        if ("SUCCESS".equals(trade_state)) {
                            rechargeSuccess(record.getId(), resultMap.get("transaction_id"), resultMap.get("total_fee"));
                        } else {
                            updatePollTimes(record.getId(), 1);
                        }
                    }
                }
                if (channel.equals(RechargeChannel.ALIPAY.name())) {

                    if (record.getPollTimes() > 10) {
                        closeOrderAlipay(record.getId(), record.getOrderNo());
                        continue;
                    }
                    AlipayTradeQueryResponse response = MyAlipay.query(record.getOrderNo());
                    if ("TRADE_SUCCESS".equals(response.tradeStatus)){
                        aliPayOrderCallBack(record.getOrderNo(),"TRADE_SUCCESS",new BigDecimal(response.totalAmount),response.tradeNo);
                    }else{
                        updatePollTimes(record.getId(), 1);
                    }

                }
            }
        }
    }


    @Transactional(rollbackFor = Throwable.class)
    public void closeOrder(Long orderId, String orderNo, Integer payType){
        MyWxPay.closeOrder(orderNo,payType);
        updateState(orderId,PayState.INVALID.getValue(),null, null, null);
    }

    @Transactional(rollbackFor = Throwable.class)
    public void closeOrderAlipay(Long orderId, String orderNo){
        MyAlipay.closeOrder(orderNo);
        updateState(orderId,PayState.INVALID.getValue(),null, null, null);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void iosRecharge(Long userId, String transactionId, String payload, String productId){
        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);
        FuntimeRechargeConf rechargeConf = rechargeConfMapper.getRechargeConfByProductId(productId);
        if (rechargeConf==null){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_CONF_NOT_EXISTS.getValue(),ErrorMsgEnum.RECHARGE_CONF_NOT_EXISTS.getDesc());
        }
        if (userAccountRechargeRecordMapper.checkTransactionIdExist(transactionId)>0){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_TRANSACTIONID_EXISTS.getValue(),ErrorMsgEnum.RECHARGE_TRANSACTIONID_EXISTS.getDesc());
        }

        IosPayUtil.iosPay(transactionId, payload);

        String max_recharge = parameterService.getParameterValueByKey("max_recharge");
        BigDecimal maxRecharge = max_recharge == null?new BigDecimal(100000):new BigDecimal(max_recharge);
        if (rechargeConf.getRechargeRmb().subtract(maxRecharge).doubleValue()>0){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_NUM_OUT.getValue(),ErrorMsgEnum.RECHARGE_NUM_OUT.getDesc());
        }
        Integer hornNum = rechargeConf.getHornNum();
        Integer goldNum = rechargeConf.getGoldNum();
        //首充送三个
        if (isFirstRecharge(userId)){
            String first_recharge_horn = parameterService.getParameterValueByKey("first_recharge_horn");
            String first_recharge_gold = parameterService.getParameterValueByKey("first_recharge_gold");
            hornNum += Integer.parseInt(first_recharge_horn==null?"3":first_recharge_horn);
            goldNum += Integer.parseInt(first_recharge_gold==null?"100":first_recharge_gold);
        }

        String blue_to_level = parameterService.getParameterValueByKey("blue_to_level");
        String level_to_wealth = parameterService.getParameterValueByKey("level_to_wealth");
        int levelVal = rechargeConf.getRechargeNum().multiply(new BigDecimal(blue_to_level)).intValue();
        int wealthVal = new BigDecimal(levelVal).multiply(new BigDecimal(level_to_wealth)).intValue();

        FuntimeUserAccountRechargeRecord record = new FuntimeUserAccountRechargeRecord();
        record.setUserId(userId);
        record.setPayType(4);
        record.setCompleteTime(new Date());
        record.setRechargeConfId(rechargeConf.getId());
        record.setRmb(rechargeConf.getRechargeRmb());
        record.setRechargeCardId(transactionId);
        record.setHornNum(hornNum);
        record.setGoldNum(goldNum);
        record.setAmount(rechargeConf.getRechargeNum());
        record.setLevelVal(levelVal);
        record.setWealthVal(wealthVal);
        String orderNo = "I"+StringUtil.createOrderId();
        saveAccountRechargeRecord(record,System.currentTimeMillis(),PayState.PAIED.getValue(), orderNo);

        //用户总充值数(等级值)
        int total = userAccount.getLevelVal()+levelVal;

        //充值等级
        Map<String,Object> userLevelMap = userAccountRechargeRecordMapper.getUserLevel(total);

        if (userLevelMap==null){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_LEVEL_NOT_EXISTS.getValue(),ErrorMsgEnum.RECHARGE_LEVEL_NOT_EXISTS.getDesc());
        }
        Integer userLevel = userLevelMap.get("level")==null?0:Integer.parseInt(userLevelMap.get("level").toString());
        String levelUrl = userLevelMap.get("levelUrl")==null?"":userLevelMap.get("levelUrl").toString();

        if (!userLevel.equals(userAccount.getLevel())){
            userAccountMapper.updateUserAccountLevel(record.getUserId(),userLevel,record.getAmount(),record.getHornNum(),levelVal,wealthVal, goldNum);
            updateLevelExtr(userId,userLevel,levelUrl);
        }else{
            userAccountMapper.updateUserAccountLevel(record.getUserId(),null,record.getAmount(),record.getHornNum(),levelVal,wealthVal, goldNum);
        }

        //记录日志
        saveUserAccountBlueLog(record.getUserId(),record.getAmount(),record.getId()
                , OperationType.IOSRECHARGE.getAction(),OperationType.IOSRECHARGE.getOperationType());
        Long WealthRecordId = saveUserAccountLevelWealthRecord(record.getUserId(),levelVal,wealthVal,record.getId(),LevelWealthType.RECHARGE.getValue());
        saveUserAccountLevelWealthLog(record.getUserId(),levelVal,wealthVal,WealthRecordId
                , OperationType.IOSRECHARGE.getAction(),OperationType.IOSRECHARGE.getOperationType());

        if(record.getHornNum()!=null&&record.getHornNum()>0){
            saveUserAccountHornLog(record.getUserId(),record.getHornNum(),record.getId()
                    , OperationType.IOSRECHARGE.getAction(),OperationType.IOSRECHARGE.getOperationType());
        }
        if(record.getGoldNum()!=null&&record.getGoldNum()>0){
            saveUserAccountGoldLog(record.getUserId(),new BigDecimal(record.getGoldNum()),record.getId()
                    , OperationType.IOSRECHARGE.getAction(),OperationType.IOSRECHARGE.getOperationType());
        }

    }

    private void updateLevelExtr(Long userId,Integer userLevel,String levelUrl){
        Integer type = headwearService.getCurrnetHeadwear(userId);
        if (type == null||type == 1) {
            String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
            boolean flag = TencentUtil.portraitSet(userSig, userId.toString(), userLevel, levelUrl);
            if (!flag) {
                throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(), ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
            }
            Long roomId = roomService.checkUserIsInMic(userId);
            if (roomId != null) {
                roomService.sendRoomInfoNotice(roomId);

            }
        }
    }

    @Override
    public void portraitSetLevelUrl(Long userId,String levelUrl){
        String userSig = UsersigUtil.getUsersig(Constant.TENCENT_YUN_IDENTIFIER);
        boolean flag = TencentUtil.portraitSet(userSig, userId.toString(), null, levelUrl);
        if (!flag) {
            throw new BusinessException(ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getValue(), ErrorMsgEnum.USER_SYNC_TENCENT_ERROR.getDesc());
        }
        Long roomId = roomService.checkUserIsInMic(userId);
        if (roomId != null) {
            roomService.sendRoomInfoNotice(roomId);

        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Map<String,String> createRecharge(FuntimeUserAccountRechargeRecord record, String ip, String trade_type){
        if (!userService.checkUserExists(record.getUserId())){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeRechargeConf rechargeConf = rechargeConfMapper.selectByPrimaryKey(record.getRechargeConfId());
        if (rechargeConf==null){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_CONF_NOT_EXISTS.getValue(),ErrorMsgEnum.RECHARGE_CONF_NOT_EXISTS.getDesc());
        }
        String max_recharge = parameterService.getParameterValueByKey("max_recharge");
        BigDecimal maxRecharge = max_recharge == null?new BigDecimal(100000):new BigDecimal(max_recharge);
        if (rechargeConf.getRechargeRmb().subtract(maxRecharge).doubleValue()>0){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_NUM_OUT.getValue(),ErrorMsgEnum.RECHARGE_NUM_OUT.getDesc());
        }


        String blue_to_level = parameterService.getParameterValueByKey("blue_to_level");
        String level_to_wealth = parameterService.getParameterValueByKey("level_to_wealth");
        int levelVal = rechargeConf.getRechargeNum().multiply(new BigDecimal(blue_to_level)).intValue();
        int wealthVal = new BigDecimal(levelVal).multiply(new BigDecimal(level_to_wealth)).intValue();

        record.setRmb(rechargeConf.getRechargeRmb());
        record.setHornNum(rechargeConf.getHornNum());
        record.setGoldNum(rechargeConf.getGoldNum());
        record.setAmount(rechargeConf.getRechargeNum());
        record.setLevelVal(levelVal);
        record.setWealthVal(wealthVal);
        String orderNo = "A"+StringUtil.createOrderId();
        Long id = saveAccountRechargeRecord(record,System.currentTimeMillis(),PayState.START.getValue(), orderNo);
        Map<String, String> orderMap  = unifiedOrder(ip, "WEB", id.toString()
                    , rechargeConf.getRechargeRmb().multiply(new BigDecimal(100)).setScale(0, BigDecimal.ROUND_HALF_UP).toString(), orderNo, trade_type, record.getOpenid(), record.getPayType());

        orderMap.put("payType", "1");

        return orderMap;

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Map<String,Object> createRecharge(FuntimeUserAccountRechargeRecord record){
        if (!userService.checkUserExists(record.getUserId())){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeRechargeConf rechargeConf = rechargeConfMapper.selectByPrimaryKey(record.getRechargeConfId());
        if (rechargeConf==null){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_CONF_NOT_EXISTS.getValue(),ErrorMsgEnum.RECHARGE_CONF_NOT_EXISTS.getDesc());
        }
        String max_recharge = parameterService.getParameterValueByKey("max_recharge");
        BigDecimal maxRecharge = max_recharge == null?new BigDecimal(100000):new BigDecimal(max_recharge);
        if (rechargeConf.getRechargeRmb().subtract(maxRecharge).doubleValue()>0){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_NUM_OUT.getValue(),ErrorMsgEnum.RECHARGE_NUM_OUT.getDesc());
        }


        String blue_to_level = parameterService.getParameterValueByKey("blue_to_level");
        String level_to_wealth = parameterService.getParameterValueByKey("level_to_wealth");
        int levelVal = rechargeConf.getRechargeNum().multiply(new BigDecimal(blue_to_level)).intValue();
        int wealthVal = new BigDecimal(levelVal).multiply(new BigDecimal(level_to_wealth)).intValue();

        record.setRmb(rechargeConf.getRechargeRmb());
        record.setHornNum(rechargeConf.getHornNum());
        record.setGoldNum(rechargeConf.getGoldNum());
        record.setAmount(rechargeConf.getRechargeNum());
        record.setLevelVal(levelVal);
        record.setWealthVal(wealthVal);
        String orderNo = "Z"+StringUtil.createOrderId();
        saveAccountRechargeRecord(record,System.currentTimeMillis(),PayState.START.getValue(), orderNo);
        Map<String, Object> orderMap  = MyAlipay.alipay("触娱充值",orderNo,rechargeConf.getRechargeRmb().toString());
        orderMap.put("payType", "2");
        return orderMap;

    }

    public Map<String, String> unifiedOrder(String ip, String imei, String orderId, String totalFee, String orderNo, String trade_type, String openid, Integer payType) {

        if (TradeType.APP.getValue().equals(trade_type)) {
            return MyWxPay.unifiedOrder(totalFee, ip, orderNo, imei, notifyUrl, orderId, trade_type,payType);
        }
        else if (TradeType.JSAPI.getValue().equals(trade_type)) {
            return MyWxPay.unifiedOrder(totalFee, ip, orderNo, imei, notifyUrl, orderId, trade_type,openid,payType);
        }else{
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }

    }



    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Map<String,String> paySuccess(Long orderId, String transaction_id, String total_fee) {
        Map<String,String> result = new HashMap<>();

        try {
            rechargeSuccess(orderId,transaction_id,total_fee);

        }catch (Exception e){
            result.put("return_code", "FAIL");
            result.put("return_msg", ErrorMsgEnum.ORDER_NOT_EXISTS.getDesc());
        }
        result.put("return_code", "SUCCESS");
        result.put("return_msg", "OK");
        return result;

    }
    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void payFail(Long orderId, String transaction_id){
        FuntimeUserAccountRechargeRecord record = userAccountRechargeRecordMapper.selectByPrimaryKey(orderId);
        if(record==null){
            return;
        }
        if (PayState.PAIED.getValue().equals(record.getState())){
            return;
        }else if (PayState.START.getValue().equals(record.getState())||PayState.PAYING.getValue().equals(record.getState())) {
            //状态变更
            updateState(orderId, PayState.FAIL.getValue(),transaction_id, null, null);
        }else{
            return;
        }
    }

    public void rechargeSuccess(Long recordId, String transaction_id, String total_fee){
        FuntimeUserAccountRechargeRecord record = userAccountRechargeRecordMapper.selectByPrimaryKey(recordId);
        if(record==null){
            return;
        }
        if (PayState.PAIED.getValue().equals(record.getState())){
            return;
        }else if (PayState.START.getValue().equals(record.getState())||PayState.FAIL.getValue().equals(record.getState())
                ||PayState.PAYING.getValue().equals(record.getState())) {

            FuntimeUserAccount userAccount = userAccountMapper.selectByUserId(record.getUserId());
            if (userAccount==null){
                return;
            }
            if (new BigDecimal(total_fee).intValue()!=record.getRmb().multiply(new BigDecimal(100)).intValue()){
                log.info("支付回调中金额与系统订单金额不一致,微信订单金额:{},系统订单金额:{}",total_fee,record.getRmb().multiply(new BigDecimal(100)).toString());
                return;
            }
            Integer hornNum = null;
            Integer goldNum = null;
            //首充送三个
            if (isFirstRecharge(record.getUserId())){
                String first_recharge_horn = parameterService.getParameterValueByKey("first_recharge_horn");
                String first_recharge_gold = parameterService.getParameterValueByKey("first_recharge_gold");
                hornNum = record.getHornNum()==null?0:record.getHornNum()
                        + Integer.parseInt(first_recharge_horn==null?"3":first_recharge_horn);
                goldNum = record.getGoldNum() == null?0:record.getGoldNum()+Integer.parseInt(first_recharge_gold==null?"100":first_recharge_gold);
            }
            //状态变更
            updateState(recordId, PayState.PAIED.getValue(),transaction_id,hornNum,goldNum);

            //用户总充值数(等级值)
            int total = userAccount.getLevelVal()+record.getLevelVal();

            //充值等级
            Map<String,Object> userLevelMap = userAccountRechargeRecordMapper.getUserLevel(total);

            if (userLevelMap==null){
                return;
            }
            Integer userLevel = userLevelMap.get("level")==null?0:Integer.parseInt(userLevelMap.get("level").toString());
            String levelUrl = userLevelMap.get("levelUrl")==null?"":userLevelMap.get("levelUrl").toString();
            int levelVal = record.getLevelVal();
            int wealthVal = record.getWealthVal();
            if (!userLevel.equals(userAccount.getLevel())){
                userAccountMapper.updateUserAccountLevel(record.getUserId(),userLevel,record.getAmount(),hornNum,levelVal, wealthVal,goldNum);
                updateLevelExtr(record.getUserId(),userLevel,levelUrl);
            }else{
                userAccountMapper.updateUserAccountLevel(record.getUserId(),null,record.getAmount(),hornNum,levelVal, wealthVal, goldNum);
            }
            //记录日志
            saveUserAccountBlueLog(record.getUserId(),record.getAmount(),record.getId()
                    , OperationType.RECHARGE.getAction(),OperationType.RECHARGE.getOperationType());
            Long WealthRecordId = saveUserAccountLevelWealthRecord(record.getUserId(),levelVal,wealthVal,record.getId(),LevelWealthType.RECHARGE.getValue());
            saveUserAccountLevelWealthLog(record.getUserId(),levelVal,wealthVal,WealthRecordId
                    , OperationType.RECHARGE.getAction(),OperationType.RECHARGE.getOperationType());

            if(record.getHornNum()!=null&&record.getHornNum()>0){
                saveUserAccountHornLog(record.getUserId(),record.getHornNum(),record.getId()
                        , OperationType.RECHARGE.getAction(),OperationType.RECHARGE.getOperationType());
            }
            if(record.getGoldNum()!=null&&record.getGoldNum()>0){
                saveUserAccountGoldLog(record.getUserId(),new BigDecimal(record.getGoldNum()),record.getId()
                        , OperationType.RECHARGE.getAction(),OperationType.RECHARGE.getOperationType());
            }

            return;
        }else{
            log.info("订单号: {} 的订单已失效,请重新下单",record.getOrderNo());
            return;
        }
    }


    @Transactional(rollbackFor = Throwable.class)
    @Override
    public void aliPayOrderCallBack(String outTradeNo,String tradeStatus,BigDecimal totalAmount,String buyerLogonId)  {
        if (StringUtils.isNotEmpty(outTradeNo)) {
            //根据交易编号加锁，处理高并发
            synchronized (outTradeNo) {
                FuntimeUserAccountRechargeRecord record = userAccountRechargeRecordMapper.getRechargeRecordByOrderNo(outTradeNo);
                if (PayState.PAIED.getValue().equals(record.getState())) {
                    return;
                }
                else if (PayState.START.getValue().equals(record.getState())||PayState.FAIL.getValue().equals(record.getState())
                        ||PayState.PAYING.getValue().equals(record.getState())) {
                    if (tradeStatus.equals("TRADE_FINISHED")) {
                        //交易创建，等待买家付款
                        log.info("交易创建，等待买家付款 TRADE_FINISHED");
                        return;
                    } else if (tradeStatus.equals("WAIT_BUYER_PAY")) {
                        //未付款交易超时关闭，或支付完成后全额退款
                        log.info(" 未付款交易超时关闭，或支付完成后全额退款 WAIT_BUYER_PAY");
                        return;
                    } else if (tradeStatus.equals("TRADE_CLOSED")) {
                        //交易结束，不可退款
                        log.info("交易结束，不可退款 TRADE_CLOSED");
                        return;
                    } else if (tradeStatus.equals("TRADE_SUCCESS")) {
                        //交易支付成功
                        log.info("交易支付成功 TRADE_SUCCESS");
                        //订单需支付金额总和
                        BigDecimal payNumSum = record.getRmb();
                        FuntimeUserAccount userAccount = userAccountMapper.selectByUserId(record.getUserId());
                        if (userAccount==null){
                            return;
                        }
                        //以防万一，再次校验金额
                        if (payNumSum.compareTo(totalAmount) != 0) {
                            log.error("***订单号: " + outTradeNo + "***支付宝支付金额与订单需支付金额总和不一致***支付宝支付金额为:" + totalAmount + " ***订单需支付金额总为:" + payNumSum + "***日期:" + new Date());
                            //金额异常，订单状态为支付金额异常
                            return;
                        }
                        //修改订单状态
                        Integer hornNum = null;
                        Integer goldNum = null;
                        //首充送三个
                        if (isFirstRecharge(record.getUserId())){
                            String first_recharge_horn = parameterService.getParameterValueByKey("first_recharge_horn");
                            String first_recharge_gold = parameterService.getParameterValueByKey("first_recharge_gold");
                            hornNum = record.getHornNum()==null?0:record.getHornNum()
                                    + Integer.parseInt(first_recharge_horn==null?"3":first_recharge_horn);
                            goldNum = record.getGoldNum() == null?0:record.getGoldNum()+Integer.parseInt(first_recharge_gold==null?"100":first_recharge_gold);
                        }
                        //状态变更
                        updateState(record.getId(), PayState.PAIED.getValue(),buyerLogonId,hornNum,goldNum);

                        //用户总充值数(等级值)
                        int total = userAccount.getLevelVal()+record.getLevelVal();

                        //充值等级
                        Map<String,Object> userLevelMap = userAccountRechargeRecordMapper.getUserLevel(total);

                        if (userLevelMap==null){
                            return;
                        }
                        Integer userLevel = userLevelMap.get("level")==null?0:Integer.parseInt(userLevelMap.get("level").toString());
                        String levelUrl = userLevelMap.get("levelUrl")==null?"":userLevelMap.get("levelUrl").toString();
                        int levelVal = record.getLevelVal();
                        int wealthVal = record.getWealthVal();
                        if (!userLevel.equals(userAccount.getLevel())){
                            userAccountMapper.updateUserAccountLevel(record.getUserId(),userLevel,record.getAmount(),hornNum,levelVal, wealthVal,goldNum);
                            updateLevelExtr(record.getUserId(),userLevel,levelUrl);
                        }else{
                            userAccountMapper.updateUserAccountLevel(record.getUserId(),null,record.getAmount(),hornNum,levelVal, wealthVal, goldNum);
                        }
                        //记录日志
                        saveUserAccountBlueLog(record.getUserId(),record.getAmount(),record.getId()
                                , OperationType.ALIPAYRECHARGE.getAction(),OperationType.ALIPAYRECHARGE.getOperationType());
                        Long WealthRecordId = saveUserAccountLevelWealthRecord(record.getUserId(),levelVal,wealthVal,record.getId(),LevelWealthType.RECHARGE.getValue());
                        saveUserAccountLevelWealthLog(record.getUserId(),levelVal,wealthVal,WealthRecordId
                                , OperationType.ALIPAYRECHARGE.getAction(),OperationType.ALIPAYRECHARGE.getOperationType());

                        if(record.getHornNum()!=null&&record.getHornNum()>0){
                            saveUserAccountHornLog(record.getUserId(),record.getHornNum(),record.getId()
                                    , OperationType.ALIPAYRECHARGE.getAction(),OperationType.ALIPAYRECHARGE.getOperationType());
                        }
                        if(record.getGoldNum()!=null&&record.getGoldNum()>0){
                            saveUserAccountGoldLog(record.getUserId(),new BigDecimal(record.getGoldNum()),record.getId()
                                    , OperationType.ALIPAYRECHARGE.getAction(),OperationType.ALIPAYRECHARGE.getOperationType());
                        }

                    }
                } else {
                    log.info("该订单已支付处理，交易编号为: " + outTradeNo);
                }
            }
        }
    }


    @Override
    public PageInfo<FuntimeUserAccountRechargeRecord> getRechargeDetailForPage(Integer startPage, Integer pageSize, String queryDate, Integer state, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        String startDate = queryDate+"-01 00:00:01";
        String endDate = currentFinalDay(queryDate) ;

        List<FuntimeUserAccountRechargeRecord> list = userAccountRechargeRecordMapper.getRechargeDetailForPage(startDate,endDate,userId,state);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            return new PageInfo<>(list);
        }

    }

    public String currentFinalDay(String queryDate){
        String endDate ;
        try {
            endDate = DateUtil.currentFinalDay(queryDate);
            return endDate;
        } catch (Exception e) {
            throw new BusinessException(ErrorMsgEnum.ORDER_DATE_ERROR.getValue(),ErrorMsgEnum.ORDER_DATE_ERROR.getDesc());
        }

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Long createRedpacket(FuntimeUserRedpacket redpacket) {
        String isRedpacketShow = parameterService.getParameterValueByKey("is_redpacket_show");
        if (isRedpacketShow!=null&&isRedpacketShow.equals("2")){
            throw new BusinessException(ErrorMsgEnum.DRAW_TIME_OUT.getValue(),ErrorMsgEnum.DRAW_TIME_OUT.getDesc());
        }
        if (redpacket.getType() == 1) {
            FuntimeChatroom chatroom = roomService.getChatroomById(redpacket.getRoomId());
            if (chatroom == null) {
                throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
            }
        }
        FuntimeUser user = userService.queryUserById(redpacket.getUserId());
        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        String invalid_day = parameterService.getParameterValueByKey("redpacket_invalid_day");

        redpacket.setInvalidTime(DateUtils.addMinutes(new Date(),Integer.parseInt(invalid_day)));
        redpacket.setState(RedpacketState.START.getValue());

        int k = userRedpacketMapper.insertSelective(redpacket);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        userService.updateUserAccountForSub(redpacket.getUserId(),null,redpacket.getAmount(),null);
        //新建用户日志
        saveUserAccountBlueLog(redpacket.getUserId(),redpacket.getAmount(),redpacket.getId(),OperationType.GIVEREDPACKET.getAction(),OperationType.GIVEREDPACKET.getOperationType());

        if (redpacket.getType() == 1) {
            RedPacketUtil redPacketUtil = new RedPacketUtil(redpacket.getAmount().intValue());
            Integer[] redPackets = redPacketUtil.splitRedPacket(redpacket.getAmount().intValue(), redpacket.getRedpacketNum());
            if (redPackets.length > 0) {
                List<FuntimeUserRedpacketDetail> details = new ArrayList<>();
                FuntimeUserRedpacketDetail detail;
                for (Integer red : redPackets) {
                    detail = new FuntimeUserRedpacketDetail();
                    detail.setVersion(System.currentTimeMillis());
                    detail.setAmount(new BigDecimal(red));
                    detail.setRedpacketId(redpacket.getId());

                    details.add(detail);

                }
                userRedpacketDetailMapper.insertBatch(details);
            }


            //通知
            List<String> userIds = roomService.getRoomUserByRoomIdAll(redpacket.getRoomId());
            if (userIds == null || userIds.isEmpty()) {
                throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
            }
            noticeService.notice13(redpacket.getRoomId(), userIds,user.getNickname());
            String noticeAmount = parameterService.getParameterValueByKey("redpacket_notice_amount");
            String hornLength = parameterService.getParameterValueByKey("redpacket_horn_length");
            if (noticeAmount!=null&&redpacket.getAmount().intValue()>=new BigDecimal(noticeAmount).intValue()){
                noticeService.notice10003("发了一个"+redpacket.getAmount().intValue()+"红包等你来抢!",redpacket.getUserId(),redpacket.getRoomId(),user.getNickname(),user.getSex(),user.getPortraitAddress(),hornLength);
            }
        }else{
            checkUser(redpacket.getToUserId());
            //单发
            FuntimeUserRedpacketDetail detail = new FuntimeUserRedpacketDetail();
            detail.setVersion(System.currentTimeMillis());
            detail.setAmount(redpacket.getAmount());
            detail.setRedpacketId(redpacket.getId());
            userRedpacketDetailMapper.insertSelective(detail);

        }
        if (redpacket.getType() == 1) {
            roomService.updateHotsPlus(redpacket.getRoomId(), redpacket.getAmount().divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        return redpacket.getId();

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> grabRedpacket(Long userId, Long redpacketId){
        String isRedpacketShow = parameterService.getParameterValueByKey("is_redpacket_show");
        if (isRedpacketShow!=null&&isRedpacketShow.equals("2")){
            throw new BusinessException(ErrorMsgEnum.DRAW_TIME_OUT.getValue(),ErrorMsgEnum.DRAW_TIME_OUT.getDesc());
        }
        FuntimeUserRedpacket redpacket = userRedpacketMapper.selectByPrimaryKey(redpacketId);
        if (redpacket==null){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_NOT_EXISTS.getValue(),ErrorMsgEnum.REDPACKET_IS_NOT_EXISTS.getDesc());
        }
        if (redpacket.getState() == 2){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_OVER.getValue(),ErrorMsgEnum.REDPACKET_IS_OVER.getDesc());
        }
        if (redpacket.getState() == 3||redpacket.getInvalidTime().before(new Date())){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_EMPIRE.getValue(),ErrorMsgEnum.REDPACKET_IS_EMPIRE.getDesc());
        }
        //个人
        if (redpacket.getType()==2){
            if (!userId.equals(redpacket.getToUserId())){
                throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_NOT_YOURS.getValue(),ErrorMsgEnum.REDPACKET_IS_NOT_YOURS.getDesc());
            }
            if (redpacket.getBestowCondition()==1){
                return grabRedpacketNoCondition2(userId, redpacketId,null);
            }else{
                return grabRedpacketBestowCondition2(userId,redpacketId,redpacket.getGiftId(),redpacket.getUserId(),redpacket.getRoomId());
            }

        }else{
            //房间

            if (redpacket.getBestowCondition()==1){
                return grabRedpacketNoCondition(userId, redpacketId,null);
            }else{
                if (userId.equals(redpacket.getUserId())){
                    return grabRedpacketNoCondition(userId, redpacketId,null);
                }
                return grabRedpacketBestowCondition(userId,redpacketId,redpacket.getGiftId(),redpacket.getUserId(),redpacket.getRoomId());
            }

        }

    }
    public ResultMsg<Object> grabRedpacketBestowCondition(Long userId, Long redpacketId, Integer giftId,Long toUserId,Long roomId) {

        //礼物赠送
        ResultMsg<Object> resultMsg = createGiftTrans(userId,toUserId,giftId,1,"红包赠送",GiveChannel.REDPACKET.getValue(), roomId);
        if (ErrorMsgEnum.SUCCESS.getValue().equals(resultMsg.getCode())){
            Long recordId = Long.parseLong(resultMsg.getData().toString());
            return grabRedpacketNoCondition(userId, redpacketId,recordId);
        }else{
            return resultMsg;
        }


    }
    public ResultMsg<Object> grabRedpacketBestowCondition2(Long userId, Long redpacketId, Integer giftId,Long toUserId,Long roomId) {

        //礼物赠送
        ResultMsg<Object> resultMsg = createGiftTrans(userId,toUserId,giftId,1,"红包赠送",GiveChannel.REDPACKET.getValue(), roomId);
        if (ErrorMsgEnum.SUCCESS.getValue().equals(resultMsg.getCode())){
            Long recordId = Long.parseLong(resultMsg.getData().toString());
            return grabRedpacketNoCondition2(userId, redpacketId,recordId);
        }else{
            return resultMsg;
        }


    }

    //无条件
    public ResultMsg<Object> grabRedpacketNoCondition2(Long userId, Long redpacketId,Long giftRecordId) {

        checkUser(userId);
        //有没有抢过
        if(!checkRedpacketGrab(userId,redpacketId)){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_GRABED.getValue(),ErrorMsgEnum.REDPACKET_IS_GRABED.getDesc());
        }

        List<FuntimeUserRedpacketDetail> details = userRedpacketDetailMapper.queryDetailByRedId(redpacketId);

        if (details==null||details.isEmpty()){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_OVER.getValue(),ErrorMsgEnum.REDPACKET_IS_OVER.getDesc());
        }

        FuntimeUserRedpacketDetail detail = details.get(0);
        //修改红包明细
        updateDetailById(detail.getId(),userId,detail.getVersion(),System.currentTimeMillis());

        //新建红包记录
        Long recordId = saveAccountRedpacketRecord(userId,detail.getId(),detail.getAmount(),detail.getCreateTime(),giftRecordId);

        //用户账户增加
        userService.updateUserAccountForPlus(userId,null,detail.getAmount(),null);

        //新建用户日志
        saveUserAccountBlueLog(userId,detail.getAmount(),recordId,OperationType.GRABREDPACKET.getAction(),OperationType.GRABREDPACKET.getOperationType());

        updateRedpacketState(redpacketId,RedpacketState.SUCCESS.getValue());

        Map<String,Object> result = new HashMap<>();
        result.put("amount",detail.getAmount().intValue());
        return new ResultMsg<>(result);

    }


    //无条件
    public ResultMsg<Object> grabRedpacketNoCondition(Long userId, Long redpacketId,Long giftRecordId) {

        checkUser(userId);
        //有没有抢过
        if(!checkRedpacketGrab(userId,redpacketId)){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_GRABED.getValue(),ErrorMsgEnum.REDPACKET_IS_GRABED.getDesc());
        }

        List<FuntimeUserRedpacketDetail> details = userRedpacketDetailMapper.queryDetailByRedId(redpacketId);

        if (details==null||details.isEmpty()){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_OVER.getValue(),ErrorMsgEnum.REDPACKET_IS_OVER.getDesc());
        }
        //是否最后一个
        boolean isLastOne = details.size()==1;

        FuntimeUserRedpacketDetail detail = details.get(0);
        //修改红包明细
        updateDetailById(detail.getId(),userId,detail.getVersion(),System.currentTimeMillis());

        //新建红包记录
        Long recordId = saveAccountRedpacketRecord(userId,detail.getId(),detail.getAmount(),detail.getCreateTime(),giftRecordId);

        //用户账户增加
        userService.updateUserAccountForPlus(userId,null,detail.getAmount(),null);

        //新建用户日志
        saveUserAccountBlueLog(userId,detail.getAmount(),recordId,OperationType.GRABREDPACKET.getAction(),OperationType.GRABREDPACKET.getOperationType());

        //最后一个打标签
        if(isLastOne){

            tagSetup(redpacketId);

            updateRedpacketState(redpacketId,RedpacketState.SUCCESS.getValue());
        }

        Map<String,Object> result = new HashMap<>();
        result.put("amount",detail.getAmount().intValue());
        return new ResultMsg<>(result);

    }

    public void updateRedpacketState(Long redpacketId,Integer state){
        int k = userRedpacketMapper.updateStateById(state,redpacketId);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }
    }

    private void tagSetup(Long redpacketId) {

        List<FuntimeUserAccountRedpacketRecord> records = userAccountRedpacketRecordMapper.getRedpacketRecordByredId(redpacketId);
        if(records==null||records.isEmpty()||records.size()<=4){
            return;
            //throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

        //从大到小有序,相同时间早的排前面
        int[] arrays = new int[records.size()];
        for (int i =0 ;i<arrays.length;i++){
            arrays[i] = records.get(i).getAmount().intValue();
        }


        int max = arrays[0];
        int min = arrays[arrays.length-1];
        records.get(0).setTagId(RedpacketTag.SORT_1.getValue());//首榜
        records.get(arrays.length-1).setTagId(RedpacketTag.SORT_4.getValue());//小可怜

        if (max==min){
            for (int i = 1;i<arrays.length-1;i++){
                records.get(i).setTagId(RedpacketTag.SORT_1.getValue());
            }
        }else {

            for (int i = 1; i < arrays.length-1 ; i++) {
                if (arrays[i] == max) {
                    records.get(i).setTagId(RedpacketTag.SORT_1.getValue());//首榜
                }

                if (arrays[i] == min) {
                    records.get(i).setTagId(RedpacketTag.SORT_4.getValue());//小可怜
                }
            }

            int second = 0;
            for (int i = 1; i < arrays.length - 1; i++) {

                if (arrays[i] < max && arrays[i] > min) {
                    second = arrays[i];
                    records.get(i).setTagId(RedpacketTag.SORT_2.getValue());//运气王
                    break;
                }
            }
            //有第二的
            if (second > 0) {
                for (int i = 2; i < arrays.length - 1; i++) {
                    if (arrays[i]==second){
                        records.get(i).setTagId(RedpacketTag.SORT_2.getValue());//运气王
                    }
                }
                int third = 0;
                for (int i = 2; i < arrays.length - 1; i++) {
                    if (arrays[i] < second && arrays[i] > min) {
                        third = arrays[i];
                        records.get(i).setTagId(RedpacketTag.SORT_3.getValue());//小幸运
                        break;
                    }
                }

                //有第三
                if (third > 0) {
                    for (int i = 3; i < arrays.length - 1; i++) {
                        if (arrays[i]==third){
                            records.get(i).setTagId(RedpacketTag.SORT_3.getValue());//运气王
                        }
                    }
                    //无第三
                } else {

                }

                //无第二的,数组所有值不是最大就是最小
            } else {
                for (int i = 1; i < arrays.length - 1; i++) {
                    if (arrays[i] == max) {
                        records.get(i).setTagId(RedpacketTag.SORT_1.getValue());//首榜
                    }
                    if (arrays[i] == min) {
                        records.get(i).setTagId(RedpacketTag.SORT_4.getValue());//小可怜
                    }
                }
            }

            for (FuntimeUserAccountRedpacketRecord record : records){
                if (record.getTagId()!=null){
                    userAccountRedpacketRecordMapper.updateTagById(record.getId(),record.getTagId());
                }
            }
        }
    }


    @Override
    public PageInfo<FuntimeUserRedpacket> getRedpacketOfSendForPage(Integer startPage, Integer pageSize, String queryDate, Long userId) {
        PageHelper.startPage(startPage,pageSize);

        String startDate = queryDate+"-01 00:00:01";
        String endDate = currentFinalDay(queryDate) ;

        List<FuntimeUserRedpacket> list = userRedpacketMapper.getRedpacketInfoByUserId(startDate,endDate,userId);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            return new PageInfo<>(list);
        }
    }

    @Override
    public PageInfo<FuntimeUserAccountRedpacketRecord> getRedpacketOfRecieveForPage(Integer startPage, Integer pageSize, String queryDate, Long userId) {
        PageHelper.startPage(startPage,pageSize);

        String startDate = queryDate+"-01 00:00:01";
        String endDate = currentFinalDay(queryDate) ;

        List<FuntimeUserAccountRedpacketRecord> list = userAccountRedpacketRecordMapper.getRedpacketOfRecieveForPage(startDate,endDate,userId);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            return new PageInfo<>(list);
        }
    }

    @Override
    public BigDecimal querySnedSumAmountByGrab(Long userId, String queryDate) {
        String startDate = queryDate+"-01 00:00:01";
        String endDate = currentFinalDay(queryDate) ;
        return userRedpacketDetailMapper.querySnedSumAmountByGrab(startDate,endDate,userId);
    }

    @Override
    public BigDecimal getSumGrabAmountById(Long userId, String queryDate) {
        if (queryDate!=null) {
            String startDate = queryDate + "-01 00:00:01";
            String endDate = currentFinalDay(queryDate);
            return userAccountRedpacketRecordMapper.getSumGrabAmountById(startDate, endDate, userId);
        }else{
            return userAccountRedpacketRecordMapper.getSumGrabAmountById(null, null, userId);
        }
    }

    @Override
    public List<Map<String,Object>> getSumGrabTagsById(Long userId, String queryDate) {
        String startDate = queryDate+"-01 00:00:01";
        String endDate = currentFinalDay(queryDate) ;
        return userAccountRedpacketRecordMapper.getSumGrabTagsById(startDate,endDate,userId);
    }

    @Override
    public ResultMsg<Object> sendGiftForKnapsack(Long userId, String toUserIds, Integer giftId, Integer giftNum, String desc, Integer giveChannel, Long roomId) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        String[] toUserIdArray = toUserIds.split(",");

        if (Arrays.asList(toUserIdArray).contains(userId.toString())){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_NOT_SELF.getValue(),ErrorMsgEnum.REDPACKET_IS_NOT_SELF.getDesc());
        }

        FuntimeUser user = getUserById(userId);

        FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (funtimeGift==null){
            throw new BusinessException(ErrorMsgEnum.GIFT_NOT_EXISTS.getValue(),ErrorMsgEnum.GIFT_NOT_EXISTS.getDesc());
        }

        Integer userRole = roomService.getUserRole(roomId,userId);

        userRole = userRole == null?4:userRole;

        Integer amount= funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice().intValue()*giftNum:funtimeGift.getActivityPrice().intValue()*giftNum;
        Integer total = amount*toUserIdArray.length;
        Integer itemNum = userAccountMapper.getItemNumByUserId(userId, giftId, 1);
        //背包礼物不足
        if (itemNum<giftNum*toUserIdArray.length){
            resultMsg.setCode(ErrorMsgEnum.USER_BAG_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_BAG_NOT_EN.getDesc());
            return resultMsg;
        }


        int k = userAccountMapper.updateUserKnapsackSub(userAccountMapper.checkUserKnapsackExist(userId, giftId, 1),giftNum*toUserIdArray.length);

        if (k!=1){
            resultMsg.setCode(ErrorMsgEnum.DATA_ORER_ERROR.getValue());
            resultMsg.setMsg(ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            return resultMsg;
        }
        String noticeAmount = parameterService.getParameterValueByKey("gift_notice_amount");
        String giftHornLength = parameterService.getParameterValueByKey("gift_horn_length");
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");
        BigDecimal black = new BigDecimal(blue_to_black).multiply(new BigDecimal(amount)).setScale(2, RoundingMode.DOWN);
        for (String toUserIdStr : toUserIdArray) {
            Long toUserId = Long.valueOf(toUserIdStr);
            FuntimeUser toUser = userService.queryUserById(toUserId);
            if (toUser==null){
                throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
            }
            Long recordId = saveFuntimeUserAccountGifttransRecord(userId, desc, new BigDecimal(amount)
                    , giftNum, giftId, funtimeGift.getGiftName(), toUserId, giveChannel,roomId,OperationType.GIVEGIFTBAG.getOperationType());

            Integer charmVal = new BigDecimal(blue_to_charm).multiply(new BigDecimal(amount)).intValue();
            //用户收加上黑钻,魅力值
            userService.updateUserAccountForPlusGift(toUserId, black, giftNum,charmVal);
            saveUserAccountCharmRecord(toUserId,charmVal,recordId,1);
            //用户送的日志
            saveUserAccountBlueLog(userId, new BigDecimal(amount), recordId
                    , OperationType.GIVEGIFTBAG.getAction(), OperationType.GIVEGIFTBAG.getOperationType());
            //用户收的日志
            saveUserAccountBlackLog(toUserId, black, recordId, OperationType.RECEIVEBAGGIFT.getAction()
                    , OperationType.RECEIVEBAGGIFT.getOperationType());

            if (giveChannel.equals(GiveChannel.ROOM.getValue())) {

                RoomGiftNotice notice = new RoomGiftNotice();
                notice.setCount(giftNum);
                notice.setSpecialEffect(funtimeGift.getSpecialEffect());
                notice.setGiftName(funtimeGift.getGiftName());
                notice.setFromImg(user.getPortraitAddress());
                notice.setFromName(user.getNickname());
                notice.setFromUid(String.valueOf(userId));
                notice.setGid(String.valueOf(giftId));
                notice.setGiftImg(funtimeGift.getAnimationUrl());
                notice.setRid(String.valueOf(roomId));
                notice.setToImg(toUser.getPortraitAddress());
                notice.setToName(toUser.getNickname());
                notice.setToUid(String.valueOf(toUserId));
                notice.setFromSex(user.getSex());
                notice.setToSex(toUser.getSex());
                notice.setUserRole(userRole);
                int type = funtimeGift.getSpecialEffect();
                if (type == SpecialEffectType.E_4.getValue()) {
                    type = SpecialEffectType.E_3.getValue();
                    notice.setType(Constant.ROOM_GIFT_SEND_ROOM_ALL);
                    //发送通知全服
                    notice.setSpecialEffect(type);
                    noticeService.notice21(notice);

                }
                List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
                if (userIds == null || userIds.isEmpty()) {
                    throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
                }
                notice.setSpecialEffect(type);
                notice.setType(Constant.ROOM_GIFT_SEND);
                //发送通知
                noticeService.notice8(notice, userIds);
                if (noticeAmount!=null){
                    if (total>=new BigDecimal(noticeAmount).intValue()){
                        noticeService.notice10002("送给"+toUser.getNickname(),userId,roomId,user.getNickname(),user.getSex(),user.getPortraitAddress(),funtimeGift.getGiftName(),giftNum,giftHornLength);
                    }
                }
            }
        }
        resultMsg.setData(JsonUtil.getMap("giftNum",itemNum-giftNum));
        if (roomId!=null) {
            roomService.updateHotsPlus(roomId, new BigDecimal(total).divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        return resultMsg;

    }

    @Override
    public void setCarTask() {
        List<Map<String,Object>> list = carMapper.getCarInfoForExpire();
        FuntimeUser user;
        if (list!=null&&!list.isEmpty()){
            for (Map<String,Object> map : list){
                Long id = Long.parseLong(map.get("id").toString());
                Long userId = Long.parseLong(map.get("userId").toString());
                Integer carId = Integer.parseInt(map.get("carId").toString());
                carMapper.deleteUserCarById(id);

                user = userService.queryUserById(userId);
                if (user == null){
                    continue;
                }

                if (user.getCarId()==null||user.getCarId().equals(carId)){
                    Integer carId2 = carMapper.getUserCarIdByUserId(userId);
                    userService.updateUserCar(userId,carId2);
                }
            }
        }
    }

    @Override
    public List<Map<String, Object>> getUserCarByUserId(Long userId) {
        return carMapper.getUserCarByUserId(userId);
    }

    @Override
    public List<Map<String, Object>> getCarList(Long userId) {
        FuntimeUser user = getUserById(userId);
        List<Map<String, Object>> carList = carMapper.getCarList(userId);
        if (carList!=null){
            for (Map<String, Object> map : carList) {
                Integer carNumber = Integer.parseInt(map.get("carNumber").toString());
                if (user.getCarId()!=null&&user.getCarId().equals(carNumber)){
                    map.put("isCurrent",1);
                }
                map.put("priceTag",carMapper.getPriceTagByCarNumber(carNumber));
            }
        }
        return carList;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> buyCar(Long userId, Integer id) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);
        if (userAccount==null){
            resultMsg.setCode(ErrorMsgEnum.USER_NOT_EXISTS.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
            return resultMsg;
        }
        Map<String, Object> carInfoMap = getCarInfoById(id);
        if (carInfoMap == null){
            resultMsg.setCode(ErrorMsgEnum.PARAMETER_ERROR.getValue());
            resultMsg.setMsg(ErrorMsgEnum.PARAMETER_ERROR.getDesc());
            return resultMsg;
        }
        BigDecimal price = new BigDecimal(carInfoMap.get("price").toString());
        if (userAccount.getBlueDiamond().subtract(price).doubleValue()<0){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            Map<String, Object> map = new HashMap<>();
            map.put("userBlueAmount",userAccount.getBlueDiamond().intValue());
            map.put("price",price.intValue());
            resultMsg.setData(map);
            return resultMsg;
        }


        FuntimeUserAccountCarRecord record = new FuntimeUserAccountCarRecord();
        record.setCarId(Integer.parseInt(carInfoMap.get("carId").toString()));
        record.setDays(Integer.parseInt(carInfoMap.get("days").toString()));
        record.setPrice(new BigDecimal(carInfoMap.get("price").toString()));
        record.setUserId(userId);

        int k = carMapper.insertCarRecord(record);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        userService.updateUserAccountForSub(userId,null,price,null);
        saveUserAccountBlueLog(userId,price,record.getId(),OperationType.BUY_CAR.getAction(),OperationType.BUY_CAR.getOperationType());
        Long userCarId = carMapper.getUserCarById(userId, Integer.parseInt(carInfoMap.get("carId").toString()));

        carInfoMap.put("userId",userId);
        if (userCarId == null){
            k = carMapper.insertUserCar(carInfoMap);
            if (k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
        }else{
            carInfoMap.put("userCarId",userCarId);
            k = carMapper.updateUserCar(carInfoMap);
            if (k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
        }
        userService.updateUserCar(userId,Integer.parseInt(carInfoMap.get("carId").toString()));
        resultMsg.setData(JsonUtil.getMap("content","剩余"+record.getDays()+"天"));
        return resultMsg;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void setCar(Long userId, Integer carId) {
        Long userCarId = carMapper.getUserCarById(userId, carId);
        if (userCarId == null){
            throw new BusinessException(ErrorMsgEnum.USER_CAR_NOT_EXIST.getValue(),ErrorMsgEnum.USER_CAR_NOT_EXIST.getDesc());
        }
        checkUser(userId);
        userService.updateUserCar(userId,carId);
    }

    @Override
    public List<Map<String, Object>> getLevelConf(Long userId) {
        return userAccountMapper.getLevelConf();
    }

    @Override
    public Map<String, Object> getGoldConvertConf(Long userId) {
        Map<String, Object> resultMap = new HashMap<>();
        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);
        resultMap.put("userGoldAmount",userAccount.getGoldCoin().intValue());
        resultMap.put("userBlueAmount",userAccount.getBlueDiamond());
        resultMap.put("conf",userConvertRecordMapper.getGoldConvertConf(null));
        return resultMap;
    }

    @Override
    public void goldConvert(Long userId, Integer id) {
        List<Map<String,Object>> confs = userConvertRecordMapper.getGoldConvertConf(id);
        if (confs == null || confs.isEmpty()){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
        }
        Map<String,Object> conf = confs.get(0);
        Integer blueAmount = Integer.parseInt(conf.get("blueAmount").toString());
        Integer goldAmount = Integer.parseInt(conf.get("goldAmount").toString());
        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);
        if (userAccount.getBlueDiamond().intValue()-blueAmount<0){
            throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
        }
        BigDecimal convertRatio = new BigDecimal(goldAmount/blueAmount).setScale(2,BigDecimal.ROUND_HALF_UP);
        Long recordId = saveFuntimeUserConvertRecord(userId,convertRatio,ConvertType.BLUE_GOLD.getValue(),new BigDecimal(blueAmount),new BigDecimal(goldAmount));

        int k = userAccountMapper.updateUserAccountGoldConvert(userId,goldAmount,new BigDecimal(blueAmount));

        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        //用户减的日志
        saveUserAccountBlueLog(userId,new BigDecimal(blueAmount),recordId
                ,OperationType.GOLD_CONVERT_OUT.getAction(),OperationType.GOLD_CONVERT_OUT.getOperationType());

        //用户加的日志
        saveUserAccountGoldLog(userId,new BigDecimal(goldAmount),recordId,OperationType.GOLD_CONVERT_IN.getAction()
                ,OperationType.GOLD_CONVERT_IN.getOperationType());

    }

    @Override
    public List<Map<String,Object>> getBoxList() {
        return boxMapper.getBoxList();
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> createGiftTrans(Long userId, String toUserIds, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannelId, Long roomId) {

        ResultMsg<Object> resultMsg = new ResultMsg<>();

        String[] toUserIdArray = toUserIds.split(",");

        if (Arrays.asList(toUserIdArray).contains(userId.toString())){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_NOT_SELF.getValue(),ErrorMsgEnum.REDPACKET_IS_NOT_SELF.getDesc());
        }

        FuntimeUser user = getUserById(userId);

        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);

        FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (funtimeGift==null){
            throw new BusinessException(ErrorMsgEnum.GIFT_NOT_EXISTS.getValue(),ErrorMsgEnum.GIFT_NOT_EXISTS.getDesc());
        }

        Integer userRole = roomService.getUserRole(roomId,userId);

        userRole = userRole == null?4:userRole;

        BigDecimal price = funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice():funtimeGift.getActivityPrice();

        Integer amount= price.intValue()*giftNum;
        Integer total = amount*toUserIdArray.length;
        //账户余额不足
        if (userAccount.getBlueDiamond().intValue()<total){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            resultMsg.setData(JsonUtil.getMap("amount",total));
            return resultMsg;
        }
        //用户送减去蓝钻
        userService.updateUserAccountForSub(userId, null, new BigDecimal(total), null);
        String noticeAmount = parameterService.getParameterValueByKey("gift_notice_amount");
        String giftHornLength = parameterService.getParameterValueByKey("gift_horn_length");
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");
        BigDecimal black = new BigDecimal(blue_to_black).multiply(new BigDecimal(amount)).setScale(2, RoundingMode.DOWN);
        for (String toUserIdStr : toUserIdArray) {
            Long toUserId = Long.valueOf(toUserIdStr);
            FuntimeUser toUser = userService.queryUserById(toUserId);
            if (toUser==null){
                throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
            }
            Long recordId = saveFuntimeUserAccountGifttransRecord(userId, operationDesc, new BigDecimal(amount)
                    , giftNum, giftId, funtimeGift.getGiftName(), toUserId, giveChannelId, roomId, OperationType.GIVEGIFT.getOperationType());

            Integer charmVal = new BigDecimal(blue_to_charm).multiply(new BigDecimal(amount)).intValue();
            //用户收加上黑钻,魅力值
            userService.updateUserAccountForPlusGift(toUserId, black, giftNum,charmVal);
            saveUserAccountCharmRecord(toUserId,charmVal,recordId,1);
            //用户送的日志
            saveUserAccountBlueLog(userId, new BigDecimal(amount), recordId
                    , OperationType.GIVEGIFT.getAction(), OperationType.GIVEGIFT.getOperationType());

            //用户收的日志
            saveUserAccountBlackLog(toUserId, black, recordId, OperationType.RECEIVEGIFT.getAction()
                    , OperationType.RECEIVEGIFT.getOperationType());

            if (giveChannelId.equals(GiveChannel.ROOM.getValue())) {

                RoomGiftNotice notice = new RoomGiftNotice();
                notice.setCount(giftNum);
                notice.setSpecialEffect(funtimeGift.getSpecialEffect());
                notice.setGiftName(funtimeGift.getGiftName());
                notice.setFromImg(user.getPortraitAddress());
                notice.setFromName(user.getNickname());
                notice.setFromUid(String.valueOf(userId));
                notice.setGid(String.valueOf(giftId));
                notice.setGiftImg(funtimeGift.getAnimationUrl());
                notice.setRid(String.valueOf(roomId));
                notice.setToImg(toUser.getPortraitAddress());
                notice.setToName(toUser.getNickname());
                notice.setToUid(String.valueOf(toUserId));
                notice.setFromSex(user.getSex());
                notice.setToSex(toUser.getSex());
                notice.setUserRole(userRole);
                int type = funtimeGift.getSpecialEffect();
                if (type == SpecialEffectType.E_4.getValue()) {
                    type = SpecialEffectType.E_3.getValue();
                    notice.setType(Constant.ROOM_GIFT_SEND_ROOM_ALL);
                    //发送通知全服
                    notice.setSpecialEffect(type);
                    noticeService.notice21(notice);

                }
                List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
                if (userIds == null || userIds.isEmpty()) {
                    throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
                }
                notice.setSpecialEffect(type);
                notice.setType(Constant.ROOM_GIFT_SEND);
                //发送通知
                noticeService.notice8(notice, userIds);
                if (noticeAmount!=null){
                    if (total>=new BigDecimal(noticeAmount).intValue()){
                        noticeService.notice10002("送给"+toUser.getNickname(),userId,roomId,user.getNickname(),user.getSex(),user.getPortraitAddress(),funtimeGift.getGiftName(),giftNum,giftHornLength);
                    }
                }
            }
        }
        if (roomId!=null) {
            roomService.updateHotsPlus(roomId, new BigDecimal(total).divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        return resultMsg;
    }

    private Map<Long,Map<Integer,Integer>> getDrawId(Integer boxNumber,String[] userIds,Integer num){
        List<FuntimeBoxConf> list = boxMapper.getBoxConfByBoxNumber(boxNumber);
        int probabilityTotal = 1;
        Map<String, FuntimeBoxConf> probabilityMap = new HashMap<>();
        for (FuntimeBoxConf boxConf : list) {
            int temp = probabilityTotal;
            probabilityTotal += boxConf.getProbability();
            probabilityMap.put(temp + "-" + probabilityTotal, boxConf);

        }
        Map<Long,Map<Integer,Integer>> userGiftMap = new HashMap<>();
        Map<Integer,Integer> giftToNumMap ;
        for (String userId : userIds){
            giftToNumMap = new HashMap<>();
            for (int i = 0 ;i<num ;i++) {
                int random = RandomUtils.nextInt(1, probabilityTotal);
                FuntimeBoxConf conf = getBoxConf(probabilityMap, random);
                Integer drawId = conf.getDrawId();

                giftToNumMap.put(drawId, giftToNumMap.get(drawId)==null?1:giftToNumMap.get(drawId)+1);

            }
            userGiftMap.put(Long.parseLong(userId),giftToNumMap);
        }

        return userGiftMap;
    }
    private Map<Long,Map<Integer,Integer>> getDrawId(Integer boxNumber,List<Long> userIds,Integer num){
        List<FuntimeBoxConf> list = boxMapper.getBoxConfByBoxNumber(boxNumber);
        int probabilityTotal = 1;
        Map<String, FuntimeBoxConf> probabilityMap = new HashMap<>();
        for (FuntimeBoxConf boxConf : list) {
            int temp = probabilityTotal;
            probabilityTotal += boxConf.getProbability();
            probabilityMap.put(temp + "-" + probabilityTotal, boxConf);

        }
        Map<Long,Map<Integer,Integer>> userGiftMap = new HashMap<>();
        Map<Integer,Integer> giftToNumMap ;
        for (Long userId : userIds){
            giftToNumMap = new HashMap<>();
            for (int i = 0 ;i<num ;i++) {
                int random = RandomUtils.nextInt(1, probabilityTotal);
                FuntimeBoxConf conf = getBoxConf(probabilityMap, random);
                Integer drawId = conf.getDrawId();

                giftToNumMap.put(drawId, giftToNumMap.get(drawId)==null?1:giftToNumMap.get(drawId)+1);

            }
            userGiftMap.put(userId,giftToNumMap);
        }

        return userGiftMap;
    }
    private FuntimeBoxConf getBoxConf(Map<String,FuntimeBoxConf> map,int random){
        for (Map.Entry<String,FuntimeBoxConf> entry : map.entrySet()){
            String key = entry.getKey();
            String[] array = key.split("-");
            int key1 = Integer.parseInt(array[0]);
            int key2 = Integer.parseInt(array[1]);
            if (random>=key1&&random<key2){
                return entry.getValue();
            }
        }
        return null;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> sendGiftForBox(Long userId, String toUserIds, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannelId, Long roomId) {

        ResultMsg<Object> resultMsg = new ResultMsg<>();

        String[] toUserIdArray = toUserIds.split(",");

        if (Arrays.asList(toUserIdArray).contains(userId.toString())){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_NOT_SELF.getValue(),ErrorMsgEnum.REDPACKET_IS_NOT_SELF.getDesc());
        }

        FuntimeUser user = getUserById(userId);

        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);

        FuntimeBox box = boxMapper.getBoxInfoByBoxNumber(giftId);
        //FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (box==null){
            throw new BusinessException(ErrorMsgEnum.BOX_NOT_EXISTS.getValue(),ErrorMsgEnum.BOX_NOT_EXISTS.getDesc());
        }

        Integer userRole = roomService.getUserRole(roomId,userId);

        userRole = userRole == null?4:userRole;

        //BigDecimal price = funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice():funtimeGift.getActivityPrice();
        BigDecimal price = box.getPrice();

        Integer amount= price.intValue()*giftNum;
        Integer total = amount*toUserIdArray.length;
        //账户余额不足
        if (userAccount.getBlueDiamond().intValue()<total){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            resultMsg.setData(JsonUtil.getMap("amount",total));
            return resultMsg;
        }
        //用户送减去蓝钻
        userService.updateUserAccountForSub(userId, null, new BigDecimal(total), null);
        String noticeAmount = parameterService.getParameterValueByKey("gift_notice_amount");
        String giftHornLength = parameterService.getParameterValueByKey("gift_horn_length");
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");

        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);

        List<Map<String,Object>> noticeDatas = new ArrayList<>();
        Map<String,Object> noticeData;
        Map<Long,Map<Integer,Integer>> randomGiftsMap = getDrawId(giftId,toUserIdArray,giftNum);
        for (Map.Entry<Long,Map<Integer,Integer>> entry : randomGiftsMap.entrySet()){
            Long toUserId = entry.getKey();
            FuntimeUser toUser = userService.queryUserById(toUserId);
            if (toUser==null){
                throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
            }

            for (Map.Entry<Integer,Integer> entry2 : entry.getValue().entrySet()) {
                noticeData = new HashMap<>();
                Integer toGiftId = entry2.getKey();
                Integer num = entry2.getValue();
                FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(entry2.getKey());
                if (funtimeGift == null){
                    throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
                }
                BigDecimal giftPrice = funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice():funtimeGift.getActivityPrice();
                BigDecimal giftAmount = giftPrice.multiply(new BigDecimal(num).setScale(2,BigDecimal.ROUND_HALF_DOWN));
                Long recordId = saveFuntimeUserAccountGifttransRecord(userId, operationDesc, giftAmount
                        , num, toGiftId, funtimeGift.getGiftName(), toUserId, giveChannelId, roomId, OperationType.GIFT_BOX_OUT.getOperationType(),box.getPrice().intValue());

                Integer charmVal = new BigDecimal(blue_to_charm).multiply(giftAmount).intValue();
                BigDecimal black = new BigDecimal(blue_to_black).multiply(giftAmount).setScale(2, RoundingMode.DOWN);
                //用户收加上黑钻,魅力值
                userService.updateUserAccountForPlusGift(toUserId, black, num, charmVal);
                saveUserAccountCharmRecord(toUserId, charmVal, recordId, 1);
                //用户送的日志
                saveUserAccountBlueLog(userId, new BigDecimal(num).multiply(price).setScale(2,BigDecimal.ROUND_HALF_DOWN), recordId
                        , OperationType.GIFT_BOX_OUT.getAction(), OperationType.GIFT_BOX_OUT.getOperationType());

                //用户收的日志
                saveUserAccountBlackLog(toUserId, black, recordId, OperationType.GIFT_BOX_IN.getAction()
                        , OperationType.GIFT_BOX_IN.getOperationType());

                String msg = "送给"+toUser.getNickname()+num+"个"+funtimeGift.getGiftName()+"("+box.getBoxName()+")";

                noticeData.put("giftImg",funtimeGift.getImageUrl());
                noticeData.put("giftName",funtimeGift.getGiftName());
                noticeData.put("giftNum",num);
                noticeData.put("userImage",toUser.getPortraitAddress());
                noticeDatas.add(noticeData);

                msg = "<font color='#FFDE00'>"+msg+"</font>";
                noticeService.notice11Or14(userId,null,msg,roomId,Constant.ROOM_MSG_NORMAL,userIds,userRole,0);

                if (noticeAmount!=null){
                    if (total>=new BigDecimal(noticeAmount).intValue()){
                        noticeService.notice10002("送给"+toUser.getNickname(),userId,roomId,user.getNickname(),user.getSex(),user.getPortraitAddress(),funtimeGift.getGiftName()+"("+box.getBoxName()+")",num,giftHornLength);
                    }
                }
            }

        }

        JSONObject noticeMap = new JSONObject();
        noticeMap.put("list",noticeDatas);
        noticeMap.put("boxUrl",box.getAnimationUrl());

        noticeService.notice39(noticeMap,userIds);
        if (roomId!=null) {
            roomService.updateHotsPlus(roomId, new BigDecimal(total).divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        return resultMsg;
    }

    public ResultMsg<Object> createGiftTrans(Long userId, Long toUserId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannelId, Long roomId) {

        ResultMsg<Object> resultMsg = new ResultMsg<>();

        FuntimeUser user = userService.queryUserById(userId);
        FuntimeUser toUser = userService.queryUserById(toUserId);
        if (user==null||toUser==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (userId.equals(toUserId)){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_NOT_SELF.getValue(),ErrorMsgEnum.REDPACKET_IS_NOT_SELF.getDesc());
        }
        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);

        FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (funtimeGift==null){
            throw new BusinessException(ErrorMsgEnum.GIFT_NOT_EXISTS.getValue(),ErrorMsgEnum.GIFT_NOT_EXISTS.getDesc());
        }
        BigDecimal price = funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice():funtimeGift.getActivityPrice();

        Integer amount= price.intValue()*giftNum;
        //账户余额不足
        if (userAccount.getBlueDiamond().intValue()<amount){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            resultMsg.setData(JsonUtil.getMap("amount",amount));
            return resultMsg;
        }

        Integer userRole = roomService.getUserRole(roomId,userId);

        userRole = userRole == null?4:userRole;
        String noticeAmount = parameterService.getParameterValueByKey("gift_notice_amount");
        String giftHornLength = parameterService.getParameterValueByKey("gift_horn_length");
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");

        Long recordId = saveFuntimeUserAccountGifttransRecord(userId, operationDesc, new BigDecimal(amount)
                , giftNum, giftId, funtimeGift.getGiftName(), toUserId, giveChannelId, roomId, OperationType.GIVEGIFTREDPACKET.getOperationType());

        BigDecimal black = new BigDecimal(blue_to_black).multiply(new BigDecimal(amount)).setScale(2, RoundingMode.DOWN);

        //用户送减去蓝钻
        userService.updateUserAccountForSub(userId, null, new BigDecimal(amount), null);
        Integer charmVal = new BigDecimal(blue_to_charm).multiply(new BigDecimal(amount)).intValue();
        //用户收加上黑钻
        userService.updateUserAccountForPlusGift(toUserId, black, giftNum, charmVal);
        saveUserAccountCharmRecord(userId,charmVal,recordId,1);
        //用户送的日志
        saveUserAccountBlueLog(userId, new BigDecimal(amount), recordId
                , OperationType.GIVEGIFTREDPACKET.getAction(), OperationType.GIVEGIFTREDPACKET.getOperationType());

        //用户收的日志
        saveUserAccountBlackLog(toUserId, black, recordId, OperationType.RECEIVEGIFTREDPACKET.getAction()
                , OperationType.RECEIVEGIFTREDPACKET.getOperationType());

        if (roomId != null) {

            RoomGiftNotice notice = new RoomGiftNotice();
            notice.setCount(giftNum);
            notice.setSpecialEffect(funtimeGift.getSpecialEffect());
            notice.setGiftName(funtimeGift.getGiftName());
            notice.setFromImg(user.getPortraitAddress());
            notice.setFromName(user.getNickname());
            notice.setFromUid(String.valueOf(userId));
            notice.setGid(String.valueOf(giftId));
            notice.setGiftImg(funtimeGift.getAnimationUrl());
            notice.setRid(String.valueOf(roomId));
            notice.setToImg(toUser.getPortraitAddress());
            notice.setToName(toUser.getNickname());
            notice.setToUid(String.valueOf(toUserId));
            notice.setFromSex(user.getSex());
            notice.setToSex(toUser.getSex());
            notice.setUserRole(userRole);
            int type = funtimeGift.getSpecialEffect();
            if (type == SpecialEffectType.E_4.getValue()) {
                type = SpecialEffectType.E_3.getValue();
                notice.setType(Constant.ROOM_GIFT_SEND_ROOM_ALL);
                //发送通知全服
                notice.setSpecialEffect(type);
                noticeService.notice21(notice);

            }
            List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
            if (userIds == null || userIds.isEmpty()) {
                throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
            }
            notice.setSpecialEffect(type);
            notice.setType(Constant.ROOM_GIFT_SEND);
            //发送通知
            noticeService.notice8(notice, userIds);
            if (noticeAmount!=null){
                if (amount>=new BigDecimal(noticeAmount).intValue()){
                    noticeService.notice10002("送给"+toUser.getNickname(),userId,roomId,user.getNickname(),user.getSex(),user.getPortraitAddress(),funtimeGift.getGiftName(),giftNum, giftHornLength);
                }
            }
        }
        if (roomId!=null) {
            roomService.updateHotsPlus(roomId, new BigDecimal(amount).divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        resultMsg.setData(recordId);
        return resultMsg;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void createGiftTrans(Long userId, Long toUserId, Integer giftId, Integer giftNum) {

        FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (funtimeGift==null){
            throw new BusinessException(ErrorMsgEnum.GIFT_NOT_EXISTS.getValue(),ErrorMsgEnum.GIFT_NOT_EXISTS.getDesc());
        }
        BigDecimal price = funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice():funtimeGift.getActivityPrice();

        Integer amount= price.intValue()*giftNum;
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");
        Long recordId = saveFuntimeUserAccountGifttransRecord(userId, "送礼物-活动赠送", new BigDecimal(amount)
                , giftNum, giftId, funtimeGift.getGiftName(), toUserId, 3, null, OperationType.GIVEGIFTACTIVITY.getOperationType());

        BigDecimal black = new BigDecimal(blue_to_black).multiply(new BigDecimal(amount)).setScale(2, RoundingMode.DOWN);

        //用户送减去蓝钻
        //userService.updateUserAccountForSub(userId, null, new BigDecimal(amount), null);
        Integer charmVal = new BigDecimal(blue_to_charm).multiply(new BigDecimal(amount)).intValue();
        //用户收加上黑钻
        userService.updateUserAccountForPlusGift(toUserId, black, giftNum, charmVal);
        //saveUserAccountCharmRecord(userId,charmVal,recordId,1);
        //用户送的日志
        saveUserAccountBlueLog(userId, new BigDecimal(amount), recordId
                , OperationType.GIVEGIFTACTIVITY.getAction(), OperationType.GIVEGIFTACTIVITY.getOperationType());

        //用户收的日志
        saveUserAccountBlackLog(toUserId, black, recordId, OperationType.RECEIVEGIFTACTIVITY.getAction()
                , OperationType.RECEIVEGIFTACTIVITY.getOperationType());

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> sendGiftForRoom(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId) {

        ResultMsg<Object> resultMsg = new ResultMsg<>();

        FuntimeUser user = getUserById(userId);

        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);

        FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (funtimeGift==null){
            throw new BusinessException(ErrorMsgEnum.GIFT_NOT_EXISTS.getValue(),ErrorMsgEnum.GIFT_NOT_EXISTS.getDesc());
        }

        FuntimeChatroom chatroom = roomService.getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<Long> toUserIdArray = roomService.getRoomUserByRoomId(roomId,userId);
        if (toUserIdArray==null||toUserIdArray.isEmpty()){
            if (userId.equals(chatroom.getUserId())) {
                throw new BusinessException(ErrorMsgEnum.ROOM_USER_IS_EMPTY.getValue(), ErrorMsgEnum.ROOM_USER_IS_EMPTY.getDesc());
            }else{
                toUserIdArray = new ArrayList<>(1);
                toUserIdArray.add(chatroom.getUserId());
            }
        }

        int userNum = toUserIdArray.size();

        BigDecimal price = funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice():funtimeGift.getActivityPrice();

        Integer amount= price.intValue()*giftNum;
        //账户余额不足
        if (userAccount.getBlueDiamond().intValue()<amount*userNum){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            resultMsg.setData(JsonUtil.getMap("amount",amount*userNum));
            return resultMsg;
        }
        //用户送减去蓝钻
        userService.updateUserAccountForSub(userId, null, new BigDecimal(amount*userNum), null);

        Integer userRole = roomService.getUserRole(roomId,userId);

        userRole = userRole == null?4:userRole;
        String noticeAmount = parameterService.getParameterValueByKey("gift_notice_amount");
        String giftHornLength = parameterService.getParameterValueByKey("gift_horn_length");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        BigDecimal black = new BigDecimal(blue_to_black).multiply(new BigDecimal(amount)).setScale(2, RoundingMode.DOWN);
        for (Long toUserId : toUserIdArray) {

            Long recordId = saveFuntimeUserAccountGifttransRecord(userId, operationDesc, new BigDecimal(amount)
                    , giftNum, giftId, funtimeGift.getGiftName(), toUserId, giveChannel, roomId, OperationType.GIVEGIFT.getOperationType());

            Integer charmVal = new BigDecimal(blue_to_charm).multiply(new BigDecimal(amount)).intValue();
            //用户收加上黑钻
            userService.updateUserAccountForPlusGift(toUserId, black, giftNum, charmVal);
            saveUserAccountCharmRecord(toUserId,charmVal,recordId,1);
            //用户送的日志
            saveUserAccountBlueLog(userId, new BigDecimal(amount), recordId
                    , OperationType.GIVEGIFT.getAction(), OperationType.GIVEGIFT.getOperationType());

            //用户收的日志
            saveUserAccountBlackLog(toUserId, black, recordId, OperationType.RECEIVEGIFT.getAction()
                    , OperationType.RECEIVEGIFT.getOperationType());

        }



        RoomGiftNotice notice = new RoomGiftNotice();
        notice.setCount(giftNum);

        notice.setGiftName(funtimeGift.getGiftName());
        notice.setFromImg(user.getPortraitAddress());
        notice.setFromName(user.getNickname());
        notice.setFromUid(String.valueOf(userId));
        notice.setGid(String.valueOf(giftId));
        notice.setGiftImg(funtimeGift.getAnimationUrl());
        notice.setRid(String.valueOf(roomId));
        notice.setToImg(chatroom.getAvatarUrl());
        notice.setToName("全房");
        notice.setFromSex(user.getSex());
        notice.setUserRole(userRole);
        int type = funtimeGift.getSpecialEffect();
        if (type == SpecialEffectType.E_4.getValue()) {
            type = SpecialEffectType.E_3.getValue();
            notice.setType(Constant.ROOM_GIFT_SEND_ROOM_ALL);
            //发送通知全服
            notice.setSpecialEffect(type);
            noticeService.notice21(notice);

        }
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        notice.setSpecialEffect(type);
        notice.setType(Constant.ROOM_GIFT_SEND_ROOM);
        //发送通知

        noticeService.notice19(notice, userIds);
        if (noticeAmount!=null) {
            if (amount * userNum >= new BigDecimal(noticeAmount).intValue()) {
                for (Long toUserId : toUserIdArray) {
                    FuntimeUser toUser = getUserById(toUserId);
                    noticeService.notice10002("送给"+toUser.getNickname(),userId,roomId,user.getNickname(),user.getSex(),user.getPortraitAddress(),funtimeGift.getGiftName(),giftNum, giftHornLength);
                }
            }
        }
        if (roomId!=null) {
            roomService.updateHotsPlus(roomId, new BigDecimal(amount * userNum).divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        return resultMsg;

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> sendGiftForRoom2(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId) {

        ResultMsg<Object> resultMsg = new ResultMsg<>();

        FuntimeUser user = getUserById(userId);

        FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (funtimeGift==null){
            throw new BusinessException(ErrorMsgEnum.GIFT_NOT_EXISTS.getValue(),ErrorMsgEnum.GIFT_NOT_EXISTS.getDesc());
        }

        FuntimeChatroom chatroom = roomService.getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<Long> toUserIdArray = roomService.getRoomUserByRoomId(roomId,userId);
        if (toUserIdArray==null||toUserIdArray.isEmpty()){
            if (userId.equals(chatroom.getUserId())) {
                throw new BusinessException(ErrorMsgEnum.ROOM_USER_IS_EMPTY.getValue(), ErrorMsgEnum.ROOM_USER_IS_EMPTY.getDesc());
            }else{
                toUserIdArray = new ArrayList<>(1);
                toUserIdArray.add(chatroom.getUserId());
            }
        }

        int userNum = toUserIdArray.size();

        BigDecimal price = funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice():funtimeGift.getActivityPrice();

        Integer amount= price.intValue()*giftNum;
        Integer itemNum = userAccountMapper.getItemNumByUserId(userId, giftId, 1);
        //背包礼物不足
        if (itemNum<giftNum*userNum){
            resultMsg.setCode(ErrorMsgEnum.USER_BAG_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_BAG_NOT_EN.getDesc());
            return resultMsg;
        }
        int k = userAccountMapper.updateUserKnapsackSub(userAccountMapper.checkUserKnapsackExist(userId, giftId, 1),giftNum*userNum);
        if (k!=1){
            resultMsg.setCode(ErrorMsgEnum.DATA_ORER_ERROR.getValue());
            resultMsg.setMsg(ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            return resultMsg;
        }
        Integer userRole = roomService.getUserRole(roomId,userId);

        userRole = userRole == null?4:userRole;
        String noticeAmount = parameterService.getParameterValueByKey("gift_notice_amount");
        String giftHornLength = parameterService.getParameterValueByKey("gift_horn_length");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        BigDecimal black = new BigDecimal(blue_to_black).multiply(new BigDecimal(amount)).setScale(2, RoundingMode.DOWN);
        for (Long toUserId : toUserIdArray) {

            Long recordId = saveFuntimeUserAccountGifttransRecord(userId, operationDesc, new BigDecimal(amount)
                    , giftNum, giftId, funtimeGift.getGiftName(), toUserId, giveChannel, roomId, OperationType.GIVEGIFTBAG.getOperationType());

            Integer charmVal = new BigDecimal(blue_to_charm).multiply(new BigDecimal(amount)).intValue();
            //用户收加上黑钻
            userService.updateUserAccountForPlusGift(toUserId, black, giftNum, charmVal);
            saveUserAccountCharmRecord(toUserId,charmVal,recordId,1);
            //用户送的日志
            saveUserAccountBlueLog(userId, new BigDecimal(amount), recordId
                    , OperationType.GIVEGIFTBAG.getAction(), OperationType.GIVEGIFTBAG.getOperationType());

            //用户收的日志
            saveUserAccountBlackLog(toUserId, black, recordId, OperationType.RECEIVEBAGGIFT.getAction()
                    , OperationType.RECEIVEBAGGIFT.getOperationType());

        }

        RoomGiftNotice notice = new RoomGiftNotice();
        notice.setCount(giftNum);

        notice.setGiftName(funtimeGift.getGiftName());
        notice.setFromImg(user.getPortraitAddress());
        notice.setFromName(user.getNickname());
        notice.setFromUid(String.valueOf(userId));
        notice.setGid(String.valueOf(giftId));
        notice.setGiftImg(funtimeGift.getAnimationUrl());
        notice.setRid(String.valueOf(roomId));
        notice.setToImg(chatroom.getAvatarUrl());
        notice.setToName("全房");
        notice.setFromSex(user.getSex());
        notice.setUserRole(userRole);
        int type = funtimeGift.getSpecialEffect();
        if (type == SpecialEffectType.E_4.getValue()) {
            type = SpecialEffectType.E_3.getValue();
            notice.setType(Constant.ROOM_GIFT_SEND_ROOM_ALL);
            //发送通知全服
            notice.setSpecialEffect(type);
            noticeService.notice21(notice);

        }
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        notice.setSpecialEffect(type);
        notice.setType(Constant.ROOM_GIFT_SEND_ROOM);
        //发送通知

        noticeService.notice19(notice, userIds);

        if (noticeAmount!=null) {
            if (amount * userNum >= new BigDecimal(noticeAmount).intValue()) {
                for (Long toUserId : toUserIdArray) {
                    FuntimeUser toUser = getUserById(toUserId);
                    noticeService.notice10002("送给"+toUser.getNickname(),userId,roomId,user.getNickname(),user.getSex(),user.getPortraitAddress(),funtimeGift.getGiftName(),giftNum, giftHornLength);
                }
            }
        }
        resultMsg.setData(JsonUtil.getMap("giftNum",itemNum-giftNum*userNum));
        if (roomId!=null) {
            roomService.updateHotsPlus(roomId, new BigDecimal(amount * userNum).divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        return resultMsg;

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> sendGiftForRoomBox3(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId) {

        ResultMsg<Object> resultMsg = new ResultMsg<>();
        FuntimeChatroom chatroom = roomService.getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<Long> toUserIdArray = roomService.getRoomUserByRoomId(roomId,userId);
        if (toUserIdArray==null||toUserIdArray.isEmpty()){
            if (userId.equals(chatroom.getUserId())) {
                throw new BusinessException(ErrorMsgEnum.ROOM_USER_IS_EMPTY.getValue(), ErrorMsgEnum.ROOM_USER_IS_EMPTY.getDesc());
            }else{
                toUserIdArray = new ArrayList<>(1);
                toUserIdArray.add(chatroom.getUserId());
            }
        }

        int userNum = toUserIdArray.size();

        FuntimeUser user = getUserById(userId);

        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);

        FuntimeBox box = boxMapper.getBoxInfoByBoxNumber(giftId);
        //FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (box==null){
            throw new BusinessException(ErrorMsgEnum.BOX_NOT_EXISTS.getValue(),ErrorMsgEnum.BOX_NOT_EXISTS.getDesc());
        }

        Integer userRole = roomService.getUserRole(roomId,userId);

        userRole = userRole == null?4:userRole;

        BigDecimal price = box.getPrice();

        Integer amount= price.intValue()*giftNum;
        Integer total = amount*userNum;
        //账户余额不足
        if (userAccount.getBlueDiamond().intValue()<total){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            resultMsg.setData(JsonUtil.getMap("amount",total));
            return resultMsg;
        }
        //用户送减去蓝钻
        userService.updateUserAccountForSub(userId, null, new BigDecimal(total), null);
        String noticeAmount = parameterService.getParameterValueByKey("gift_notice_amount");
        String giftHornLength = parameterService.getParameterValueByKey("gift_horn_length");
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");

        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);

        List<Map<String,Object>> noticeDatas = new ArrayList<>();
        Map<String,Object> noticeData;
        Map<Long,Map<Integer,Integer>> randomGiftsMap = getDrawId(giftId,toUserIdArray,giftNum);
        for (Map.Entry<Long,Map<Integer,Integer>> entry : randomGiftsMap.entrySet()){
            Long toUserId = entry.getKey();
            FuntimeUser toUser = userService.queryUserById(toUserId);
            if (toUser==null){
                throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
            }

            for (Map.Entry<Integer,Integer> entry2 : entry.getValue().entrySet()) {
                noticeData = new HashMap<>();
                Integer toGiftId = entry2.getKey();
                Integer num = entry2.getValue();
                FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(entry2.getKey());
                if (funtimeGift == null){
                    throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
                }
                BigDecimal giftPrice = funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice():funtimeGift.getActivityPrice();
                BigDecimal giftAmount = giftPrice.multiply(new BigDecimal(num).setScale(2,BigDecimal.ROUND_HALF_DOWN));
                Long recordId = saveFuntimeUserAccountGifttransRecord(userId, operationDesc, giftAmount
                        , num, toGiftId, funtimeGift.getGiftName(), toUserId, giveChannel, roomId, OperationType.GIFT_BOX_OUT.getOperationType(),box.getPrice().intValue());

                Integer charmVal = new BigDecimal(blue_to_charm).multiply(giftAmount).intValue();
                BigDecimal black = new BigDecimal(blue_to_black).multiply(giftAmount).setScale(2, RoundingMode.DOWN);
                //用户收加上黑钻,魅力值
                userService.updateUserAccountForPlusGift(toUserId, black, num, charmVal);
                saveUserAccountCharmRecord(toUserId, charmVal, recordId, 1);
                //用户送的日志
                saveUserAccountBlueLog(userId, new BigDecimal(num).multiply(price).setScale(2,BigDecimal.ROUND_HALF_DOWN), recordId
                        , OperationType.GIFT_BOX_OUT.getAction(), OperationType.GIFT_BOX_OUT.getOperationType());

                //用户收的日志
                saveUserAccountBlackLog(toUserId, black, recordId, OperationType.GIFT_BOX_IN.getAction()
                        , OperationType.GIFT_BOX_IN.getOperationType());

                String msg = "送给"+toUser.getNickname()+num+"个"+funtimeGift.getGiftName()+"("+box.getBoxName()+")";

                noticeData.put("giftImg",funtimeGift.getImageUrl());
                noticeData.put("giftName",funtimeGift.getGiftName());
                noticeData.put("giftNum",num);
                noticeData.put("userImage",toUser.getPortraitAddress());
                noticeDatas.add(noticeData);
                msg = "<font color='#FFDE00'>"+msg+"</font>";
                noticeService.notice11Or14(userId,null,msg,roomId,Constant.ROOM_MSG_NORMAL,userIds,userRole,0);

                if (noticeAmount!=null){
                    if (total>=new BigDecimal(noticeAmount).intValue()){
                        noticeService.notice10002("送给"+toUser.getNickname(),userId,roomId,user.getNickname(),user.getSex(),user.getPortraitAddress(),funtimeGift.getGiftName()+"("+box.getBoxName()+")",num,giftHornLength);
                    }
                }
            }

        }

        JSONObject noticeMap = new JSONObject();
        noticeMap.put("list",noticeDatas);
        noticeMap.put("boxUrl",box.getAnimationUrl());

        noticeService.notice39(noticeMap,userIds);
        if (roomId!=null) {
            roomService.updateHotsPlus(roomId, new BigDecimal(total).divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        return resultMsg;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> sendGiftForMic(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();

        FuntimeUser user = getUserById(userId);

        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);

        FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (funtimeGift==null){
            throw new BusinessException(ErrorMsgEnum.GIFT_NOT_EXISTS.getValue(),ErrorMsgEnum.GIFT_NOT_EXISTS.getDesc());
        }
        FuntimeChatroom chatroom = roomService.getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<Long> toUserIdArray = roomService.getMicUserIdByRoomId(roomId,userId);
        if (toUserIdArray==null||toUserIdArray.isEmpty()){
            if (userId.equals(chatroom.getUserId())) {
                throw new BusinessException(ErrorMsgEnum.ROOM_MICUSER_IS_EMPTY.getValue(), ErrorMsgEnum.ROOM_MICUSER_IS_EMPTY.getDesc());
            }else{
                toUserIdArray = new ArrayList<>(1);
                toUserIdArray.add(chatroom.getUserId());
            }
        }

        int userNum = toUserIdArray.size();

        Integer amount= funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice().intValue()*giftNum:funtimeGift.getActivityPrice().intValue()*giftNum;
        //账户余额不足
        if (userAccount.getBlueDiamond().intValue()<amount*userNum){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            resultMsg.setData(JsonUtil.getMap("amount",amount*userNum));
            return resultMsg;
        }
        //用户送减去蓝钻
        userService.updateUserAccountForSub(userId, null, new BigDecimal(amount*userNum), null);

        Integer userRole = roomService.getUserRole(roomId,userId);

        userRole = userRole == null?4:userRole;
        String noticeAmount = parameterService.getParameterValueByKey("gift_notice_amount");
        String giftHornLength = parameterService.getParameterValueByKey("gift_horn_length");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        BigDecimal black = new BigDecimal(blue_to_black).multiply(new BigDecimal(amount)).setScale(2, RoundingMode.DOWN);
        for (Long toUserId : toUserIdArray) {

            Long recordId = saveFuntimeUserAccountGifttransRecord(userId, operationDesc, new BigDecimal(amount)
                    , giftNum, giftId, funtimeGift.getGiftName(), toUserId, giveChannel, roomId, OperationType.GIVEGIFT.getOperationType());

            Integer charmVal = new BigDecimal(blue_to_charm).multiply(new BigDecimal(amount)).intValue();
            //用户收加上黑钻
            userService.updateUserAccountForPlusGift(toUserId, black, giftNum, charmVal);

            saveUserAccountCharmRecord(toUserId,charmVal,recordId,1);
            //用户送的日志
            saveUserAccountBlueLog(userId, new BigDecimal(amount), recordId
                    , OperationType.GIVEGIFT.getAction(), OperationType.GIVEGIFT.getOperationType());

            //用户收的日志
            saveUserAccountBlackLog(toUserId, black, recordId, OperationType.RECEIVEGIFT.getAction()
                    , OperationType.RECEIVEGIFT.getOperationType());

        }

        RoomGiftNotice notice = new RoomGiftNotice();
        notice.setCount(giftNum);

        notice.setGiftName(funtimeGift.getGiftName());
        notice.setFromImg(user.getPortraitAddress());
        notice.setFromName(user.getNickname());
        notice.setFromUid(String.valueOf(userId));
        notice.setGid(String.valueOf(giftId));
        notice.setGiftImg(funtimeGift.getAnimationUrl());
        notice.setRid(String.valueOf(roomId));
        notice.setToImg(chatroom.getAvatarUrl());
        notice.setToName(chatroom.getName());
        notice.setFromSex(user.getSex());
        notice.setUserRole(userRole);
        int type = funtimeGift.getSpecialEffect();
        if (type == SpecialEffectType.E_4.getValue()) {
            type = SpecialEffectType.E_3.getValue();
            notice.setType(Constant.ROOM_GIFT_SEND_ROOM_ALL);
            //发送通知全服
            notice.setSpecialEffect(type);
            noticeService.notice21(notice);

        }
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        notice.setSpecialEffect(type);
        notice.setType(Constant.ROOM_GIFT_SEND_ROOM);
        //发送通知
        noticeService.notice19(notice, userIds);
        if (noticeAmount!=null) {
            if (amount * userNum >= new BigDecimal(noticeAmount).intValue()) {
                for (Long toUserId : toUserIdArray) {
                    FuntimeUser toUser = getUserById(toUserId);
                    noticeService.notice10002("送给"+toUser.getNickname(),userId,roomId,user.getNickname(),user.getSex(),user.getPortraitAddress(),funtimeGift.getGiftName(),giftNum, giftHornLength);
                }
            }
        }
        if (roomId!=null) {
            roomService.updateHotsPlus(roomId, new BigDecimal(amount * userNum).divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        return resultMsg;
    }


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> sendGiftForMic2(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        FuntimeUser user = getUserById(userId);
        FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (funtimeGift==null){
            throw new BusinessException(ErrorMsgEnum.GIFT_NOT_EXISTS.getValue(),ErrorMsgEnum.GIFT_NOT_EXISTS.getDesc());
        }
        FuntimeChatroom chatroom = roomService.getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<Long> toUserIdArray = roomService.getMicUserIdByRoomId(roomId,userId);
        if (toUserIdArray==null||toUserIdArray.isEmpty()){
            if (userId.equals(chatroom.getUserId())) {
                throw new BusinessException(ErrorMsgEnum.ROOM_MICUSER_IS_EMPTY.getValue(), ErrorMsgEnum.ROOM_MICUSER_IS_EMPTY.getDesc());
            }else{
                toUserIdArray = new ArrayList<>(1);
                toUserIdArray.add(chatroom.getUserId());
            }
        }

        int userNum = toUserIdArray.size();

        Integer amount= funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice().intValue()*giftNum:funtimeGift.getActivityPrice().intValue()*giftNum;
        Integer itemNum = userAccountMapper.getItemNumByUserId(userId, giftId, 1);
        //背包礼物不足
        if (itemNum<giftNum*userNum){
            resultMsg.setCode(ErrorMsgEnum.USER_BAG_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_BAG_NOT_EN.getDesc());
            return resultMsg;
        }
        int k = userAccountMapper.updateUserKnapsackSub(userAccountMapper.checkUserKnapsackExist(userId, giftId, 1),giftNum*userNum);
        if (k!=1){
            resultMsg.setCode(ErrorMsgEnum.DATA_ORER_ERROR.getValue());
            resultMsg.setMsg(ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            return resultMsg;
        }

        Integer userRole = roomService.getUserRole(roomId,userId);

        userRole = userRole == null?4:userRole;
        String noticeAmount = parameterService.getParameterValueByKey("gift_notice_amount");
        String giftHornLength = parameterService.getParameterValueByKey("gift_horn_length");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        BigDecimal black = new BigDecimal(blue_to_black).multiply(new BigDecimal(amount)).setScale(2, RoundingMode.DOWN);
        for (Long toUserId : toUserIdArray) {

            Long recordId = saveFuntimeUserAccountGifttransRecord(userId, operationDesc, new BigDecimal(amount)
                    , giftNum, giftId, funtimeGift.getGiftName(), toUserId, giveChannel, roomId, OperationType.GIVEGIFTBAG.getOperationType());

            Integer charmVal = new BigDecimal(blue_to_charm).multiply(new BigDecimal(amount)).intValue();
            //用户收加上黑钻
            userService.updateUserAccountForPlusGift(toUserId, black, giftNum, charmVal);

            saveUserAccountCharmRecord(toUserId,charmVal,recordId,1);
            //用户送的日志
            saveUserAccountBlueLog(userId, new BigDecimal(amount), recordId
                    , OperationType.GIVEGIFTBAG.getAction(), OperationType.GIVEGIFTBAG.getOperationType());

            //用户收的日志
            saveUserAccountBlackLog(toUserId, black, recordId, OperationType.RECEIVEBAGGIFT.getAction()
                    , OperationType.RECEIVEBAGGIFT.getOperationType());

        }

        RoomGiftNotice notice = new RoomGiftNotice();
        notice.setCount(giftNum);

        notice.setGiftName(funtimeGift.getGiftName());
        notice.setFromImg(user.getPortraitAddress());
        notice.setFromName(user.getNickname());
        notice.setFromUid(String.valueOf(userId));
        notice.setGid(String.valueOf(giftId));
        notice.setGiftImg(funtimeGift.getAnimationUrl());
        notice.setRid(String.valueOf(roomId));
        notice.setToImg(chatroom.getAvatarUrl());
        notice.setToName(chatroom.getName());
        notice.setFromSex(user.getSex());
        notice.setUserRole(userRole);
        int type = funtimeGift.getSpecialEffect();
        if (type == SpecialEffectType.E_4.getValue()) {
            type = SpecialEffectType.E_3.getValue();
            notice.setType(Constant.ROOM_GIFT_SEND_ROOM_ALL);
            //发送通知全服
            notice.setSpecialEffect(type);
            noticeService.notice21(notice);

        }
        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);
        if (userIds == null || userIds.isEmpty()) {
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(), ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        notice.setSpecialEffect(type);
        notice.setType(Constant.ROOM_GIFT_SEND_ROOM);
        //发送通知
        noticeService.notice19(notice, userIds);
        if (noticeAmount!=null) {
            if (amount * userNum >= new BigDecimal(noticeAmount).intValue()) {
                for (Long toUserId : toUserIdArray) {
                    FuntimeUser toUser = getUserById(toUserId);
                    noticeService.notice10002("送给"+toUser.getNickname(),userId,roomId,user.getNickname(),user.getSex(),user.getPortraitAddress(),funtimeGift.getGiftName(),giftNum, giftHornLength);
                }
            }
        }
        resultMsg.setData(JsonUtil.getMap("giftNum",itemNum-giftNum*userNum));
        if (roomId!=null) {
            roomService.updateHotsPlus(roomId, new BigDecimal(amount * userNum).divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        return resultMsg;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> sendGiftForMicBox3(Long userId, Integer giftId, Integer giftNum, String operationDesc, Integer giveChannel, Long roomId){

        ResultMsg<Object> resultMsg = new ResultMsg<>();
        FuntimeChatroom chatroom = roomService.getChatroomById(roomId);
        if (chatroom == null){
            throw new BusinessException(ErrorMsgEnum.ROOM_NOT_EXISTS.getValue(),ErrorMsgEnum.ROOM_NOT_EXISTS.getDesc());
        }
        List<Long> toUserIdArray = roomService.getMicUserIdByRoomId(roomId,userId);
        if (toUserIdArray==null||toUserIdArray.isEmpty()){
            if (userId.equals(chatroom.getUserId())) {
                throw new BusinessException(ErrorMsgEnum.ROOM_USER_IS_EMPTY.getValue(), ErrorMsgEnum.ROOM_USER_IS_EMPTY.getDesc());
            }else{
                toUserIdArray = new ArrayList<>(1);
                toUserIdArray.add(chatroom.getUserId());
            }
        }

        int userNum = toUserIdArray.size();

        FuntimeUser user = getUserById(userId);

        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);

        FuntimeBox box = boxMapper.getBoxInfoByBoxNumber(giftId);
        if (box==null){
            throw new BusinessException(ErrorMsgEnum.BOX_NOT_EXISTS.getValue(),ErrorMsgEnum.BOX_NOT_EXISTS.getDesc());
        }

        Integer userRole = roomService.getUserRole(roomId,userId);

        userRole = userRole == null?4:userRole;

        BigDecimal price = box.getPrice();

        Integer amount= price.intValue()*giftNum;
        Integer total = amount*userNum;
        //账户余额不足
        if (userAccount.getBlueDiamond().intValue()<total){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            resultMsg.setData(JsonUtil.getMap("amount",total));
            return resultMsg;
        }
        //用户送减去蓝钻
        userService.updateUserAccountForSub(userId, null, new BigDecimal(total), null);
        String noticeAmount = parameterService.getParameterValueByKey("gift_notice_amount");
        String giftHornLength = parameterService.getParameterValueByKey("gift_horn_length");
        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");
        String blue_to_charm = parameterService.getParameterValueByKey("blue_to_charm");

        List<String> userIds = roomService.getRoomUserByRoomIdAll(roomId);

        List<Map<String,Object>> noticeDatas = new ArrayList<>();
        Map<String,Object> noticeData;
        Map<Long,Map<Integer,Integer>> randomGiftsMap = getDrawId(giftId,toUserIdArray,giftNum);
        for (Map.Entry<Long,Map<Integer,Integer>> entry : randomGiftsMap.entrySet()){
            Long toUserId = entry.getKey();
            FuntimeUser toUser = userService.queryUserById(toUserId);
            if (toUser==null){
                throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
            }

            for (Map.Entry<Integer,Integer> entry2 : entry.getValue().entrySet()) {
                noticeData = new HashMap<>();
                Integer toGiftId = entry2.getKey();
                Integer num = entry2.getValue();
                FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(entry2.getKey());
                if (funtimeGift == null){
                    throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
                }
                BigDecimal giftPrice = funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice():funtimeGift.getActivityPrice();
                BigDecimal giftAmount = giftPrice.multiply(new BigDecimal(num).setScale(2,BigDecimal.ROUND_HALF_DOWN));
                Long recordId = saveFuntimeUserAccountGifttransRecord(userId, operationDesc, giftAmount
                        , num, toGiftId, funtimeGift.getGiftName(), toUserId, giveChannel, roomId, OperationType.GIFT_BOX_OUT.getOperationType(),box.getPrice().intValue());

                Integer charmVal = new BigDecimal(blue_to_charm).multiply(giftAmount).intValue();
                BigDecimal black = new BigDecimal(blue_to_black).multiply(giftAmount).setScale(2, RoundingMode.DOWN);
                //用户收加上黑钻,魅力值
                userService.updateUserAccountForPlusGift(toUserId, black, num, charmVal);
                saveUserAccountCharmRecord(toUserId, charmVal, recordId, 1);
                //用户送的日志
                saveUserAccountBlueLog(userId, new BigDecimal(num).multiply(price).setScale(2,BigDecimal.ROUND_HALF_DOWN), recordId
                        , OperationType.GIFT_BOX_OUT.getAction(), OperationType.GIFT_BOX_OUT.getOperationType());

                //用户收的日志
                saveUserAccountBlackLog(toUserId, black, recordId, OperationType.GIFT_BOX_IN.getAction()
                        , OperationType.GIFT_BOX_IN.getOperationType());

                String msg = "送给"+toUser.getNickname()+num+"个"+funtimeGift.getGiftName()+"("+box.getBoxName()+")";

                noticeData.put("giftImg",funtimeGift.getImageUrl());
                noticeData.put("giftName",funtimeGift.getGiftName());
                noticeData.put("giftNum",num);
                noticeData.put("userImage",toUser.getPortraitAddress());
                noticeDatas.add(noticeData);
                msg = "<font color='#FFDE00'>"+msg+"</font>";
                noticeService.notice11Or14(userId,null,msg,roomId,Constant.ROOM_MSG_NORMAL,userIds,userRole,0);

                if (noticeAmount!=null){
                    if (total>=new BigDecimal(noticeAmount).intValue()){
                        noticeService.notice10002("送给"+toUser.getNickname(),userId,roomId,user.getNickname(),user.getSex(),user.getPortraitAddress(),funtimeGift.getGiftName()+"("+box.getBoxName()+")",num,giftHornLength);
                    }
                }
            }

        }

        JSONObject noticeMap = new JSONObject();
        noticeMap.put("list",noticeDatas);
        noticeMap.put("boxUrl",box.getAnimationUrl());

        noticeService.notice39(noticeMap,userIds);
        if (roomId!=null) {
            roomService.updateHotsPlus(roomId, new BigDecimal(total).divide(new BigDecimal(10), 0, BigDecimal.ROUND_UP).intValue());
        }
        return resultMsg;
    }


    @Override
    public FuntimeUserAccount getUserAccountByUserId(Long userId){
        FuntimeUserAccount userAccount = userAccountMapper.selectByUserId(userId);
        if (userAccount==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        return userAccount;
    }

    public FuntimeUser getUserById(Long userId){
        FuntimeUser user = userService.queryUserById(userId);

        if (user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        return user;
    }

    @Override
    public List<FuntimeRechargeConf> getRechargeConf(Integer platform) {
        return rechargeConfMapper.getRechargeConf(platform);
    }

    @Override
    public FuntimeUserRedpacket getRedpacketInfoById(Long id, Long userId) {
        FuntimeUserRedpacket redpacket = userRedpacketMapper.getRedpacketInfoById(id,userId);

        return redpacket;
    }

    @Override
    public Integer diamondConvertTrial(Long userId, String from, String to, BigDecimal amount, int value) {
        BigDecimal val = convert(from,to);

        BigDecimal toAmount = amount.multiply(val).setScale(2,RoundingMode.HALF_UP);


        return toAmount.intValue();
    }

    @Override
    public BigDecimal getRatio(Long userId, String from, String to) {
        return convert(from,to);
    }

    @Override
    public PageInfo<Map<String,Object>> getGiftsByUserId(Integer startPage, Integer pageSize, Long userId) {
        PageHelper.startPage(startPage,pageSize);

        List<Map<String,Object>> list = userAccountGifttransRecordMapper.getGiftsByUserId(userId);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{

            return new PageInfo<>(list);
        }
    }

    @Override
    public void updateStateForInvalid() {
        List<FuntimeUserRedpacket> redpacketListInvalid = userRedpacketMapper.getRedpacketListInvalid();
        if (redpacketListInvalid!=null&&!redpacketListInvalid.isEmpty()) {
            for (FuntimeUserRedpacket redpacket : redpacketListInvalid){
                userService.updateUserAccountForPlus(redpacket.getUserId(),null,redpacket.getGrabAmount(),null);
                saveUserAccountBlueLog(redpacket.getUserId(),redpacket.getGrabAmount(),redpacket.getId(),OperationType.REDPACKETINVALID.getAction(),OperationType.REDPACKETINVALID.getOperationType());
                userRedpacketMapper.updateStateForInvalid(redpacket.getId());
            }
        }



    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void diamondConvert(Long userId, String from, String to, BigDecimal amount,Integer convertType) {
        FuntimeUserAccount userAccount = userAccountMapper.selectByUserId(userId);
        if (userAccount.getBlackDiamond().subtract(amount).doubleValue()<0){
            throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLACK_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_BLACK_NOT_EN.getDesc());
        }
        BigDecimal val = convert(from,to);

        BigDecimal toAmount = amount.multiply(val).setScale(2,RoundingMode.HALF_UP);
        String blue_to_level = parameterService.getParameterValueByKey("blue_to_level");
        String level_to_wealth = parameterService.getParameterValueByKey("level_to_wealth");
        int levelVal = toAmount.multiply(new BigDecimal(blue_to_level)).intValue();
        int wealthVal = new BigDecimal(levelVal).multiply(new BigDecimal(level_to_wealth)).intValue();

        Long recordId = saveFuntimeUserConvertRecord(userId,val,convertType,amount,toAmount);
        Long WealthRecordId = saveUserAccountLevelWealthRecord(userId,levelVal,wealthVal,recordId,LevelWealthType.CONVERT.getValue());
        //用户总充值数(等级值)
        int total = userAccount.getLevelVal()+levelVal;

        //充值等级
        Map<String,Object> userLevelMap = userAccountRechargeRecordMapper.getUserLevel(total);

        if (userLevelMap==null){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_LEVEL_NOT_EXISTS.getValue(),ErrorMsgEnum.RECHARGE_LEVEL_NOT_EXISTS.getDesc());
        }
        Integer userLevel = userLevelMap.get("level")==null?0:Integer.parseInt(userLevelMap.get("level").toString());
        String levelUrl = userLevelMap.get("levelUrl")==null?"":userLevelMap.get("levelUrl").toString();

        if (!userLevel.equals(userAccount.getLevel())){
            userAccountMapper.updateUserAccountForConvert(userId,userLevel,toAmount,amount,levelVal,wealthVal);
            updateLevelExtr(userId,userLevel,levelUrl);
        }else{
            userAccountMapper.updateUserAccountForConvert(userId,null,toAmount,amount,levelVal,wealthVal);
        }

        //用户加的日志
        saveUserAccountBlueLog(userId,toAmount,recordId
                ,OperationType.BLACK_BLUE_IN.getAction(),OperationType.BLACK_BLUE_IN.getOperationType());

        //用户减的日志
        saveUserAccountBlackLog(userId,amount,recordId,OperationType.BLACK_BLUE_OUT.getAction()
                ,OperationType.BLACK_BLUE_OUT.getOperationType());

        saveUserAccountLevelWealthLog(userId,levelVal,wealthVal,WealthRecordId
                , OperationType.BLACK_BLUE_IN.getAction(),OperationType.BLACK_BLUE_IN.getOperationType());



    }

    @Override
    public PageInfo<FuntimeUserConvertRecord> getUserConvertRecordForPage(Integer startPage, Integer pageSize, Long userId, String queryDate,Integer convertType) {
        PageHelper.startPage(startPage,pageSize);

        String startDate = queryDate+"-01 00:00:01";
        String endDate = currentFinalDay(queryDate) ;

        List<FuntimeUserConvertRecord> list = userConvertRecordMapper.getUserConvertRecordForPage(convertType,startDate,endDate,userId);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            return new PageInfo<>(list);
        }
    }

    @Override
    public PageInfo<FuntimeUserAccountGifttransRecord> getGiftOfSendForPage(Integer startPage, Integer pageSize, String queryDate, Long userId) {
        PageHelper.startPage(startPage,pageSize);

        String startDate = null;
        String endDate = null;
        if(StringUtils.isNotBlank(queryDate)) {
            startDate = queryDate + "-01 00:00:01";
            endDate = currentFinalDay(queryDate);
        }

        List<FuntimeUserAccountGifttransRecord> list = userAccountGifttransRecordMapper.getGiftOfSendForPage(startDate,endDate,userId);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{

            return new PageInfo<>(list);
        }
    }

    @Override
    public PageInfo<FuntimeUserAccountGifttransRecord> getGiftOfRecieveForPage(Integer startPage, Integer pageSize, String queryDate, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        String startDate = null;
        String endDate = null;
        if(StringUtils.isNotBlank(queryDate)) {
            startDate = queryDate + "-01 00:00:01";
            endDate = currentFinalDay(queryDate);
        }

        List<FuntimeUserAccountGifttransRecord> list = userAccountGifttransRecordMapper.getGiftOfRecieveForPage(startDate,endDate,userId);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{

            return new PageInfo<>(list);
        }
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public boolean applyWithdrawal(Long userId, BigDecimal blackAmount, BigDecimal preRmbAmount, BigDecimal preChannelAmount, BigDecimal amount, String ip) {

        FuntimeUser user = userService.queryUserById(userId);
        if(user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        //用户已封禁
        if (user.getState()==2){
            throw new BusinessException(ErrorMsgEnum.USER_IS_DELETE.getValue(),ErrorMsgEnum.USER_IS_DELETE.getDesc());
        }

        //未绑定手机
        if (StringUtils.isBlank(user.getPhoneNumber())){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_PHONE_NOT_BIND.getValue(),ErrorMsgEnum.WITHDRAWAL_PHONE_NOT_BIND.getDesc());
        }
        FuntimeUserAccount userAccount = getUserAccountByUserId(userId);

        if (userAccount.getBlackDiamond().subtract(blackAmount).doubleValue()<0){
            throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLACK_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_BLACK_NOT_EN.getDesc());
        }
        //是否绑定微信
        FuntimeUserThird userThird = userService.queryUserThirdIdByType(userId, Constant.LOGIN_WX);
        if (userThird==null){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_WX_NOT_BIND.getValue(),ErrorMsgEnum.WITHDRAWAL_WX_NOT_BIND.getDesc());
        }
        //检查实际提现金额限制
        checkWithdrawalConf(userId,amount);
        FuntimeUserValid userValid = new FuntimeUserValid();
        if(checkRmbAmountValid(preRmbAmount)) {
            //是否实名认证
            userValid = userService.queryValidInfoByUserId(userId);
            if (userValid == null) {
                throw new BusinessException(ErrorMsgEnum.USER_NOT_REALNAME_VALID.getValue(), ErrorMsgEnum.USERVALID_IS_NOT_VALID.getDesc());
            }
        }
        //获取配置表中渠道费
        BigDecimal channelAmount = getServiceAmount(preRmbAmount.intValue());

        //检查前端渠道费
        if (channelAmount.subtract(preChannelAmount).intValue()!=0){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_CHANNELAMOUNT_ERROR.getValue(),ErrorMsgEnum.WITHDRAWAL_CHANNELAMOUNT_ERROR.getDesc());
        }

        //检查待处理的条数
        checkWithdrawalRecordPendingTrial(userId);

        //是否首次提现

        //boolean firstTime = checkWithdrawalRecordIsFirst(userId);


        //非首次试算金额必须未10倍数
        /*
        if(blackAmount.subtract(new BigDecimal(10)).doubleValue()<0
                ||blackAmount.intValue()%10>0){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_PRERMBAMOUNT_100_ERROR.getValue(),ErrorMsgEnum.WITHDRAWAL_PRERMBAMOUNT_100_ERROR.getDesc());
        }*/


        //黑对RMB比例
        BigDecimal ratio = convert("black", "rmb");
        //检查前端传过来的数字
        checkAppAmount(blackAmount,preRmbAmount,channelAmount,amount,ratio);

        Integer withdrawalType = getWithdrawalType(preRmbAmount);
        //获取提现号码
        String withdrawalCard = getWithdrawalCard(withdrawalType,userThird.getOpenid(),userValid.getDepositCardReal());

        String nickname = getNickname(withdrawalType,userThird.getNickname(),userValid.getDepositCard());
        //获取审核类型
        int trialType = getTrialType(preRmbAmount);

        String orderNo = "D"+StringUtil.createOrderId();

        Long recordId = saveFuntimeUserAccountWithdrawalRecord(userId,withdrawalType
                ,withdrawalCard,amount,blackAmount,ratio,channelAmount,preRmbAmount,trialType,orderNo,nickname,userAccount.getBlackDiamond());

        //减去用户黑钻
        userService.updateUserAccountForSub(userId,blackAmount,null,null);

        //用户日志
        saveUserAccountBlackLog(userId,blackAmount,recordId,OperationType.WITHDRAWAL.getAction(),OperationType.WITHDRAWAL.getOperationType());
        //自动转账
        if (trialType == 1){
            try {
                Map<String, String> resp = MyWxPay.mmpaymkttransfers(1, orderNo, ip, userThird.getOpenid(), String.valueOf(amount.multiply(new BigDecimal(100)).intValue()));

                String payment_no = resp.get("payment_no");
                //转账成功更新状态和第三方订单
                updateFuntimeUserAccountWithdrawalRecord(recordId,3,null,payment_no);
                return true;
            }catch (BusinessException e){
                if (e.getCode().equals(ErrorMsgEnum.MMPAYMKTTRANSFER_SIMPLE_BAN.getValue())){
                    throw new BusinessException(e.getCode(),e.getMsg());
                }else{
                    //失败改为手动审核
                    updateFuntimeUserAccountWithdrawalRecord(recordId,null,2,null);
                }
            }catch (Exception e){
                //失败改为手动审核
                updateFuntimeUserAccountWithdrawalRecord(recordId,null,2,null);
            }

        }
        return false;

    }

    private boolean checkRmbAmountValid(BigDecimal preRmbAmount) {
        String withdrawal_valid_amount = parameterService.getParameterValueByKey("withdrawal_valid_amount");
        if (withdrawal_valid_amount == null){
            return true;
        }
        if (preRmbAmount.subtract(new BigDecimal(withdrawal_valid_amount)).intValue()>=0){
            return true;
        }else{
            return false;
        }
    }

    private String getNickname(Integer withdrawalType, String nickname, String depositCard) {

        if (WithdrawalType.WXPAY.getValue() == withdrawalType){
            return nickname;
        }else{
            return "银行卡尾号:"+depositCard.substring(depositCard.length()-4);
        }
    }


    private Integer getWithdrawalType(BigDecimal preRmbAmount) {
        String withdrawal_wx_amount = parameterService.getParameterValueByKey("withdrawal_wx_amount");
        BigDecimal wx_amount = withdrawal_wx_amount == null?new BigDecimal(5000):new BigDecimal(withdrawal_wx_amount);
        if (preRmbAmount.subtract(wx_amount).doubleValue()<0){

            return WithdrawalType.WXPAY.getValue();
        }else{
            return WithdrawalType.DESPOSIT_CARD.getValue();
        }

    }

    private void checkAppAmount(BigDecimal blackAmount, BigDecimal preRmbAmount, BigDecimal channelAmount, BigDecimal amount,BigDecimal ratio){

        if (blackAmount.multiply(ratio).doubleValue()!=preRmbAmount.doubleValue()){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_PRERMBAMOUNT_ERROR.getValue(),ErrorMsgEnum.WITHDRAWAL_PRERMBAMOUNT_ERROR.getDesc());
        }
        if (amount.doubleValue()!=preRmbAmount.subtract(channelAmount).doubleValue()){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_RMBAMOUNT_ERROR.getValue(),ErrorMsgEnum.WITHDRAWAL_RMBAMOUNT_ERROR.getDesc());
        }
    }

    private void checkWithdrawalRecordPendingTrial(Long userId){
        int count = userAccountWithdrawalRecordMapper.getWithdrawalRecordByUserId(userId);
        if(count>0){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_OPERATION_LIMIT.getValue(),ErrorMsgEnum.WITHDRAWAL_OPERATION_LIMIT.getDesc()+",公众号:"+parameterService.getParameterValueByKey("wechat_subscription"));
        }
    }



    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> doSign(Long userId) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        int currentDate = DateUtil.getCurrentInt();
        Long id = signMapper.getSignCheck(currentDate,userId);
        if (id != null){
            log.error("用户{}已签到",userId);
            resultMsg.setCode(ErrorMsgEnum.USER_SIGN_ERROR.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_SIGN_ERROR.getValue());
            return resultMsg;
        }
        FuntimeSignRecord record = new FuntimeSignRecord();
        record.setSignDate(currentDate);
        record.setUserId(userId);
        int k = signMapper.saveSignRecord(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        String signVal = parameterService.getParameterValueByKey("sign_val");
        userService.updateUserAccountGoldCoinPlus(userId,Integer.parseInt(signVal));
        saveUserAccountGoldLog(userId,new BigDecimal(signVal),record.getId(),OperationType.GOLD_SIGN_IN.getAction(),OperationType.GOLD_SIGN_IN.getOperationType());

        resultMsg.setData(JsonUtil.getMap("goldAmount",signVal));
        return resultMsg;
    }

    @Override
    public boolean checkWithdrawalRecordIsFirst(Long userId){
        int count = userAccountWithdrawalRecordMapper.getWithdrawalRecordCountBySucc(userId);
        if(count>0){
            return false;
        }else{
            return true;
        }
    }

    @Override
    public PageInfo<FuntimeUserAccountWithdrawalRecord> getWithdrawalForPage(Integer startPage, Integer pageSize, String queryDate, Integer state, Long userId) {
        PageHelper.startPage(startPage,pageSize);
        String startDate = null;
        String endDate = null;
        if(StringUtils.isNotBlank(queryDate)) {
            startDate = queryDate + "-01 00:00:01";
            endDate = currentFinalDay(queryDate);
        }

        List<FuntimeUserAccountWithdrawalRecord> list = userAccountWithdrawalRecordMapper.getWithdrawalForPage(startDate,endDate,userId,state);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{
            return new PageInfo<>(list);
        }
    }

    @Override
    public PageInfo<FuntimeUserRedpacket> getRedpacketListByRoomId(Integer startPage, Integer pageSize, Long roomId,Long userId) {
        PageHelper.startPage(startPage,pageSize);
        List<FuntimeUserRedpacket> list = userRedpacketMapper.getRedpacketListByRoomId(roomId,userId);
        if(list==null||list.isEmpty()){
            return new PageInfo<>();
        }else{

            return new PageInfo<>(list);
        }
    }

    @Override
    public List<FuntimeUserAccountRedpacketRecord> getRecordListByRedId(Long redpacketId) {
        List<FuntimeUserAccountRedpacketRecord> records = userAccountRedpacketRecordMapper.getRedpacketRecordByRedpacketId(redpacketId);

        return records;
    }


    private void checkWithdrawalConf(Long userId,BigDecimal rmbAmount){
        //每次的最小值
        String withdrawal_min_once = parameterService.getParameterValueByKey("withdrawal_min_once");
        if (rmbAmount.doubleValue()<new BigDecimal(withdrawal_min_once).doubleValue()){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_MIN_LIMIT.getValue(),ErrorMsgEnum.WITHDRAWAL_MIN_LIMIT.getDesc().replace("#",withdrawal_min_once).replace("@",String.valueOf(Integer.parseInt(withdrawal_min_once)*10)));
        }
        //每日最大限额
        String startDate = DateUtil.getCurrentDayStart();
        String endDate = DateUtil.getCurrentDayEnd();
        BigDecimal dayAmount = userAccountWithdrawalRecordMapper.getSumAmountForDay(startDate,endDate,userId);
        dayAmount = dayAmount ==null?new BigDecimal(0):dayAmount;
        String withdrawal_max_day = parameterService.getParameterValueByKey("withdrawal_max_day");
        if (new BigDecimal(withdrawal_max_day).subtract(dayAmount.add(rmbAmount)).doubleValue()<0){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_DAY_LIMIT.getValue(),ErrorMsgEnum.WITHDRAWAL_DAY_LIMIT.getDesc());
        }
        //每月最大次数
        String withdrawal_maxtime_month = parameterService.getParameterValueByKey("withdrawal_maxtime_month");
        int count = userAccountWithdrawalRecordMapper.getCountForMonth(startDate,endDate,userId);
        if (count>=Integer.parseInt(withdrawal_maxtime_month)){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_MONTH_LIMIT.getValue(),ErrorMsgEnum.WITHDRAWAL_MONTH_LIMIT.getDesc());
        }

    }

    private int getTrialType(BigDecimal preRmbAmount){
        //自动转账的最高限额
        String withdrawal_auto_amount = parameterService.getParameterValueByKey("withdrawal_auto_amount");
        //一级审核最高限额
        String withdrawal_first_trial_amount = parameterService.getParameterValueByKey("withdrawal_first_trial_amount");

        if (preRmbAmount.subtract(new BigDecimal(withdrawal_auto_amount)).doubleValue()<0){
            return 1;
        }else if (preRmbAmount.subtract(new BigDecimal(withdrawal_first_trial_amount)).doubleValue()<0){
            return 2;
        }else{
            return 3;
        }
    }

    private Long saveFuntimeUserAccountWithdrawalRecord(Long userId, Integer withdrawalType,
                                                        String withdrawalCard, BigDecimal rmbAmount,
                                                        BigDecimal blackAmount, BigDecimal ratio, BigDecimal channelAmount,
                                                        BigDecimal preRmbAmount, int trialType, String orderNo, String nickname,
                                                        BigDecimal blackDiamond) {
        FuntimeUserAccountWithdrawalRecord record = new FuntimeUserAccountWithdrawalRecord();
        record.setTrialType(trialType);
        record.setAmount(rmbAmount);
        record.setBlackDiamond(blackAmount);
        record.setBlackRmbRatio(ratio);
        record.setPreRmbAmount(preRmbAmount);
        record.setCardNumber(withdrawalCard);
        record.setChannelAmount(channelAmount);
        record.setOrderNo(orderNo);
        record.setUserId(userId);
        record.setWithdrawalType(withdrawalType);
        record.setVersion(System.currentTimeMillis());
        record.setState(1);
        record.setNickname(nickname);
        record.setPreBlackAmount(blackDiamond);

        int k = userAccountWithdrawalRecordMapper.insertSelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

        return record.getId();

    }

    private void updateFuntimeUserAccountWithdrawalRecord(Long id, Integer state, Integer trialType, String payment_no){
        FuntimeUserAccountWithdrawalRecord record = new FuntimeUserAccountWithdrawalRecord();
        record.setId(id);
        record.setState(state);
        record.setTrialType(trialType);
        record.setThirdOrderNumber(payment_no);
        int k = userAccountWithdrawalRecordMapper.updateByPrimaryKeySelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }

    }


    private BigDecimal getServiceAmount(int rmb) {
        BigDecimal channelAmount = withdrawalConfMapper.getServiceAmount(rmb);
        if (channelAmount==null){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
        }
        return channelAmount;
    }


    public String getWithdrawalCard(Integer withdrawalType, String openid, String depositCard){

        if (WithdrawalType.DESPOSIT_CARD.getValue()==withdrawalType.intValue()){
            return depositCard;
        }else if (WithdrawalType.WXPAY.getValue()==withdrawalType.intValue()){

            return openid;
        }else{
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
    }



    public Long saveFuntimeUserConvertRecord(Long userId,BigDecimal convertRatio,Integer convertType,BigDecimal fromAmount,BigDecimal toAmount){
        FuntimeUserConvertRecord record = new FuntimeUserConvertRecord();
        record.setConvertRatio(convertRatio);
        record.setConvertType(convertType);
        record.setFromAmount(fromAmount);
        record.setToAmount(toAmount);
        record.setUserId(userId);
        int k = userConvertRecordMapper.insertSelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }
        return record.getId();
    }

    public BigDecimal convert(String from, String to){
        StringBuffer key = new StringBuffer();
        key.append(from).append("_to_").append(to);

        String val = parameterService.getParameterValueByKey(key.toString());

        if (StringUtils.isBlank(val)){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
        }

        return new BigDecimal(val);
    }


    public Long saveFuntimeUserAccountGifttransRecord(Long userId, String operationDesc, BigDecimal amount, Integer num, Integer giftId
            , String giftName, Long toUserId, Integer giveChannelId, Long roomId, String operationType){
        FuntimeUserAccountGifttransRecord record = new FuntimeUserAccountGifttransRecord();
        record.setActionType(OperationType.GIVEGIFT.getAction());
        record.setAmount(amount);
        record.setCreateTime(new Date());
        record.setGiftId(giftId);
        record.setGiftName(giftName);
        record.setRoomId(roomId);
        record.setGiveChannelId(giveChannelId);
        record.setNum(num);
        record.setOperationDesc(operationDesc);
        record.setOperationType(operationType);
        record.setOrderNo("C"+StringUtil.createOrderId());
        record.setState(1);
        record.setToUserId(toUserId);
        record.setUserId(userId);
        record.setVersion(System.currentTimeMillis());
        record.setCompleteTime(new Date());
        int k = userAccountGifttransRecordMapper.insertSelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }
        return record.getId();
    }

    public Long saveFuntimeUserAccountGifttransRecord(Long userId, String operationDesc, BigDecimal amount, Integer num, Integer giftId
            , String giftName, Long toUserId, Integer giveChannelId, Long roomId, String operationType,Integer boxBasic){
        FuntimeUserAccountGifttransRecord record = new FuntimeUserAccountGifttransRecord();
        record.setActionType(OperationType.GIVEGIFT.getAction());
        record.setAmount(amount);
        record.setCreateTime(new Date());
        record.setGiftId(giftId);
        record.setGiftName(giftName);
        record.setRoomId(roomId);
        record.setBoxBasic(boxBasic);
        record.setGiveChannelId(giveChannelId);
        record.setNum(num);
        record.setOperationDesc(operationDesc);
        record.setOperationType(operationType);
        record.setOrderNo("C"+StringUtil.createOrderId());
        record.setState(1);
        record.setToUserId(toUserId);
        record.setUserId(userId);
        record.setVersion(System.currentTimeMillis());
        record.setCompleteTime(new Date());
        int k = userAccountGifttransRecordMapper.insertSelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }
        return record.getId();
    }

    public void checkUser(Long userId){
        if(!userService.checkUserExists(userId)){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
    }


    public void updateDetailById(Long id, Long userId, Long version, long currentTimeMillis) {
        int k = userRedpacketDetailMapper.updateDetailById(id,userId,version,currentTimeMillis);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }
    }


    public Long saveAccountRedpacketRecord(Long userId, Long detailId, BigDecimal amount, Date createTime,Long giftRecordId) {
        FuntimeUserAccountRedpacketRecord record = new FuntimeUserAccountRedpacketRecord();
        record.setAmount(amount);
        record.setDetailId(detailId);
        record.setOrderNo("B"+StringUtil.createOrderId());
        record.setSendTime(createTime);
        record.setUserId(userId);
        record.setGiftRecordId(giftRecordId);
        int k = userAccountRedpacketRecordMapper.insertSelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return record.getId();
    }



    public boolean checkRedpacketGrab(Long userId,Long redpacketId){
        FuntimeUserRedpacketDetail detail = userRedpacketDetailMapper.queryDetailByRedIdAndUserId(redpacketId,userId);
        if(detail==null){
            return true;
        }else{
            return false;
        }
    }
    public Long saveUserAccountCharmRecord(Long userId, Integer charmVal,Long recordId,Integer type){
        FuntimeUserAccountCharmRecord record = new FuntimeUserAccountCharmRecord();
        record.setRelationId(recordId);
        record.setType(type);
        record.setCharmVal(charmVal);
        record.setUserId(userId);
        int k = userAccountGifttransRecordMapper.insertCharmRecord(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return record.getId();
    }
    @Override
    public Long saveUserAccountLevelWealthRecord(Long userId, Integer levelVal,Integer wealthVal,Long recordId,Integer type){
        FuntimeUserAccountLevelWealthRecord record = new FuntimeUserAccountLevelWealthRecord();
        record.setLevelVal(levelVal);
        record.setRelationId(recordId);
        record.setType(type);
        record.setWealthVal(wealthVal);
        record.setUserId(userId);
        int k = userAccountLevelWealthLogMapper.insertRecord(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return record.getId();
    }

    @Override
    public void saveUserAccountLevelWealthLog(Long userId, Integer levelVal,Integer wealthVal,Long recordId,String actionType,String operationType){
        FuntimeUserAccountLevelWealthLog levelWealthLog = new FuntimeUserAccountLevelWealthLog();
        levelWealthLog.setUserId(userId);
        levelWealthLog.setLevelVal(levelVal);
        levelWealthLog.setWealthVal(wealthVal);
        levelWealthLog.setRelationId(recordId);
        levelWealthLog.setActionType(actionType);
        levelWealthLog.setOperationType(operationType);
        int k = userAccountLevelWealthLogMapper.insert(levelWealthLog);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public Map<String, Object> getBulletOfFish(Long userId, Long roomId) {
        Map<String, Object> fish = userAccountMapper.getBulletOfFish(userId);
        if(fish==null||fish.isEmpty()){
            int k = userAccountMapper.insertFishAccount(userId,0,10);
            if (k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
            insertFishAccountRecord(userId,10,0, roomId);
            fish = new HashMap<>();
            fish.put("bullet",10);
            fish.put("score",0);
        }
        return fish;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveScoreOfFish(Long userId, Integer score, Integer bullet) {
        Map<String, Object> fish = userAccountMapper.getBulletOfFish(userId);
        if (fish == null||fish.isEmpty()){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        Integer bulletPre = Integer.parseInt(fish.get("bullet").toString());
        if (bulletPre-bullet<0){
            throw new BusinessException(ErrorMsgEnum.USER_BULLET_NO_EN.getValue(),ErrorMsgEnum.USER_BULLET_NO_EN.getDesc());
        }
        int k = userAccountMapper.insertFishRecord(userId,score,bullet);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        k = userAccountMapper.saveScoreOfFish(userId,score,bullet);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public Long insertFishAccountRecord(Long userId, Integer bullet, int parseInt, Long roomId) {

        FuntimeUserAccountFishRecord record = new FuntimeUserAccountFishRecord();
        record.setBullet(bullet);
        record.setBulletPrice(parseInt);
        record.setUserId(userId);
        record.setRoomId(roomId);
        int k = userAccountMapper.insertFishAccountRecord(record);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return record.getId();
    }

    @Override
    public List<Map<String, Object>> getFishRanklist(int endCount, Integer type) {
        String startDate = null;
        String endDate = null;
        if (type == 3){
            startDate = DateUtil.getCurrentWeekStart();
            endDate = DateUtil.getCurrentWeekEnd();
        }else if (type == 2){
            startDate = DateUtil.getCurrentMonthStart();
            endDate = DateUtil.getCurrentMonthEnd();
        }
        if (type == 1) {
            return userAccountMapper.getFishRanklist(endCount);
        }else {
            return userAccountMapper.getFishRanklist2(endCount,startDate,endDate);
        }
    }

    @Override
    public void updateBulletForPlus(Long userId, int bullet) {
        if (userAccountMapper.updateBulletForPlus(userId,bullet)!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public void saveUserKnapsack(Long userId, int type, Integer drawId, int num) {
        Long knapsackId = userAccountMapper.checkUserKnapsackExist(userId, drawId, type);
        int k;
        if (knapsackId == null){
            k = userAccountMapper.insertUserKnapsack(userId, type, drawId, num, System.currentTimeMillis());
            if (k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
        }else{
            k = userAccountMapper.updateUserKnapsackPlus(knapsackId,num);
            if (k!=1){
                throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
            }
        }

    }

    @Override
    public List<Map<String, Object>> getUserKnapsackByUserId(Long userId) {
        List<Map<String, Object>> list = giftMapper.getGiftByKnapsack(userId);

        return list;

    }

    @Override
    public FuntimeGift getGiftById(Integer id) {
        return giftMapper.selectByPrimaryKey(id);
    }

    @Override
    public Map<String,Object> getCarInfoById(Integer id) {
        return carMapper.getCarInfoById(id);
    }

    @Override
    public Map<String, Object> getCarInfoByCarId(Integer carId) {
        return carMapper.getCarInfoByCarId(carId);
    }

    @Override
    public void drawCar(Map<String,Object> map) {
        Integer days = Integer.parseInt(map.get("days").toString());
        Integer carId = Integer.parseInt(map.get("carId").toString());
        Long userId = Long.parseLong(map.get("userId").toString());
        FuntimeUser user = getUserById(userId);
        if (user.getCarId()==null){
            userService.updateUserCar(userId,carId);
        }
        Long userCarId = carMapper.getUserCarById(Long.parseLong(map.get("userId").toString()), Integer.parseInt(map.get("carId").toString()));
        int k;
        if (userCarId == null){
            map.put("endTime",DateUtils.addDays(new Date(),days));

            k = carMapper.insertUserCar(map);

        }else{
            map.put("userCarId",userCarId);
            k = carMapper.updateUserCar(map);

        }
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

    }



    @Override
    public void saveUserAccountGoldLog(Long userId, BigDecimal amount,Long recordId,String actionType,String operationType){
        FuntimeUserAccountGoldLog goldLog = new FuntimeUserAccountGoldLog();
        goldLog.setUserId(userId);
        goldLog.setAmount(amount);
        goldLog.setRelationId(recordId);
        goldLog.setActionType(actionType);
        goldLog.setOperationType(operationType);
        int k = userAccountGoldLogMapper.insertSelective(goldLog);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
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

    @Override
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

    @Override
    public void saveUserAccountBlackLog(Long userId, BigDecimal amount,Long recordId,String actionType,String operationType){
        FuntimeUserAccountBlackLog blackLog = new FuntimeUserAccountBlackLog();
        blackLog.setUserId(userId);
        blackLog.setAmount(amount);
        blackLog.setRelationId(recordId);
        blackLog.setActionType(actionType);
        blackLog.setOperationType(operationType);
        int k = userAccountBlackLogMapper.insertSelective(blackLog);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }


    public Long saveAccountRechargeRecord(FuntimeUserAccountRechargeRecord rechargeRecord,Long version, Integer state,String orderNo){

        rechargeRecord.setState(state);
        rechargeRecord.setVersion(version);
        rechargeRecord.setOrderNo(orderNo);

        int k = userAccountRechargeRecordMapper.insertSelective(rechargeRecord);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return rechargeRecord.getId();
    }

    public void updateState(Long id, Integer state, String transaction_id, Integer hornNum, Integer goldNum){
        FuntimeUserAccountRechargeRecord record = new FuntimeUserAccountRechargeRecord();
        record.setId(id);
        record.setState(state);
        record.setHornNum(hornNum);
        record.setGoldNum(goldNum);
        record.setRechargeCardId(transaction_id);
        record.setCompleteTime(new Date());
        int k = userAccountRechargeRecordMapper.updateByPrimaryKeySelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    public void updatePollTimes(Long id, Integer pollTimes){
        FuntimeUserAccountRechargeRecord record = new FuntimeUserAccountRechargeRecord();
        record.setId(id);
        record.setPollTimes(pollTimes);
        int k = userAccountRechargeRecordMapper.updateByPrimaryKeySelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }
    @Override
    public void updateRechargeRecordState(Long id,Integer state){
        FuntimeUserAccountRechargeRecord record = new FuntimeUserAccountRechargeRecord();
        record.setId(id);
        record.setState(state);
        int k = userAccountRechargeRecordMapper.updateByPrimaryKeySelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }




}
