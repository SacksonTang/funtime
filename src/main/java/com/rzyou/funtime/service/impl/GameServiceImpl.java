package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.*;
import com.rzyou.funtime.entity.*;
import com.rzyou.funtime.mapper.FuntimeGameMapper;
import com.rzyou.funtime.service.*;
import com.rzyou.funtime.utils.DateUtil;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

/**
 * 2020/2/27
 * LLP-LX
 */
@Service
@Slf4j
public class GameServiceImpl implements GameService {

    @Autowired
    UserService userService;
    @Autowired
    ParameterService parameterService;
    @Autowired
    AccountService accountService;
    @Autowired
    RoomService roomService;
    @Autowired
    NoticeService noticeService;
    @Autowired
    FuntimeGameMapper gameMapper;

    public List<FuntimeGameYaoyaoConf> getYaoyaoConf(int id){
        return gameMapper.getYaoyaoConf(id);
    }

    @Override
    public boolean getYaoyaoShowConf(int type, Long userId){
        FuntimeUserAccount userAccount = userService.getUserAccountInfoById(userId);
        if (userAccount == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        Integer isDate = gameMapper.getGameShowConf2(userAccount.getLevel(), GameCodeEnum.YAOYAOLE.getValue());
        if(isDate == null){
            return false;
        }
        if (isDate == 1) {
            if (gameMapper.getGameShowConf(type, GameCodeEnum.YAOYAOLE.getValue()) < 1) {
                return false;
            }
        }
        return true;
    }

    @Override
    public boolean getFishShowConf(int type, Long userId) {
        FuntimeUserAccount userAccount = userService.getUserAccountInfoById(userId);
        if (userAccount == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        Integer isDate = gameMapper.getGameShowConf2(userAccount.getLevel(), GameCodeEnum.FISH.getValue());
        if(isDate == null){
            return false;
        }
        if (isDate == 1) {
            if (gameMapper.getGameShowConf(type, GameCodeEnum.FISH.getValue()) < 1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean getSmasheggShowConf(int type, Long userId, Integer level) {

        Integer isDate = gameMapper.getGameShowConf2(level, GameCodeEnum.EGG.getValue());
        if(isDate == null){
            return false;
        }
        if (isDate == 1) {
            if (gameMapper.getGameShowConf(type, GameCodeEnum.EGG.getValue()) < 1) {
                return false;
            }
        }

        return true;
    }

    @Override
    public boolean getCircleShowConf(int type, Long userId, Integer level) {

        Integer isDate = gameMapper.getGameShowConf2(level, GameCodeEnum.CIRCLE.getValue());
        if(isDate == null){
            return false;
        }
        if (isDate == 1) {
            if (gameMapper.getGameShowConf(type, GameCodeEnum.CIRCLE.getValue()) < 1) {
                return false;
            }
        }
        return true;
    }

    void updateActualPoolForPlus(Integer id,Integer amount){
        int k = gameMapper.updateActualPoolForPlus(id,amount);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }
    void updateActualPoolForSub(Integer id,Integer amount){
        int k = gameMapper.updateActualPoolForSub(id,amount);
        if(k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
    }

    @Override
    public List<FuntimeGameYaoyaoPool> getYaoyaoPool(Integer type) {
        return gameMapper.getYaoyaoPool(type);
    }

    @Override
    @Transactional( rollbackFor = Throwable.class)
    public Map<String,Object> drawing(Integer id, Long userId, Long roomId){
        FuntimeGameYaoyaoPool poolInfo = gameMapper.getPoolInfoById(id);
        if (poolInfo==null){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);

        if (userAccount == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeUser user = userService.queryUserById(userId);

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
                    ,new BigDecimal(100).multiply(poolPer).intValue(),poolInfo.getQuota(), roomId);
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
            if (type == 2&&roomId!=null) {
                roomService.updateHotsPlus(roomId,new BigDecimal(poolInfo.getQuota()/10).setScale(0,BigDecimal.ROUND_UP).intValue() );
            }
            result.put("actualPool",poolInfo.getActualPool()+subActualPool.intValue());
            return result;
        }

        BigDecimal drawAmount = conf.getDrawType()==1?new BigDecimal(poolInfo.getActualPool()).multiply(conf.getDrawVal()).setScale(0,BigDecimal.ROUND_HALF_UP)
                :new BigDecimal(poolInfo.getQuota()).multiply(conf.getDrawVal()).setScale(0,BigDecimal.ROUND_HALF_UP);
        result.put("drawAmount",drawAmount.intValue());
        sendNotice(GameCodeEnum.YAOYAOLE.getValue(),user.getNickname(),null,drawAmount.intValue());
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
                ,userExchangeAmount,roomId);
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

        if (type == 2&&roomId!=null) {

            roomService.updateHotsPlus(roomId,new BigDecimal(poolInfo.getQuota()/10).setScale(0,BigDecimal.ROUND_UP).intValue() );
        }
        return result;
    }

    private void sendNotice(Integer gameCode,String nickname,String giftName,Integer price){
        String val ;
        if (gameCode.equals(GameCodeEnum.YAOYAOLE.getValue())) {
            val = parameterService.getParameterValueByKey("game_notice_amount");
        }else if (gameCode.equals(GameCodeEnum.EGG.getValue())){
            val = parameterService.getParameterValueByKey("game_notice_amount_egg");
        }else if (gameCode.equals(GameCodeEnum.CIRCLE.getValue())){
            val = parameterService.getParameterValueByKey("game_notice_amount_circle");
        }else{
            return;
        }
        if (price < new BigDecimal(val).intValue()) {
            return;
        }
        Map<String, Object> gameInfoMap = gameMapper.getGameInfoByCode(gameCode);
        String name = gameInfoMap.get("name").toString();
        String icon = gameInfoMap.get("icon").toString();
        String resourceUrl = gameInfoMap.get("resourceUrl").toString();
        Integer needLevel = Integer.parseInt(gameInfoMap.get("needLevel").toString());
        String content = "";
        if (gameCode.equals(GameCodeEnum.YAOYAOLE.getValue())){
            content = nickname+"在"+GameCodeEnum.YAOYAOLE.getDesc()+"中摇到"+price+"钻,运气爆表,活动限时开放,立即参与>";
        }else if (gameCode.equals(GameCodeEnum.EGG.getValue())){
            if (giftName == null) {
                content = nickname + "在"+GameCodeEnum.EGG.getDesc()+"中砸到" + price + "钻,运气爆表,活动限时开放,立即参与>";
            }else{
                content = nickname + "在"+GameCodeEnum.EGG.getDesc()+"中砸到"+giftName+"/" + price + "钻,运气爆表,活动限时开放,立即参与>";
            }
        }else if (gameCode.equals(GameCodeEnum.CIRCLE.getValue())){
            if (giftName == null) {
                content = nickname + "在"+GameCodeEnum.CIRCLE.getDesc()+"中抽到" + price + "钻,运气爆表,活动限时开放,立即参与>";
            }else{
                content = nickname + "在"+GameCodeEnum.CIRCLE.getDesc()+"中抽到"+giftName+"/" + price + "钻,运气爆表,活动限时开放,立即参与>";
            }
        }
        List<String> userIds = roomService.getAllRoomUserByLevel(needLevel);

        if (userIds!=null&&!userIds.isEmpty()) {
            name = name+"喜讯";
            if (gameCode.equals(GameCodeEnum.YAOYAOLE.getValue())) {
                noticeService.notice34(userIds, icon, name, content, "#FF97BA", resourceUrl);
            }else{
                noticeService.notice33(userIds, icon, name, content, "#FF97BA", resourceUrl);
            }
        }
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

    private FuntimeGameSmashEggConf getSmashConf(Map<String,FuntimeGameSmashEggConf> map,int random){
        for (Map.Entry<String,FuntimeGameSmashEggConf> entry : map.entrySet()){
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

    private FuntimeGameCircleConf getCircleConf(Map<String,FuntimeGameCircleConf> map,int random){
        for (Map.Entry<String,FuntimeGameCircleConf> entry : map.entrySet()){
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

        gameMapper.insertYaoyaoPoolHisotry(DateUtil.getLastWeekStart(),DateUtil.getLastWeekEnd());

        gameMapper.updateYaoyaoPoolTask();

    }

    private Long saveYaoyaoRecord(Long userId, int type, int random, String drawInfo, int drawAmount, Integer basicAmount, int userAmont, Integer poolAmount, int poolPercent, Integer userExchangeAmount, Long roomId) {
        FuntimeUserAccountYaoyaoRecord record = new FuntimeUserAccountYaoyaoRecord();
        record.setUserId(userId);
        record.setRoomId(roomId);
        record.setType(type);
        record.setDrawRandom(random);
        record.setDrawInfo(drawInfo);
        record.setDrawAmount(drawAmount);
        record.setBasicAmount(basicAmount);
        record.setPoolAmount(poolAmount);
        record.setPoolPercent(poolPercent);
        record.setUserAmont(userAmont);
        record.setUserExchangeAmount(userExchangeAmount);
        int k = gameMapper.insertYaoyaoRecord(record);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return record.getId();
    }

    private Long saveSmashEggRecord(Long userId, Integer blueAmount, Integer drawRandom, Integer drawNumber,
                                    Integer type, Integer drawType, Integer drawId, BigDecimal drawVal, Long roomId){
        FuntimeUserAccountSmashEggRecord record = new FuntimeUserAccountSmashEggRecord();
        record.setUserId(userId);
        record.setRoomId(roomId);
        record.setBlueAmount(blueAmount);
        record.setDrawId(drawId);
        record.setDrawNumber(drawNumber);
        record.setDrawRandom(drawRandom);
        record.setDrawType(drawType);
        record.setDrawVal(drawVal);
        record.setType(type);
        int k = gameMapper.insertSmashEggRecord(record);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return record.getId();

    }

    private Long saveCircleRecord(Long userId, Integer blueAmount, Integer drawRandom, Integer drawNumber,
                                  Integer drawType, Integer drawId, BigDecimal drawVal, Long roomId){
        FuntimeUserAccountCircleRecord record = new FuntimeUserAccountCircleRecord();
        record.setUserId(userId);
        record.setRoomId(roomId);
        record.setBlueAmount(blueAmount);
        record.setDrawId(drawId);
        record.setDrawNumber(drawNumber);
        record.setDrawRandom(drawRandom);
        record.setDrawType(drawType);
        record.setDrawVal(drawVal);
        int k = gameMapper.insertCircleRecord(record);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return record.getId();

    }
    private Long saveCircleActivityRecord(Long userId, Integer drawRandom, Integer drawNumber,
                                          Integer drawType, Integer drawId, BigDecimal drawVal, Integer activityId){
        FuntimeUserAccountCircleRecord record = new FuntimeUserAccountCircleRecord();
        record.setUserId(userId);
        record.setActivityId(activityId);
        record.setDrawId(drawId);
        record.setDrawNumber(drawNumber);
        record.setDrawRandom(drawRandom);
        record.setDrawType(drawType);
        record.setDrawVal(drawVal);
        int k = gameMapper.insertCircleActivityRecord(record);
        if (k!=1){
            throw new BusinessException(ErrorMsgEnum.DATA_ORER_ERROR.getValue(),ErrorMsgEnum.DATA_ORER_ERROR.getDesc());
        }
        return record.getId();

    }


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public Map<String, Object> getBulletOfFish(Long userId, Long roomId) {
        Map<String, Object> map = accountService.getBulletOfFish(userId,roomId);
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
    public ResultMsg<Object> buyBullet(Long userId, Integer bullet, Integer type, Long roomId) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        String bulletPrice = parameterService.getParameterValueByKey("bullet_price");
        String bulletPriceGold = parameterService.getParameterValueByKey("bullet_price_gold");
        FuntimeUserAccount userAccount = userService.getUserAccountInfoById(userId);
        if (userAccount == null){
            resultMsg.setCode(ErrorMsgEnum.USER_NOT_EXISTS.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
            return resultMsg;
        }
        BigDecimal amount;
        Map<String, Object> map;
        if (type == 1) {
            amount = new BigDecimal(bullet  *1000/Integer.parseInt(bulletPriceGold));
            if (userAccount.getGoldCoin().subtract(amount).intValue() < 0) {
                resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_GOLD_NOT_EN.getValue());
                resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_GOLD_NOT_EN.getDesc());
                boolean bool = getCircleShowConf(1,userId,userAccount.getLevel());
                map = new HashMap<>();
                map.put("userBlueAmount",userAccount.getBlueDiamond().intValue());
                map.put("price",amount.intValue());
                map.put("isCircleShow",bool);
                resultMsg.setData(map);
                return resultMsg;
            }
        }else{
            amount = new BigDecimal(bullet *100/Integer.parseInt(bulletPrice));
            if (userAccount.getBlueDiamond().subtract(amount).intValue() < 0) {
                resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
                resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
                map = new HashMap<>();
                map.put("userBlueAmount",userAccount.getBlueDiamond().intValue());
                map.put("price",amount.intValue());
                resultMsg.setData(map);
                return resultMsg;
            }
        }
        Long recordId = accountService.insertFishAccountRecord(userId,bullet,Integer.parseInt(bulletPrice),roomId);
        accountService.updateBulletForPlus(userId,bullet);
        if (type == 1){
            userService.updateUserAccountGoldCoinSub(userId,amount.intValue());
            accountService.saveUserAccountGoldLog(userId,amount,recordId
                    ,OperationType.BUY_BULLET.getAction(),OperationType.BUY_BULLET.getOperationType());

        }else {
            userService.updateUserAccountForSub(userId, null, amount, null);
            accountService.saveUserAccountBlueLog(userId,amount,recordId,OperationType.BUY_BULLET.getAction(),OperationType.BUY_BULLET.getOperationType());
            if (roomId!=null&&roomId>0) {
                roomService.updateHotsPlus(roomId,amount.divide(new BigDecimal(10)).setScale(0, BigDecimal.ROUND_UP).intValue());
            }
        }

        return resultMsg;
    }

    @Override
    public Map<String, Object> getFishRanklist(Long curUserId, Integer type) {
        Map<String, Object> resultMap = new HashMap<>();
        String count = parameterService.getParameterValueByKey("fish_rank_count");
        resultMap.put("rankCount",count);
        int endCount = Integer.parseInt(count);
        List<Map<String, Object>> list = accountService.getFishRanklist(endCount,type);
        if (list==null||list.isEmpty()){
            resultMap.put("rankingList",null);
            return resultMap;
        }

        resultMap.put("rankingList",list);
        return resultMap;
    }

    @Override
    public List<Map<String, Object>> getGameList(Long userId, Long roomId) {
        FuntimeUserAccount userAccount = userService.getUserAccountInfoById(userId);
        if (userAccount == null){
            return null;
        }

        List<Map<String, Object>> mapList = gameMapper.getGameList(userAccount.getLevel(),1);
        if (mapList!=null&&!mapList.isEmpty()){
            Iterator it = mapList.iterator();
            while (it.hasNext()){
                Map<String, Object> map = (Map<String, Object>) it.next();
                if (roomId!=null) {
                    if ("1005".equals(map.get("gameCode").toString())) {
                        FuntimeChatroom chatroom = roomService.getChatroomById(roomId);
                        if (!chatroom.getUserId().equals(userId)) {
                            if (roomService.getChatroomManager(roomId, userId) == null) {
                                it.remove();
                                continue;
                            }
                        }

                    }
                }

                int isDate = Integer.parseInt(map.get("isDate").toString());
                if (isDate == 1) {
                    int count = gameMapper.getGameShowConf(2, Integer.parseInt(map.get("gameCode").toString()));
                    if (count<1){
                        it.remove();
                    }
                }
            }
        }
        return mapList;
    }

    @Override
    public Map<String, Object> getSmashEggConf(Long userId) {
        Map<String,Object> resultMap = new HashMap<>();
        FuntimeUserAccount userAccount = userService.getUserAccountInfoById(userId);
        if (userAccount == null){
            return null;
        }
        resultMap.put("userBlueAmount",userAccount.getBlueDiamond().intValue());
        //金价
        resultMap.put("eggPriceGold",parameterService.getParameterValueByKey("room_game_egg_price_gold"));
        //银价
        resultMap.put("eggPriceSilver",parameterService.getParameterValueByKey("room_game_egg_price_silver"));
        //铜价
        resultMap.put("eggPriceCopper",parameterService.getParameterValueByKey("room_game_egg_price_copper"));

        return resultMap;

    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> eggDrawing(Long userId, Integer counts, Integer type, Long roomId) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);
        if (userAccount == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeUser user = userService.queryUserById(userId);
        if (!getSmasheggShowConf(2, userId,userAccount.getLevel())){
            throw new BusinessException(ErrorMsgEnum.DRAW_TIME_OUT.getValue(),ErrorMsgEnum.DRAW_TIME_OUT.getDesc());
        }
        Integer price ;
        if (type == 1){
            price = Integer.parseInt(parameterService.getParameterValueByKey("room_game_egg_price_gold"));
        }else if (type == 2){
            price = Integer.parseInt(parameterService.getParameterValueByKey("room_game_egg_price_silver"));
        }else if (type == 3){
            price = Integer.parseInt(parameterService.getParameterValueByKey("room_game_egg_price_copper"));
        }else{
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }
        int consumeAmount = price*counts;
        //钻石
        if (userAccount.getBlueDiamond().subtract(new BigDecimal(consumeAmount)).doubleValue()<0){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            Map<String,Object> map = new HashMap<>();
            map.put("userBlueAmount",userAccount.getBlueDiamond().intValue());
            map.put("price",consumeAmount);
            resultMsg.setData(map);
            return resultMsg;
        }

        List<Map<String,Object>> drawList = new ArrayList<>();
        Map<String,Object> drawMap ;
        for (int i = 0;i<counts;i++) {
            drawMap = new HashMap<>();
            List<FuntimeGameSmashEggConf> list = getSmashEggConfs(type);
            int probabilityTotal = 1;
            Map<String, FuntimeGameSmashEggConf> probabilityMap = new HashMap<>();
            for (FuntimeGameSmashEggConf smashEggConf : list) {
                int temp = probabilityTotal;
                probabilityTotal += smashEggConf.getProbability();
                probabilityMap.put(temp + "-" + probabilityTotal, smashEggConf);

            }
            int random = RandomUtils.nextInt(1, probabilityTotal);
            FuntimeGameSmashEggConf conf = getSmashConf(probabilityMap, random);
            Integer drawId = conf.getDrawId();

            Long recordId = saveSmashEggRecord(userId,price, random, conf.getDrawNumber()
                    , type, conf.getDrawType(), drawId, conf.getDrawVal(),roomId);
            //礼物
            String noticeGiftName = null;
            Integer noticePrice = 0;
            if (conf.getDrawType() == 1) {
                FuntimeGift gift = accountService.getGiftById(drawId);
                if (gift == null){
                    throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
                }
                drawMap.put("drawUrl",gift.getImageUrl());
                drawMap.put("drawName",gift.getGiftName());
                drawMap.put("drawVal",gift.getActivityPrice().intValue()+"钻");
                noticeGiftName = gift.getGiftName();
                noticePrice = gift.getActivityPrice().intValue();
                accountService.saveUserKnapsack(userId, 1, drawId, 1);
                userService.updateUserAccountForSub(userId, null, new BigDecimal(price), null);
                accountService.saveUserAccountBlueLog(userId, new BigDecimal(price), recordId
                        , OperationType.SMASHEGG_OUT.getAction(), OperationType.SMASHEGG_OUT.getOperationType());
                //蓝钻
            } else if (conf.getDrawType() == 2) {
                drawMap.put("drawUrl",conf.getDrawUrl());
                drawMap.put("drawName","蓝钻");
                drawMap.put("drawVal",conf.getDrawVal());
                noticePrice = conf.getDrawVal().intValue();
                if (price - conf.getDrawVal().intValue() > 0) {
                    userService.updateUserAccountForSub(userId, null, new BigDecimal(price - conf.getDrawVal().intValue()), null);
                    accountService.saveUserAccountBlueLog(userId, new BigDecimal(price - conf.getDrawVal().intValue()), recordId
                            , OperationType.SMASHEGG_OUT.getAction(), OperationType.SMASHEGG_OUT.getOperationType());
                }
                if (price - conf.getDrawVal().intValue() < 0) {
                    userService.updateUserAccountForPlus(userId, null, new BigDecimal(conf.getDrawVal().intValue() - price), null);
                    accountService.saveUserAccountBlueLog(userId, new BigDecimal(conf.getDrawVal().intValue() - price), recordId
                            , OperationType.SMASHEGG_IN.getAction(), OperationType.SMASHEGG_IN.getOperationType());
                }
                //金币
            } else if (conf.getDrawType() == 3) {
                drawMap.put("drawUrl",conf.getDrawUrl());
                drawMap.put("drawName","金币");
                drawMap.put("drawVal",conf.getDrawVal());
                //蓝钻减少
                userService.updateUserAccountForSub(userId, null, new BigDecimal(price), null);
                accountService.saveUserAccountBlueLog(userId, new BigDecimal(price), recordId
                        , OperationType.SMASHEGG_OUT.getAction(), OperationType.SMASHEGG_OUT.getOperationType());
                //金币增加
                userService.updateUserAccountGoldCoinPlus(userId, conf.getDrawVal().intValue());
                accountService.saveUserAccountGoldLog(userId, conf.getDrawVal(), recordId
                        , OperationType.SMASHEGG_IN.getAction(), OperationType.SMASHEGG_IN.getOperationType());

                //背景
            } else if (conf.getDrawType() == 4) {
                Map<String, Object> map = roomService.getBackgroundThumbnailById(drawId);
                if (map == null){
                    throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
                }
                drawMap.put("drawUrl",Constant.COS_URL_PREFIX+Constant.DEFAULT_BACKGROUND_ICON);
                drawMap.put("drawName","房间背景x"+roomService.getBackgroundDaysById(drawId)+"天");
                drawMap.put("drawVal",new BigDecimal(map.get("activityPrice").toString()).intValue()+"钻");
                noticeGiftName = "房间背景";
                noticePrice = new BigDecimal(map.get("activityPrice").toString()).intValue();
                roomService.drawBackground(drawId, userId);
                userService.updateUserAccountForSub(userId, null, new BigDecimal(price), null);
                accountService.saveUserAccountBlueLog(userId, new BigDecimal(price), recordId
                        , OperationType.SMASHEGG_OUT.getAction(), OperationType.SMASHEGG_OUT.getOperationType());
                //喇叭
            } else if (conf.getDrawType() == 5) {
                drawMap.put("drawUrl",conf.getDrawUrl());
                drawMap.put("drawName","喇叭x"+conf.getDrawVal());
                drawMap.put("drawVal",new BigDecimal(parameterService.getParameterValueByKey("horn_price")).multiply(conf.getDrawVal()).intValue()+"钻");
                userService.updateUserAccountForSub(userId, null, new BigDecimal(price), null);
                accountService.saveUserAccountBlueLog(userId, new BigDecimal(price), recordId
                        , OperationType.SMASHEGG_OUT.getAction(), OperationType.SMASHEGG_OUT.getOperationType());
                userService.updateUserAccountForPlus(userId, null, null, conf.getDrawVal().intValue());
                accountService.saveUserAccountHornLog(userId, conf.getDrawVal().intValue(), recordId
                        , OperationType.SMASHEGG_IN.getAction(), OperationType.SMASHEGG_IN.getOperationType());
            //座驾
            } else if (conf.getDrawType() == 6){
                Map<String,Object> map = accountService.getCarInfoById(drawId);
                if (map == null){
                    throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
                }
                drawMap.put("drawUrl",map.get("thumbnailUrl"));
                drawMap.put("drawName",map.get("carName").toString()+"x"+map.get("days").toString()+"天");
                drawMap.put("drawVal",new BigDecimal(map.get("price").toString()).intValue()+"钻");
                noticeGiftName = map.get("carName").toString();
                noticePrice = new BigDecimal(map.get("price").toString()).intValue();
                map.put("userId",userId);
                accountService.drawCar(map);
                userService.updateUserAccountForSub(userId, null, new BigDecimal(price), null);
                accountService.saveUserAccountBlueLog(userId, new BigDecimal(price), recordId
                        , OperationType.SMASHEGG_OUT.getAction(), OperationType.SMASHEGG_OUT.getOperationType());

            }else{
                return null;
            }
            drawList.add(drawMap);
            sendNotice(GameCodeEnum.EGG.getValue(),user.getNickname(),noticeGiftName,noticePrice);
        }

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("userBlueAmount",userAccount.getBlueDiamond().subtract(new BigDecimal(consumeAmount)).intValue());
        resultMap.put("drawInfo",drawList);

        resultMsg.setData(resultMap);

        if (roomId!=null) {

            roomService.updateHotsPlus(roomId,new BigDecimal(consumeAmount/10).setScale(0, BigDecimal.ROUND_UP).intValue());
        }

        return resultMsg;
    }

    @Override
    public Map<String, Object> getCircleConf(Long userId) {
        Map<String,Object> resultMap = new HashMap<>();
        FuntimeUserAccount userAccount = userService.getUserAccountInfoById(userId);
        if (userAccount == null){
            return null;
        }
        resultMap.put("userBlueAmount",userAccount.getBlueDiamond().intValue());
        //转盘价格
        resultMap.put("circlePrice",parameterService.getParameterValueByKey("room_game_circle_price"));

        return resultMap;
    }

    @Override
    public ResultMsg<Object> getCircleActivityConf(String activityNo, String channelNo) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        Map<String,Object> resultMap = new HashMap<>();
        Integer hours = gameMapper.getActivityHours(activityNo, channelNo);
        if (hours == null){
            resultMsg.setCode(ErrorMsgEnum.DRAW_TIME_OUT.getValue());
            resultMsg.setMsg(ErrorMsgEnum.DRAW_TIME_OUT.getDesc());
            return resultMsg;
        }
        //转盘价格
        resultMap.put("text","请在注册ID后"+hours+"小时内领取，否则失效。");

        resultMsg.setData(resultMap);
        return resultMsg;
    }

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> circleDrawing(Long userId, Integer counts, Long roomId) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        FuntimeUserAccount userAccount = accountService.getUserAccountByUserId(userId);
        if (userAccount == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        FuntimeUser user = userService.queryUserById(userId);
        if (!getSmasheggShowConf(2, userId, userAccount.getLevel())){
            throw new BusinessException(ErrorMsgEnum.DRAW_TIME_OUT.getValue(),ErrorMsgEnum.DRAW_TIME_OUT.getDesc());
        }
        Integer price = Integer.parseInt(parameterService.getParameterValueByKey("room_game_circle_price"));

        int consumeAmount = price*counts;
        //钻石
        if (userAccount.getBlueDiamond().subtract(new BigDecimal(consumeAmount)).doubleValue()<0){
            resultMsg.setCode(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue());
            resultMsg.setMsg(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            Map<String,Object> map = new HashMap<>();
            map.put("userBlueAmount",userAccount.getBlueDiamond().intValue());
            map.put("price",consumeAmount);
            resultMsg.setData(map);
            return resultMsg;
        }

        List<Map<String,Object>> drawList = new ArrayList<>();
        Map<String,Object> drawMap ;
        for (int i = 0;i<counts;i++) {
            drawMap = new HashMap<>();
            List<FuntimeGameCircleConf> list = getCircleConfs();
            int probabilityTotal = 1;
            Map<String, FuntimeGameCircleConf> probabilityMap = new HashMap<>();
            for (FuntimeGameCircleConf circleConf : list) {
                int temp = probabilityTotal;
                probabilityTotal += circleConf.getProbability();
                probabilityMap.put(temp + "-" + probabilityTotal, circleConf);

            }
            int random = RandomUtils.nextInt(1, probabilityTotal);
            FuntimeGameCircleConf conf = getCircleConf(probabilityMap, random);
            Integer drawId = conf.getDrawId();

            Long recordId = saveCircleRecord(userId,price, random, conf.getDrawNumber()
                    , conf.getDrawType(), drawId, conf.getDrawVal(),roomId);
            drawMap.put("drawNumber",conf.getDrawNumber());
            String noticeGiftName = null;
            Integer noticePrice = 0;
            //礼物
            if (conf.getDrawType() == 1) {
                FuntimeGift gift = accountService.getGiftById(drawId);
                if (gift == null){
                    throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
                }
                drawMap.put("drawUrl",gift.getImageUrl());
                drawMap.put("drawName",gift.getGiftName());
                drawMap.put("drawVal",gift.getActivityPrice().intValue()+"钻");
                noticeGiftName = gift.getGiftName();
                noticePrice = gift.getActivityPrice().intValue();
                accountService.saveUserKnapsack(userId, 1, drawId, 1);
                userService.updateUserAccountForSub(userId, null, new BigDecimal(price), null);
                accountService.saveUserAccountBlueLog(userId, new BigDecimal(price), recordId
                        , OperationType.CIRCLE_OUT.getAction(), OperationType.CIRCLE_OUT.getOperationType());
                //蓝钻
            } else if (conf.getDrawType() == 2) {
                drawMap.put("drawUrl",conf.getDrawUrl());
                drawMap.put("drawName","蓝钻");
                drawMap.put("drawVal",conf.getDrawVal());
                noticePrice = conf.getDrawVal().intValue();
                if (price - conf.getDrawVal().intValue() > 0) {
                    userService.updateUserAccountForSub(userId, null, new BigDecimal(price - conf.getDrawVal().intValue()), null);
                    accountService.saveUserAccountBlueLog(userId, new BigDecimal(price - conf.getDrawVal().intValue()), recordId
                            , OperationType.CIRCLE_OUT.getAction(), OperationType.CIRCLE_OUT.getOperationType());
                }
                if (price - conf.getDrawVal().intValue() < 0) {
                    userService.updateUserAccountForPlus(userId, null, new BigDecimal(conf.getDrawVal().intValue() - price), null);
                    accountService.saveUserAccountBlueLog(userId, new BigDecimal(conf.getDrawVal().intValue() - price), recordId
                            , OperationType.CIRCLE_IN.getAction(), OperationType.CIRCLE_IN.getOperationType());
                }
                //金币
            } else if (conf.getDrawType() == 3) {
                drawMap.put("drawUrl",conf.getDrawUrl());
                drawMap.put("drawName","金币");
                drawMap.put("drawVal",conf.getDrawVal());
                //蓝钻减少
                userService.updateUserAccountForSub(userId, null, new BigDecimal(price), null);
                accountService.saveUserAccountBlueLog(userId, new BigDecimal(price), recordId
                        , OperationType.CIRCLE_OUT.getAction(), OperationType.CIRCLE_OUT.getOperationType());
                //金币增加
                userService.updateUserAccountGoldCoinPlus(userId, conf.getDrawVal().intValue());
                accountService.saveUserAccountGoldLog(userId, conf.getDrawVal(), recordId
                        , OperationType.CIRCLE_IN.getAction(), OperationType.CIRCLE_IN.getOperationType());

                //背景
            } else if (conf.getDrawType() == 4) {
                Map<String,Object> map = roomService.getBackgroundThumbnailById(drawId);
                if (map == null){
                    throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
                }
                drawMap.put("drawUrl",Constant.COS_URL_PREFIX+Constant.DEFAULT_BACKGROUND_ICON);
                drawMap.put("drawName","房间背景x"+roomService.getBackgroundDaysById(drawId)+"天");
                drawMap.put("drawVal",new BigDecimal(map.get("activityPrice").toString()).intValue()+"钻");
                noticeGiftName = "房间背景";
                noticePrice = new BigDecimal(map.get("activityPrice").toString()).intValue();
                roomService.drawBackground(drawId, userId);
                userService.updateUserAccountForSub(userId, null, new BigDecimal(price), null);
                accountService.saveUserAccountBlueLog(userId, new BigDecimal(price), recordId
                        , OperationType.CIRCLE_OUT.getAction(), OperationType.CIRCLE_OUT.getOperationType());
                //喇叭
            } else if (conf.getDrawType() == 5) {
                drawMap.put("drawUrl",conf.getDrawUrl());
                drawMap.put("drawName","喇叭x"+conf.getDrawVal());
                drawMap.put("drawVal",new BigDecimal(parameterService.getParameterValueByKey("horn_price")).multiply(conf.getDrawVal()).intValue()+"钻");
                userService.updateUserAccountForSub(userId, null, new BigDecimal(price), null);
                accountService.saveUserAccountBlueLog(userId, new BigDecimal(price), recordId
                        , OperationType.CIRCLE_OUT.getAction(), OperationType.CIRCLE_OUT.getOperationType());
                userService.updateUserAccountForPlus(userId, null, null, conf.getDrawVal().intValue());
                accountService.saveUserAccountHornLog(userId, conf.getDrawVal().intValue(), recordId
                        , OperationType.CIRCLE_IN.getAction(), OperationType.CIRCLE_IN.getOperationType());
                //座驾
            } else if (conf.getDrawType() == 6){
                Map<String,Object> map = accountService.getCarInfoById(drawId);
                if (map == null){
                    throw new BusinessException(ErrorMsgEnum.PARAMETER_CONF_ERROR.getValue(),ErrorMsgEnum.PARAMETER_CONF_ERROR.getDesc());
                }
                drawMap.put("drawUrl",map.get("thumbnailUrl"));
                drawMap.put("drawName",map.get("carName").toString()+"x"+map.get("days").toString()+"天");
                drawMap.put("drawVal",new BigDecimal(map.get("price").toString()).intValue()+"钻");
                noticeGiftName = map.get("carName").toString();
                noticePrice = new BigDecimal(map.get("price").toString()).intValue();
                map.put("userId",userId);
                accountService.drawCar(map);
                userService.updateUserAccountForSub(userId, null, new BigDecimal(price), null);
                accountService.saveUserAccountBlueLog(userId, new BigDecimal(price), recordId
                        , OperationType.CIRCLE_OUT.getAction(), OperationType.CIRCLE_OUT.getOperationType());
            } else {
                return null;
            }
            drawMap.put("drawNumber",conf.getDrawNumber());
            drawList.add(drawMap);
            sendNotice(GameCodeEnum.CIRCLE.getValue(),user.getNickname(),noticeGiftName,noticePrice);
        }

        Map<String,Object> resultMap = new HashMap<>();
        resultMap.put("userBlueAmount",userAccount.getBlueDiamond().subtract(new BigDecimal(consumeAmount)).intValue());
        resultMap.put("drawInfo",drawList);

        resultMsg.setData(resultMap);

        if (roomId!=null) {

            roomService.updateHotsPlus(roomId,new BigDecimal(consumeAmount/10).setScale(0, BigDecimal.ROUND_UP).intValue());
        }
        return resultMsg;
    }


    @Override
    @Transactional(rollbackFor = Throwable.class)
    public ResultMsg<Object> circleActivityDrawing(Long userId, String activityNo, String channelNo) {
        ResultMsg<Object> resultMsg = new ResultMsg<>();
        Map<String,Object> map = gameMapper.getActivityInfo(userId, activityNo, channelNo);
        if (map == null||map.isEmpty()){
            resultMsg.setCode(ErrorMsgEnum.DRAW_TIME_OUT.getValue());
            resultMsg.setMsg(ErrorMsgEnum.DRAW_TIME_OUT.getDesc());
            return resultMsg;
        }
        if ("1".equals(map.get("activityFlag").toString())){
            resultMsg.setCode(ErrorMsgEnum.DRAW_TIME_OUT.getValue());
            resultMsg.setMsg(ErrorMsgEnum.DRAW_TIME_OUT.getDesc());
            return resultMsg;
        }
        if ("1".equals(map.get("userFlag").toString())){
            resultMsg.setCode(ErrorMsgEnum.DRAW_ACTIVITY_EMPIRE.getValue());
            resultMsg.setMsg(ErrorMsgEnum.DRAW_ACTIVITY_EMPIRE.getDesc().replace("x",map.get("hours").toString()));
            return resultMsg;
        }

        Long recordId = gameMapper.getCircleActivityRecordByUserId(userId);
        if (recordId!=null){
            resultMsg.setCode(ErrorMsgEnum.DRAW_ACTIVITY_USER_EXIST.getValue());
            resultMsg.setMsg(ErrorMsgEnum.DRAW_ACTIVITY_USER_EXIST.getDesc());
            return resultMsg;
        }
        Integer activityId = Integer.parseInt(map.get("id").toString());
        if (map.get("activityLimit")!=null){
            Integer counts = gameMapper.getCircleActivityRecordCounts(activityId);
            if (counts>Integer.parseInt(map.get("activityLimit").toString())){
                resultMsg.setCode(ErrorMsgEnum.DRAW_TIME_OUT.getValue());
                resultMsg.setMsg(ErrorMsgEnum.DRAW_TIME_OUT.getDesc());
                return resultMsg;
            }
        }


        Map<String,Object> drawMap = new HashMap<>();
        List<FuntimeGameCircleConf> list = getCircleActivityConf2();
        int probabilityTotal = 1;
        Map<String, FuntimeGameCircleConf> probabilityMap = new HashMap<>();
        for (FuntimeGameCircleConf circleConf : list) {
            int temp = probabilityTotal;
            probabilityTotal += circleConf.getProbability();
            probabilityMap.put(temp + "-" + probabilityTotal, circleConf);

        }
        int random = RandomUtils.nextInt(1, probabilityTotal);
        FuntimeGameCircleConf conf = getCircleConf(probabilityMap, random);
        Integer drawId = conf.getDrawId();

        saveCircleActivityRecord(userId, random, conf.getDrawNumber()
                , conf.getDrawType(), drawId, conf.getDrawVal(),activityId);
        //礼物
        if (conf.getDrawType() == 1) {

            accountService.createGiftTrans(userService.getUserInfoByShowId("10000").getId(),userId,conf.getDrawId(),1);
        }
        userService.insertUserActivity(userId,activityId);
        drawMap.put("drawNumber",conf.getDrawNumber());
        resultMsg.setData(drawMap);
        return resultMsg;
    }

    private List<FuntimeGameCircleConf> getCircleConfs() {
        return gameMapper.getCircleConfs();
    }

    private List<FuntimeGameCircleConf> getCircleActivityConf2() {
        return gameMapper.getCircleActivityConf();
    }


    private List<FuntimeGameSmashEggConf> getSmashEggConfs(Integer type) {
        return gameMapper.getSmashEggConfs(type);
    }

}
