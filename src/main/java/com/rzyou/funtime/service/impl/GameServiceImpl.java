package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.BusinessException;
import com.rzyou.funtime.common.ErrorMsgEnum;
import com.rzyou.funtime.entity.FuntimeGameYaoyaoConf;
import com.rzyou.funtime.entity.FuntimeGameYaoyaoPool;
import com.rzyou.funtime.entity.FuntimeUserAccount;
import com.rzyou.funtime.mapper.FuntimeGameYaoyaoMapper;
import com.rzyou.funtime.mapper.FuntimeUserAccountMapper;
import com.rzyou.funtime.service.GameService;
import com.rzyou.funtime.service.UserService;
import org.apache.commons.lang3.RandomUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
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
    FuntimeGameYaoyaoMapper gameYaoyaoMapper;
    @Autowired
    FuntimeUserAccountMapper userAccountMapper;

    public List<FuntimeGameYaoyaoConf> getYaoyaoConf(){
        return gameYaoyaoMapper.getYaoyaoConf();
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
        FuntimeUserAccount userAccount = userAccountMapper.selectByUserId(userId);

        if (userAccount == null){
            throw new BusinessException(ErrorMsgEnum.USER_NOT_EXISTS.getValue(),ErrorMsgEnum.USER_NOT_EXISTS.getDesc());
        }
        int type = poolInfo.getType();
        if (type == 1){
            //金币
            if (userAccount.getGoldCoin().subtract(new BigDecimal(poolInfo.getQuota())).doubleValue()<0){
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_GOLD_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_GOLD_NOT_EN.getDesc());
            }
        }
        if (type == 2){
            //钻石
            if (userAccount.getBlackDiamond().subtract(new BigDecimal(poolInfo.getQuota())).doubleValue()<0){
                throw new BusinessException(ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getValue(),ErrorMsgEnum.USER_ACCOUNT_BLUE_NOT_EN.getDesc());
            }
        }
        /*
        if (poolInfo.getActualPool()-poolInfo.getQuota()<0){
            throw new BusinessException(ErrorMsgEnum.DRAW_POOL_NOT_EN.getValue(),ErrorMsgEnum.DRAW_POOL_NOT_EN.getDesc());
        }*/

        List<FuntimeGameYaoyaoConf> list = getYaoyaoConf();
        int probabilityTotal = 1;
        Map<String,FuntimeGameYaoyaoConf> probabilityMap = new HashMap<>();
        for (FuntimeGameYaoyaoConf yaoyaoConf : list){
            int temp = probabilityTotal;
            probabilityTotal += yaoyaoConf.getProbability();
            probabilityMap.put(temp+"-"+probabilityTotal,yaoyaoConf);
        }
        int random = RandomUtils.nextInt(1,probabilityTotal);
        FuntimeGameYaoyaoConf conf = null;
        for (Map.Entry<String,FuntimeGameYaoyaoConf> entry : probabilityMap.entrySet()){
            String key = entry.getKey();
            String[] array = key.split("-");
            int key1 = Integer.parseInt(array[0]);
            int key2 = Integer.parseInt(array[1]);
            if (random>=key1&&random<key2){
                conf = entry.getValue();
                break;
            }
        }

        if (conf == null){
            throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(),ErrorMsgEnum.PARAMETER_ERROR.getDesc());
        }

        Map<String,Object> result = new HashMap<>();
        result.put("number1",conf.getNumber1());
        result.put("number2",conf.getNumber2());
        result.put("number3",conf.getNumber3());
        BigDecimal subActualPool = new BigDecimal(poolInfo.getQuota()).multiply(new BigDecimal(0.8)).setScale(0,BigDecimal.ROUND_HALF_UP);

        if (conf.getDrawVal().doubleValue()<=0){
            //不中奖
            //奖池增加
            if (poolInfo.getActualPool()-subActualPool.intValue()<0){
                throw new BusinessException(ErrorMsgEnum.DRAW_POOL_NOT_EN.getValue(),ErrorMsgEnum.DRAW_POOL_NOT_EN.getDesc());
            }
            updateActualPoolForPlus(id,subActualPool.intValue());

            if (type == 1) {
                result.put("userAmount",userAccount.getGoldCoin().intValue()-poolInfo.getQuota());
                userService.updateUserAccountGoldCoinSub(userId, poolInfo.getQuota());
            } else if (type == 2) {
                result.put("userAmount",userAccount.getBlueDiamond().intValue()-poolInfo.getQuota());
                userService.updateUserAccountForSub(userId, null, new BigDecimal(poolInfo.getQuota()), null);
            } else {
                throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(), ErrorMsgEnum.PARAMETER_ERROR.getDesc());
            }
            result.put("actualPool",poolInfo.getActualPool()-subActualPool.intValue());
            return result;
        }

        BigDecimal drawAmount = conf.getDrawType()==1?new BigDecimal(poolInfo.getActualPool()).multiply(conf.getDrawVal()).setScale(0,BigDecimal.ROUND_HALF_UP)
                :new BigDecimal(poolInfo.getQuota()).multiply(conf.getDrawVal()).setScale(0,BigDecimal.ROUND_HALF_UP);
        BigDecimal actualAmount = drawAmount.subtract(subActualPool);
        if (poolInfo.getActualPool()-actualAmount.intValue()<0){
            throw new BusinessException(ErrorMsgEnum.DRAW_POOL_NOT_EN.getValue(),ErrorMsgEnum.DRAW_POOL_NOT_EN.getDesc());
        }
        updateActualPoolForSub(id,actualAmount.intValue());

        result.put("drawAmount",drawAmount.intValue());
        result.put("actualPool",poolInfo.getActualPool()-actualAmount.intValue());

        if (drawAmount.intValue()-poolInfo.getQuota()<0) {
            if (type == 1) {
                result.put("userAmount",userAccount.getGoldCoin().intValue()-poolInfo.getQuota()+drawAmount.intValue());
                userService.updateUserAccountGoldCoinSub(userId, poolInfo.getQuota()-drawAmount.intValue());
            } else if (type == 2) {
                result.put("userAmount",userAccount.getBlueDiamond().intValue()-poolInfo.getQuota()+drawAmount.intValue());
                userService.updateUserAccountForSub(userId, null, new BigDecimal(poolInfo.getQuota()).subtract(drawAmount), null);
            } else {
                throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(), ErrorMsgEnum.PARAMETER_ERROR.getDesc());
            }
        }

        if (drawAmount.intValue()-poolInfo.getQuota()>0) {
            if (type == 1) {
                result.put("userAmount",userAccount.getGoldCoin().intValue()-poolInfo.getQuota()+drawAmount.intValue());
                userService.updateUserAccountGoldCoinPlus(userId, drawAmount.intValue()-poolInfo.getQuota());
            } else if (type == 2) {
                result.put("userAmount",userAccount.getBlueDiamond().intValue()-poolInfo.getQuota()+drawAmount.intValue());
                userService.updateUserAccountForPlus(userId, null, drawAmount.subtract(new BigDecimal(poolInfo.getQuota())), null);
            } else {
                throw new BusinessException(ErrorMsgEnum.PARAMETER_ERROR.getValue(), ErrorMsgEnum.PARAMETER_ERROR.getDesc());
            }
        }

        return result;
    }
}
