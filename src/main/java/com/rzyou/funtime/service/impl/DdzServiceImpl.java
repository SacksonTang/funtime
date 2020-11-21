package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.OperationType;
import com.rzyou.funtime.entity.FuntimeDdz;
import com.rzyou.funtime.entity.FuntimeUser;
import com.rzyou.funtime.entity.FuntimeUserAccount;
import com.rzyou.funtime.mapper.FuntimeDdzMapper;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.DdzService;
import com.rzyou.funtime.service.ParameterService;
import com.rzyou.funtime.service.UserService;
import com.rzyou.funtime.utils.DateUtil;
import org.apache.commons.lang3.time.DateUtils;
import org.apache.ibatis.annotations.Param;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class DdzServiceImpl implements DdzService {

    @Autowired
    FuntimeDdzMapper ddzMapper;
    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;
    @Autowired
    ParameterService parameterService;

    @Override
    @Transactional(rollbackFor = Throwable.class)
    public void goldChange(FuntimeDdz ddz) {

        ddzMapper.insertDdzRecord(ddz);
        if (ddz.getUser1()!=null&&ddz.getGold1()!=null){
            userService.updateUserAccountGoldCoinPlus(ddz.getUser1(),ddz.getGold1());
            if (ddz.getGold1()>0){
                accountService.saveUserAccountGoldLog(ddz.getUser1(),new BigDecimal(ddz.getGold1()),ddz.getId(), OperationType.DDZ_GOLD_IN.getAction(),OperationType.DDZ_GOLD_IN.getOperationType());
            }else {
                accountService.saveUserAccountGoldLog(ddz.getUser1(), new BigDecimal(ddz.getGold1()), ddz.getId(), OperationType.DDZ_GOLD_OUT.getAction(), OperationType.DDZ_GOLD_OUT.getOperationType());
            }
        }
        if (ddz.getUser2()!=null&&ddz.getGold2()!=null){
            userService.updateUserAccountGoldCoinPlus(ddz.getUser2(),ddz.getGold2());
            if (ddz.getGold2()>0){
                accountService.saveUserAccountGoldLog(ddz.getUser2(),new BigDecimal(ddz.getGold2()),ddz.getId(), OperationType.DDZ_GOLD_IN.getAction(),OperationType.DDZ_GOLD_IN.getOperationType());
            }else {
                accountService.saveUserAccountGoldLog(ddz.getUser2(), new BigDecimal(ddz.getGold2()), ddz.getId(), OperationType.DDZ_GOLD_OUT.getAction(), OperationType.DDZ_GOLD_OUT.getOperationType());
            }
        }
        if (ddz.getUser3()!=null&&ddz.getGold3()!=null){
            userService.updateUserAccountGoldCoinPlus(ddz.getUser3(),ddz.getGold3());
            if (ddz.getGold3()>0){
                accountService.saveUserAccountGoldLog(ddz.getUser3(),new BigDecimal(ddz.getGold3()),ddz.getId(), OperationType.DDZ_GOLD_IN.getAction(),OperationType.DDZ_GOLD_IN.getOperationType());
            }else {
                accountService.saveUserAccountGoldLog(ddz.getUser3(), new BigDecimal(ddz.getGold3()), ddz.getId(), OperationType.DDZ_GOLD_OUT.getAction(), OperationType.DDZ_GOLD_OUT.getOperationType());
            }
        }
    }

    @Override
    public List<Map<String, Object>> getRankList() {

        String startDate;
        String endDate;
        int hours = DateUtil.getCurrentHours();
        if (hours<20){
            startDate = DateUtil.getLastDay()+ " 20:00:00";
            endDate = DateUtil.getCurrentDateTime(DateUtil.YYYY_MM_DD)+ " 01:00:00";
        }else {
            startDate = DateUtil.getCurrentDateTime(DateUtil.YYYY_MM_DD)+ " 20:00:00";
            endDate = DateUtil.getDateTime(DateUtils.addDays(new Date(),1),DateUtil.YYYY_MM_DD)+ " 02:00:00";
        }
        String counts = parameterService.getParameterValueByKey("ddz_rank_count");
        counts = counts == null?"10":counts;
        return ddzMapper.getRankList(startDate,endDate,Integer.parseInt(counts));
    }


}
