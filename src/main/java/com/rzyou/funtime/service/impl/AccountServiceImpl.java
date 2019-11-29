package com.rzyou.funtime.service.impl;

import com.github.pagehelper.PageHelper;
import com.github.pagehelper.PageInfo;
import com.rzyou.funtime.common.*;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.*;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.DateUtil;
import com.rzyou.funtime.utils.RedPacketUtil;
import com.rzyou.funtime.utils.StringUtil;

import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
@Slf4j
@Service
public class AccountServiceImpl implements AccountService {

    @Autowired
    UserService userService;
    @Autowired
    ParameterService parameterService;

    @Autowired
    FuntimeUserAccountWithdrawalRecordMapper userAccountWithdrawalRecordMapper;
    @Autowired
    FuntimeUserAccountRechargeRecordMapper userAccountRechargeRecordMapper;
    @Autowired
    FuntimeUserAccountGifttransRecordMapper userAccountGifttransRecordMapper;
    @Autowired
    FuntimeUserAccountBlueLogMapper userAccountBlueLogMapper;
    @Autowired
    FuntimeUserAccountMapper userAccountMapper;
    @Autowired
    FuntimeUserAccountHornLogMapper userAccountHornLogMapper;
    @Autowired
    FuntimeUserAccountBlackLogMapper userAccountBlackLogMapper;
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


    @Override
    public FuntimeUserAccountRechargeRecord getRechargeRecordById(Long id) {
        return userAccountRechargeRecordMapper.selectByPrimaryKey(id);
    }

    @Override
    @Transactional
    public Map<String,Object> createRecharge(FuntimeUserAccountRechargeRecord record){
        FuntimeRechargeConf rechargeConf = rechargeConfMapper.selectByPrimaryKey(record.getRechargeConfId());
        if (rechargeConf==null){
            throw new BusinessException(ErrorMsgEnum.RECHARGE_CONF_NOT_EXISTS.getValue(),ErrorMsgEnum.RECHARGE_CONF_NOT_EXISTS.getDesc());
        }
        record.setRmb(rechargeConf.getRechargeRmb());
        record.setHornNum(rechargeConf.getHornNum());
        record.setAmount(rechargeConf.getRechargeNum());
        String orderNo = "A"+StringUtil.createOrderId();
        Long id = saveAccountRechargeRecord(record,System.currentTimeMillis(),PayState.START.getValue(), orderNo);
        Map<String,Object> result = new HashMap<>();
        result.put("orderId",id);
        result.put("orderNo",orderNo);
        return result;

    }

    @Override
    @Transactional
    public Map<String,String> paySuccess(Long orderId) {
        Map<String,String> result = new HashMap<>();
        FuntimeUserAccountRechargeRecord record = userAccountRechargeRecordMapper.selectByPrimaryKey(orderId);
        if(record==null){
            result.put("return_code", "FAIL");
            result.put("return_msg", ErrorMsgEnum.ORDER_NOT_EXISTS.getDesc());
            return result;
        }
        if (PayState.PAIED.getValue().equals(record.getState())){
            result.put("return_code", "SUCCESS");
            result.put("return_msg", "OK");
            return result;
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
            result.put("return_code", "SUCCESS");
            result.put("return_msg", "OK");
            return result;
        }else{
            log.info("订单号: {} 的订单已失效,请重新下单",record.getOrderNo());
            result.put("return_code", "FAIL");
            result.put("return_msg", ErrorMsgEnum.ORDER_IS_INVALID.getDesc());
            return result;
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
    @Transactional
    public void createRedpacket(FuntimeUserRedpacket redpacket) {

        String invalid_day = parameterService.getParameterValueByKey("redpacket_invalid_day");

        redpacket.setInvalidTime(DateUtils.addDays(new Date(),Integer.parseInt(invalid_day)));
        redpacket.setState(RedpacketState.START.getValue());

        int k = userRedpacketMapper.insertSelective(redpacket);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }

        RedPacketUtil redPacketUtil = new RedPacketUtil(redpacket.getAmount().intValue());
        Integer[] redPackets = redPacketUtil.splitRedPacket(redpacket.getAmount().intValue(), redpacket.getRedpacketNum());
        if(redPackets.length>0) {
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

    }

    @Override
    @Transactional
    public Map<String,Object> grabRedpacket(Long userId, Long redpacketId){

        FuntimeUserRedpacket redpacket = userRedpacketMapper.selectByPrimaryKey(redpacketId);
        if (redpacket==null){
            throw new BusinessException(ErrorMsgEnum.REDPACKET_IS_NOT_EXISTS.getValue(),ErrorMsgEnum.REDPACKET_IS_NOT_EXISTS.getDesc());
        }
        if (redpacket.getBestowCondition()==1){
            return grabRedpacketNoCondition(userId, redpacketId,null);
        }else{
            return grabRedpacketBestowCondition(userId,redpacketId,redpacket.getGiftId(),redpacket.getUserId());
        }

    }
    public Map<String,Object> grabRedpacketBestowCondition(Long userId, Long redpacketId, Integer giftId,Long toUserId) {

        //礼物赠送
        Long recordId = createGiftTrans(userId,toUserId,giftId,1,"红包赠送",GiveChannel.REDPACKET.getValue());

        return grabRedpacketNoCondition(userId, redpacketId,recordId);

    }


    //无条件
    public Map<String,Object> grabRedpacketNoCondition(Long userId, Long redpacketId,Long giftRecordId) {

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
        boolean isLastOne = details.size()==1?true:false;

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
        return result;

    }

    public void updateRedpacketState(Long redpacketId,Integer state){
        int k = userRedpacketMapper.updateStateById(state,redpacketId);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }
    }

    private void tagSetup(Long redpacketId) {

        List<FuntimeUserAccountRedpacketRecord> records = userAccountRedpacketRecordMapper.getRedpacketRecordByredId(redpacketId);
        if(records==null||records.isEmpty()||records.size()<=5){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
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
        String startDate = queryDate+"-01 00:00:01";
        String endDate = currentFinalDay(queryDate) ;
        return userAccountRedpacketRecordMapper.getSumGrabAmountById(startDate,endDate,userId);
    }

    @Override
    public List<Map<String,Object>> getSumGrabTagsById(Long userId, String queryDate) {
        String startDate = queryDate+"-01 00:00:01";
        String endDate = currentFinalDay(queryDate) ;
        return userAccountRedpacketRecordMapper.getSumGrabTagsById(startDate,endDate,userId);
    }

    @Override
    @Transactional
    public Long createGiftTrans(Long userId, Long toUserId, Integer giftId, Integer giftNum,String operationDesc,Integer giveChannelId) {

        checkUser(userId);
        checkUser(toUserId);
        FuntimeUserAccount userAccount = userAccountMapper.selectByUserId(userId);
        if (userAccount==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeGift funtimeGift = giftMapper.selectByPrimaryKey(giftId);
        if (funtimeGift==null){
            throw new BusinessException(ErrorMsgEnum.GIFT_NOT_EXISTS.getValue(),ErrorMsgEnum.GIFT_NOT_EXISTS.getDesc());
        }

        Integer amount= funtimeGift.getActivityPrice()==null?funtimeGift.getOriginalPrice().intValue()*giftNum:funtimeGift.getActivityPrice().intValue()*giftNum;
        //账户余额不足
        if (userAccount.getBlueDiamond().intValue()<amount){
            throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
        }

        String blue_to_black = parameterService.getParameterValueByKey("blue_to_black");

        Long recordId = saveFuntimeUserAccountGifttransRecord(userId,operationDesc,new BigDecimal(amount)
                ,giftNum,giftId,funtimeGift.getGiftName(),toUserId,giveChannelId);

        BigDecimal black = new BigDecimal(blue_to_black).multiply(new BigDecimal(amount)).setScale(2, RoundingMode.HALF_UP);

        //用户送减去蓝钻
        userService.updateUserAccountForSub(userId,null,new BigDecimal(amount),null);

        //用户收加上黑钻
        userService.updateUserAccountForPlus(toUserId,black,null,null);

        //用户送的日志
        saveUserAccountBlueLog(userId,new BigDecimal(amount),recordId
                ,OperationType.GIVEGIFT.getAction(),OperationType.GIVEGIFT.getOperationType());

        //用户收的日志
        saveUserAccountBlackLog(toUserId,black,recordId,OperationType.RECEIVEGIFT.getAction()
                ,OperationType.RECEIVEGIFT.getOperationType());

        return recordId;
    }

    @Override
    public void updateStateForInvalid() {
        userRedpacketMapper.updateStateForInvalid();
    }

    @Override
    @Transactional
    public void diamondConvert(Long userId, String from, String to, BigDecimal amount,Integer convertType) {

        BigDecimal val = convert(from,to,amount);

        BigDecimal toAmount = amount.multiply(val).setScale(2,RoundingMode.HALF_UP);

        Long recordId = saveFuntimeUserConvertRecord(userId,val,convertType,amount,toAmount);

        //用户送减去黑钻
        userService.updateUserAccountForSub(userId,amount,null,null);

        //用户收加上蓝钻
        userService.updateUserAccountForPlus(userId,null,toAmount,null);

        //用户减的日志
        saveUserAccountBlueLog(userId,toAmount,recordId
                ,OperationType.BLACK_BLUE_IN.getAction(),OperationType.BLACK_BLUE_IN.getOperationType());

        //用户加的日志
        saveUserAccountBlackLog(userId,amount,recordId,OperationType.BLACK_BLUE_OUT.getAction()
                ,OperationType.BLACK_BLUE_OUT.getOperationType());


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
    @Transactional
    public void applyWithdrawal(Long userId,Integer withdrawalType, BigDecimal blackAmount) {

        userService.checkAgreementByuserId(userId,UserAgreementType.WITHDRAWAL_AGREEMENT.getValue());

        FuntimeUser user = userService.queryUserById(userId);
        if(user==null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        //待处理的条数
        int count = userAccountWithdrawalRecordMapper.getWithdrawalRecordByUserId(userId);
        if(count>0){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_OPERATION_LIMIT.getValue(),ErrorMsgEnum.WITHDRAWAL_OPERATION_LIMIT.getDesc());
        }

        checkWithdrawalConf(userId,blackAmount);

        String withdrawalCard = getWithdrawalCard(userId, withdrawalType);

        //黑对RMB比例
        BigDecimal val = convert("black", "rmb", blackAmount);

        BigDecimal rmbAmount = val.multiply(blackAmount).setScale(2,RoundingMode.HALF_UP);

        BigDecimal channelAmount = getServiceAmount(blackAmount.intValue());

        Long recordId = saveFuntimeUserAccountWithdrawalRecord(userId,withdrawalType,withdrawalCard,rmbAmount,blackAmount,val,channelAmount);

        //减去用户黑钻
        userService.updateUserAccountForSub(userId,blackAmount,null,null);

        //用户日志
        saveUserAccountBlackLog(userId,blackAmount,recordId,OperationType.WITHDRAWAL.getAction(),OperationType.WITHDRAWAL.getOperationType());

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

    private void checkWithdrawalConf(Long userId,BigDecimal black){
        //每次的最小值
        String withdrawal_min_once = parameterService.getParameterValueByKey("withdrawal_min_once");
        if (black.doubleValue()<new BigDecimal(withdrawal_min_once).doubleValue()){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_MIN_LIMIT.getValue(),ErrorMsgEnum.WITHDRAWAL_MIN_LIMIT.getDesc());
        }
        //每日最大限额
        String startDate = DateUtil.getCurrentDayStart();
        String endDate = DateUtil.getCurrentDayEnd();
        BigDecimal dayAmount = userAccountWithdrawalRecordMapper.getSumAmountForDay(startDate,endDate,userId);
        dayAmount = dayAmount ==null?new BigDecimal(0):dayAmount;
        String withdrawal_max_day = parameterService.getParameterValueByKey("withdrawal_max_day");
        if (new BigDecimal(withdrawal_max_day).subtract(dayAmount).doubleValue()<0){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_DAY_LIMIT.getValue(),ErrorMsgEnum.WITHDRAWAL_DAY_LIMIT.getDesc());
        }
        //每月最大次数
        String withdrawal_maxtime_month = parameterService.getParameterValueByKey("withdrawal_maxtime_month");
        int count = userAccountWithdrawalRecordMapper.getCountForMonth(startDate,endDate,userId);
        if (count>=Integer.parseInt(withdrawal_maxtime_month)){
            throw new BusinessException(ErrorMsgEnum.WITHDRAWAL_MONTH_LIMIT.getValue(),ErrorMsgEnum.WITHDRAWAL_MONTH_LIMIT.getDesc());
        }

    }

    private Long saveFuntimeUserAccountWithdrawalRecord(Long userId, Integer withdrawalType, String withdrawalCard, BigDecimal rmbAmount, BigDecimal blackAmount, BigDecimal val, BigDecimal channelAmount) {
        FuntimeUserAccountWithdrawalRecord record = new FuntimeUserAccountWithdrawalRecord();

        record.setAmount(rmbAmount);
        record.setBlackDiamond(blackAmount);
        record.setBlackRmbRatio(val);
        record.setCardNumber(withdrawalCard);
        record.setChannelAmount(channelAmount);
        record.setOrderNo("D"+StringUtil.createOrderId());
        record.setUserId(userId);
        record.setWithdrawalType(withdrawalType);
        record.setVersion(System.currentTimeMillis());
        record.setState(1);

        int k = userAccountWithdrawalRecordMapper.insertSelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.UNKNOWN_ERROR.getValue(),ErrorMsgEnum.UNKNOWN_ERROR.getDesc());
        }
        return record.getId();

    }

    private BigDecimal getServiceAmount(int black) {
        BigDecimal channelAmount = withdrawalConfMapper.getServiceAmount(black);
        if (channelAmount==null){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
        }
        return channelAmount;
    }


    public String getWithdrawalCard(Long userId,Integer withdrawalType){
        FuntimeUserValid userValid = userService.queryValidInfoByUserId(userId);
        if (userValid==null){
            throw new BusinessException(ErrorMsgEnum.USERVALID_IS_NOT_VALID.getValue(),ErrorMsgEnum.USERVALID_IS_NOT_VALID.getDesc());
        }
        if (WithdrawalType.DESPOSIT_CARD.getValue()==withdrawalType.intValue()){
            return userValid.getDepositCard();
        }else if (WithdrawalType.WXPAY.getValue()==withdrawalType.intValue()){
            return userValid.getWxNo();
        }else if (WithdrawalType.ALIPAY.getValue()==withdrawalType.intValue()){
            return userValid.getAlipayNo();
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

    public BigDecimal convert(String from, String to, BigDecimal amount){
        StringBuffer key = new StringBuffer();
        key.append(from).append("_to_").append(to);

        String val = parameterService.getParameterValueByKey(key.toString());

        if (StringUtils.isBlank(val)){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
        }

        return new BigDecimal(val);
    }


    public Long saveFuntimeUserAccountGifttransRecord(Long userId,String operationDesc,BigDecimal amount,Integer num,Integer giftId
            ,String giftName,Long toUserId,Integer giveChannelId){
        FuntimeUserAccountGifttransRecord record = new FuntimeUserAccountGifttransRecord();
        record.setActionType(OperationType.GIVEGIFT.getAction());
        record.setAmount(amount);
        record.setCreateTime(new Date());
        record.setGiftId(giftId);
        record.setGiftName(giftName);
        record.setGiveChannelId(giveChannelId);
        record.setNum(num);
        record.setOperationDesc(operationDesc);
        record.setOperationType(OperationType.GIVEGIFT.getOperationType());
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
        FuntimeUser user = userService.queryUserById(userId);
        if(user==null){
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

    public void updateState(Long id,Integer state){
        FuntimeUserAccountRechargeRecord record = new FuntimeUserAccountRechargeRecord();
        record.setId(id);
        record.setState(state);
        record.setCompleteTime(new Date());
        int k = userAccountRechargeRecordMapper.updateByPrimaryKeySelective(record);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }




}
