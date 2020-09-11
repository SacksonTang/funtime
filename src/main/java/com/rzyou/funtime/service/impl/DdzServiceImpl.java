package com.rzyou.funtime.service.impl;

import com.rzyou.funtime.common.OperationType;
import com.rzyou.funtime.entity.FuntimeDdz;
import com.rzyou.funtime.mapper.FuntimeDdzMapper;
import com.rzyou.funtime.service.AccountService;
import com.rzyou.funtime.service.DdzService;
import com.rzyou.funtime.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Service
public class DdzServiceImpl implements DdzService {

    @Autowired
    FuntimeDdzMapper ddzMapper;
    @Autowired
    UserService userService;
    @Autowired
    AccountService accountService;

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
}
