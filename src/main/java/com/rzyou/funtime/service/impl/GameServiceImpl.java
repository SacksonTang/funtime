package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.common.OperationType;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.FuntimeGameYaoyaoMapper;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.GameService;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.DateUtil;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 2020/2/27
 * LLP-LX
 */
@Service
public class GameServiceImpl implements GameService {

    @Autowired
    UserService userService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    AccountService accountService;
    @Autowired
    FuntimeGameYaoyaoMapper gameYaoyaoMapper;

    public List<FuntimeGameYaoyaoConf> getYaoyaoConf(int id){
        return gameYaoyaoMapper.getYaoyaoConf(id);
    }

    @Override
    public boolean getYaoyaoShowConf(int type, Long userId){
        FuntimeUserAccount userAccount = userService.getUserAccountInfoById(userId);
        if (userAccount == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        if (!parameterService.getParameterValueByKey("yaoyao_show").equals("1")){
            return false;
        }
        String yaoyaoNeedLevel = parameterService.getParameterValueByKey("yaoyao_need_level");
        int level = yaoyaoNeedLevel==null?0:Integer.parseInt(yaoyaoNeedLevel);
        if (userAccount.getLevel()<level){
            return false;
        }
        if (gameYaoyaoMapper.getYaoyaoShowConf(type)<1){
            return false;
        }
        return true;
    }

    void updateActualPoolForPlus(Integer id,Integer amount){
        int k = gameYaoyaoMapper.updateActualPoolForPlus(id,amount);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }
    void updateActualPoolForSub(Integer id,Integer amount){
        int k = gameYaoyaoMapper.updateActualPoolForSub(id,amount);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public List<FuntimeGameYaoyaoPool> getYaoyaoPool(Integer type) {
        return gameYaoyaoMapper.getYaoyaoPool(type);
    }

    @Override
    @Transactional( rollbackFor = Throwable.class)
    public Map<String,Object> drawing(Integer id,Long userId){
        FuntimeGameYaoyaoPool poolInfo = gameYaoyaoMapper.getPoolInfoById(id);
        if (poolInfo==null){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);

        if (userAccount == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }

        int type = poolInfo.getType();
        if (!getYaoyaoShowConf(type, userId)){
            throw new BusinessException(ErrorMsgEnum.DRAW_TIME_OUT.getValue(),ErrorMsgEnum.DRAW_TIME_OUT.getDesc());
        }
        Integer userAmount = type == 1?userAccount.getGoldCoin().intValue():userAccount.getBlueDiamond().intValue();
        if (type == 1){
            //金币
            if (userAccount.getGoldCoin().subtract(new BigDecimal(poolInfo.getQuota())).doubleValue()<0){
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_GOLD_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_GOLD_NOT_EN.getDesc());
            }
        }
        if (type == 2){
            //钻石
            if (userAccount.getBlueDiamond().subtract(new BigDecimal(poolInfo.getQuota())).doubleValue()<0){
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            }
        }

        List<FuntimeGameYaoyaoConf> list = getYaoyaoConf(id);
        int probabilityTotal = 1;
        int noDraw = 1;
        Map<String,FuntimeGameYaoyaoConf> probabilityMap = new HashMap<>();
        Map<String,FuntimeGameYaoyaoConf> noDrawProbabilityMap = new HashMap<>();
        for (FuntimeGameYaoyaoConf yaoyaoConf : list){
            int temp = probabilityTotal;
            probabilityTotal += yaoyaoConf.getProbability();
            probabilityMap.put(temp+"-"+probabilityTotal,yaoyaoConf);
            if (yaoyaoConf.getDrawVal().intValue() == 0){
                temp = noDraw;
                noDraw += yaoyaoConf.getProbability();
                noDrawProbabilityMap.put(temp+"-"+noDraw,yaoyaoConf);
            }
        }
        FuntimeGameYaoyaoConf conf;
        int random;
        if (poolInfo.getActualPool() == 0){
            random = RandomUtils.nextInt(1,noDraw);
            conf = getConf(noDrawProbabilityMap,random);
        }else {
            random = RandomUtils.nextInt(1, probabilityTotal);
            conf = getConf(probabilityMap,random);
        }
        if (conf == null){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }

        String drawInfo = conf.getNumber1()+","+conf.getNumber2()+","+conf.getNumber3()+"/"+conf.getDrawType()+"/"+conf.getDrawVal();

        Map<String,Object> result = new HashMap<>();
        result.put("number1",conf.getNumber1());
        result.put("number2",conf.getNumber2());
        result.put("number3",conf.getNumber3());
        String percent = parameterService.getParameterValueByKey("pool_percent");
        BigDecimal poolPer = percent == null?new BigDecimal(0.8):new BigDecimal(percent);
        BigDecimal subActualPool = new BigDecimal(poolInfo.getQuota()).multiply(poolPer).setScale(0,BigDecimal.ROUND_HALF_UP);

        if (conf.getDrawVal().doubleValue()<=0){
            //不中奖
            //奖池增加
            Long recordId = saveYaoyaoRecord(userId,type,random,drawInfo,0
                    ,poolInfo.getQuota(),userAmount,poolInfo.getActualPool()
                    ,new BigDecimal(100).multiply(poolPer).intValue(),poolInfo.getQuota());
            updateActualPoolForPlus(id,subActualPool.intValue());
            if (type == 1) {
                result.put("userAmount",userAccount.getGoldCoin().intValue()-poolInfo.getQuota());

                userService.updateUserAccountGoldCoinSub(userId, poolInfo.getQuota());
                accountService.saveUserAccountGoldLog(userId,new BigDecimal(poolInfo.getQuota()),recordId
                        ,OperationType.YAOYAOLE_OUT.getAction(),OperationType.YAOYAOLE_OUT.getOperationType());
            } else if (type == 2) {
                result.put("userAmount",userAccount.getBlueDiamond().intValue()-poolInfo.getQuota());

                accountService.saveUserAccountBlueLog(userId,new BigDecimal(poolInfo.getQuota()),recordId
                        ,OperationType.YAOYAOLE_OUT.getAction(),OperationType.YAOYAOLE_OUT.getOperationType());
                userService.updateUserAccountForSub(userId, null, new BigDecimal(poolInfo.getQuota()), null);
            } else {
                throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(), ErrorMsgEnum.PARAMETER_ERROR.getDesc());
            }

            result.put("actualPool",poolInfo.getActualPool()+subActualPool.intValue());
            return result;
        }

        BigDecimal drawAmount = conf.getDrawType()==1?new BigDecimal(poolInfo.getActualPool()).multiply(conf.getDrawVal()).setScale(0,BigDecimal.ROUND_HALF_UP)
                :new BigDecimal(poolInfo.getQuota()).multiply(conf.getDrawVal()).setScale(0,BigDecimal.ROUND_HALF_UP);
        result.put("drawAmount",drawAmount.intValue());
        //中奖减去奖池增加的就是奖池需要减去的
        BigDecimal actualAmount = drawAmount.subtract(subActualPool);
        if (poolInfo.getActualPool()-actualAmount.intValue()<0){
            actualAmount = new BigDecimal(poolInfo.getActualPool());
            drawAmount = new BigDecimal(poolInfo.getActualPool());
        }
        updateActualPoolForSub(id,actualAmount.intValue());

        result.put("actualPool",poolInfo.getActualPool()-actualAmount.intValue());

        Integer userExchangeAmount = drawAmount.intValue()-poolInfo.getQuota()<0?poolInfo.getQuota()-drawAmount.intValue():drawAmount.intValue()-poolInfo.getQuota();
        Long recordId = saveYaoyaoRecord(userId,type,random,drawInfo,drawAmount.intValue()
                ,poolInfo.getQuota(),userAmount,poolInfo.getActualPool()
                ,new BigDecimal(100).multiply(poolPer).intValue()
                ,userExchangeAmount);
        if (drawAmount.intValue()-poolInfo.getQuota()<0) {
            result.put("userAmount",userAmount-poolInfo.getQuota()+drawAmount.intValue());

            if (type == 1) {
                userService.updateUserAccountGoldCoinSub(userId, poolInfo.getQuota()-drawAmount.intValue());
                accountService.saveUserAccountGoldLog(userId,new BigDecimal(poolInfo.getQuota()).subtract(drawAmount),recordId
                        ,OperationType.YAOYAOLE_OUT.getAction(),OperationType.YAOYAOLE_OUT.getOperationType());

            } else if (type == 2) {
                userService.updateUserAccountForSub(userId, null, new BigDecimal(poolInfo.getQuota()).subtract(drawAmount), null);

                accountService.saveUserAccountBlueLog(userId,new BigDecimal(poolInfo.getQuota()).subtract(drawAmount),recordId
                        ,OperationType.YAOYAOLE_OUT.getAction(),OperationType.YAOYAOLE_OUT.getOperationType());

            } else {
                throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(), ErrorMsgEnum.PARAMETER_ERROR.getDesc());
            }
        }

        if (drawAmount.intValue()-poolInfo.getQuota()>0) {
            result.put("userAmount",userAmount-poolInfo.getQuota()+drawAmount.intValue());
            if (type == 1) {

                userService.updateUserAccountGoldCoinPlus(userId, drawAmount.intValue()-poolInfo.getQuota());

                accountService.saveUserAccountGoldLog(userId,drawAmount.subtract(new BigDecimal(poolInfo.getQuota())),recordId
                        ,OperationType.YAOYAOLE_IN.getAction(),OperationType.YAOYAOLE_IN.getOperationType());

            } else if (type == 2) {
                userService.updateUserAccountForPlus(userId, null, drawAmount.subtract(new BigDecimal(poolInfo.getQuota())), null);

                accountService.saveUserAccountBlueLog(userId,drawAmount.subtract(new BigDecimal(poolInfo.getQuota())),recordId
                        ,OperationType.YAOYAOLE_IN.getAction(),OperationType.YAOYAOLE_IN.getOperationType());

            } else {
                throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(), ErrorMsgEnum.PARAMETER_ERROR.getDesc());
            }
        }

        return result;
    }

    private FuntimeGameYaoyaoConf getConf(Map<String,FuntimeGameYaoyaoConf> map,int random){
        for (Map.Entry<String,FuntimeGameYaoyaoConf> entry : map.entrySet()){
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
    public void updateYaoyaoPoolTask() {

        gameYaoyaoMapper.insertYaoyaoPoolHisotry(DateUtil.getLastWeekStart(),DateUtil.getLastWeekEnd());

        gameYaoyaoMapper.updateYaoyaoPoolTask();

    }

    private Long saveYaoyaoRecord(Long userId, int type, int random, String drawInfo, int drawAmount, Integer basicAmount, int userAmont, Integer poolAmount, int poolPercent, Integer userExchangeAmount) {
        FuntimeUserAccountYaoyaoRecord record = new FuntimeUserAccountYaoyaoRecord();
        record.setUserId(userId);
        record.setType(type);
        record.setDrawRandom(random);
        record.setDrawInfo(drawInfo);
        record.setDrawAmount(drawAmount);
        record.setBasicAmount(basicAmount);
        record.setPoolAmount(poolAmount);
        record.setPoolPercent(poolPercent);
        record.setUserAmont(userAmont);
        record.setUserExchangeAmount(userExchangeAmount);
        int k = gameYaoyaoMapper.insertYaoyaoRecord(record);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return record.getId();
    }


    @Override
    public Map<String, Object> getBulletOfFish(Long userId) {
        Map<String, Object> map = accountService.getBulletOfFish(userId);
        FuntimeUserAccount userAccount = userService.getUserAccountInfoById(userId);
        String bulletPrice = parameterService.getParameterValueByKey("bullet_price");
        String bulletPriceGold = parameterService.getParameterValueByKey("bullet_price_gold");
        map.put("blueAmount",userAccount.getBlueDiamond().intValue());
        map.put("bulletPrice",bulletPrice);
        map.put("bulletPriceGold",bulletPriceGold);
        return map;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void saveScoreOfFish(Long userId, Integer score, Integer bullet) {
        accountService.saveScoreOfFish(userId,score,bullet);
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void buyBullet(Long userId, Integer bullet, Integer type) {
        String bulletPrice = parameterService.getParameterValueByKey("bullet_price");
        String bulletPriceGold = parameterService.getParameterValueByKey("bullet_price_gold");
        FuntimeUserAccount userAccount = userService.getUserAccountInfoById(userId);
        if (userAccount == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        Long recordId = accountService.insertFishAccountRecord(userId,bullet,Integer.parseInt(bulletPrice));
        accountService.updateBulletForPlus(userId,bullet);
        if (type == 1){
            BigDecimal goldAmount = new BigDecimal((bullet / 1000) * Integer.parseInt(bulletPriceGold));
            if (userAccount.getGoldCoin().subtract(goldAmount).intValue() < 0) {
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_GOLD_NOT_EN.getValue(), ErrorMsgEnum.USER_ACCOUNT_GOLD_NOT_EN.getDesc());
            }
            userService.updateUserAccountGoldCoinSub(userId,goldAmount.intValue());
            accountService.saveUserAccountGoldLog(userId,goldAmount,recordId
                    ,OperationType.BUY_BULLET.getAction(),OperationType.BUY_BULLET.getOperationType());

        }else {
            BigDecimal blueAmount = new BigDecimal((bullet / 100) * Integer.parseInt(bulletPrice));
            if (userAccount.getBlueDiamond().subtract(blueAmount).intValue() < 0) {
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue(), ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            }
            userService.updateUserAccountForSub(userId, null, blueAmount, null);
            accountService.saveUserAccountBlueLog(userId,blueAmount,recordId,OperationType.BUY_BULLET.getAction(),OperationType.BUY_BULLET.getOperationType());
        }



    }

    @Override
    public Map<String, Object> getFishRanklist(Long curUserId) {
        Map<String, Object> resultMap = new HashMap<>();
        String count = parameterService.getParameterValueByKey("fish_rank_count");
        resultMap.put("rankCount",count);
        int startCount = 1;
        int endCount = Integer.parseInt(count);
        List<Map<String, Object>> list = accountService.getFishRanklist(startCount,endCount);
        if (list==null||list.isEmpty()){
            resultMap.put("rankingList",null);
            return resultMap;
        }
        FuntimeUser user = userService.queryUserById(curUserId);
        FuntimeUserAccount userAccount= accountService.getUserAccountByUserId(curUserId);
        Map<String,Object> myInfoMap = new HashMap<>();
        myInfoMap.put("nickname",user.getNickname());
        myInfoMap.put("portraitAddress",user.getPortraitAddress());
        myInfoMap.put("signText",user.getSignText());
        myInfoMap.put("showId",user.getShowId());
        myInfoMap.put("sex",user.getSex());
        myInfoMap.put("level",userAccount.getLevel());
        myInfoMap.put("levelUrl",userAccount.getLevelUrl());
        boolean isRankMe = false;
        for (int i =0;i<list.size();i++){
            Map<String, Object> map = list.get(i);
            String userId = map.get("userId").toString();
            if (userId.equals(curUserId.toString())){
                isRankMe = true;
                myInfoMap.put("isRankMe",true);
                myInfoMap.put("mySort", i+1);
                myInfoMap.put("myScore", map.get("score"));
                if (i == 0){
                    myInfoMap.put("diffScore",0);
                }else{
                    BigDecimal currentScore = new BigDecimal(map.get("score").toString());
                    BigDecimal lastScore = new BigDecimal(list.get(i-1).get("score").toString());
                    myInfoMap.put("diffScore",lastScore.subtract(currentScore).intValue());
                }

                resultMap.put("user",myInfoMap);
            }
        }
        if (!isRankMe){
            myInfoMap.put("isRankMe",false);
            resultMap.put("user",myInfoMap);
        }
        resultMap.put("rankingList",list);
        return resultMap;
    }

}
